package org.apereo.portal.url;

import org.apereo.portal.portlet.om.IPortletWindow;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import javax.servlet.http.HttpServletRequest;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;

/**
 * Test class for {@link ResourceUrlProviderImpl}.
 */
public class ResourceUrlProviderImplTest {

    @Mock
    private IPortletWindow portletWindowMock;

    @Mock
    private HttpServletRequest requestMock;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        requestMock = mock(HttpServletRequest.class);
    }

    /**
     * Test method for {@link ResourceUrlProviderImpl#setAbsoluteURL(String)}.
     */
    @Test
    public void testSetAbsoluteURL() {
        ResourceUrlProviderImpl urlProvider = new ResourceUrlProviderImpl(portletWindowMock, requestMock);
        String absolutePath = "http://test.com/test";
        urlProvider.setAbsoluteURL(absolutePath);
        assertEquals(absolutePath, urlProvider.toString());
    }

    /**
     * Test method for {@link ResourceUrlProviderImpl#setFullPath(String)}.
     */
    @Test
    public void testSetFullPath() {
        ResourceUrlProviderImpl urlProvider = new ResourceUrlProviderImpl(portletWindowMock, requestMock);
        String fullPath = "/portlet/resource";
        urlProvider.setFullPath(fullPath);
        assertEquals(fullPath, urlProvider.toString());
    }

    /**
     * Test method for {@link ResourceUrlProviderImpl#equals(Object)} and {@link ResourceUrlProviderImpl#hashCode()}.
     */
    @Test
    public void testEqualsAndHashCode() {
        ResourceUrlProviderImpl urlProvider1 = new ResourceUrlProviderImpl(portletWindowMock, requestMock);
        ResourceUrlProviderImpl urlProvider2 = new ResourceUrlProviderImpl(portletWindowMock, requestMock);

        String path = "/portlet/resource";
        urlProvider1.setFullPath(path);
        urlProvider2.setFullPath(path);

        assertEquals(urlProvider1, urlProvider2);
        assertEquals(urlProvider1.hashCode(), urlProvider2.hashCode());
    }
}
