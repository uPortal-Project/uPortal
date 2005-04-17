/* Copyright 2004 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/

package org.jasig.portal.channels.error.error2xml;

import org.jasig.portal.ExceptionHelper;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Generic ThrowableToElement which will translate any Throwable to an
 * Element representing it.
 * Other IThrowableToElement implementations may use this implementation as
 * a utility to perform the generic Throwable translation and then add to the results.
 * @author andrew.petro@yale.edu
 * @version $Revision$ $Date$
 * @since uPortal 2.5
 */
public class ThrowableToElement 
    implements IThrowableToElement {
    
    private Log log = LogFactory.getLog(getClass());
    
    /* (non-Javadoc)
     * @see org.jasig.portal.channels.error.tt.ThrowableToElement#throwableToElement(java.lang.Throwable, org.w3c.dom.Document)
     */
    public Element throwableToElement(Throwable t, Document parentDoc) 
        throws IllegalArgumentException {
        if (this.log.isTraceEnabled())
            log.trace("Entering throwableToElement for throwable [" + t + "]");
        if (t == null)
            throw new IllegalArgumentException("Cannot translate a null throwable.");
        if (parentDoc == null)
            throw new IllegalArgumentException("Cannot create an element for a null Document.");
        
        Element excEl = parentDoc.createElement("throwable");
        String m = t.getMessage();
        if (m != null) {
            Element emEl = parentDoc.createElement("message");
            emEl.appendChild(parentDoc.createTextNode(m));
            excEl.appendChild(emEl);
        }

        Element stEl = parentDoc.createElement("stack");
        String stackTrace = ExceptionHelper.shortStackTrace(t);
        stEl.appendChild(parentDoc.createTextNode(stackTrace));
        excEl.appendChild(stEl);
        
        excEl.setAttribute("class", t.getClass().getName());
        excEl.setAttribute("renderedAs", Throwable.class.getName());
        
        return excEl;
    }

    /* (non-Javadoc)
     * @see org.jasig.portal.channels.error.tt.ThrowableToElement#supports(java.lang.Class)
     */
    public boolean supports(Class c) throws IllegalArgumentException {
        if (c == null)
            throw new IllegalArgumentException("Supports is undefined on null");
        if (! Throwable.class.isAssignableFrom(c))
            throw new IllegalArgumentException("c is not a class extending Throwable. c=[" + c.getName() + "]");
        // if it's a throwable, we support it.
        return true;
    }

}
