package org.jasig.portal.spring.spel;

import static org.mockito.Mockito.when;

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

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        provider = new PortalSpELServiceImpl();
        
        when(request.getContextPath()).thenReturn("/uPortal");
    }
    
    @Test
    public void testParser() {
        String replaced = provider.parseString("${1+1}/${3+4}/path", request);
        assert "2/7/path".equals(replaced);
    }
    
}
