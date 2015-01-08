/**
 * Licensed to Apereo under one or more contributor license
 * agreements. See the NOTICE file distributed with this work
 * for additional information regarding copyright ownership.
 * Apereo licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License.  You may obtain a
 * copy of the License at the following location:
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.jasig.portal.portlets.googleanalytics;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.jasig.portal.spring.beans.factory.ObjectMapperFactoryBean;
import org.junit.Before;
import org.junit.Test;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class GoogleAnalyticsConfigControllerTest {
    private ObjectMapper objectMapper;
    
    @Before
    public void setup() throws Exception {
        final ObjectMapperFactoryBean omfb = new ObjectMapperFactoryBean();
        omfb.afterPropertiesSet();
        objectMapper = omfb.getObject();
    }
    
    @Test
    public void testJsonConversion() throws Exception {
        final String json = "{\"defaultConfig\":{\"name\":\"Default Property\",\"propertyId\":\"ABCD\",\"config\":[{\"name\":\"f\",\"value\":\"e\"},{\"name\":\"a\",\"value\":\"e\"}],\"dimensionGroups\":[{\"name\":\"r\",\"value\":\"t\"}]},\"institutions\":[{\"name\":\"foobar\",\"propertyId\":\"1234\",\"config\":[],\"dimensionGroups\":[]},{\"name\":\"test\",\"propertyId\":\"\",\"config\":[],\"dimensionGroups\":[]}]}";

        JsonNode jsonData = objectMapper.readTree(json);
        assertNotNull(jsonData);
        
        jsonData = objectMapper.readValue(json, JsonNode.class);
        assertNotNull(jsonData);
        
        
        final String jsonStr = objectMapper.writeValueAsString(jsonData);
        
        assertEquals(json, jsonStr);
    }
}
