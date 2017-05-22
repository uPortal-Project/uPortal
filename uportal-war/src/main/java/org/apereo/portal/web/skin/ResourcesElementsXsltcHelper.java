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
package org.apereo.portal.web.skin;

import java.util.Collections;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apereo.portal.rendering.xslt.TransformerConfigurationSourceAdapter;
import org.apereo.portal.utils.cache.CacheKey;
import org.jasig.resourceserver.aggr.om.Resources;
import org.jasig.resourceserver.utils.aggr.ResourcesElementsProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.w3c.dom.NodeList;

/**
 * Used by Xalan to transform a {@link Resources} to the correct HTML head elements (script tags for
 * javascript and link tags for css).
 */
public class ResourcesElementsXsltcHelper extends TransformerConfigurationSourceAdapter {
    protected final Log logger = LogFactory.getLog(this.getClass());

    public static final String RESOURCES_ELEMENTS_HELPER = "RESOURCES_ELEMENTS_HELPER";

    public static ResourcesElementsXsltcHelper getElmenentsProvider(Object object) {
        return (ResourcesElementsXsltcHelper) object;
    }

    public static HttpServletRequest getHttpServletRequest(Object request) {
        return (HttpServletRequest) request;
    }

    private ResourcesElementsProvider resourcesElementsProvider;

    @Autowired
    public void setResourcesDao(ResourcesElementsProvider resourcesElementsProvider) {
        this.resourcesElementsProvider = resourcesElementsProvider;
    }

    public String getResourcesParameter(HttpServletRequest request, String skinXml, String name) {
        return this.resourcesElementsProvider.getResourcesParameter(request, skinXml, name);
    }

    public NodeList getResourcesXmlFragment(HttpServletRequest request, String skinXml) {
        return this.resourcesElementsProvider.getResourcesXmlFragment(request, skinXml);
    }

    @Override
    public Map<String, Object> getParameters(
            HttpServletRequest request, HttpServletResponse response) {
        return Collections.singletonMap(RESOURCES_ELEMENTS_HELPER, (Object) this);
    }

    @Override
    public CacheKey getCacheKey(HttpServletRequest request, HttpServletResponse response) {
        return CacheKey.build(
                ResourcesElementsXsltcHelper.class.getName(),
                this.resourcesElementsProvider.getIncludedType(request));
    }
}
