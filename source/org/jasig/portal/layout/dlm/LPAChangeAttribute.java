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
 * Layout processing action for changing a node's attribute in the user's
 * layout.
 * 
 * @author mboyd@sungardsct.com
 */
public class LPAChangeAttribute implements ILayoutProcessingAction
{
    private String nodeId = null;
    private String name = null;
    private IPerson person = null;
    private Element ilfNode = null;
    private String value = null;
    
    LPAChangeAttribute(String nodeId, String name, String value, IPerson p, 
            Element ilfNode)
    {
        this.nodeId = nodeId;
        this.name = name;
        this.person = p;
        this.ilfNode = ilfNode;
        this.value = value;
    }
    
    /**
     * Apply the attribute change.
     */
    public void perform() throws PortalException
    {
        // push the change into the PLF
        if (nodeId.startsWith(Constants.FRAGMENT_ID_USER_PREFIX))
        {
            // we are dealing with an incorporated node, so get a plf ghost
            // node, set its attribute, then add a directive indicating the
            // attribute that should be pushed into the ilf
            Element plfNode = HandlerUtils
            .getPLFNode( ilfNode, person,
                         true, // create node if not found
                         false ); // don't create children
            plfNode.setAttribute(name, value);
            EditManager.addEditDirective(plfNode, name, person);
        }
        else
        {
            // node owned by user so change attribute in child directly
            Document plf = (Document) person.getAttribute( Constants.PLF );
            Element plfNode = plf.getElementById(nodeId);
            if (plfNode != null) // should always be non-null
                plfNode.setAttribute(name, value);
        }
        // push the change into the ILF
        ilfNode.setAttribute(name, value);
    }
}
