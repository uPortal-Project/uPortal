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
        }
        else
        {
            // node owned by user so change attribute in child directly
            Document plf = RDBMDistributedLayoutStore.getPLF(person);
            Element plfNode = plf.getElementById(nodeId);
            if (plfNode != null) // should always be non-null
            {
                plfNode.setAttribute(name, value);
            }
        }
        /*
         * push the change into the ILF if not the name attribute. For names
         * the rendering will inject the localized name via a special processor.
         * So it doesn't matter what is in the ILF's folder name attribute.
         */
        if (!name.equals(Constants.ATT_NAME))
        		// should always be non-null
        {
            ilfNode.setAttribute(name, value);
        }
    }
}
