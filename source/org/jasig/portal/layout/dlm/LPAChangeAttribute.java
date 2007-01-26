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
 * @author Mark Boyd
 */
public class LPAChangeAttribute implements ILayoutProcessingAction
{
    private String nodeId = null;
    private String name = null;
    private IPerson person = null;
    private Element ilfNode = null;
    private String value = null;
    private boolean isFragmentOwner = false;
    
    LPAChangeAttribute(String nodeId, String name, String value, IPerson p, 
            Element ilfNode, boolean isFragmentOwner)
    {
        this.nodeId = nodeId;
        this.name = name;
        this.person = p;
        this.ilfNode = ilfNode;
        this.value = value;
        this.isFragmentOwner = isFragmentOwner;
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
            // attribute that should be pushed into the ilf during merge
            Element plfNode = HandlerUtils
            .getPLFNode( ilfNode, person,
                         true, // create node if not found
                         false ); // don't create children
            plfNode.setAttribute(name, value);
            /*
             * add directive to hold override value. This is not necessary for
             * folder names since they are overridden at render time with their
             * locale specific version and persisted via a different mechanism.
             */
            EditManager.addEditDirective(plfNode, name, person);

            // name attribute takes special handling since it is localizable
            if (name.equals(Constants.ATT_NAME) && 
                    DistributedLayoutManager.ContextHolder.labelPolicy != null)
            {
                String plfId = plfNode.getAttribute(Constants.ATT_PLF_ID);
                DistributedLayoutManager.ContextHolder.labelPolicy
                    .updateNodeLabel(plfId, person.getID(), false, value);
            }
        }
        else
        {
            // node owned by user so change attribute in child directly
            Document plf = RDBMDistributedLayoutStore.getPLF(person);
            Element plfNode = plf.getElementById(nodeId);
            if (plfNode != null) // should always be non-null
            {
                // name attribute takes special handling since it is localizable
                if (name.equals(Constants.ATT_NAME) && 
                        DistributedLayoutManager.ContextHolder.labelPolicy != null)
                {
                    String plfId = plfNode.getAttribute(Constants.ATT_PLF_ID);
                    DistributedLayoutManager.ContextHolder.labelPolicy
                            .updateNodeLabel(nodeId, person.getID(),
                                    isFragmentOwner, value);
                }
                else
                {
                    plfNode.setAttribute(name, value);
                }
            }
        }
        /*
         * push the change into the ILF if not the name attribute. For names
         * the rendering will inject the localized name via a special processor.
         * So it doesn't matter what is in the ILF's folder name attribute.
         */
        if (!name.equals(Constants.ATT_NAME))
        {
            ilfNode.setAttribute(name, value);
        }
    }
}
