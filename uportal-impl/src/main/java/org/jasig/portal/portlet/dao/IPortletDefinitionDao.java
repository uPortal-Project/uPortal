/**
 * Copyright (c) 2000-2009, Jasig, Inc.
 * See license distributed with this file and available online at
 * https://www.ja-sig.org/svn/jasig-parent/tags/rel-10/license-header.txt
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
}
