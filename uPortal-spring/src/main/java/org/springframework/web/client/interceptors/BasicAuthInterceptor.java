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
package org.springframework.web.client.interceptors;

import java.io.IOException;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.core.env.PropertyResolver;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.util.Assert;

/**
 * Interceptor for RestTemplate that add the headers required for Basic authentication.
 *
 */
public class BasicAuthInterceptor implements ClientHttpRequestInterceptor {
    private String id;
    private PropertyResolver propertyResolver;
    private String authHeader;

    @Required
    public void setId(String id) {
        this.id = id;
    }

    @Autowired
    public void setPropertyResolver(final PropertyResolver propertyResolver) {
        this.propertyResolver = propertyResolver;
    }

    @Override
    public ClientHttpResponse intercept(
            HttpRequest req, byte[] bytes, ClientHttpRequestExecution execution)
            throws IOException {
        req.getHeaders().add(Headers.Authorization.name(), getAuthHeader());

        return execution.execute(req, bytes);
    }

    private synchronized String getAuthHeader() {
        if (authHeader == null) {
            String authCode =
                    propertyResolver.getProperty(
                            "org.jasig.rest.interceptor.basic-auth." + id + ".authCode");

            if (StringUtils.isBlank(authCode)) {
                String username =
                        propertyResolver.getProperty(
                                "org.jasig.rest.interceptor.basic-auth." + id + ".username");
                String password =
                        propertyResolver.getProperty(
                                "org.jasig.rest.interceptor.basic-auth." + id + ".password");

                Assert.notNull(
                        username,
                        "The property \"org.jasig.rest.interceptor.basic-auth."
                                + id
                                + ".username\" must be set.");
                Assert.notNull(
                        password,
                        "The property \"org.jasig.rest.interceptor.basic-auth."
                                + id
                                + ".password\" must be set.");

                String auth = username + ":" + password;
                authCode = new String(Base64.encodeBase64(auth.getBytes()));
            }

            authHeader = "Basic " + authCode;
        }

        return authHeader;
    }
}
