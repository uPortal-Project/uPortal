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

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.namespace.QName;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.StartElement;
import org.apereo.portal.layout.IStylesheetUserPreferencesService;
import org.apereo.portal.layout.IStylesheetUserPreferencesService.PreferencesScope;
import org.apereo.portal.layout.IUserLayoutManager;
import org.apereo.portal.layout.dao.IStylesheetDescriptorDao;
import org.apereo.portal.layout.om.ILayoutAttributeDescriptor;
import org.apereo.portal.layout.om.IStylesheetDescriptor;
import org.apereo.portal.spring.spel.IPortalSpELService;
import org.apereo.portal.user.IUserInstanceManager;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.expression.EvaluationException;
import org.springframework.expression.Expression;
import org.springframework.expression.ParseException;
import org.springframework.web.context.request.WebRequest;

/** JUnit test class for StyleAttributeSource tests with SpEL variables. */
public class StylesheetAttributeSourceForSpELVariablesTest {

    private String layoutAttributeName;
    private String localEventName;
    private String spelExpression;
    private String studentId;
    private String subscribeId;
    private List<ILayoutAttributeDescriptor> layoutAttributeDescriptors;
    private PreferencesScope stylesheetPreferencesScope = PreferencesScope.STRUCTURE;

    @Mock private HttpServletRequest request;
    @Mock private HttpServletResponse response;
    @Mock private StartElement startEvent;
    @Mock private QName qName;
    @Mock private Attribute subscribeIdAttr;

    @Mock private IStylesheetDescriptor stylesheetDescriptor;

    @Mock private IPortalSpELService portalSpELService;
    @Mock private IUserInstanceManager userInstanceManager;
    @Mock private IStylesheetDescriptorDao stylesheetDescriptorDao;
    @Mock private IStylesheetUserPreferencesService stylesheetUserPreferencesService;

