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
package org.apereo.portal.portlets.search.google;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.when;

import java.io.ByteArrayInputStream;
import java.net.URI;
import java.util.LinkedList;
import java.util.List;
import javax.portlet.PortletRequest;
import org.apache.commons.io.IOUtils;
import org.apereo.portal.search.SearchRequest;
import org.apereo.portal.search.SearchResults;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.client.ClientHttpRequest;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.RestTemplate;

/** Tests a GoogleAjaxSearchService */
@RunWith(MockitoJUnitRunner.class)
public class GoogleCustomSearchServiceTest {
    @InjectMocks
    private GoogleCustomSearchService googleSearchController = new GoogleCustomSearchService();

    @InjectMocks private RestTemplate restTemplate = new RestTemplate();
    @Mock private PortletRequest portletRequest;
    @Mock private ClientHttpRequestFactory clientHttpRequestFactory;
    @Mock private ClientHttpRequest clientHttpRequest;
    @Mock private ClientHttpResponse clientHttpResponse;
    @Mock private HttpHeaders requestHttpHeaders;
    @Mock private HttpHeaders responseHttpHeaders;

    @Before
    public void setup() throws Exception {
        // Add handling of text/javascript content type
        final MappingJackson2HttpMessageConverter converter =
                new MappingJackson2HttpMessageConverter();
        final List<MediaType> supportedMediaTypes =
                new LinkedList<MediaType>(converter.getSupportedMediaTypes());
        final MediaType textJavascriptMediaType =
                new MediaType(
                        "text", "javascript", MappingJackson2HttpMessageConverter.DEFAULT_CHARSET);
        supportedMediaTypes.add(textJavascriptMediaType);
        converter.setSupportedMediaTypes(supportedMediaTypes);
        restTemplate.getMessageConverters().add(converter);

        this.googleSearchController.setRestOperations(restTemplate);
        this.googleSearchController.setKey("12345");

        // Uncomment to make real requests
        // restTemplate.setRequestFactory(new CommonsClientHttpRequestFactory());

        when(clientHttpRequest.getHeaders()).thenReturn(requestHttpHeaders);
        when(clientHttpRequest.execute()).thenReturn(clientHttpResponse);
        when(clientHttpResponse.getStatusCode()).thenReturn(HttpStatus.OK);
        when(clientHttpResponse.getHeaders()).thenReturn(responseHttpHeaders);
        when(responseHttpHeaders.getContentType()).thenReturn(textJavascriptMediaType);
    }

    @Test
    public void testGoogleSearchController() throws Exception {
        final String json =
                IOUtils.toString(
                        this.getClass()
                                .getResourceAsStream(
                                        "/org/apereo/portal/portlets/search/google/result.json"));

        when(clientHttpRequestFactory.createRequest(
                        new URI(
                                "https://www.googleapis.com/customsearch/v1?q=news&key=12345&userIp=128.104.17.46&start=1&cx="),
                        HttpMethod.GET))
                .thenReturn(clientHttpRequest);
        when(clientHttpResponse.getBody()).thenReturn(new ByteArrayInputStream(json.getBytes()));
        when(responseHttpHeaders.getContentLength()).thenReturn((long) json.length());
        when(portletRequest.getProperty("REMOTE_ADDR")).thenReturn("128.104.17.46");

        final SearchRequest query = new SearchRequest();
        query.setSearchTerms("news");

        final SearchResults results =
                googleSearchController.getSearchResults(portletRequest, query);

        assertNotNull(results);
        assertEquals(2, results.getSearchResult().size());
    }
}
