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

package org.jasig.portal.layout.dlm.providers;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jasig.portal.PortalException;
import org.jasig.portal.ThemeStylesheetUserPreferences;
import org.jasig.portal.UserPreferences;
import org.jasig.portal.layout.dlm.DistributedLayoutManager;
import org.jasig.portal.layout.dlm.processing.IParameterProcessor;
import org.jasig.portal.security.IPerson;

/**
 * A class used to handle requests to delete channels when the delete button is
 * pressed in the regular layout view.
 *  
 * @author mark.boyd@sungardhe.com
 */
public class RegularViewChannelRemover implements IParameterProcessor
{
    private static final Log LOG = LogFactory.getLog(RegularViewChannelRemover.class);
    private DistributedLayoutManager dlm = null;
    
    /**
     * Captures the passed-in DistributedLayoutManager instance for use when 
     * processing parameters.
     * 
     * @see org.jasig.portal.layout.dlm.processing.IParameterProcessor#setResources(org.jasig.portal.security.IPerson, org.jasig.portal.layout.dlm.DistributedLayoutManager)
     */
    public void setResources(IPerson person, DistributedLayoutManager dlm)
    {
        this.dlm = dlm;
    }

    /**
     * Watches for uP_remove_target request parameter containing the node ID of 
     * a layout node to be deleted. If seen then it calls 
     * DistributedLayoutManager.deleteNode() passing that ID.
     *  
     * @see org.jasig.portal.layout.dlm.processing.IParameterProcessor#processParameters(org.jasig.portal.UserPreferences, javax.servlet.http.HttpServletRequest)
     */
    public void processParameters(UserPreferences prefs, HttpServletRequest request)
    {
        String chanNodeId = request.getParameter("uP_remove_target");
        
        if (chanNodeId != null && ! chanNodeId.equals(""))
        {
            try
            {
                dlm.deleteNode(chanNodeId);
                dlm.saveUserLayout();
            } catch (PortalException e)
            {
                LOG.error("Unable to delete node " + chanNodeId, e);
            }
        }
    }
}
