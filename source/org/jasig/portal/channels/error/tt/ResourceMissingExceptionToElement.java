/* Copyright 2004 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/

package org.jasig.portal.channels.error.tt;

import org.jasig.portal.ResourceMissingException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Translates ResourceMissingException instances to XML Elements.
 * @author andrew.petro@yale.edu
 * @version $Revision$ $Date$
 */
public class ResourceMissingExceptionToElement 
    implements IThrowableToElement {

    
    private ThrowableToElement throwableToElement = new ThrowableToElement();
    
    /* (non-Javadoc)
     * @see org.jasig.portal.channels.error.tt.IThrowableToElement#throwableToElement(java.lang.Throwable, org.w3c.dom.Document)
     */
    public Element throwableToElement(Throwable t, Document parentDoc) 
        throws IllegalArgumentException {
       if (t == null)
           throw new IllegalArgumentException("Can only translate non-null throwables.");
       if (! (t instanceof ResourceMissingException))
           throw new IllegalArgumentException("Can only translate ResourceMissingExceptions.");
       if (parentDoc == null)
           throw new IllegalArgumentException("Must have non-null parent doc.");
       
       ResourceMissingException rme = (ResourceMissingException) t;
       
       // use basic ThrowableToElement to boostrap a basic Element.
       Element element = this.throwableToElement.throwableToElement(t, parentDoc);
       element.setAttribute("renderedAs", ResourceMissingException.class.getName());
       
       Element resourceEl = parentDoc.createElement("resource");
       Element uriEl = parentDoc.createElement("uri");
       uriEl.appendChild(parentDoc.createTextNode(rme.getResourceURI()));
       resourceEl.appendChild(uriEl);
       Element descriptionEl = parentDoc.createElement("description");
       descriptionEl.appendChild(parentDoc.createTextNode(rme.getResourceDescription()));
       resourceEl.appendChild(descriptionEl);
       element.appendChild(resourceEl);
       
       return element;
    }

    /* (non-Javadoc)
     * @see org.jasig.portal.channels.error.tt.IThrowableToElement#supports(java.lang.Class)
     */
    public boolean supports(Class c) throws IllegalArgumentException {
        if (c == null)
            throw new IllegalArgumentException("Supports undefined on null");
        if (!Throwable.class.isAssignableFrom(c))
            throw new IllegalArgumentException("Supports undefined on classes " +
                    "which are not and do not extend Throwable: " + c.getName());
        
        return (ResourceMissingException.class.isAssignableFrom(c));
    }

}


/* ResourceMissingExceptionToElement.java
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