/* Copyright 2005 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/

package org.jasig.portal.layout.dlm;

import org.jasig.portal.PortalException;
import org.jasig.portal.security.IPerson;
import org.w3c.dom.Element;

/**
 * Layout processing action to reset an attribute to the value specified by the
 * owning fragment.
 * 
 * @author mboyd@sungardsct.com
 */
public class LPA_ResetAttribute implements ILayoutProcessingAction
{
    private String nodeId = null;
    private String name = null;
    private IPerson person = null;
    private Element ilfNode = null;
    private String fragmentValue = null;
    
    LPA_ResetAttribute(String nodeId, String name, String fragmentValue, IPerson p, 
            Element ilfNode)
    {
        this.nodeId = nodeId;
        this.name = name;
        this.person = p;
        this.ilfNode = ilfNode;
        this.fragmentValue = fragmentValue;
    }
    
    /**
     * Reset a parameter to not override the value specified by a fragment.
     * This is done by removing the parm edit in the PLF and setting the value
     * in the ILF to the passed-in fragment value.
     */
    public void perform() throws PortalException
    {
        /*
         * push the change into the PLF
         */
        if (nodeId.startsWith(Constants.FRAGMENT_ID_USER_PREFIX))
        {
            // remove the parm edit
            EditManager.removeEditDirective(nodeId, name, person);
        }
        /*
         * push the fragment value into the ILF.
         */
        ilfNode.setAttribute(name, fragmentValue);
    }
}
