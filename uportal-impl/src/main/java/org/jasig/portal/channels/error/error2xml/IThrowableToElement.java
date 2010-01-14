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

import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Interface for translating from a Throwable to an Element.
 * @author andrew.petro@yale.edu
 * @version $Revision$ $Date$
 * @since uPortal 2.5
 * @deprecated All IChannel implementations should be migrated to portlets
 */
@Deprecated
public interface IThrowableToElement {

    // Exceptions as Elements look like this:
    //  <exception code="N">
    //   <resource><uri/><description/></resource>
    //   <timeout value="N"/>
    //  </exception>
    
    /**
     * Obtain an Element representing the throwable.
     * Throws IllegalArgumentException if t is null.
     * Throws IllegalArgumentException if t is not supported, as indicated by
     * the supports() method.
     * @param t a supported throwable
     * @param parentDoc document into which the element is to go
     * @return an Element representing the Element
     * @throws IllegalArgumentException if t is null.
     * @throws IllegalArgumentException if t is unsupported.
     */
    public Element throwableToElement(Throwable t, Document parentDoc)
        throws IllegalArgumentException;
    
    /**
     * Returns true if the implementation knows how to translate an instance
     * of the given class into an Element (throwableToElement() will return non-null) 
     * for instances of the class, which must extend Throwable).  Returns false
     * if the implementation will return null for throwableToNode() on an instance
     * of the class.  Throws IllegalArgumentException if c is not a class which
     * extends Throwable.  Throws IllegalArgumentException if c is null.
     * @param c a Class extending Throwable
     * @return true if handles instances of c, false otherwise
     * @throws IllegalArgumentException if c is null or does not extend Throwable
     */
    public boolean supports(Class c)
        throws IllegalArgumentException;
    
}