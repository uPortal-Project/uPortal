/* Copyright 2004 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/

package org.jasig.portal.channels.error.tt;

import org.apache.xerces.dom.DocumentImpl;
import org.jasig.portal.ResourceMissingException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Testcase for ResourceMissingExceptionToElement.
 * @author andrew.petro@yale.edu
 * @version $Revision$ $Date$
 */
public final class ResourceMissingExceptionToElementTest extends
        AbstractThrowableToElementTest {

    private ResourceMissingExceptionToElement rmeToElement = 
        new ResourceMissingExceptionToElement();

    /**
     * Test the aspects of the XML production for RMEs
     * that differ from basic Throwable representation.
     */
    public void testRMEToElement() {
        ResourceMissingException rme = 
            new ResourceMissingException("http://www.somewhere.com", 
                "A description", "A message");
        Document dom = new DocumentImpl();
        
        Element elem = this.rmeToElement.throwableToElement(rme, dom);
        
        assertEquals("throwable", elem.getNodeName());
        NodeList resourceList = elem.getElementsByTagName("resource");
        assertEquals(1, resourceList.getLength());
        Node resourceNode = resourceList.item(0);
        
        NodeList uriList = ((Element) resourceNode).getElementsByTagName("uri");
        assertEquals(1, uriList.getLength());
        Node uri = uriList.item(0);
        assertEquals("http://www.somewhere.com", uri.getFirstChild().getNodeValue());
        
        NodeList descriptionList = ((Element) resourceNode).getElementsByTagName("description");
        assertEquals(1, descriptionList.getLength());
        Node description = descriptionList.item(0);
        assertEquals("A description", description.getFirstChild().getNodeValue());
        
    }

    /* (non-Javadoc)
     * @see org.jasig.portal.channels.error.tt.AbstractThrowableToElementTest#getThrowableToElementInstance()
     */
    protected IThrowableToElement getThrowableToElementInstance() {
        return this.rmeToElement;
    }

    /* (non-Javadoc)
     * @see org.jasig.portal.channels.error.tt.AbstractThrowableToElementTest#supportedThrowable()
     */
    protected Throwable supportedThrowable() {
        return new ResourceMissingException("http://www.somewhere.com", 
                "A description", "A message");
    }

    /* (non-Javadoc)
     * @see org.jasig.portal.channels.error.tt.AbstractThrowableToElementTest#unsupportedThrowable()
     */
    protected Throwable unsupportedThrowable() {
        return new IllegalStateException("Just a test exception.");
    }

}