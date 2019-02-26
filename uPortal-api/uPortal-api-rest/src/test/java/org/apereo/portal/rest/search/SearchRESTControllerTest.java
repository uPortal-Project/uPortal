/**
 * Licensed to Apereo under one or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information regarding copyright ownership. Apereo
 * licenses this file to you under the Apache License, Version 2.0 (the "License"); you may not use
 * this file except in compliance with the License. You may obtain a copy of the License at the
 * following location:
 *
 * <p>
 *
 * <p>http://www.apache.org/licenses/LICENSE-2.0
 *
 * <p>
 *
 * <p>Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apereo.portal.rest.search;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class SearchRESTControllerTest {
    @InjectMocks private SearchRESTController searchRESTController;

    // For the SearchRESTController
    @Spy private Set<ISearchStrategy> searchStrategies = new HashSet<>();

    @Mock private ISearchStrategy strategy;

    @Mock private HttpServletRequest req;

    private MockHttpServletResponse res;

    private MockMvc mockMvc;

    public SearchRESTControllerTest() {
    }

    @Before
    public void setup() throws Exception {
        res = new MockHttpServletResponse();
        res.setOutputStreamAccessAllowed(true);
        MockitoAnnotations.initMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(searchRESTController).build();
        searchStrategies.add(new ISearchStrategy() {
            @Override
            public String getResultTypeName() {
                return "person";
            }

            @Override
            public List<?> search(String query, HttpServletRequest request) {
                final List<String> someDefinedResults = new ArrayList<>();
                someDefinedResults.add("test person data");
                return someDefinedResults;
            }
        });
        searchStrategies.add(new ISearchStrategy() {
            @Override
            public String getResultTypeName() {
                return "portlets";
            }

            @Override
            public List<?> search(String query, HttpServletRequest request) {
                final List<String> someDefinedResults = new ArrayList<>();
                someDefinedResults.add("test portlets data");
                return someDefinedResults;
            }
        });
    }

    @Test
    public void testSearchTypeMultiple() {
        try{
            ResultActions resultActions = mockMvc.perform(MockMvcRequestBuilders.get("/v5-0/portal/search").param("q", "bar").param("type", "portlets,person"));
            MvcResult mvcResult = resultActions.andReturn();
            Assert.assertEquals(200, mvcResult.getResponse().getStatus());
            Assert.assertEquals("{\"person\":[\"test person data\"],\"portlets\":[\"test portlets data\"]}", mvcResult.getResponse().getContentAsString());
        } catch (Exception e) {
            Assert.fail("Exception occurred:  " + e.getMessage());
        }
    }

    @Test
    public void testSearchTypeMultipleReversedAndExtra() {
        try{
            ResultActions resultActions = mockMvc.perform(MockMvcRequestBuilders.get("/v5-0/portal/search").param("q", "bar").param("type", "person,portlets,frameworks"));
            MvcResult mvcResult = resultActions.andReturn();
            Assert.assertEquals(200, mvcResult.getResponse().getStatus());
            Assert.assertEquals("{\"person\":[\"test person data\"],\"portlets\":[\"test portlets data\"]}", mvcResult.getResponse().getContentAsString());
        } catch (Exception e) {
            Assert.fail("Exception occurred:  " + e.getMessage());
        }
    }

    @Test
    public void testSearchTypeSingle() {
        try{
            ResultActions resultActions = mockMvc.perform(MockMvcRequestBuilders.get("/v5-0/portal/search").param("q", "bar").param("type", "portlets,person"));
            MvcResult mvcResult = resultActions.andReturn();
            Assert.assertEquals(200, mvcResult.getResponse().getStatus());
            Assert.assertEquals("{\"person\":[\"test person data\"],\"portlets\":[\"test portlets data\"]}", mvcResult.getResponse().getContentAsString());
        } catch (Exception e) {
            Assert.fail("Exception occurred:  " + e.getMessage());
        }
    }


    @Test
    public void testSearchTypeRandom() {
        try{
            ResultActions resultActions = mockMvc.perform(MockMvcRequestBuilders.get("/v5-0/portal/search").param("q", "bar").param("type", "a,b,c"));
            MvcResult mvcResult = resultActions.andReturn();
            Assert.assertEquals(404, mvcResult.getResponse().getStatus());
        } catch (Exception e) {
            Assert.fail("Exception occurred:  " + e.getMessage());
        }
    }


    @Test
    public void testSearchTypeRandomAndEmpty() {
        try{
            ResultActions resultActions = mockMvc.perform(MockMvcRequestBuilders.get("/v5-0/portal/search").param("q", "bar").param("type", "a,b,c,"));
            MvcResult mvcResult = resultActions.andReturn();
            Assert.assertEquals(200, mvcResult.getResponse().getStatus());
            Assert.assertEquals("{\"person\":[\"test person data\"],\"portlets\":[\"test portlets data\"]}", mvcResult.getResponse().getContentAsString());
        } catch (Exception e) {
            Assert.fail("Exception occurred:  " + e.getMessage());
        }
    }

    @Test
    public void testSearchTypeNone() {
        try{
            ResultActions resultActions = mockMvc.perform(MockMvcRequestBuilders.get("/v5-0/portal/search").param("q", "bar"));
            MvcResult mvcResult = resultActions.andReturn();
            Assert.assertEquals(200, mvcResult.getResponse().getStatus());
            Assert.assertEquals("{\"person\":[\"test person data\"],\"portlets\":[\"test portlets data\"]}", mvcResult.getResponse().getContentAsString());
        } catch (Exception e) {
            Assert.fail("Exception occurred:  " + e.getMessage());
        }
    }

    @Test
    public void testSearchTypeEmpty() {
        try{
            ResultActions resultActions = mockMvc.perform(MockMvcRequestBuilders.get("/v5-0/portal/search").param("q", "bar").param("type", ""));
            MvcResult mvcResult = resultActions.andReturn();
            Assert.assertEquals(200, mvcResult.getResponse().getStatus());
            Assert.assertEquals("{\"person\":[\"test person data\"],\"portlets\":[\"test portlets data\"]}", mvcResult.getResponse().getContentAsString());
        } catch (Exception e) {
            Assert.fail("Exception occurred:  " + e.getMessage());
        }
    }

    @Test
    public void testSearchQueryNone() {
        try{
            ResultActions resultActions = mockMvc.perform(MockMvcRequestBuilders.get("/v5-0/portal/search"));
            MvcResult mvcResult = resultActions.andReturn();
            Assert.assertEquals(400, mvcResult.getResponse().getStatus());
        } catch (Exception e) {
            Assert.fail("Exception occurred:  " + e.getMessage());
        }
    }

}
