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

package org.jasig.portal.channels.error.error2xml;

import org.jasig.portal.ResourceMissingException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Translates ResourceMissingException instances to XML Elements.
 * @author andrew.petro@yale.edu
 * @version $Revision$ $Date$
 * @since uPortal 2.5
 * @deprecated All IChannel implementations should be migrated to portlets
 */
@Deprecated
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
