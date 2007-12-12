/**
 * Copyright 2007 The JA-SIG Collaborative.  All rights reserved.
 * See license distributed with this file and
 * available online at http://www.uportal.org/license.html
 */
package org.jasig.portal.portlet.dao;

import java.util.Set;

import org.jasig.portal.portlet.om.IPortletDefinitionId;
import org.jasig.portal.portlet.om.IPortletEntity;
import org.jasig.portal.portlet.om.IPortletEntityId;

/**
 * @author Eric Dalquist
 * @version $Revision$
 */
public interface IPortletEntityDao {
    public IPortletEntity createPortletEntity(IPortletDefinitionId portletDefinitionId, String channelSubscribeId, int userId);
    
    public void updatePortletEntity(IPortletEntity portletEntity);
    
    public IPortletEntity getPortletEntity(IPortletEntityId portletEntityId);
    
    public IPortletEntity getPortletEntity(String channelSubscribeId, int userId);
    
    public Set<IPortletEntity> getPortletEntities(IPortletDefinitionId portletDefinitionId);
    
    public void deletePortletEntity(IPortletEntity portletEntity);
}
