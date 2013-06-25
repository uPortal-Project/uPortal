package org.jasig.portal.events.tincan.json;

import static org.junit.Assert.assertEquals;

import java.net.URI;
import java.util.Locale;

import org.codehaus.jackson.map.ObjectMapper;
import org.jasig.portal.events.tincan.UrnBuilder;
import org.jasig.portal.events.tincan.om.LocalizedString;
import org.jasig.portal.events.tincan.om.LrsActor;
import org.jasig.portal.events.tincan.om.LrsObject;
import org.jasig.portal.events.tincan.om.LrsStatement;
import org.jasig.portal.events.tincan.om.LrsVerb;
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
    public void testLrsObjectSerialize() throws Exception {
        final UrnBuilder idBuilder = new UrnBuilder("UTF-8", "tincan", "uportal", "activities", "portlet", "fname");
        final URI id = idBuilder.getUri();
        
        final LrsObject lrsObject = new LrsObject(id, "Activity", 
                ImmutableMap.of("name", new LocalizedString(new Locale("en", "us"), "Portlet Name"),
                                "description", new LocalizedString(new Locale("en", "us"), "Portlet Description")));
        
        final String result = this.objectMapper.writeValueAsString(lrsObject);
        
        assertEquals("{\"id\":\"urn:tincan:uportal:activities:portlet:fname\",\"objectType\":\"Activity\",\"definition\":{\"name\":{\"en-US\":\"Portlet Name\"},\"description\":{\"en-US\":\"Portlet Description\"}}}", result);
    }
    
    @Test
    public void testLrsStatementSerialize() throws Exception {
        final LrsActor lrsActor = new LrsActor("user@example.com", "John Doe");
        
        final UrnBuilder idBuilder = new UrnBuilder("UTF-8", "tincan", "uportal", "activities", "portlet", "fname");
        final URI id = idBuilder.getUri();
        
        final LrsObject lrsObject = new LrsObject(id, "Activity", 
                ImmutableMap.of("name", new LocalizedString(new Locale("en", "us"), "Portlet Name"),
                                "description", new LocalizedString(new Locale("en", "us"), "Portlet Description")));

        final LrsStatement lrsStatement = new LrsStatement(lrsActor, LrsVerb.INITIALIZED, lrsObject);
        
        final String result = this.objectMapper.writeValueAsString(lrsStatement);
        
        assertEquals("{\"actor\":{\"mbox\":\"user@example.com\",\"name\":\"John Doe\",\"objectType\":\"Agent\"},\"verb\":{\"id\":\"http://adlnet.gov/expapi/verbs/initialized\",\"display\":{\"en-us\":\"initialized\"}},\"object\":{\"id\":\"urn:tincan:uportal:activities:portlet:fname\",\"objectType\":\"Activity\",\"definition\":{\"name\":{\"en-US\":\"Portlet Name\"},\"description\":{\"en-US\":\"Portlet Description\"}}}}", result);
    }
}
