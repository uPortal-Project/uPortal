/* Copyright 2004 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/

package org.jasig.portal.channels.error.tt;

import org.apache.xerces.dom.DocumentImpl;
import org.jasig.portal.ExceptionHelper;
import org.jasig.portal.channels.error.error2xml.IThrowableToElement;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import junit.framework.TestCase;

/**
 * Abstract TestCase for testing conformance to the IThrowableToElement
 * interface.
 * @author andrew.petro@yale.edu
 * @version $Revision$ $Date$
 */
public abstract class AbstractThrowableToElementTest extends TestCase {

    /*
     * @see TestCase#setUp()
     */
    protected void setUp() throws Exception {
        super.setUp();
    }

    /*
     * @see TestCase#tearDown()
     */
    protected void tearDown() throws Exception {
        super.tearDown();
    }
    
    public final void testThrowableToElement() {

        IThrowableToElement throwableToElement 
            = getThrowableToElementInstance();
        
        Throwable supportedThrowable
            = supportedThrowable();
        
        Class supportedThrowableClass = supportedThrowable.getClass();
        
        assertTrue(throwableToElement.supports(supportedThrowableClass));
        
        Document dom = new DocumentImpl();
       
        Element e = throwableToElement.throwableToElement(supportedThrowable, dom);
        assertEquals(dom, e.getOwnerDocument());
        assertEquals("throwable", e.getNodeName());
        assertEquals(supportedThrowableClass.getName(), 
                e.getAttribute("renderedAs"));
        assertEquals(supportedThrowableClass.getName(),
                e.getAttribute("class"));
        NodeList nodeList = e.getElementsByTagName("message");
        assertEquals(1, nodeList.getLength());
        Node messageNode = nodeList.item(0);
        Node messageTextNode = messageNode.getFirstChild();
        
        // TODO: should XML encode the message before checking this.
        assertEquals(supportedThrowable.getMessage(), 
                messageTextNode.getNodeValue());
        
        NodeList stackList = e.getElementsByTagName("stack");
        assertEquals(1, stackList.getLength());
        Node stackNode = stackList.item(0);
        
        // Need a String representation of printed stack trace for comparison.
        
        String exceptionString = 
            ExceptionHelper.shortStackTrace(supportedThrowable);
        
        assertEquals(exceptionString, stackNode.getFirstChild().getNodeValue());
        
    }
    
    /**
     * Test that throwableToElement throws IAE for an unsupported Throwable.
     */
    public final void testThrowableToElementUnsupported(){
        try{
            Document dom = new DocumentImpl();
            IThrowableToElement throwableToElement 
                = getThrowableToElementInstance();
            Throwable unsupportedThrowable = unsupportedThrowable();
            throwableToElement.throwableToElement(unsupportedThrowable, dom);
        } catch (IllegalArgumentException iae) {
            // good
            return;
        } 
        fail("Should have thrown IAE.");
    }
    
    /**
     * Test that calling throwableToElement() with a null Throwable argument
     * throws IllegalArgumentException
     */
    public final void testThrowableToElementNullThrowable() {
        try{
            Document dom = new DocumentImpl();
            IThrowableToElement throwableToElement 
                = getThrowableToElementInstance();
            throwableToElement.throwableToElement(null, dom);
        } catch (IllegalArgumentException iae) {
            // good
            return;
        } 
        fail("Should have thrown IAE.");
    }
    
    /**
     * Test that calling throwableToElement() with a null Document argument
     * throws IllegalArgumentException
     */
    public final void testThrowableToElementNullDocument() {
        Throwable t = new Throwable();
        try{
            IThrowableToElement throwableToElement 
                = getThrowableToElementInstance();
            throwableToElement.throwableToElement(t, null);
        } catch (IllegalArgumentException iae) {
            // good
            return;
        } 
        fail("Should have thrown IAE.");
    }

    /**
     * Test that calling supports(null) throws
     * IllegalArgumentException.
     */
    public final void testSupportsNull() {
       try{
           IThrowableToElement throwableToElement 
               = getThrowableToElementInstance();
           throwableToElement.supports(null);
       } catch (IllegalArgumentException iae) {
           // good
           return;
       } 
       fail("Should have thrown IAE.");
    }
    
    /**
     * Test that calling supports(c) throws
     * IllegalArgumentException when c is not and does not extend Throwable.
     */
    public final void testSupportsNonThrowable() {
       try{
           IThrowableToElement throwableToElement 
               = getThrowableToElementInstance();
           throwableToElement.supports(Integer.class);
       } catch (IllegalArgumentException iae) {
           // good
           return;
       } 
       fail("Should have thrown IAE.");
    }
    
    /**
     * Test that calling supports(c) returns either true or false (does not throw
     * an exception) when the class is Throwable.
     */
    public void testSupportsThrowable() {
       IThrowableToElement throwableToElement 
           = getThrowableToElementInstance();
       throwableToElement.supports(Throwable.class);
    }
    
    /**
     * Test that supports() returns false for an unsupported throwable.
     */
    public  final void testUnsupported(){
        IThrowableToElement throwableToElement 
            = getThrowableToElementInstance();
        Throwable unsupportedThrowable = unsupportedThrowable();
        if (unsupportedThrowable != null)
            assertFalse(throwableToElement.supports(Throwable.class));
    }
    
    /**
     * Test that supports() returns true for a supported throwable.
     */
    public final void testSupported(){
        IThrowableToElement throwableToElement 
            = getThrowableToElementInstance();
        Throwable supportedThrowable = supportedThrowable();
        if (supportedThrowable != null)
            assertTrue(throwableToElement.supports(supportedThrowable.getClass()));
    }
    
    
    /**
     * Return an instance of the IThrowableToElement specifically to be tested
     * by the TestCase extending this abstract TestCase.
     * @return
     */
    protected abstract IThrowableToElement getThrowableToElementInstance();
    
    /**
     * Return a Throwable instance that is supported by the IThrowableToElement
     * under test, so we can test common Throwable representation
     * including stack trace presentation, class, message, and renderedAs 
     * attributes.  The returned Throwable should be of a class such that the
     * IThrowableToElement implementation will set its name as both the "class" and
     * "renderedAs" attributes of the <throwable/> XML production.
     * @return a supported Throwable.
     */
    protected abstract Throwable supportedThrowable();
    
    /**
     * Return a Throwable instance that is not supported by the IThrowableToElement
     * under test.  If the IThrowableToElement under test supports all Throwables, 
     * return null.
     * @return an unsupported Throwable, or null.
     */
    protected abstract Throwable unsupportedThrowable();

}