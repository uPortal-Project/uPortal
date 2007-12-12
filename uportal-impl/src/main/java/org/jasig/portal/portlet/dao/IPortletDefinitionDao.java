/**
 * Copyright 2007 The JA-SIG Collaborative.  All rights reserved.
 * See license distributed with this file and
 * available online at http://www.uportal.org/license.html
 */
package org.jasig.portal.portlet.dao;

import java.util.Set;

import org.jasig.portal.portlet.om.IPortletDefinition;
import org.jasig.portal.portlet.om.IPortletDefinitionId;

/**
 * @author Eric Dalquist
 * @version $Revision$
 */
public interface IPortletDefinitionDao {
    public IPortletDefinition createPortletDefinition(int channelPublishId, String portletApplicaitonId, String portletName);
    
    public void updatePortletDefinition(IPortletDefinition portletDefinition);
    
    public IPortletDefinition getPortletDefinition(IPortletDefinitionId portletDefinitionId);
    
    public IPortletDefinition getPortletDefinition(int channelPublishId);
    
    public Set<IPortletDefinition> getPortletDefinitions(String portletApplicaitonId, String portletName);
    
    public void deletePortletDefinition(IPortletDefinition portletDefinition);
}
