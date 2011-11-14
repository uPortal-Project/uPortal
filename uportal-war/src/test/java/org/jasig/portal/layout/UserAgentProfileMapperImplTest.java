package org.jasig.portal.layout;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

import javax.servlet.http.HttpServletRequest;

import org.jasig.portal.security.IPerson;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class UserAgentProfileMapperImplTest {

    UserAgentProfileMapper mapper = new UserAgentProfileMapper();
    @Mock IPerson person;
    @Mock HttpServletRequest request;
    
    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        
        mapper.setDefaultProfileName("profile");
        mapper.setUserAgentHeader("agent");
    }

    @Test
    public void testDefault() {
        final String fname = mapper.getProfileFname(person, request);
        assertEquals("profile", fname);
    }
    
    @Test
    public void testMobileProfile() {
        when(request.getHeader("agent")).thenReturn("iPhone", "Android", "Blackberry");
                
        final String fname = mapper.getProfileFname(person, request);
        assertEquals("mobileDefault", fname);
       
    }
    
}
