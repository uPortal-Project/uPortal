/**
 * Copyright 2007 The JA-SIG Collaborative.  All rights reserved.
 * See license distributed with this file and
 * available online at http://www.uportal.org/license.html
 */
package org.jasig.portal.portlet.registry;

import org.apache.pluto.PortletContainerException;
import org.apache.pluto.descriptors.portlet.PortletAppDD;
import org.apache.pluto.descriptors.portlet.PortletDD;
import org.jasig.portal.portlet.om.IPortletDefinition;
import org.jasig.portal.portlet.om.IPortletDefinitionId;
import org.jasig.portal.utils.Tuple;

/**
 * Provides methods for creating and accessing {@link IPortletDefinition} and related objects.
 * 
 * @author Eric Dalquist
 * @version $Revision$
 */
public interface IPortletDefinitionRegistry {
    /**
     * Get an existing portlet definition for the definition id. If no definition exists for the id null will be returned.
     * 
     * @param portletDefinitionId The id of the definition to retrieve
     * @return The portlet definition for the id, null if no definition exists for the id.
     * @throws IllegalArgumentException If portletDefinitionId is null.
     */
    public IPortletDefinition getPortletDefinition(IPortletDefinitionId portletDefinitionId);
    
    /**
     * Get an existing portlet definition for the channel publish id. If no definition exists for the id null will be
     * returned.
     * 
     * @param channelPublishId The id of the {@link org.jasig.portal.ChannelDefinition} this portlet definition represents.
     * @return The portlet definition for the channelPublishId, null if no definition exists for the id.
     * @throws IllegalArgumentException If channelPublishId is null.
     */
    public IPortletDefinition getPortletDefinition(int channelPublishId);
    
    /**
     * Creates a new, persisted, portlet definition for the published channel. If the
     * {@link org.jasig.portal.ChannelDefinition} for the channelPublishId can't be found or an definition already
     * exists for the channel definition id an exception will be thrown.
     * 
     * @param channelPublishId The id of the {@link org.jasig.portal.ChannelDefinition} this portlet definition represents.
     * @return A new definition for the parameters
     * @throws org.springframework.dao.DataIntegrityViolationException If a definition already exists for the specified
     *         channelPublishId
     * @throws org.springframework.dao.DataRetrievalFailureException If no {@link org.jasig.portal.ChannelDefinition} can
     *         be found for the publish ID or the channel definition does not have the required channel parameters
     *         {@link org.jasig.portal.channels.portlet.IPortletAdaptor#CHANNEL_PARAM__PORTLET_APPLICATION_ID} and
     *         {@link org.jasig.portal.channels.portlet.IPortletAdaptor#CHANNEL_PARAM__PORTLET_NAME}.
     */
    public IPortletDefinition createPortletDefinition(int channelPublishId);
    
    /**
     * Convience for {@link #getPortletDefinition(int)} and {@link #createPortletDefinition(int)}. If
     * the get returns null the definition will be created and returned.
     * 
     * @see #getPortletDefinition(int)
     * @see #createPortletDefinition(int)
     */
    public IPortletDefinition getOrCreatePortletDefinition(int channelPublishId);

    /**
     * Persists changes to a IPortletDefinition.
     * 
     * @param portletDefinition The IPortletDefinition to store changes to.
     * @throws IllegalArgumentException If portletDefinition is null
     */
    public void updatePortletDefinition(IPortletDefinition portletDefinition);
    
    /**
     * Gets the parent portlet descriptor for the entity specified by the definition id.
     * 
     * @param portletDefinitionId The definition ID to get the parent descriptor for.
     * @return The parent portlet descriptor for the definition, null if no definition exists for the id. 
     * @throws IllegalArgumentException if portletDefinitionId is null
     */
    public PortletDD getParentPortletDescriptor(IPortletDefinitionId portletDefinitionId) throws PortletContainerException;

    /**
     * Gets the parent portlet application descriptor for the entity specified by the definition id.
     * 
     * @param portletDefinitionId The definition ID to get the parent application descriptor for.
     * @return The parent portlet descriptor for the application definition, null if no definition exists for the id. 
     * @throws IllegalArgumentException if portletDefinitionId is null
     */
    public PortletAppDD getParentPortletApplicationDescriptor(IPortletDefinitionId portletDefinitionId) throws PortletContainerException;
    
    /**
     * Get the portletApplicationId and portletName for the specified portlet definition. The portletApplicationId
     * will be {@link Tuple#first} and the portletName will be {@link Tuple#second}
     */
    public Tuple<String, String> getPortletDescriptorKeys(IPortletDefinition portletDefinition);
}
