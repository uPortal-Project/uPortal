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
package org.apereo.portal.redirect;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import javax.servlet.http.HttpServletRequest;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class PortletRedirectionControllerTest {

    PortletRedirectionController controller = new PortletRedirectionController();
    Map<String, IRedirectionUrl> services;

    @Mock HttpServletRequest request;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);

        when(request.getParameterValues("id")).thenReturn(new String[] {"student"});
    }

    @Test
    public void testExternalUrl() throws URISyntaxException {
        ExternalRedirectionUrl url = new ExternalRedirectionUrl();
        url.setUrl("http://somewhere.com/something");

        Map<String, String[]> additionalParameters = new HashMap<String, String[]>();
        additionalParameters.put("action", new String[] {"show"});
        additionalParameters.put("list", new String[] {"v1", "v2"});
        url.setAdditionalParameters(additionalParameters);

        Map<String, String> dynamicParameters = new HashMap<String, String>();
        dynamicParameters.put("id", "username");
        url.setDynamicParameters(dynamicParameters);

        String expected =
                "http://somewhere.com/something?action=show&list=v1&list=v2&username=student";
        String actual = controller.getUrlString(url, request, new ArrayList<String>());
        URI uri1 = new URI(expected);
        URI uri2 = new URI(actual);

        assertTrue(
                uri1.getPath().equals(uri2.getPath())
                        && compareQueryParameters(uri1.getQuery(), uri2.getQuery()));
    }

    private static boolean compareQueryParameters(String query1, String query2) {
        Set<String> params1 = new HashSet<>(Arrays.asList(query1.split("&")));
        Set<String> params2 = new HashSet<>(Arrays.asList(query2.split("&")));
        return params1.equals(params2);
    }
}
