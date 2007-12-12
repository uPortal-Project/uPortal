/**
 * Copyright 2007 The JA-SIG Collaborative.  All rights reserved.
 * See license distributed with this file and
 * available online at http://www.uportal.org/license.html
 */
package org.jasig.portal.portlet.om;



/**
 * A portlet entity represents what a user subscribes to (adds to their layout) in
 * the portal object model.
 * 
 * @author Eric Dalquist
 * @version $Revision$
 */
public interface IPortletEntity {
    /**
     * @return The unique identifier for this portlet entity.
     */
    public IPortletEntityId getPortletEntityId();
    
    /**
     * @return The ID of the parent portlet defintion
     */
    public IPortletDefinitionId getPortletDefinitionId();
    
    /**
     * @return The subscribe ID for the channel underlying this entity.
     */
    public String getChannelSubscribeId();
    
    /**
     * @return The ID of the user this entity is for. 
     */
    public int getUserId();
    
    /**
     * @return The preferences for this portlet entity, will not be null.
     */
    public IPortletPreferences getPortletPreferences();
    
    /**
     * @param portletPreferences The preferences for this portlet entity.
     * @throws IllegalArgumentException If preferences is null.
     */
    public void setPortletPreferences(IPortletPreferences portletPreferences);
    
}
