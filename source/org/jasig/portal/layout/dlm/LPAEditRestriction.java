/* Copyright 2005 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/

package org.jasig.portal.layout.dlm;

import org.jasig.portal.PortalException;
import org.jasig.portal.security.IPerson;
import org.w3c.dom.Element;

/**
 * Layout processing action for changnig the set of restrictions for a fragment
 * node.
 * 
 * @author mboyd@sungardsct.com
 */
public class LPAEditRestriction implements ILayoutProcessingAction
{
    private IPerson person = null;
    private Element ilfNode = null;
    private boolean moveAllowed = false;
    private boolean deleteAllowed = false;
    private boolean editAllowed = false;
    private boolean addChildAllowed = false;

    LPAEditRestriction(IPerson p, Element ilfNode, boolean moveAllowed,
            boolean deleteAllowed, boolean editAllowed, boolean addChildAllowed)
    {
        this.person = p;
        this.ilfNode = ilfNode;
        this.moveAllowed = moveAllowed;
        this.deleteAllowed = deleteAllowed;
        this.editAllowed = editAllowed;
        this.addChildAllowed = addChildAllowed;
    }

    /**
     * Pushes the indicated restriction attribute changes into both the ILF and
     * PLF versions of the layouts since this will only be done when editing a
     * fragment.
     */
    public void perform() throws PortalException
    {
        Element plfNode = HandlerUtils.getPLFNode(ilfNode, person, false, false);

        if (plfNode == null)
            return;
        
        changeRestriction(Constants.ATT_MOVE_ALLOWED, plfNode, moveAllowed);
        changeRestriction(Constants.ATT_MOVE_ALLOWED, ilfNode, moveAllowed);
        changeRestriction(Constants.ATT_DELETE_ALLOWED, plfNode, deleteAllowed);
        changeRestriction(Constants.ATT_DELETE_ALLOWED, ilfNode, deleteAllowed);
        changeRestriction(Constants.ATT_EDIT_ALLOWED, plfNode, editAllowed);
        changeRestriction(Constants.ATT_EDIT_ALLOWED, ilfNode, editAllowed);

        if (plfNode.getAttribute(Constants.ATT_CHANNEL_ID).equals(""))
        {
            // if channel id is empty then this is not a channel.
            // childAllowed is always true for channel ad-hoc parameters.
            changeRestriction(Constants.ATT_ADD_CHILD_ALLOWED, plfNode,
                    addChildAllowed);
            changeRestriction(Constants.ATT_ADD_CHILD_ALLOWED, ilfNode,
                    addChildAllowed);
        }
    }

    private void changeRestriction(String name, Element element,
            boolean value)
    {
        // The default value for these attributes,
        // if not included on an element, is true so we remove the attribute if
        // we see a parm value of true and add the attribute with a value of
        // false if a parm value is false. This also saves on the number of
        // rows in the up_layout_param table and hence on read and write time.

        if (value == false)
            element.setAttributeNS(Constants.NS_URI, name, "false");
        else
            element.removeAttribute(name);
    }
}
