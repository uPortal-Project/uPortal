/* Copyright 2001 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/

package org.jasig.portal.utils;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Prior to uPortal 2.5 this interface provided an ability to access DOM 3 functionality
 * on top of a DOM 2 core Document implementation.
 * 
 * As of uPortal 2.5, uPortal has adopted the JAXP 1.3 standard DOM 3 core Document
 * implementation (as available in the core JDK 1.5 release).  This does not imply a 
 * requirement of JDK 1.5 -- these libraries are available as an endorsed extenstion for
 * JDK 1.4.
 * 
 * Therefore, as of uPortal 2.5, there is no reason to write code to the IPortalDocument
 * interface.  Instead, client code should be written to the core Document interface.
 * 
 * This interface is formally deprecated.  No new code should be written to this interface
 * and all existing clients of this interface should be updated to consume the core
 * DOM3 Document interface.
 *
 * @author Nick Bolton
 * @version $Revision$
 * @deprecated use DOM 3 Documents instead.
 */
public interface IPortalDocument extends Document {

    /**
     * Registers an identifier name with a specified element node.
     *
     * @param idName a key used to store an <code>Element</code> object.
     * @param element an <code>Element</code> object to map.
     * document.
     */
    public void putIdentifier(String idName, Element element);

   /**
     * Copies the element cache from the source document. This will
     * provide equivalent mappings from IDs to elements in this
     * document provided the elements exist in the source document.
     *
     * @param sourceDoc The source doc to copy from.
     */
    public void copyCache(IPortalDocument sourceDoc);
}
