/**
 * Licensed to Jasig under one or more contributor license
 * agreements. See the NOTICE file distributed with this work
 * for additional information regarding copyright ownership.
 * Jasig licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a
 * copy of the License at:
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.jasig.portal.rendering.xslt;

import java.util.Collections;
import java.util.Locale;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.jasig.portal.i18n.LocaleManager;
import org.jasig.portal.user.IUserInstance;
import org.jasig.portal.user.IUserInstanceManager;
import org.jasig.portal.utils.cache.CacheKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Present's the user's preferred locale as a transformer parameter
 * 
 * @author Eric Dalquist
 * @version $Revision$
 */
public class LocaleTransformerConfigurationSource extends TransformerConfigurationSourceAdapter {
    protected final Logger logger = LoggerFactory.getLogger(this.getClass());
    
    private IUserInstanceManager userInstanceManager;
    
    @Autowired
    public void setUserInstanceManager(IUserInstanceManager userInstanceManager) {
        this.userInstanceManager = userInstanceManager;
    }
    
    private LocaleManager getLocaleManager(HttpServletRequest request) {
        final IUserInstance userInstance = this.userInstanceManager.getUserInstance(request);
        return userInstance.getLocaleManager();
    }

    /* (non-Javadoc)
     * @see org.jasig.portal.rendering.xslt.TransformerConfigurationSource#getParameters(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
     */
    @Override
    public Map<String, Object> getParameters(HttpServletRequest request, HttpServletResponse response) {
        final LocaleManager localeManager = this.getLocaleManager(request);
        
        final Locale[] locales = localeManager.getLocales();
        if (locales != null && locales.length > 0 && locales[0] != null) {
            final String locale = locales[0].toString();
            final String xslLocale = locale.replace('_', '-');
            this.logger.debug("Setting USER_LANG to {}", xslLocale);
            return Collections.singletonMap("USER_LANG", (Object)xslLocale);
        }
        
        return null;
    }

    /* (non-Javadoc)
     * @see org.jasig.portal.rendering.xslt.TransformerConfigurationSource#getCacheKey(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
     */
    @Override
    public CacheKey getCacheKey(HttpServletRequest request, HttpServletResponse response) {
        final LocaleManager localeManager = this.getLocaleManager(request);
        
        final Locale[] locales = localeManager.getLocales();
        if (locales != null && locales.length > 0 && locales[0] != null) {
            final String locale = locales[0].toString();
            final String xslLocale = locale.replace('_', '-');
            return CacheKey.build(this.getClass().getName(), xslLocale);
        }
        
        return null;
    }

}