    @InjectMocks private MyStylesheetAttributeSource source = new MyStylesheetAttributeSource();

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        this.layoutAttributeName = "studentId";
        this.localEventName = "blah";
        this.spelExpression = "person.attributeMap['studentId']";
        this.studentId = "12345678";
        this.subscribeId = "100";
        this.layoutAttributeDescriptors = new ArrayList<ILayoutAttributeDescriptor>();
        given(
                        this.stylesheetUserPreferencesService.getStylesheetDescriptor(
                                this.request, PreferencesScope.STRUCTURE))
                .willReturn(this.stylesheetDescriptor);
        given(this.stylesheetDescriptor.getLayoutAttributeDescriptors())
                .willReturn(this.layoutAttributeDescriptors);
        given(this.startEvent.getName()).willReturn(this.qName);
        given(this.startEvent.getAttributeByName(IUserLayoutManager.ID_ATTR_NAME))
                .willReturn(this.subscribeIdAttr);
        given(this.subscribeIdAttr.getValue()).willReturn(this.subscribeId);
        given(this.qName.getLocalPart()).willReturn(this.localEventName);
    }

    @After
    public void tearDown() throws Exception {}

    @Test
    public void
            getAdditionalAttributesMethodShouldReturnSpELEvaluatedValueForLayoutAttributeDefault() {
        // given
        final String layoutAttributeValue =
                this.wrapAsSpELExpressionAttributeVariable(this.spelExpression);
        this.layoutAttributeWithDefaultValueFoundForName(
                this.layoutAttributeName, layoutAttributeValue);
        this.layoutAttributeValueNotFoundInUserPreferences(this.layoutAttributeName);
        this.spELEvaluatedValueForInputWillBe(this.spelExpression, this.studentId);
        // when
        final Iterator<Attribute> resultsIterator =
                this.source.getAdditionalAttributes(this.request, this.response, this.startEvent);
        final List<Attribute> results = this.getResultsUsingIterator(resultsIterator);
        // then
        this.assertSingleResultWithCorrectValues(results, this.layoutAttributeName, this.studentId);
    }

    @Test
    public void
            getAdditionalAttributesMethodShouldReturnSpELEvaluatedValueForUserPreferenceLayoutAttribute() {
        // given
        final String layoutAttributeValue =
                this.wrapAsSpELExpressionAttributeVariable(this.spelExpression);
        this.layoutAttributeWithNoDefaultValueFoundForName(this.layoutAttributeName);
        this.layoutAttributeValueFoundInUserPreferences(
                this.layoutAttributeName, layoutAttributeValue);
        this.spELEvaluatedValueForInputWillBe(this.spelExpression, this.studentId);
        // when
        final Iterator<Attribute> resultsIterator =
                this.source.getAdditionalAttributes(this.request, this.response, this.startEvent);
        final List<Attribute> results = this.getResultsUsingIterator(resultsIterator);
        // then
        this.assertSingleResultWithCorrectValues(results, this.layoutAttributeName, this.studentId);
    }

    @Test
    public void getAdditionalAttributesMethodShouldNotDoSpELEvaluationForNonSpELVariable() {
        // given
        final String layoutAttributeValue =
                this.spelExpression; // note that this value is not wrapped in ${ ... }
        this.layoutAttributeWithNoDefaultValueFoundForName(this.layoutAttributeName);
        this.layoutAttributeValueFoundInUserPreferences(
                this.layoutAttributeName, layoutAttributeValue);
        // when
        final Iterator<Attribute> resultsIterator =
                this.source.getAdditionalAttributes(this.request, this.response, this.startEvent);
        final List<Attribute> results = this.getResultsUsingIterator(resultsIterator);
        // then
        verify(this.portalSpELService, never()).parseExpression(any(String.class));
        verify(this.portalSpELService, never())
                .getValue(any(Expression.class), any(WebRequest.class), eq(String.class));
        this.assertSingleResultWithCorrectValues(
                results, this.layoutAttributeName, layoutAttributeValue);
    }

    @Test
    public void
            getAdditionalAttributesMethodShouldReturnNonSpELEvaluatedLayoutAttributeValueWhenParseExceptionThrown() {
        // given
        final String layoutAttributeValue =
                this.wrapAsSpELExpressionAttributeVariable(this.spelExpression);
        this.layoutAttributeWithDefaultValueFoundForName(
                this.layoutAttributeName, layoutAttributeValue);
        this.layoutAttributeValueNotFoundInUserPreferences(this.layoutAttributeName);
        this.spELParsingForInputThrowsParseException(this.spelExpression);
        // when
        final Iterator<Attribute> resultsIterator =
                this.source.getAdditionalAttributes(this.request, this.response, this.startEvent);
        final List<Attribute> results = this.getResultsUsingIterator(resultsIterator);
        // then
        verify(this.portalSpELService, never())
                .getValue(any(Expression.class), any(WebRequest.class), eq(String.class));
        this.assertSingleResultWithCorrectValues(
                results, this.layoutAttributeName, layoutAttributeValue);
    }

    @Test
    public void
            getAdditionalAttributesMethodShouldReturnNonSpELEvaluatedLayoutAttributeValueWhenEvaluationExceptionThrown() {
        // given
        final String layoutAttributeValue =
                this.wrapAsSpELExpressionAttributeVariable(this.spelExpression);
        this.layoutAttributeWithDefaultValueFoundForName(
                this.layoutAttributeName, layoutAttributeValue);
        this.layoutAttributeValueNotFoundInUserPreferences(this.layoutAttributeName);
        this.spELEvaluationForInputThrowsEvaluationException(this.spelExpression);
        // when
        final Iterator<Attribute> resultsIterator =
                this.source.getAdditionalAttributes(this.request, this.response, this.startEvent);
        final List<Attribute> results = this.getResultsUsingIterator(resultsIterator);
        // then
        this.assertSingleResultWithCorrectValues(
                results, this.layoutAttributeName, layoutAttributeValue);
    }

    private void layoutAttributeValueFoundInUserPreferences(
            final String layoutAttributeName, final String value) {
        given(
                        this.stylesheetUserPreferencesService.getLayoutAttribute(
                                this.request,
                                this.stylesheetPreferencesScope,
                                this.subscribeId,
                                layoutAttributeName))
                .willReturn(value);
    }

    private void layoutAttributeValueNotFoundInUserPreferences(final String layoutAttributeName) {
        given(
                        this.stylesheetUserPreferencesService.getLayoutAttribute(
                                this.request,
                                this.stylesheetPreferencesScope,
                                this.subscribeId,
                                layoutAttributeName))
                .willReturn(null);
    }

    private void layoutAttributeWithDefaultValueFoundForName(
            final String layoutAttributeName, final String defaultValue) {
        this.layoutAttributeFoundForName(layoutAttributeName, defaultValue);
    }

    private void layoutAttributeWithNoDefaultValueFoundForName(final String layoutAttributeName) {
        this.layoutAttributeFoundForName(layoutAttributeName, null);
    }

    private void layoutAttributeFoundForName(
            final String layoutAttributeName, final String defaultValue) {
        final ILayoutAttributeDescriptor layoutAttributeDescriptor =
                mock(ILayoutAttributeDescriptor.class);
        this.layoutAttributeDescriptorReturnsTargetElementNames(
                layoutAttributeDescriptor, this.localEventName);
        this.layoutAttributeDescriptors.add(layoutAttributeDescriptor);
        given(layoutAttributeDescriptor.getName()).willReturn(layoutAttributeName);
        given(layoutAttributeDescriptor.getDefaultValue()).willReturn(defaultValue);
    }

    private void layoutAttributeDescriptorReturnsTargetElementNames(
            ILayoutAttributeDescriptor descriptor, final String... strings) {
        final Set<String> results = new HashSet<String>(strings.length);
        for (int i = 0; i < strings.length; i++) {
            results.add(strings[i]);
        }
        given(descriptor.getTargetElementNames()).willReturn(results);
    }

    private void spELEvaluatedValueForInputWillBe(final String input, final String value) {
        final Expression expression = mock(Expression.class);
        given(this.portalSpELService.parseExpression(input)).willReturn(expression);
        given(
                        this.portalSpELService.getValue(
                                eq(expression), any(WebRequest.class), eq(String.class)))
                .willReturn(value);
    }

    private void spELParsingForInputThrowsParseException(final String input) {
        given(this.portalSpELService.parseExpression(input))
                .willThrow(new ParseException(0, input));
    }

    private void spELEvaluationForInputThrowsEvaluationException(final String input) {
        final Expression expression = mock(Expression.class);
        given(this.portalSpELService.parseExpression(input)).willReturn(expression);
        given(
                        this.portalSpELService.getValue(
                                eq(expression), any(WebRequest.class), eq(String.class)))
                .willThrow(new EvaluationException(input));
    }

    private List<Attribute> getResultsUsingIterator(final Iterator<Attribute> iterator) {
        final List<Attribute> results = new ArrayList<Attribute>();
        while (iterator.hasNext()) {
            results.add(iterator.next());
        }
        return results;
    }

    private void assertSingleResultWithCorrectValues(
            List<Attribute> results, final String name, final String value) {
        assertEquals(1, results.size());
        assertEquals(name, results.get(0).getName().getLocalPart());
        assertEquals(value, results.get(0).getValue());
    }

    private String wrapAsSpELExpressionAttributeVariable(final String value) {
        return "${" + value + "}";
    }

    class MyStylesheetAttributeSource extends StylesheetAttributeSource {
        private final Logger logger = LoggerFactory.getLogger(MyStylesheetAttributeSource.class);

        @Override
        public PreferencesScope getStylesheetPreferencesScope(HttpServletRequest request) {
            return stylesheetPreferencesScope;
        }

        @Override
        public Logger getLogger() {
            return logger;
        }
    }
}
