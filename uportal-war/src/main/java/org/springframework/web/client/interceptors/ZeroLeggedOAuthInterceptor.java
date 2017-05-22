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
import java.net.URI;
import java.net.URISyntaxException;
import javax.annotation.Resource;
import net.oauth.OAuthAccessor;
import net.oauth.OAuthConsumer;
import net.oauth.OAuthException;
import net.oauth.OAuthMessage;
import net.oauth.OAuthServiceProvider;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.core.env.PropertyResolver;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.util.Assert;

/**
 * Interceptor to add the authorization headers required for 0-legged oauth signing of requests.
 *
 */
public class ZeroLeggedOAuthInterceptor implements ClientHttpRequestInterceptor {
    private String id;
    private PropertyResolver propertyResolver;
    private RealmOAuthConsumer consumer;

    @Required
    public void setId(String id) {
        this.id = id;
    }

    @Resource(name = "propertyResolver")
    public void setPropertyResolver(final PropertyResolver resolver) {
        this.propertyResolver = resolver;
    }

    /**
     * Intercept a request and add the oauth headers.
     *
     * @param req the request
     * @param body the request body
     * @param execution the request execution.
     * @return the request response
     * @throws IOException on error
     */
    @Override
    public ClientHttpResponse intercept(
            HttpRequest req, byte[] body, ClientHttpRequestExecution execution) throws IOException {
        Assert.notNull(propertyResolver);
        Assert.notNull(id);

        try {
            String authString = getOAuthAuthString(req);
            req.getHeaders().add(Headers.Authorization.name(), authString);
        } catch (Exception e) {
            throw new IOException("Error building OAuth header", e);
        }

        return execution.execute(req, body);
    }

    /**
     * Get the oauth Authorization string.
     *
     * @param req the request
     * @return the Authorization string
     */
    private String getOAuthAuthString(HttpRequest req)
            throws OAuthException, IOException, URISyntaxException {
        RealmOAuthConsumer consumer = getConsumer();
        OAuthAccessor accessor = new OAuthAccessor(consumer);

        String method = req.getMethod().name();
        URI uri = req.getURI();

        OAuthMessage msg = accessor.newRequestMessage(method, uri.toString(), null);
        return msg.getAuthorizationHeader(consumer.getRealm());
    }

    /**
     * Get the OAuthConsumer. Will initialize it lazily.
     *
     * @return the OAuthConsumer object.
     */
    private synchronized RealmOAuthConsumer getConsumer() {
        // could just inject these, but I kinda prefer pushing this out
        // to the properties file...
        if (consumer == null) {
            OAuthServiceProvider serviceProvider = new OAuthServiceProvider("", "", "");
            String realm =
                    propertyResolver.getProperty(
                            "org.jasig.rest.interceptor.oauth." + id + ".realm");
            String consumerKey =
                    propertyResolver.getProperty(
                            "org.jasig.rest.interceptor.oauth." + id + ".consumerKey");
            String secretKey =
                    propertyResolver.getProperty(
                            "org.jasig.rest.interceptor.oauth." + id + ".secretKey");

            Assert.notNull(
                    consumerKey,
                    "The property \"org.jasig.rest.interceptor.oauth."
                            + id
                            + ".consumerKey\" must be set.");
            Assert.notNull(
                    secretKey,
                    "The property \"org.jasig.rest.interceptor.oauth."
                            + id
                            + ".secretKey\" must be set.");

            consumer = new RealmOAuthConsumer(consumerKey, secretKey, realm, serviceProvider);
        }

        return consumer;
    }

    /** Custom consumer that also tracks the realm. */
    private static class RealmOAuthConsumer extends OAuthConsumer {
        private String realm;

        public RealmOAuthConsumer(
                final String consumerKey,
                final String consumerSecret,
                final String realm,
                final OAuthServiceProvider serviceProvider) {
            super(null, consumerKey, consumerSecret, serviceProvider);
            this.realm = realm;
        }

        public String getRealm() {
            return realm;
        }
    }
}
