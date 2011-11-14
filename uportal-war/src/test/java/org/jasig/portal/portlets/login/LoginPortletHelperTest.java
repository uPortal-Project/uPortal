package org.jasig.portal.portlets.login;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.Map;

import javax.portlet.PortletRequest;
import javax.portlet.PortletSession;
import javax.servlet.http.HttpServletRequest;

import org.jasig.portal.IUserPreferencesManager;
import org.jasig.portal.IUserProfile;
import org.jasig.portal.portlets.login.LoginPortletHelper;
import org.jasig.portal.security.mvc.LoginController;
import org.jasig.portal.url.IPortalRequestUtils;
import org.jasig.portal.user.IUserInstance;
import org.jasig.portal.user.IUserInstanceManager;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class LoginPortletHelperTest {

    LoginPortletHelper helper = new LoginPortletHelper();
    Map<String,String> mappings = new HashMap<String,String>();
    @Mock IPortalRequestUtils portalRequestUtils;
    @Mock PortletRequest portletRequest;
    @Mock PortletSession session;
    @Mock HttpServletRequest servletRequest;
    @Mock IUserInstanceManager userInstanceManager;
    @Mock IUserInstance userInstance;
    @Mock IUserPreferencesManager userPreferencesManager;
    @Mock IUserProfile userProfile;
    
    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        helper.setPortalRequestUtils(portalRequestUtils);
        helper.setUserInstanceManager(userInstanceManager);
        
        when(portletRequest.getPortletSession()).thenReturn(session);
        when(portalRequestUtils.getPortletHttpRequest(portletRequest)).thenReturn(servletRequest);
        when(userInstanceManager.getUserInstance(servletRequest)).thenReturn(userInstance);
        when(userInstance.getPreferencesManager()).thenReturn(userPreferencesManager);
        when(userPreferencesManager.getUserProfile()).thenReturn(userProfile);
        when(userProfile.getProfileFname()).thenReturn("default");
        
        mappings.put("mobile", "mobileDefault");
        mappings.put("desktop", "default");
        helper.setProfileMappings(mappings);
    }
    
    @Test
    public void testInSessionProfile() {
        when(session.getAttribute(LoginController.REQUESTED_PROFILE_KEY, PortletSession.APPLICATION_SCOPE)).thenReturn("mobile");
        String profile = helper.getSelectedProfile(portletRequest);
        assertEquals("mobile", profile);
    }
    
    @Test
    public void testCurrentProfile() {
        String profile = helper.getSelectedProfile(portletRequest); 
        assertEquals("desktop", profile);
    }
    
    @Test
    public void testUnmatchedProfile() {
        when(userProfile.getProfileFname()).thenReturn("somethingelse");
        String profile = helper.getSelectedProfile(portletRequest);
        assertNull(profile);
    }
    
}
