/* Copyright 2005 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/

package org.jasig.portal.layout.dlm;

import org.jasig.portal.PortalException;
import org.jasig.portal.security.IPerson;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Layout processing action for adding a channel parameter to the user's layout.
 * 
 * @author mboyd@sungardsct.com
 */
public class LPA_AddParameter implements ILayoutProcessingAction
{
    private String nodeId = null;
    private String name = null;
    private IPerson person = null;
    private Element ilfNode = null;
    private String value = null;
    
    LPA_AddParameter(String nodeId, String name, String value, IPerson p, 
            Element ilfNode)
    {
        this.nodeId = nodeId;
        this.name = name;
        this.person = p;
        this.ilfNode = ilfNode;
        this.value = value;
    }
    
    /**
     * Add a parameter for a channel in both the ILF and PLF using the
     * appropriate mechanisms for incorporated nodes versus owned nodes.
     */
    public void perform() throws PortalException
    {
        // push the change into the PLF
        if (nodeId.startsWith(Constants.FRAGMENT_ID_USER_PREFIX))
        {
            // we are dealing with an incorporated node
            ParameterEditManager.addParmEditDirective(ilfNode, nodeId, name, 
                    value, person);
        }
        else
        {
            // node owned by user so add parameter child directly
            Document plf = (Document) person.getAttribute( Constants.PLF );
            Element plfNode = plf.getElementById(nodeId);
            addParameterChild(plfNode, name, value);
        }
        // push the change into the ILF
        addParameterChild(ilfNode, name, value);
    }

    /**
     * Performs parameter child element creation in the passed in Element.
     * 
     * @param node the parent node in which to create the parameter.
     * @param name the name of the parameter to be created.
     * @param value the value of the parameter to be created.
     */
    static void addParameterChild(Element node, String name, String value)
    {
        if (node != null)
        {
            Document doc = node.getOwnerDocument();
            Element parm = doc.createElement( Constants.ELM_PARAMETER );
            parm.setAttribute( Constants.ATT_NAME, name );
            parm.setAttribute( Constants.ATT_VALUE, value );
            /*
             * Set the override attribute to 'yes'. This isn't persisted since
             * this is determined upon loading the layout based upon the 
             * channel definition. But for code that references this in-memory
             * value like ChannelStaticData it needs to be set. 
             */
            parm.setAttribute( Constants.ATT_OVERRIDE, Constants.CAN_OVERRIDE );
            node.appendChild( parm );
        }
    }
}
