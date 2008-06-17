/**
 * Copyright 2007 The JA-SIG Collaborative.  All rights reserved.
 * See license distributed with this file and
 * available online at http://www.uportal.org/license.html
 */
package org.jasig.portal.portlet.dao;

import org.jasig.portal.portlet.om.IPortletDefinition;
import org.jasig.portal.portlet.om.IPortletDefinitionId;

/**
 * Provides APIs for creating, storing and retrieving {@link IPortletDefinition} objects.
 * 
 * @author Eric Dalquist
 * @version $Revision$
 */
public interface IPortletDefinitionDao {
    /**
     * Creates, initializes and persists a new {@link IPortletDefinition} based on the specified channelPublishId and 
     * portletApplicationId and portletName.
     * 
     * @param channelPublishId The ID of the {@link org.jasig.portal.ChannelDefinition} this portlet definition is for.
     * @return A newly created, initialized and persisted {@link IPortletDefinition}
     * @throws org.springframework.dao.DataIntegrityViolationException If a definition already exists for the specified
     *         channelPublishId
     */
    public IPortletDefinition createPortletDefinition(int channelPublishId);
    
    /**
     * Persists changes to a {@link IPortletDefinition}.
     * 
     * @param portletDefinition The portlet definition to store the changes for
     * @throws IllegalArgumentException if portletDefinition is null.
     */
    public void updatePortletDefinition(IPortletDefinition portletDefinition);
    
    /**
     * Get a {@link IPortletDefinition} for the specified {@link IPortletDefinitionId}.
     * 
     * @param portletDefinitionId The id to get the definition for.
     * @return The portlet definition for the id, null if no definition exists for the id.
     * @throws IllegalArgumentException if portletDefinitionId is null.
     */
    public IPortletDefinition getPortletDefinition(IPortletDefinitionId portletDefinitionId);
    
    /**
     * Get a {@link IPortletDefinition} for the specified channel publish id.
     * 
     * @param channelPublishId The id to get the definition for.
     * @return The portlet definition for the id, null if no definition exists for the id.
     */
    public IPortletDefinition getPortletDefinition(int channelPublishId);
    
    /**
     * Removes the specified {@link IPortletDefinition} from the persistent store.
     * 
     * @param portletDefinition The definition to remove.
     * @throws IllegalArgumentException if portletDefinition is null.
     */
    public void deletePortletDefinition(IPortletDefinition portletDefinition);
}
