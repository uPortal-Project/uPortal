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

package org.jasig.portal.xml.xpath;

import static org.junit.Assert.assertEquals;

import java.util.Collections;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPathConstants;

import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Document;

/**
 * @author Eric Dalquist
 * @version $Revision$
 */
public class XPathPoolImplTest {
    private Document document;
    
    @Before
    public void setup() throws Exception {
        final DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        final DocumentBuilder db = dbf.newDocumentBuilder();
        this.document = db.parse(getClass().getResourceAsStream("guestLayout.xml"));
    }
    
    @Test
    public void testXPathExpressionVariables() throws Exception {
        final XPathPoolImpl xpathPool = new XPathPoolImpl();
        
        assertEquals(0, xpathPool.getNumActive());
        assertEquals(0, xpathPool.getNumIdle());
        
        final String defaultTabId1 = xpathPool.evaluate(
                "/layout/folder/folder[@type='regular' and @hidden!='true'][$defaultTab]/@ID", 
                Collections.singletonMap("defaultTab", 1), 
                this.document, 
                XPathConstants.STRING);
        
        assertEquals(0, xpathPool.getNumActive());
        assertEquals(1, xpathPool.getNumIdle());
        
        assertEquals("u16l1s3", defaultTabId1);
        
        final String defaultTabId2 = xpathPool.evaluate(
                "/layout/folder/folder[@type='regular' and @hidden!='true'][$defaultTab]/@ID", 
                Collections.singletonMap("defaultTab", 2), 
                this.document, 
                XPathConstants.STRING);
        
        assertEquals(0, xpathPool.getNumActive());
        assertEquals(1, xpathPool.getNumIdle());
        
        assertEquals("u18l1s3", defaultTabId2);
    }
}
