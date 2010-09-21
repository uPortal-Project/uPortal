/**
 * Licensed to Jasig under one or more contributor license
 * agreements. See the NOTICE file distributed with this work
 * for additional information regarding copyright ownership.
 * Jasig licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a
 * copy of the License at:
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.jasig.portal.url.xml;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.expectLastCall;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;

import static org.junit.Assert.assertTrue;

import java.io.StringWriter;

import javax.portlet.PortletMode;
import javax.portlet.WindowState;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.LogFactory;
import org.apache.pluto.container.PortletURLProvider.TYPE;
import org.custommonkey.xmlunit.Diff;
import org.custommonkey.xmlunit.XMLUnit;
import org.easymock.EasyMock;
import org.jasig.portal.mock.portlet.om.MockPortletWindowId;
import org.jasig.portal.portlet.registry.IPortletWindowRegistry;
import org.jasig.portal.url.IBasePortalUrl;
import org.jasig.portal.url.ILayoutPortalUrl;
import org.jasig.portal.url.IPortalUrlProvider;
import org.jasig.portal.url.IPortletPortalUrl;
import org.jasig.portal.xml.ResourceLoaderURIResolver;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassRelativeResourceLoader;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.util.xml.SimpleTransformErrorListener;

/**
 * @author Eric Dalquist
 * @version $Revision$
 */
public class XsltPortalUrlProviderTest {
    protected final Logger logger = LoggerFactory.getLogger(getClass());
    
    private TransformerFactory tFactory;
    private StreamSource xmlSource;
    
    @Before
    public void setup() throws Exception {
        final ResourceLoaderURIResolver resolver = new ResourceLoaderURIResolver(new ClassRelativeResourceLoader(getClass()));
        
        this.tFactory = TransformerFactory.newInstance();
        this.tFactory.setURIResolver(resolver);
        this.tFactory.setErrorListener(new SimpleTransformErrorListener(LogFactory.getLog(getClass())));
        
        xmlSource = new StreamSource(XsltPortalUrlProviderTest.class.getResourceAsStream("test.xml"));
    }
    
    
    @Test
    public void testLayoutUrl() throws Exception {
        final MockHttpServletRequest request = new MockHttpServletRequest();
        
        final ILayoutPortalUrl layoutPortalUrl = EasyMock.createMock(ILayoutPortalUrl.class);
        layoutPortalUrl.addLayoutParameter("remove_target", "foo");
        layoutPortalUrl.setAction(true);
        EasyMock.expectLastCall(); //works for all previous void calls
        
        EasyMock.expect(layoutPortalUrl.getUrlString()).andReturn("/uPortal/home/normal/render.uP?layoutUrl").times(2);

        
        final IPortalUrlProvider portalUrlProvider = EasyMock.createMock(IPortalUrlProvider.class);
        
        EasyMock.expect(portalUrlProvider.getFolderUrlByNodeId(request, "foo")).andReturn(layoutPortalUrl).times(2);
        
        
        EasyMock.replay(portalUrlProvider, layoutPortalUrl);
        
        
        
        final Transformer transformer = tFactory.newTransformer(new StreamSource(this.getClass().getResourceAsStream("layoutUrlTest.xsl")));
        
        //Setup the transformer parameters
        final XsltPortalUrlProvider xsltPortalUrlProvider = new XsltPortalUrlProvider();
        xsltPortalUrlProvider.setUrlProvider(portalUrlProvider);
        transformer.setParameter(XsltPortalUrlProvider.XSLT_PORTAL_URL_PROVIDER, xsltPortalUrlProvider);
        transformer.setParameter("CURRENT_REQUEST", request);
        

        // set up configuration in the transformer impl
        final StringWriter resultWriter = new StringWriter();
        transformer.transform(xmlSource, new StreamResult(resultWriter));
        
        EasyMock.verify(portalUrlProvider, layoutPortalUrl);

        final String result = resultWriter.getBuffer().toString();
        logger.debug(result);
        final String expected = IOUtils.toString(this.getClass().getResourceAsStream("layoutUrlResult.xml"));

        XMLUnit.setIgnoreWhitespace(true);
        Diff d = new Diff(expected, result);
        assertTrue("Transformation result differs from what's expected" + d, d.similar());
        
    }
    
    
    @Test
    public void testPortletUrl() throws Exception {
        final MockHttpServletRequest request = new MockHttpServletRequest();
        
        final IBasePortalUrl basePortalUrl = EasyMock.createMock(IBasePortalUrl.class);
        basePortalUrl.addPortalParameter("foo", "bar");
        basePortalUrl.addPortalParameter("foo", "bor");
        basePortalUrl.addPortalParameter("page", "42");
        basePortalUrl.addPortalParameter("node", "element");
        basePortalUrl.addPortalParameter("empty", "");
        EasyMock.expectLastCall(); //works for all previous void calls
        
        EasyMock.expect(basePortalUrl.getUrlString()).andReturn("/uPortal/home/normal/render.uP").times(2);

        
        final IPortalUrlProvider portalUrlProvider = EasyMock.createMock(IPortalUrlProvider.class);
        
        EasyMock.expect(portalUrlProvider.getDefaultUrl(request)).andReturn(basePortalUrl).times(2);
        
        
        EasyMock.replay(portalUrlProvider, basePortalUrl);
        
        
        
        final Transformer transformer = tFactory.newTransformer(new StreamSource(this.getClass().getResourceAsStream("portalUrlTest.xsl")));
        
        //Setup the transformer parameters
        final XsltPortalUrlProvider xsltPortalUrlProvider = new XsltPortalUrlProvider();
        xsltPortalUrlProvider.setUrlProvider(portalUrlProvider);
        transformer.setParameter(XsltPortalUrlProvider.XSLT_PORTAL_URL_PROVIDER, xsltPortalUrlProvider);
        transformer.setParameter("CURRENT_REQUEST", request);
        

        // set up configuration in the transformer impl
        final StringWriter resultWriter = new StringWriter();
        transformer.transform(xmlSource, new StreamResult(resultWriter));
        
        final String result = resultWriter.getBuffer().toString();
        logger.debug(result);
        final String expected = IOUtils.toString(this.getClass().getResourceAsStream("portalUrlResult.xml"));

        XMLUnit.setIgnoreWhitespace(true);
        Diff d = new Diff(expected, result);
        assertTrue("Transformation result differs from what's expected" + d, d.similar());
        
        EasyMock.verify(portalUrlProvider, basePortalUrl);
    }

