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

package org.jasig.portal.channels.error.tt;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.FactoryConfigurationError;
import javax.xml.parsers.ParserConfigurationException;

import org.jasig.portal.InternalTimeoutException;
import org.jasig.portal.channels.error.error2xml.IThrowableToElement;
import org.jasig.portal.channels.error.error2xml.InternalTimeoutExceptionToElement;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Testcase for InternalTimeoutExceptionToElement.
 * The superclass AbstractThrowableToElementTest is doing most of the
 * heavy lifting here.
 * @author andrew.petro@yale.edu
 * @version $Revision$ $Date$
 */
public final class InternalTimeoutExceptionToElementTest 
    extends AbstractThrowableToElementTest {

    private InternalTimeoutExceptionToElement exceptionToElement 
        = new InternalTimeoutExceptionToElement();
    
    /**
     * Test the child element "timeout".
     * Basic Element production testing accomplished in superclass.
     * @throws FactoryConfigurationError
     * @throws ParserConfigurationException
     */
    public void testITEToElement() throws ParserConfigurationException, FactoryConfigurationError {
        InternalTimeoutException ite = 
            new InternalTimeoutException("A test message", 40);
        Document dom = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
        
        Element elem = this.exceptionToElement.throwableToElement(ite, dom);
        
        assertEquals("throwable", elem.getNodeName());
        NodeList nodeList = elem.getElementsByTagName("timeout");
        assertEquals(1, nodeList.getLength());
        Node timeoutNode = nodeList.item(0);
        assertEquals("40", timeoutNode.getAttributes().getNamedItem("value").getNodeValue());
        
    }

    /* (non-Javadoc)
     * @see org.jasig.portal.channels.error.tt.AbstractThrowableToElementTest#getThrowableToElementInstance()
     */
    protected IThrowableToElement getThrowableToElementInstance() {
        return this.exceptionToElement;
    }

    /* (non-Javadoc)
     * @see org.jasig.portal.channels.error.tt.AbstractThrowableToElementTest#supportedThrowable()
     */
    protected Throwable supportedThrowable() {
        return new InternalTimeoutException("Timeout message goes here", 60);
    }

    /* (non-Javadoc)
     * @see org.jasig.portal.channels.error.tt.AbstractThrowableToElementTest#unsupportedThrowable()
     */
    protected Throwable unsupportedThrowable() {
        return new NullPointerException("An example of an unsupported Throwable.");
    }

}