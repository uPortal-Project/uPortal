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

package org.jasig.portal.security.xslt;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;

import static org.junit.Assert.assertTrue;

import java.io.InputStream;
import java.io.StringWriter;

import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.LogFactory;
import org.custommonkey.xmlunit.Diff;
import org.custommonkey.xmlunit.XMLUnit;
import org.jasig.portal.xml.ResourceLoaderURIResolver;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassRelativeResourceLoader;
import org.springframework.util.xml.SimpleTransformErrorListener;

/**
 * @author Eric Dalquist
 * @version $Revision$
 */
public class XalanMessageHelperTest {
    protected final Logger logger = LoggerFactory.getLogger(getClass());
    
    private TransformerFactory tFactory;
    private StreamSource xmlSource;
    
    @Before
    public void setup() throws Exception {
        final ResourceLoaderURIResolver resolver = new ResourceLoaderURIResolver(new ClassRelativeResourceLoader(getClass()));
        
        this.tFactory = TransformerFactory.newInstance();
        this.tFactory.setURIResolver(resolver);
        this.tFactory.setErrorListener(new SimpleTransformErrorListener(LogFactory.getLog(getClass())));
        
        this.xmlSource = new StreamSource(this.getClass().getResourceAsStream("test.xml"));
    }

    @Test
    public void testIsUserDeepMemberOf() throws Exception {
        final IXalanMessageHelper helperBean = createMock(IXalanMessageHelper.class);
        
        expect(helperBean.getMessage("im.done", "en_us")).andReturn("I'm Done!");
        
        replay(helperBean);
        
        XalanMessageHelper helper = new XalanMessageHelper();
        helper.setMessageHelper(helperBean);
        
        // set up configuration in the transformer impl
        final StringWriter resultWriter = new StringWriter();
        
        final InputStream xslStream = this.getClass().getResourceAsStream("messagesTest.xsl");
        final StreamSource xslSource = new StreamSource(xslStream);
        final Transformer transformer = tFactory.newTransformer(xslSource);
        
        transformer.setParameter("USER_LANG", "en_us");
        
        transformer.transform(xmlSource, new StreamResult(resultWriter));
        
        final String result = resultWriter.getBuffer().toString();
        logger.debug(result);
        
        final String expected = IOUtils.toString(this.getClass().getResourceAsStream("messagesResult.xml"));

        XMLUnit.setIgnoreWhitespace(true);
        Diff d = new Diff(expected, result);
        assertTrue("Transformation result differs from what's expected" + d, d.similar());
        
        verify(helperBean);
    }
}
