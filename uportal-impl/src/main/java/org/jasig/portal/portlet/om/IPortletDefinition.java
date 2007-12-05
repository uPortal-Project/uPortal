/**
 * Copyright 2007 The JA-SIG Collaborative.  All rights reserved.
 * See license distributed with this file and
 * available online at http://www.uportal.org/license.html
 */
package org.jasig.portal.portlet.om;

/**
 * A portlet definition is equivalant to a published ChannelDefinition. 
 * 
 * @author Eric Dalquist
 * @version $Revision$
 */
public interface IPortletDefinition {
    /**
     * @return The unique identifier for this portlet definition.
     */
    public IPortletDefinitionId getPortletDefinitionId();
    
    /**
     * @return The ID of the {@link org.jasig.portal.ChannelDefinition} this portlet definition is based on.
     */
    public String getChannelDefinitionId();
    
    /**
     * @return The portlet application ID of the {@link org.apache.pluto.descriptors.portlet.PortletAppDD} that this definition was created for.
     */
    public String getPortletApplicationId();
    
    /**
     * @return The porltet name of the {@link org.apache.pluto.descriptors.portlet.PortletDD} that this definition was created for.
     */
    public String getPortletName();
}
