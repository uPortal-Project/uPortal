/**
 * Licensed to Apereo under one or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information regarding copyright ownership. Apereo
 * licenses this file to you under the Apache License, Version 2.0 (the "License"); you may not use
 * this file except in compliance with the License. You may obtain a copy of the License at the
 * following location:
 *
 * <p>http://www.apache.org/licenses/LICENSE-2.0
 *
 * <p>Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apereo.portal.rendering;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Set;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventFactory;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.StartElement;
import org.apereo.portal.layout.IStylesheetUserPreferencesService;
import org.apereo.portal.layout.IStylesheetUserPreferencesService.PreferencesScope;
import org.apereo.portal.layout.IUserLayoutManager;
import org.apereo.portal.layout.om.ILayoutAttributeDescriptor;
import org.apereo.portal.layout.om.IStylesheetDescriptor;
import org.apereo.portal.layout.om.IStylesheetUserPreferences;
import org.apereo.portal.spring.spel.IPortalSpELService;
import org.apereo.portal.utils.cache.CacheKey;
import org.apereo.portal.utils.cache.CacheKey.CacheKeyBuilder;
import org.slf4j.Logger;
import org.springframework.beans.factory.BeanNameAware;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.expression.EvaluationException;
import org.springframework.expression.Expression;
import org.springframework.expression.ParseException;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;

/**
 * Base implementation of layout attribute source that feeds off of {@link IStylesheetDescriptor}
 * and {@link IStylesheetUserPreferences} data
 *
 */
public abstract class StylesheetAttributeSource implements AttributeSource, BeanNameAware {
    private final XMLEventFactory xmlEventFactory = XMLEventFactory.newFactory();
    private String name;
    protected IPortalSpELService portalSpELService;
    protected IStylesheetUserPreferencesService stylesheetUserPreferencesService;

    @Autowired
    public void setPortalSpELService(IPortalSpELService portalSpELService) {
        this.portalSpELService = portalSpELService;
    }

    @Autowired
    public void setStylesheetUserPreferencesService(
            IStylesheetUserPreferencesService stylesheetUserPreferencesService) {
        this.stylesheetUserPreferencesService = stylesheetUserPreferencesService;
    }

    @Override
    public void setBeanName(String name) {
        this.name = name;
    }

    @Override
    public final Iterator<Attribute> getAdditionalAttributes(
            HttpServletRequest request, HttpServletResponse response, StartElement event) {
        final IStylesheetDescriptor stylesheetDescriptor = this.getStylesheetDescriptor(request);

        final PreferencesScope stylesheetPreferencesScope =
                this.getStylesheetPreferencesScope(request);

        final Collection<Attribute> attributes = new LinkedList<Attribute>();

        for (final ILayoutAttributeDescriptor layoutAttributeDescriptor :
                stylesheetDescriptor.getLayoutAttributeDescriptors()) {
            final Set<String> targetElementNames =
                    layoutAttributeDescriptor.getTargetElementNames();
            final QName eventName = event.getName();
            final String localEventName = eventName.getLocalPart();
            if (targetElementNames.contains(localEventName)) {
                final Attribute subscribeIdAttr =
                        event.getAttributeByName(IUserLayoutManager.ID_ATTR_NAME);
                final String subscribeId = subscribeIdAttr.getValue();
                final String name = layoutAttributeDescriptor.getName();

                String value =
                        this.stylesheetUserPreferencesService.getLayoutAttribute(
                                request, stylesheetPreferencesScope, subscribeId, name);
                if (value == null) {
                    value = layoutAttributeDescriptor.getDefaultValue();
                }

                if (value != null) {
                    if (this.shouldDoSpelEvaluationForAttributeValue(value)) {
                        final ServletWebRequest webRequest =
                                new ServletWebRequest(request, response);
                        value = this.doSpelEvaluationForAttributeValue(webRequest, value);
                    }
                    if (value != null) {
                        final Attribute attribute = xmlEventFactory.createAttribute(name, value);
                        attributes.add(attribute);
                    }
                }
            }
        }

        return attributes.iterator();
    }

    @Override
    public final CacheKey getCacheKey(HttpServletRequest request, HttpServletResponse response) {
        final PreferencesScope stylesheetPreferencesScope =
                this.getStylesheetPreferencesScope(request);

        final CacheKeyBuilder<String, String> cacheKeyBuilder = CacheKey.builder(this.name);

        final Iterable<String> layoutAttributeNodeIds =
                this.stylesheetUserPreferencesService.getAllLayoutAttributeNodeIds(
                        request, stylesheetPreferencesScope);
        for (final String nodeId : layoutAttributeNodeIds) {
            cacheKeyBuilder.add(nodeId);
            this.stylesheetUserPreferencesService.populateLayoutAttributes(
                    request, stylesheetPreferencesScope, nodeId, cacheKeyBuilder);
        }

        return cacheKeyBuilder.build();
    }

    public IStylesheetDescriptor getStylesheetDescriptor(HttpServletRequest request) {
        final PreferencesScope stylesheetPreferencesScope =
                this.getStylesheetPreferencesScope(request);
        return this.stylesheetUserPreferencesService.getStylesheetDescriptor(
                request, stylesheetPreferencesScope);
    }

    public abstract PreferencesScope getStylesheetPreferencesScope(HttpServletRequest request);

    protected abstract Logger getLogger();

    protected boolean shouldDoSpelEvaluationForAttributeValue(final String attributeValue) {
        return this.portalSpELService != null
                && attributeValue != null
                && attributeValue.startsWith("${")
                && attributeValue.endsWith("}");
    }

    protected String getValueForSpelEvaluation(final String attributeValue) {
        if (attributeValue != null
                && attributeValue.startsWith("${")
                && attributeValue.endsWith("}")) {
            return attributeValue.substring(2, attributeValue.length() - 1);
        } else {
            return attributeValue;
        }
    }

    protected String doSpelEvaluationForAttributeValue(
            final WebRequest request, final String attributeValue) {
        String result;
        try {
            final String valueForEvaluation = this.getValueForSpelEvaluation(attributeValue);
            final Expression expression =
                    this.portalSpELService.parseExpression(valueForEvaluation);
            result = this.portalSpELService.getValue(expression, request, String.class);
        } catch (ParseException e) {
            this.getLogger()
                    .info("SpEL parse exception parsing: {}; Exception: {}", attributeValue, e);
            result = attributeValue;
        } catch (EvaluationException e) {
            this.getLogger()
                    .info(
                            "SpEL evaluation exception evaluating: {}; Exception: {}",
                            attributeValue,
                            e);
            result = attributeValue;
        }
        return result;
    }
}
