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
package org.apereo.portal.security.provider.cas.clearpass;

import java.io.InputStream;
import java.util.Properties;
import org.apache.commons.io.IOUtils;
import org.apereo.portal.security.ISecurityContext;
import org.apereo.portal.security.ISecurityContextFactory;
import org.jasig.cas.client.util.CommonUtils;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

public final class PasswordCachingCasAssertionSecurityContextFactory
        implements ISecurityContextFactory {

    private static final String DEFAULT_PORTAL_SECURITY_PROPERTY_FILE =
            "properties/security.properties";

    private static final String CLEARPASS_CAS_URL_PROPERTY =
            PasswordCachingCasAssertionSecurityContextFactory.class.getName() + ".clearPassCasUrl";

    private final String clearPassUrl;

    public PasswordCachingCasAssertionSecurityContextFactory() {
        final Resource resource =
                new ClassPathResource(
                        DEFAULT_PORTAL_SECURITY_PROPERTY_FILE, getClass().getClassLoader());
        final Properties securityProperties = new Properties();
        InputStream inputStream = null;

        try {
            inputStream = resource.getInputStream();
            securityProperties.load(inputStream);
            this.clearPassUrl = securityProperties.getProperty(CLEARPASS_CAS_URL_PROPERTY);
        } catch (final Exception e) {
            throw new RuntimeException(e);
        } finally {
            IOUtils.closeQuietly(inputStream);
        }
    }

    public ISecurityContext getSecurityContext() {
        if (CommonUtils.isNotBlank(this.clearPassUrl)) {
            return new PasswordCachingCasAssertionSecurityContext(this.clearPassUrl);
        }

        throw new IllegalStateException(
                String.format(
                        "clearPassUrl not configured.  Cannot create an instance of [%s] without it.",
                        getClass().getSimpleName()));
    }
}
