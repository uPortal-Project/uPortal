package org.jasig.portal.events.tincan.json;

import static org.junit.Assert.assertEquals;

import java.util.Locale;

import org.codehaus.jackson.map.ObjectMapper;
import org.jasig.portal.events.tincan.om.LocalizedString;
import org.jasig.portal.events.tincan.om.LrsActor;
import org.jasig.portal.events.tincan.om.LrsObject;
import org.jasig.portal.events.tincan.om.LrsVerb;
import org.jasig.portal.url.UrlStringBuilder;
import org.junit.Before;
import org.junit.Test;

import com.google.common.collect.ImmutableMap;

public class LrsDataModelSerializeTest {
    private ObjectMapper objectMapper;
    
    @Before
    public void setup() {
        this.objectMapper = new ObjectMapper();
    }
    
    @Test
    public void testLrsVerbSerialize() throws Exception {
        final String result = this.objectMapper.writeValueAsString(LrsVerb.INITIALIZED);
        assertEquals("{\"id\":\"http://adlnet.gov/expapi/verbs/initialized\",\"display\":{\"en-us\":\"initialized\"}}", result);
    }
    
    @Test
    public void testLrsActorSerialize() throws Exception {
        final LrsActor lrsActor = new LrsActor("user@example.com", "John Doe");
        
        final String result = this.objectMapper.writeValueAsString(lrsActor);
        
        assertEquals("{\"mbox\":\"user@example.com\",\"name\":\"John Doe\",\"objectType\":\"Agent\"}", result);
    }
    
    @Test
    public void testLrcObjectSerialize() throws Exception {
        final UrlStringBuilder idBuilder = new UrlStringBuilder("UTF-8", "http", "www.example.com");
        idBuilder.addPath("portlets", "fname");
        
        final LrsObject lrsObject = new LrsObject(idBuilder, "Activity", 
                ImmutableMap.of("name", new LocalizedString(new Locale("en", "us"), "Portlet Name"),
                                "description", new LocalizedString(new Locale("en", "us"), "Portlet Description")));
        
        final String result = this.objectMapper.writeValueAsString(lrsObject);
        
        assertEquals("{\"id\":\"http://www.example.com/portlets/fname\",\"objectType\":\"Activity\",\"definition\":{\"name\":{\"en_US\":\"Portlet Name\"},\"description\":{\"en_US\":\"Portlet Description\"}}}", result);
    }
}
