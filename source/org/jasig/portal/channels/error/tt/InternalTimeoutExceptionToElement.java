/* Copyright 2004 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/

package org.jasig.portal.channels.error.tt;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import org.jasig.portal.InternalTimeoutException;

/**
 * Translates from a uPortal InternalTimeoutException to an XML element.
 * @author andrew.petro@yale.edu
 * @version $Revision$ $Date$
 */
public class InternalTimeoutExceptionToElement 
    implements IThrowableToElement{

    private ThrowableToElement throwableToElement = new ThrowableToElement();
    
    /* (non-Javadoc)
     * @see org.jasig.portal.channels.error.tt.IThrowableToElement#throwableToElement(java.lang.Throwable, org.w3c.dom.Document)
     */
    public Element throwableToElement(Throwable t, Document parentDoc) 
        throws IllegalArgumentException {
        
        Element element = 
            this.throwableToElement.throwableToElement(t, parentDoc);
        
        if (! (t instanceof InternalTimeoutException))
            throw new IllegalArgumentException(t.getClass().getName() + 
                    " does not extend InternalTimeoutException");
        
        InternalTimeoutException ite = (InternalTimeoutException) t;
        Long timeoutValue = ite.getTimeoutValue();
        
        if (timeoutValue != null) {
            Element timeoutEl = parentDoc.createElement("timeout");
            timeoutEl.setAttribute("value", timeoutValue.toString());
            element.appendChild(timeoutEl);
        }
        
        element.setAttribute("renderedAs", 
                InternalTimeoutException.class.getName());

        return element;
    }

    /* (non-Javadoc)
     * @see org.jasig.portal.channels.error.tt.IThrowableToElement#supports(java.lang.Class)
     */
    public boolean supports(Class c) throws IllegalArgumentException {
        if (c == null)
            throw new IllegalArgumentException("Cannot support a null class.");
        if (! Throwable.class.isAssignableFrom(c))
            throw new IllegalArgumentException("Supports undefined on " +
                    "classes that are not and do not extend Throwable:" + c.getName());
        return InternalTimeoutException.class.isAssignableFrom(c);
    }

}