/* Copyright 2005 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/

package org.jasig.portal.layout.dlm;

import org.jasig.portal.PortalException;
import org.jasig.portal.security.IPerson;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * Layout processing action for removing a channel parameter from the user's layout.
 * 
 * @author mboyd@sungardsct.com
 */
public class LPARemoveParameter implements ILayoutProcessingAction
{
    private String nodeId = null;
    private String name = null;
    private IPerson person = null;
    private Element ilfNode = null;
    
    LPARemoveParameter(String nodeId, String name, IPerson p, 
            Element ilfNode)
    {
        this.nodeId = nodeId;
        this.name = name;
        this.person = p;
        this.ilfNode = ilfNode;
    }
    
    /**
     * Remove the parameter from a channel in both the ILF and PLF using the
     * appropriate mechanisms for incorporated nodes versus owned nodes.
     */
    public void perform() throws PortalException
    {
        // push the change into the PLF
        if (nodeId.startsWith(Constants.FRAGMENT_ID_USER_PREFIX))
        {
            // we are dealing with an incorporated node
            ParameterEditManager.removeParmEditDirective(nodeId, name, person);
        }
        else
        {
            // node owned by user so add parameter child directly
            Document plf = (Document) person.getAttribute( Constants.PLF );
            Element plfNode = plf.getElementById(nodeId);
            removeParameterChild(plfNode, name);
        }
        // push the change into the ILF
        removeParameterChild(ilfNode, name);
    }

    /**
     * @param node the parent node in which to create the parameter.
     * @param name the name of the parameter to be created.
     * @param value the value of the parameter to be created.
     */
    static void removeParameterChild(Element node, String name)
    {
        if (node != null)
        {
            NodeList params = node.getChildNodes();
            for (int i=0; i<params.getLength(); i++)
            {
                Element parm = (Element) params.item(i);
                
                if (parm.getAttribute(Constants.ATT_NAME).equals(name))
                {
                    node.removeChild(parm);
                    return;
                }
            }
        }
    }
}
