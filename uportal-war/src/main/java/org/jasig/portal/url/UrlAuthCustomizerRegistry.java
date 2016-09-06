/**
 * Licensed to Apereo under one or more contributor license
 * agreements. See the NOTICE file distributed with this work
 * for additional information regarding copyright ownership.
 * Apereo licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License.  You may obtain a
 * copy of the License at the following location:
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.jasig.portal.url;

import javax.servlet.http.HttpServletRequest;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Class to register all login/logout IAuthUrlCustomizer
 * @author  Julien Gribonvald
 * @version $Revision$
 */
public class UrlAuthCustomizerRegistry {

    /** Logger. */
    private static final Log LOG = LogFactory.getLog(UrlAuthCustomizerRegistry.class);

    private List<IAuthUrlCustomizer> registry;

    public void setRegistry(List<IAuthUrlCustomizer> registry) {
        this.registry = registry;
    }

    public String customizeUrl (final HttpServletRequest request, final String url) {
        String customizedUrl = url;
        if (registry != null && !registry.isEmpty()) {
            for (IAuthUrlCustomizer customizer: this.registry) {
                if (customizer != null && customizer.supports(request, customizedUrl)) {
                    customizedUrl = customizer.customizeUrl(request, customizedUrl);
                }
            }
            if (LOG.isDebugEnabled()) {
                LOG.debug("The url returned after customization is " + customizedUrl);
            }
            return customizedUrl;
        }
        if (LOG.isDebugEnabled()) {
            LOG.debug("No IAuthUrlCustomizer was set, the url " + customizedUrl + " wasn't modified");
        }
        return customizedUrl;
    }
}
