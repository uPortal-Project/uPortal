/* Copyright 2004 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/

package org.jasig.portal.channels.error.tt;

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


/* ThrowableThrowableToElement.java
 * 
 * Copyright (c) Oct 25, 2004 Yale University.  All rights reserved.
 *
 * THIS SOFTWARE IS PROVIDED "AS IS," AND ANY EXPRESS OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
 * MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE, ARE EXPRESSLY
 * DISCLAIMED. IN NO EVENT SHALL YALE UNIVERSITY OR ITS EMPLOYEES BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED, THE COSTS OF
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED IN ADVANCE OF THE POSSIBILITY OF SUCH
 * DAMAGE.
 * 
 * Redistribution and use of this software in source or binary forms,
 * with or without modification, are permitted, provided that the
 * following conditions are met.
 * 
 * 1. Any redistribution must include the above copyright notice and
 * disclaimer and this list of conditions in any related documentation
 * and, if feasible, in the redistributed software.
 * 
 * 2. Any redistribution must include the acknowledgment, "This product
 * includes software developed by Yale University," in any related
 * documentation and, if feasible, in the redistributed software.
 * 
 * 3. The names "Yale" and "Yale University" must not be used to endorse
 * or promote products derived from this software.
 */