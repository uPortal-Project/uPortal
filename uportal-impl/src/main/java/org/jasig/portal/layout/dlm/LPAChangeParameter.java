/* Copyright 2005 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/

package org.jasig.portal.layout.dlm;

import org.jasig.portal.PortalException;
import org.jasig.portal.security.IPerson;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Layout processing action for changing a channel parameter in a user's layout.
 * 
 * @author Mark Boyd
 */
public class LPAChangeParameter implements ILayoutProcessingAction
{
    private String nodeId = null;
    private String name = null;
    private IPerson person = null;
    private Element ilfNode = null;
    private String value = null;
    
    LPAChangeParameter(String nodeId, String name, String value, IPerson p, 
            Element ilfNode)
    {
        this.nodeId = nodeId;
        this.name = name;
        this.person = p;
        this.ilfNode = ilfNode;
        this.value = value;
    }
    
    /**
     * Change the parameter for a channel in both the ILF and PLF using the
     * appropriate mechanisms for incorporated nodes versus owned nodes.
     */
    public void perform() throws PortalException
    {
        // push the change into the PLF
        if (nodeId.startsWith(Constants.FRAGMENT_ID_USER_PREFIX))
        {
            // we are dealing with an incorporated node, adding will replace
            // an existing one for the same target node id and name
            ParameterEditManager.addParmEditDirective(ilfNode, nodeId, name, 
                    value, person);
        }
        else
        {
            // node owned by user so change existing parameter child directly
            Document plf = RDBMDistributedLayoutStore.getPLF(person);
            Element plfNode = plf.getElementById(nodeId);
            changeParameterChild(plfNode, name, value);
        }
        // push the change into the ILF
        changeParameterChild(ilfNode, name, value);
    }

    /**
     * Look in the passed-in element for a parameter child element whose value
     * is to be changed to the passed-in value.
     *  
     * @param node the parent node in which to create the parameter.
     * @param name the name of the parameter to be created.
     * @param value the value of the parameter to be created.
     */
    static void changeParameterChild(Element node, String name, String value)
    {
        if (node != null)
        {
            boolean foundIt = false;
            
            for (Element parm = (Element) node.getFirstChild(); parm != null;
                parm = (Element) parm.getNextSibling())
            {
                Attr att = parm.getAttributeNode(Constants.ATT_NAME);
                if (att != null && att.getValue().equals(name))
                {
                    parm.setAttribute(Constants.ATT_VALUE, value);
                    foundIt = true;
                    break;
                }
            }
            if (! foundIt) // if didn't find it add a new one
                LPAAddParameter.addParameterChild(node, name, value);
        }
    }
}
