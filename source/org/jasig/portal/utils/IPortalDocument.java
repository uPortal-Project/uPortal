/* Copyright 2001 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/

package org.jasig.portal.utils;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * An interface that allows a Document to cache elements by known keys.
 * This is used to locally store and manager the ID element mappings 
 * regardless of the actual DOM implementation.
 *
 * @author Nick Bolton
 * @version $Revision$
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
