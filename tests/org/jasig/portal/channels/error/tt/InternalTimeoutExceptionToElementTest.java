/* Copyright 2004 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/

package org.jasig.portal.channels.error.tt;

import org.apache.xerces.dom.DocumentImpl;
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
     */
    public void testITEToElement() {
        InternalTimeoutException ite = 
            new InternalTimeoutException("A test message", 40);
        Document dom = new DocumentImpl();
        
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