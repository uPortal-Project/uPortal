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

package org.jasig.portal.web.skin;

import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;

import static org.junit.Assert.assertTrue;

import java.io.InputStream;
import java.io.StringWriter;

import javax.servlet.ServletContext;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.LogFactory;
import org.custommonkey.xmlunit.Diff;
import org.custommonkey.xmlunit.XMLUnit;
import org.easymock.EasyMock;
import org.jasig.portal.xml.ResourceLoaderURIResolver;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.ClassRelativeResourceLoader;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.util.xml.SimpleTransformErrorListener;

/**
 * @author Eric Dalquist
 * @version $Revision$
 */
public class ResourcesElementsProviderTest {
    protected final Logger logger = LoggerFactory.getLogger(getClass());
    
    private TransformerFactory tFactory;
    private StreamSource xmlSource;
    
    @Before
    public void setup() throws Exception {
        final ResourceLoaderURIResolver resolver = new ResourceLoaderURIResolver();
        resolver.setResourceLoader(new ClassRelativeResourceLoader(getClass()));
        
        this.tFactory = TransformerFactory.newInstance();
        this.tFactory.setURIResolver(resolver);
        this.tFactory.setErrorListener(new SimpleTransformErrorListener(LogFactory.getLog(getClass())));
        
        xmlSource = new StreamSource(ResourcesElementsProviderTest.class.getResourceAsStream("testSourceStream.xml"));
    }
    
    
    @Test
    public void testPortletUrl() throws Exception {
        final MockHttpServletRequest request = new MockHttpServletRequest();
        
        
        
        final ServletContext mockContext = EasyMock.createMock(ServletContext.class);
        final InputStream testResourcesStream = new ClassPathResource("org/jasig/portal/web/skin/resources1.xml").getInputStream();
        expect(mockContext.getResourceAsStream("/media/skins/test/uportal3/uportal3_aggr.skin.xml")).andReturn(testResourcesStream);
        
        final ResourcesDaoImpl resourcesDao = new ResourcesDaoImpl();
        resourcesDao.setServletContext(mockContext);
        
        replay(mockContext);
        
        final Transformer transformer = tFactory.newTransformer(new StreamSource(this.getClass().getResourceAsStream("resources1.xsl")));
        
        //Setup the transformer parameters
        final ResourcesElementsProvider provider = new ResourcesElementsProvider();
        provider.setResourcesDao(resourcesDao);
        transformer.setParameter(ResourcesElementsProvider.RESOURCES_ELEMENTS_PROVIDER, provider);
        transformer.setParameter("CURRENT_REQUEST", request);
        

        // set up configuration in the transformer impl
        final StringWriter resultWriter = new StringWriter();
        transformer.transform(xmlSource, new StreamResult(resultWriter));
        
        verify(mockContext);
        
        final String result = resultWriter.getBuffer().toString();
        this.logger.debug(result);
        final String expected = IOUtils.toString(this.getClass().getResourceAsStream("resources1-result.xml"));

        XMLUnit.setIgnoreWhitespace(true);
        Diff d = new Diff(expected, result);
        assertTrue("Transformation result differs from what's expected" + d, d.similar());
    }
}
