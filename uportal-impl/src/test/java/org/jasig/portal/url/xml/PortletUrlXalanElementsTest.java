/**
 * Copyright 2007 The JA-SIG Collaborative.  All rights reserved.
 * See license distributed with this file and
 * available online at http://www.uportal.org/license.html
 */
package org.jasig.portal.url.xml;

import java.io.StringWriter;

import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import junit.framework.TestCase;

import org.apache.commons.io.IOUtils;
import org.custommonkey.xmlunit.Diff;
import org.custommonkey.xmlunit.XMLUnit;
import org.easymock.EasyMock;
import org.jasig.portal.portlet.registry.IPortletWindowRegistry;
import org.jasig.portal.url.IPortalPortletUrl;
import org.jasig.portal.url.IPortalUrlProvider;
import org.springframework.mock.web.MockHttpServletRequest;

/**
 * @author Eric Dalquist
 * @version $Revision$
 */
public class PortletUrlXalanElementsTest extends TestCase {

    public void testPortalUrlElement() throws Exception {
        final MockHttpServletRequest request = new MockHttpServletRequest();
        
        
        final IPortletWindowRegistry portletWindowRegistry = EasyMock.createMock(IPortletWindowRegistry.class);
        
        
        final IPortalPortletUrl portalPortletUrl = EasyMock.createMock(IPortalPortletUrl.class);
        portalPortletUrl.addPortletParameter("foo", "bar");
        portalPortletUrl.addPortletParameter("foo", "bor");
        portalPortletUrl.addPortletParameter("page", "42");
        portalPortletUrl.addPortletParameter("node", "element");
        portalPortletUrl.addPortletParameter("empty", "");
        portalPortletUrl.addPortalParameter("something", "for the portal");
        portalPortletUrl.setAction(false);
        EasyMock.expectLastCall(); //works for all previous void calls
        
        EasyMock.expect(portalPortletUrl.getUrlString()).andReturn("/uPortal/home/normal/bookmarks.1/render.uP");

        
        final IPortalUrlProvider portalUrlProvider = EasyMock.createMock(IPortalUrlProvider.class);
        
        
        EasyMock.expect(portalUrlProvider.getPortletUrlByFName(request, "bookmarks")).andReturn(portalPortletUrl);
        
        
        
        EasyMock.replay(portalUrlProvider, portletWindowRegistry, portalPortletUrl);
        
        
        
        final TransformerFactory tFactory = TransformerFactory.newInstance();
        final Transformer transformer = tFactory.newTransformer(new StreamSource(this.getClass().getResourceAsStream("portletUrlTest.xsl")));
        
        //Setup the transformer parameters
        transformer.setParameter(PortletUrlXalanElements.PORTAL_URL_PROVIDER_PARAMETER, portalUrlProvider);
        transformer.setParameter(PortletUrlXalanElements.CURRENT_PORTAL_REQUEST, request);
        transformer.setParameter(PortletUrlXalanElements.PORTLET_WINDOW_REGISTRY_PARAMETER, portletWindowRegistry);
        
        
        final StringWriter resultWriter = new StringWriter();

        // set up configuration in the transformer impl
        final StreamSource sourceStream = new StreamSource(this.getClass().getResourceAsStream("test.xml"));
        transformer.transform(sourceStream, new StreamResult(resultWriter));
        
        final String result = resultWriter.getBuffer().toString();
        final String expected = IOUtils.toString(this.getClass().getResourceAsStream("portletUrlResult.xml"));

        XMLUnit.setIgnoreWhitespace(true);
        Diff d = new Diff(expected, result);
        assertTrue("Transformation result differs from what's expected" + d, d.similar());
        
        EasyMock.verify(portalUrlProvider, portletWindowRegistry, portalPortletUrl);
    }
}
