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
public class FragmentNodeInfo
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
