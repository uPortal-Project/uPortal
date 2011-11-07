package org.jasig.portal.layout;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.jasig.portal.security.IPerson;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class SessionAttributeProfileMapperImplTest {
    
    SessionAttributeProfileMapperImpl mapper = new SessionAttributeProfileMapperImpl();
    @Mock IPerson person;
    @Mock HttpServletRequest request;
    @Mock HttpSession session;
    
    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        when(request.getSession(false)).thenReturn(session);
        
        mapper.setDefaultProfileName("profile");
        mapper.setAttributeName("key");
        
        Map<String,String> mappings = new HashMap<String,String>();
        mappings.put("key1", "fname1");
        mappings.put("key2", "fname2");
        mapper.setMappings(mappings);
    }
    
    @Test
    public void testDefault() {
        final String fname = mapper.getProfileFname(person, request);
        assertEquals("profile", fname);
    }

    @Test
    public void testMatchedProfile() {
        when(session.getAttribute("key")).thenReturn("key2");
        final String fname = mapper.getProfileFname(person, request);
        assertEquals("fname2", fname);
    }

}
