package org.jasig.portal.portlets.googleanalytics;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;
import org.jasig.portal.spring.beans.factory.ObjectMapperFactoryBean;
import org.junit.Before;
import org.junit.Test;

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
