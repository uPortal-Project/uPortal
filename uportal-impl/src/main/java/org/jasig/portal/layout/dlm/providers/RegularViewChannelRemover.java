/* Copyright 2006 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
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
            } catch (PortalException e)
            {
                LOG.error("Unable to delete node " + chanNodeId, e);
            }
        }
    }
}
