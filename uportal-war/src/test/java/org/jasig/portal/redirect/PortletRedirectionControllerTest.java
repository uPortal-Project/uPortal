package org.jasig.portal.redirect;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

/**
 * @author Jen Bourey, jennifer.bourey@gmail.com
 */
public class PortletRedirectionControllerTest {
    
    PortletRedirectionController controller = new PortletRedirectionController();
    Map<String, IRedirectionUrl> services;
    
    @Mock HttpServletRequest request;
    
    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        
        when(request.getParameterValues("id")).thenReturn(new String[]{"student"});
    }
    
    @Test
    public void testExternalUrl() {
        ExternalRedirectionUrl url = new ExternalRedirectionUrl();
        url.setUrl("http://somewhere.com/something");
        
        Map<String,String[]> additionalParameters = new HashMap<String, String[]>();
        additionalParameters.put("action", new String[]{"show"});
        additionalParameters.put("list", new String[]{"v1","v2"});
        url.setAdditionalParameters(additionalParameters);
        
        Map<String,String> dynamicParameters = new HashMap<String, String>();
        dynamicParameters.put("id", "username");
        url.setDynamicParameters(dynamicParameters);
                
        String expected = "http://somewhere.com/something?action=show&list=v1&list=v2&username=student";
        String actual = controller.getUrlString(url, request);
        assertEquals(expected, actual);
    }

}
