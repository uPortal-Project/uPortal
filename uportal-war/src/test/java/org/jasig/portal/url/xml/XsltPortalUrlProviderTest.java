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

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.io.StringWriter;

import javax.portlet.WindowState;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamReader;
import javax.xml.transform.Source;
import javax.xml.transform.Templates;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stax.StAXSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.LogFactory;
import org.custommonkey.xmlunit.Diff;
import org.custommonkey.xmlunit.XMLUnit;
import org.jasig.portal.mock.portlet.om.MockPortletWindowId;
import org.jasig.portal.portlet.om.IPortletWindow;
import org.jasig.portal.portlet.om.IPortletWindowId;
import org.jasig.portal.portlet.registry.IPortletWindowRegistry;
import org.jasig.portal.url.IPortalUrlBuilder;
import org.jasig.portal.url.IPortalUrlProvider;
import org.jasig.portal.url.IPortletUrlBuilder;
import org.jasig.portal.url.UrlType;
import org.jasig.portal.xml.ResourceLoaderURIResolver;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassRelativeResourceLoader;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.util.xml.FixedXMLEventStreamReader;
import org.springframework.util.xml.SimpleTransformErrorListener;

/**
 * @author Eric Dalquist
 * @version $Revision$
 */
@RunWith(MockitoJUnitRunner.class)
public class XsltPortalUrlProviderTest {
    protected final Logger logger = LoggerFactory.getLogger(getClass());
    
    @InjectMocks private XsltPortalUrlProvider xsltPortalUrlProvider = new XsltPortalUrlProvider();
    @Mock private IPortletWindowRegistry portletWindowRegistry;
    @Mock private IPortalUrlProvider portalUrlProvider;
    @Mock private IPortalUrlBuilder portalUrlBuilder;
    @Mock private IPortletUrlBuilder portletUrlBuilder;
    @Mock private IPortletWindow portletWindow;

    
    private TransformerFactory tFactory;
    private Source xmlSource;
    private Templates xslTemplate;
    private String expected;
    
    @Before
    public void setup() throws Exception {
        final ResourceLoaderURIResolver resolver = new ResourceLoaderURIResolver(new ClassRelativeResourceLoader(getClass()));
        
        this.tFactory = TransformerFactory.newInstance();
        this.tFactory.setURIResolver(resolver);
        this.tFactory.setErrorListener(new SimpleTransformErrorListener(LogFactory.getLog(getClass())));
        
        //Load the XML document so it reads the same way the rendering pipeline reads XML
        final XMLInputFactory xmlInputFactory = XMLInputFactory.newFactory();
        final XMLEventReader eventReader = xmlInputFactory.createXMLEventReader(this.getClass().getResourceAsStream("test.xml"));
        final XMLStreamReader streamReader = new FixedXMLEventStreamReader(eventReader);
        xmlSource = new StAXSource(streamReader);
        
        xslTemplate = tFactory.newTemplates(new StreamSource(this.getClass().getResourceAsStream("test.xsl")));
        
        expected = IOUtils.toString(this.getClass().getResourceAsStream("result.xml"));
    }
    
    @Test
    public void testDefaultUrl() throws Exception {
        final MockHttpServletRequest request = new MockHttpServletRequest();
        
        when(portalUrlProvider.getDefaultUrl(request)).thenReturn(portalUrlBuilder);
        when(portalUrlBuilder.getUrlString()).thenReturn("/uPortal/home/normal/render.uP?layoutUrl");
        
        final Transformer transformer = xslTemplate.newTransformer();
        transformer.setParameter(XsltPortalUrlProvider.XSLT_PORTAL_URL_PROVIDER, xsltPortalUrlProvider);
        transformer.setParameter("CURRENT_REQUEST", request);
        transformer.setParameter("TEST", "defaultUrl"); //xsl template mode to use
        

        // set up configuration in the transformer impl
        final StringWriter resultWriter = new StringWriter();
        transformer.transform(xmlSource, new StreamResult(resultWriter));
        
        verify(portalUrlBuilder).getUrlString();
        verifyNoMoreInteractions(portalUrlBuilder);
        
        final String result = resultWriter.getBuffer().toString();
        logger.debug(result);

        XMLUnit.setIgnoreWhitespace(true);
        Diff d = new Diff(expected, result);
        assertTrue("Transformation result differs from what's expected" + d, d.similar());
    }
    
    @Test
    public void testLayoutUrlById() throws Exception {
        final MockHttpServletRequest request = new MockHttpServletRequest();
        
        when(portalUrlProvider.getPortalUrlBuilderByLayoutNode(request, "foo", UrlType.ACTION)).thenReturn(portalUrlBuilder);
        when(portalUrlBuilder.getUrlString()).thenReturn("/uPortal/home/normal/render.uP?layoutUrl");
        
        final Transformer transformer = xslTemplate.newTransformer();
        transformer.setParameter(XsltPortalUrlProvider.XSLT_PORTAL_URL_PROVIDER, xsltPortalUrlProvider);
        transformer.setParameter("CURRENT_REQUEST", request);
        transformer.setParameter("TEST", "layoutUrlById"); //xsl template mode to use
        

        // set up configuration in the transformer impl
        final StringWriter resultWriter = new StringWriter();
        transformer.transform(xmlSource, new StreamResult(resultWriter));
        
        verify(portalUrlBuilder).addParameter("remove_target", "foo");
        verify(portalUrlBuilder).addParameter("save", "42");
        verify(portalUrlBuilder).getUrlString();
        verifyNoMoreInteractions(portalUrlBuilder);
        
        final String result = resultWriter.getBuffer().toString();
        logger.debug(result);

        XMLUnit.setIgnoreWhitespace(true);
        Diff d = new Diff(expected, result);
        assertTrue("Transformation result differs from what's expected" + d, d.similar());
    }

