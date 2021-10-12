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

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.hasKey;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.ByteArrayOutputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.junit.Test;
import org.springframework.core.env.PropertyResolver;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.ClientHttpRequest;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.web.client.RestTemplate;

public class BasicAuthInterceptorTest {
    @Test
    public void testInterceptorWithUsernamePassword() throws Exception {
        final String id = "test";
        final String username = "test";
        final String password = "test";

        PropertyResolver resolver = mock(PropertyResolver.class);
        when(resolver.getProperty(eq("org.jasig.rest.interceptor.basic-auth." + id + ".username")))
                .thenReturn(username);
        when(resolver.getProperty(eq("org.jasig.rest.interceptor.basic-auth." + id + ".password")))
                .thenReturn(password);

        doInterceptorTest(resolver, id, "dGVzdDp0ZXN0");
    }

    @Test
    public void testInterceptorWithAuthCode() throws Exception {
        final String id = "test";
        final String authCode = "c29tZUxvbmdVc2VybmFtZTpzb21lTG9uZ1Bhc3N3b3Jk";

        PropertyResolver resolver = mock(PropertyResolver.class);
        when(resolver.getProperty(eq("org.jasig.rest.interceptor.basic-auth." + id + ".authCode")))
                .thenReturn(authCode);

        doInterceptorTest(resolver, id, authCode);
    }

    private void doInterceptorTest(PropertyResolver resolver, String id, String expectedAuthCode)
            throws Exception {
        final String url = "http://www.test.com/lrs";
        final String data = "test";
        final String expectedHeader = "Basic " + expectedAuthCode;

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
        when(factory.createRequest(any(URI.class), any(HttpMethod.class))).thenReturn(client);

        // add the new interceptor...
        BasicAuthInterceptor interceptor = new BasicAuthInterceptor();
        interceptor.setPropertyResolver(resolver);
        interceptor.setId(id);
        List<ClientHttpRequestInterceptor> interceptors =
                new ArrayList<ClientHttpRequestInterceptor>();
        interceptors.add(interceptor);

        RestTemplate rest = new RestTemplate(factory);
        rest.setInterceptors(interceptors);

        // do it...
        rest.postForLocation(url, data, Collections.emptyMap());

        // make sure auth header is correctly set...
        assertThat(headers, hasKey(Headers.Authorization.name()));
        assertThat(headers.get(Headers.Authorization.name()), contains(expectedHeader));
    }
}
