/* Copyright 2005 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/
package org.jasig.portal.layout.dlm;

import org.w3c.dom.Attr;
import org.w3c.dom.Element;

/**
 * This class wraps folder layout elements in cached fragment definitions to
 * enable the layout manager to make informed descisions on attributes should be
 * persisted. Attributes on elements in the user's layout do not indicate
 * if they came from a user change or from a fragment's defined value. This
 * class enables the layout manager to make such determinations.
 * 
 * @version $Revision$ $Date$
 * @author mboyd@sungardsct.com
 * @since uPortal 2.6
 */
class FragmentNodeInfo
{
    protected Element node = null;
    
    FragmentNodeInfo(Element node)
    {
        this.node = node;
    }
    
    /**
     * Returns the value of an attribute or null if such an attribute is not
     * defined on the underlying element.
     */
    public String getAttributeValue(String name)
    {
        Attr att = node.getAttributeNode(name);
        if (att == null)
            return null;
        return att.getNodeValue();
    }
    
    /**
     * Returns true if an attribute can be modified for a node. This is based
     * on a single dlm:editAllowed attribute for nodes in fragments. If not
     * included then edits to node attributes are allowed. If included with a
     * value other than true then edits are prevented.
     * 
     * @param name
     * @return
     */
    public boolean canOverrideAttributes()
    {
        Attr att = node.getAttributeNode(Constants.ATT_EDIT_ALLOWED);
        
        if (att == null)
            return true;
        return att.getNodeValue().equals("true");
    }
}