    @Test
    public void testPortletUrlById() throws Exception {
        final MockHttpServletRequest request = new MockHttpServletRequest();
        final IPortletWindowId portletWindowId = new MockPortletWindowId("w1");
        
        when(portalUrlProvider.getPortalUrlBuilderByLayoutNode(request, "foo", UrlType.RENDER)).thenReturn(portalUrlBuilder);
        when(portalUrlBuilder.getPortletUrlBuilder(portletWindowId)).thenReturn(portletUrlBuilder);
        when(portalUrlBuilder.getUrlString()).thenReturn("/uPortal/home/normal/render.uP?layoutUrl");
        when(portletWindowRegistry.getOrCreateDefaultPortletWindowByLayoutNodeId(request, "foo")).thenReturn(portletWindow);
        when(portletWindow.getPortletWindowId()).thenReturn(portletWindowId);
        
        final Transformer transformer = xslTemplate.newTransformer();
        transformer.setParameter(XsltPortalUrlProvider.XSLT_PORTAL_URL_PROVIDER, xsltPortalUrlProvider);
        transformer.setParameter("CURRENT_REQUEST", request);
        transformer.setParameter("TEST", "portletUrlById"); //xsl template mode to use
        

        // set up configuration in the transformer impl
        final StringWriter resultWriter = new StringWriter();
        transformer.transform(xmlSource, new StreamResult(resultWriter));
        
        verify(portalUrlBuilder).addParameter("pageNum", "42");
        verify(portalUrlBuilder).getPortletUrlBuilder(portletWindowId);
        verify(portalUrlBuilder).getUrlString();
        verifyNoMoreInteractions(portalUrlBuilder);
        
        verify(portletUrlBuilder).setWindowState(new WindowState("maximized"));
        verify(portletUrlBuilder).addParameter("tmp", "blah");
        verify(portletUrlBuilder).setCopyCurrentRenderParameters(false);
        verifyNoMoreInteractions(portletUrlBuilder);
        
        final String result = resultWriter.getBuffer().toString();
        logger.debug(result);

        XMLUnit.setIgnoreWhitespace(true);
        Diff d = new Diff(expected, result);
        assertTrue("Transformation result differs from what's expected" + d, d.similar());
    }

    @Test
    public void testMultiPortletUrlById() throws Exception {
        final MockHttpServletRequest request = new MockHttpServletRequest();
        final IPortletWindowId portletWindowId = new MockPortletWindowId("w1");
        
        final IPortletWindow portletWindow2 = mock(IPortletWindow.class);
        final IPortletWindowId portletWindowId2 = new MockPortletWindowId("w2");
        final IPortletUrlBuilder portletUrlBuilder2 = mock(IPortletUrlBuilder.class);
        
        when(portalUrlProvider.getPortalUrlBuilderByLayoutNode(request, "foo", UrlType.RENDER)).thenReturn(portalUrlBuilder);
        when(portalUrlBuilder.getPortletUrlBuilder(portletWindowId)).thenReturn(portletUrlBuilder);
        when(portalUrlBuilder.getUrlString()).thenReturn("/uPortal/home/normal/render.uP?layoutUrl");
        when(portletWindowRegistry.getOrCreateDefaultPortletWindowByLayoutNodeId(request, "foo")).thenReturn(portletWindow);
        when(portletWindow.getPortletWindowId()).thenReturn(portletWindowId);
        
        when(portletWindowRegistry.getOrCreateDefaultPortletWindowByFname(request, "my-portlet")).thenReturn(portletWindow2);
        when(portletWindow2.getPortletWindowId()).thenReturn(portletWindowId2);
        when(portalUrlBuilder.getPortletUrlBuilder(portletWindowId2)).thenReturn(portletUrlBuilder2);
        
        final Transformer transformer = xslTemplate.newTransformer();
        transformer.setParameter(XsltPortalUrlProvider.XSLT_PORTAL_URL_PROVIDER, xsltPortalUrlProvider);
        transformer.setParameter("CURRENT_REQUEST", request);
        transformer.setParameter("TEST", "multiPortletUrlById"); //xsl template mode to use
        

        // set up configuration in the transformer impl
        final StringWriter resultWriter = new StringWriter();
        transformer.transform(xmlSource, new StreamResult(resultWriter));
        
        verify(portalUrlBuilder).addParameter("pageNum", "42");
        verify(portalUrlBuilder).getPortletUrlBuilder(portletWindowId);
        verify(portalUrlBuilder).getPortletUrlBuilder(portletWindowId2);
        verify(portalUrlBuilder).getUrlString();
        verifyNoMoreInteractions(portalUrlBuilder);
        
        verify(portletUrlBuilder).addParameter("tmp", "blah");
        verify(portletUrlBuilder).setCopyCurrentRenderParameters(false);
        verifyNoMoreInteractions(portletUrlBuilder);
        
        verify(portletUrlBuilder2).setWindowState(new WindowState("minimized"));
        verify(portletUrlBuilder2).addParameter("event", "param");
        verify(portletUrlBuilder2).setCopyCurrentRenderParameters(false);
        verifyNoMoreInteractions(portletUrlBuilder2);
        
        final String result = resultWriter.getBuffer().toString();
        logger.debug(result);

        XMLUnit.setIgnoreWhitespace(true);
        Diff d = new Diff(expected, result);
        assertTrue("Transformation result differs from what's expected" + d, d.similar());
    }
    
}
