package org.jasig.portal.spring.spel;

import static org.mockito.Mockito.when;

import javax.servlet.http.HttpServletRequest;

import org.jasig.portal.security.IPerson;
import org.jasig.portal.url.IPortalRequestUtils;
import org.jasig.portal.user.IUserInstance;
import org.jasig.portal.user.IUserInstanceManager;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.web.context.request.WebRequest;

/**
 * @author Jen Bourey, jbourey@unicon.net
 * @version $Revision$
 */
public class PortalSpELServiceImplTest {
    
    PortalSpELServiceImpl provider;
    @Mock WebRequest request;
    @Mock HttpServletRequest portalRequest;
    @Mock IPortalRequestUtils portalRequestUtils;
    @Mock IUserInstanceManager userInstanceManager;
    @Mock IUserInstance userInstance;
    @Mock IPerson person;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        provider = new PortalSpELServiceImpl();
        
        when(request.getContextPath()).thenReturn("/uPortal");
        when(portalRequestUtils.getOriginalPortalRequest(request)).thenReturn(portalRequest);
        when(userInstanceManager.getUserInstance(portalRequest)).thenReturn(userInstance);
        when(userInstance.getPerson()).thenReturn(person);
        
        provider.setPortalRequestUtils(portalRequestUtils);
        provider.setUserInstanceManager(userInstanceManager);
    }
    
    @Test
    public void testParser() {
        String replaced = provider.parseString("${1+1}/${3+4}/path", request);
        assert "2/7/path".equals(replaced);
    }
    
}
