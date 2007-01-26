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
 * @author Mark Boyd
 */
public class LPAResetAttribute implements ILayoutProcessingAction
{
    private String nodeId = null;
    private String name = null;
    private IPerson person = null;
    private Element ilfNode = null;
    private String fragmentValue = null;
    
    LPAResetAttribute(String nodeId, String name, String fragmentValue, IPerson p, 
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
            
            // handle label localization updates
            if (name.equals(Constants.ATT_NAME) && 
                    DistributedLayoutManager.ContextHolder.labelPolicy != null)
            {
                Element plfNode = HandlerUtils
                    .getPLFNode( ilfNode, person,
                          false, // create node if not found
                          false ); // don't create children
                if (plfNode != null) // will always be non-null if we get here
                {
                    String plfId = plfNode.getAttribute(Constants.ATT_PLF_ID);
                    DistributedLayoutManager.ContextHolder.labelPolicy
                        .deleteNodeLabel(nodeId, plfId, 
                            false, // only remove for current locale
                            person.getID(), false); // not a fragment owner
                }
            }
        }
        /*
         * push the fragment value into the ILF if not the name element. For the
         * name element the locale specific value will be injected during
         * layout rendering.
         */
        if (! name.equals(Constants.ATT_NAME))
        {
            ilfNode.setAttribute(name, fragmentValue);
        }
    }
}
