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
import org.w3c.dom.Element;

/**
 * Layout processing to reset a channel parameter to the value specified by the
 * owning fragment.
 * 
 * @author Mark Boyd
 */
public class LPAResetParameter implements ILayoutProcessingAction
{
    private String nodeId = null;
    private String name = null;
    private IPerson person = null;
    private Element ilfNode = null;
    private String fragmentValue = null;
    
    LPAResetParameter(String nodeId, String name, String fragmentValue, IPerson p, 
            Element ilfNode)
    {
        this.nodeId = nodeId;
        this.name = name;
        this.person = p;
        this.ilfNode = ilfNode;
        this.fragmentValue = fragmentValue;
    }
    
    /**
     * Reset the parameter to not override the value specified by a fragment.
     * This is done by removing the parm edit in the PLF and setting the value
     * in the ILF to the passed-in fragment value.
     */
    public void perform() throws PortalException
    {
        // push the change into the PLF
        if (nodeId.startsWith(Constants.FRAGMENT_ID_USER_PREFIX))
        {
            // remove the parm edit
            ParameterEditManager.removeParmEditDirective(nodeId, name, person);
        }
        // push the fragment value into the ILF
        LPAChangeParameter.changeParameterChild(ilfNode, name, fragmentValue);
    }
}
