/* Copyright 2005 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/
package org.jasig.portal.layout.dlm;

import org.w3c.dom.Attr;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * This class wraps channel layout elements in cached fragment definitions to 
 * enable the layout manager to make informed descisions on what channel 
 * parameters should be persisted. Parameter elements residing in the user's
 * layout do not indicate if they came from a user change or from a fragment's
 * defined value. This class enables the layout manager to such determinations.
 * 
 * @version $Revision$ $Date$
 * @author mboyd@sungardsct.com
 * @since uPortal 2.5 
 */
class FragmentChannelInfo extends FragmentNodeInfo
{
    FragmentChannelInfo(Element channel)
    {
        super(channel);
    }
    
    /**
     * Returns the value of an parameter or null if such a parameter is not
     * defined on the underlying channel element.
     */
    public String getParameterValue(String name)
    {
        NodeList parms = node.getChildNodes();
        
        for (int i=0; i<parms.getLength(); i++)
        {
            Element parm = (Element) parms.item(i);
            if (parm.getTagName().equals(Constants.ELM_PARAMETER))
            {
                String parmName = parm.getAttribute(Constants.ATT_NAME);
                if (parmName.equals(name)) 
                {
                    return parm.getAttribute(Constants.ATT_VALUE);
                }
            }
        }
        // if we get here then neither the fragment  nor the channel definition 
        // provided this parameter
        return null;
    }
    
    /**
     * Returns true if a parameter can be modified for a channel. This is based
     * on a single dlm:editAllowed attribute for each channel parameter in
     * fragments. If not included then edits to that channel parameter are
     * allowed. If included with a value other than true then edits to that
     * parameter for that channel are prevented.
     * 
     * @param name
     * @return
     */
    public boolean canOverrideParameter(String name)
    {
        NodeList parms = node.getChildNodes();
        
        for (int i=0; i<parms.getLength(); i++)
        {
            Element parm = (Element) parms.item(i);
            if (parm.getTagName().equals(Constants.ELM_PARAMETER))
            {
                String parmName = parm.getAttribute(Constants.ATT_NAME);
                if (parmName.equals(name))
                {
                    Attr editAllowed = parm.getAttributeNodeNS(
                            Constants.NS_URI, Constants.ATT_EDIT_ALLOWED);
                    if (editAllowed == null || 
                            ! editAllowed.getNodeValue().equals("false"))
                        return true;
                    else
                        return false;
                }
            }
        }
        // if we get here then the fragment doesn't specify this param
        return true;
    }
}
