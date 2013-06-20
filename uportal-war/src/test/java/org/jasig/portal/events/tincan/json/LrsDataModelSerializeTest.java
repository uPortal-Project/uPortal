package org.jasig.portal.events.tincan.json;

import static org.junit.Assert.assertEquals;

import org.codehaus.jackson.map.ObjectMapper;
import org.jasig.portal.events.tincan.om.LrsActor;
import org.jasig.portal.events.tincan.om.LrsVerb;
import org.junit.Before;
import org.junit.Test;

public class LrsDataModelSerializeTest {
    private ObjectMapper objectMapper;
    
    @Before
    public void setup() {
        this.objectMapper = new ObjectMapper();
    }
    
    @Test
    public void testLrsVerbSerialize() throws Exception {
        final String result = this.objectMapper.writeValueAsString(LrsVerb.INITIALIZED);
        assertEquals("{\"id\":\"http://adlnet.gov/expapi/verbs/initialized\",\"display\":{\"en-US\":\"initialized\"}}", result);
    }
    
    @Test
    public void testLrsActorSerialize() throws Exception {
        final LrsActor lrsActor = new LrsActor("user@example.com", "John Doe");
        
        final String result = this.objectMapper.writeValueAsString(lrsActor);
        
        assertEquals("{\"mbox\":\"user@example.com\",\"name\":\"John Doe\",\"objectType\":\"Agent\"}", result);
    }
}
