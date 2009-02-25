/**
 * Copyright (c) 2000-2009, Jasig, Inc.
 * See license distributed with this file and available online at
 * https://www.ja-sig.org/svn/jasig-parent/tags/rel-10/license-header.txt
 */

package org.jasig.portal.serialize;


import java.util.Hashtable;
import java.util.Map;


/**
 * Holds the state of the currently serialized element.
 *
 *
 * @version $Revision$ $Date$
 * @author <a href="mailto:arkin@intalio.com">Assaf Arkin</a>
 * @see BaseMarkupSerializer
 */
public class ElementState
{


    /**
     * The element's raw tag name (local or prefix:local).
     */
    public String rawName;


    /**
     * The element's local tag name.
     */
    public String localName;


    /**
     * The element's namespace URI.
     */
    public String namespaceURI;


    /**
     * True if element is space preserving.
     */
    public boolean preserveSpace;


    /**
     * True if element is empty. Turns false immediately
     * after serializing the first contents of the element.
     */
    public boolean empty;


    /**
     * True if the last serialized node was an element node.
     */
    public boolean afterElement;


    /**
     * True if the last serialized node was a comment node.
     */
    public boolean afterComment;


    /**
     * True if textual content of current element should be
     * serialized as CDATA section.
     */
    public boolean doCData;


    /**
     * True if textual content of current element should be
     * serialized as raw characters (unescaped).
     */
    public boolean unescaped;


    /**
     * True while inside CData and printing text as CData.
     */
    public boolean inCData;


    /**
     * Association between namespace URIs (keys) and prefixes (values).
     */
    public Map<String, String> prefixes;

    public boolean inScript;

}
