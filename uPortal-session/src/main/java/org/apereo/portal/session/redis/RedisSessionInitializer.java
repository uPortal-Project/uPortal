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
package org.apereo.portal.session.redis;

import static org.apereo.portal.session.PortalSessionConstants.REDIS_STORE_TYPE;
import static org.apereo.portal.session.PortalSessionConstants.SESSION_STORE_TYPE_ENV_PROPERTY_NAME;
import static org.apereo.portal.session.PortalSessionConstants.SESSION_STORE_TYPE_SYSTEM_PROPERTY_NAME;

import org.springframework.session.web.context.AbstractHttpSessionApplicationInitializer;

/**
 * This class is needed to enable Spring Session Redis support in uPortal. It registers the filter
 * that is needed by Spring Session to manage the session with Redis. It also ensures that the
 * filter is only registered if the session store-type is configured for Redis. The filter could
 * have instead been added to web.xml, but that would not have allowed for the feature to be
 * enabled/disabled via configuration. Note that the application properties are not available during
 * initialization, and therefore we instead check for an environment variable or system property.
 */
public class RedisSessionInitializer extends AbstractHttpSessionApplicationInitializer {

    public RedisSessionInitializer() {
        // MUST pass null here to avoid having Spring Session create a root WebApplicationContext
        // that does not work with the current uPortal setup.
        super((Class<?>[]) null);
    }

    @Override
    public void onStartup(javax.servlet.ServletContext servletContext)
            throws javax.servlet.ServletException {
        if (REDIS_STORE_TYPE.equals(this.getStoreTypeConfiguredValue())) {
            super.onStartup(servletContext);
        }
    }

    private String getStoreTypeConfiguredValue() {
        String result = System.getProperty(SESSION_STORE_TYPE_SYSTEM_PROPERTY_NAME);
        if (result == null) {
            result = System.getenv(SESSION_STORE_TYPE_ENV_PROPERTY_NAME);
        }
        return result;
    }
}
