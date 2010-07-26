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
 * Layout processing action for removing a channel parameter from the user's layout.
 * 
 * @author Mark Boyd
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
            Document plf = RDBMDistributedLayoutStore.getPLF(person);
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
            for (Element parm = (Element) node.getFirstChild(); parm != null;
                parm = (Element) parm.getNextSibling())
            {
                if (parm.getAttribute(Constants.ATT_NAME).equals(name))
                {
                    node.removeChild(parm);
                    return;
                }
            }
        }
    }
}
