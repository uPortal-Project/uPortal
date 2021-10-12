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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasKey;
import static org.hamcrest.core.Is.is;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.ByteArrayOutputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.apache.commons.lang3.StringUtils;
import org.junit.Test;
import org.mockito.ArgumentMatchers;
import org.springframework.core.env.PropertyResolver;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.ClientHttpRequest;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.web.client.RestTemplate;

public class ZeroLeggedOAuthInterceptorTest {
    @Test
    public void testInterceptor() throws Exception {
        final String url = "http://www.test.com/lrs?param1=val1&param2=val2";
        final String data = "test";
        final String id = "test";
        final String realm = "realm";
        final String consumerKey = "consumerKey";
        final String secretKey = "secretKey";

        PropertyResolver resolver = mock(PropertyResolver.class);
        when(resolver.getProperty(
                        ArgumentMatchers.eq("org.jasig.rest.interceptor.oauth." + id + ".realm")))
                .thenReturn(realm);
        when(resolver.getProperty(
                        ArgumentMatchers.eq(
                                "org.jasig.rest.interceptor.oauth." + id + ".consumerKey")))
                .thenReturn(consumerKey);
        when(resolver.getProperty(
                        ArgumentMatchers.eq(
                                "org.jasig.rest.interceptor.oauth." + id + ".secretKey")))
                .thenReturn(secretKey);

        // holder for the headers...
        HttpHeaders headers = new HttpHeaders();

        // Mock guts of RestTemplate so no need to actually hit the web...
        ClientHttpResponse resp = mock(ClientHttpResponse.class);
        when(resp.getStatusCode()).thenReturn(HttpStatus.ACCEPTED);
        when(resp.getHeaders()).thenReturn(new HttpHeaders());

        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        ClientHttpRequest client = mock(ClientHttpRequest.class);
        when(client.getHeaders()).thenReturn(headers);
        when(client.getBody()).thenReturn(buffer);
        when(client.execute()).thenReturn(resp);

        ClientHttpRequestFactory factory = mock(ClientHttpRequestFactory.class);
        when(factory.createRequest(
                        ArgumentMatchers.any(URI.class), ArgumentMatchers.any(HttpMethod.class)))
                .thenReturn(client);

        // add the new interceptor...
        ZeroLeggedOAuthInterceptor interceptor = new ZeroLeggedOAuthInterceptor();
        interceptor.setPropertyResolver(resolver);
        interceptor.setId(id);
        List<ClientHttpRequestInterceptor> interceptors = new ArrayList<>();
        interceptors.add(interceptor);

        RestTemplate rest = new RestTemplate(factory);
        rest.setInterceptors(interceptors);

        rest.postForLocation(url, data, Collections.emptyMap());

        // make sure auth header is correctly set...
        assertThat(headers, hasKey(Headers.Authorization.name()));

        String authHeader = headers.get(Headers.Authorization.name()).get(0);
        assertThat(authHeader, containsString("OAuth realm=\"" + realm + "\""));
        assertThat(authHeader, containsString("oauth_consumer_key=\"" + consumerKey + "\""));
        // for now, only supports HMAC-SHA1.  May have to fix later...
        assertThat(authHeader, containsString("oauth_signature_method=\"HMAC-SHA1\""));
        assertThat(authHeader, containsString("oauth_version=\"1.0\""));
        assertThat(authHeader, containsString("oauth_timestamp="));
        assertThat(authHeader, containsString("oauth_nonce="));
        assertThat(authHeader, containsString("oauth_signature="));

        // oauth lib will create 2 oauth_signature params if you call sign
        // multiple times.  Make sure only get 1.
        assertThat(StringUtils.countMatches(authHeader, "oauth_signature="), is(1));
    }
}