    @Test
    public void testPortalUrl() throws Exception {
        final MockHttpServletRequest request = new MockHttpServletRequest();
        
        
        final IPortletWindowRegistry portletWindowRegistry = createMock(IPortletWindowRegistry.class);
        final IPortalUrlProvider portalUrlProvider = createMock(IPortalUrlProvider.class);
        
       
        final IPortletPortalUrl portletFnameUrl = createMock(IPortletPortalUrl.class);
        portletFnameUrl.addPortletParameter("foo", "bar");
        portletFnameUrl.addPortletParameter("foo", "bor");
        portletFnameUrl.addPortletParameter("page", "42");
        portletFnameUrl.addPortletParameter("node", "element");
        portletFnameUrl.addPortletParameter("empty", "");
        portletFnameUrl.addPortalParameter("something", "for the portal");
        expectLastCall(); //works for all previous void calls
        expect(portletFnameUrl.getUrlString()).andReturn("/uPortal/home/normal/bookmarks.1/render.uP");
        
        expect(portalUrlProvider.getPortletUrlByFName(TYPE.RENDER, request, "bookmarks")).andReturn(portletFnameUrl);
        
        
        final IPortletPortalUrl portletSubscribeUrl = createMock(IPortletPortalUrl.class);
        portletSubscribeUrl.addPortletParameter("foo", "bar");
        portletSubscribeUrl.addPortletParameter("foo", "bor");
        portletSubscribeUrl.addPortletParameter("page", "42");
        portletSubscribeUrl.addPortletParameter("node", "element");
        portletSubscribeUrl.addPortletParameter("empty", "");
        portletSubscribeUrl.addPortalParameter("something", "for the portal");
        expectLastCall(); //works for all previous void calls
        expect(portletSubscribeUrl.getUrlString()).andReturn("/uPortal/home/normal/bookmarks.n1/render.uP");
        
        expect(portalUrlProvider.getPortletUrlByNodeId(TYPE.RENDER, request, "n1")).andReturn(portletSubscribeUrl);
        
        
        final MockPortletWindowId windowId = new MockPortletWindowId("123.321");
        expect(portletWindowRegistry.getPortletWindowId("123.321")).andReturn(windowId);
        
        final IPortletPortalUrl portletWindowIdUrl = createMock(IPortletPortalUrl.class);
        portletWindowIdUrl.addPortletParameter("foo", "bar");
        portletWindowIdUrl.addPortletParameter("foo", "bor");
        portletWindowIdUrl.addPortletParameter("page", "42");
        portletWindowIdUrl.addPortletParameter("node", "element");
        portletWindowIdUrl.addPortletParameter("empty", "");
        portletWindowIdUrl.addPortalParameter("something", "for the portal");
        portletWindowIdUrl.setWindowState(WindowState.MAXIMIZED);
        portletWindowIdUrl.setPortletMode(PortletMode.EDIT);
        expectLastCall(); //works for all previous void calls
        expect(portletWindowIdUrl.getUrlString()).andReturn("/uPortal/home/normal/bookmarks.n1/render.uP");
        
        expect(portalUrlProvider.getPortletUrl(TYPE.ACTION, request, windowId)).andReturn(portletWindowIdUrl);
        
        
        replay(portalUrlProvider, portletWindowRegistry, portletFnameUrl, portletSubscribeUrl, portletWindowIdUrl);
        
        
        
        final Transformer transformer = tFactory.newTransformer(new StreamSource(this.getClass().getResourceAsStream("portletUrlTest.xsl")));
        
        //Setup the transformer parameters
        final XsltPortalUrlProvider xsltPortalUrlProvider = new XsltPortalUrlProvider();
        xsltPortalUrlProvider.setPortletWindowRegistry(portletWindowRegistry);
        xsltPortalUrlProvider.setUrlProvider(portalUrlProvider);
        transformer.setParameter(XsltPortalUrlProvider.XSLT_PORTAL_URL_PROVIDER, xsltPortalUrlProvider);
        transformer.setParameter("CURRENT_REQUEST", request);
        

        // set up configuration in the transformer impl
        final StringWriter resultWriter = new StringWriter();
        transformer.transform(xmlSource, new StreamResult(resultWriter));
        
        verify(portalUrlProvider, portletWindowRegistry, portletFnameUrl, portletSubscribeUrl, portletWindowIdUrl);
        
        final String result = resultWriter.getBuffer().toString();
        logger.debug(result);
        final String expected = IOUtils.toString(this.getClass().getResourceAsStream("portletUrlResult.xml"));

        XMLUnit.setIgnoreWhitespace(true);
        Diff d = new Diff(expected, result);
        assertTrue("Transformation result differs from what's expected" + d, d.similar());
        
    }
}
