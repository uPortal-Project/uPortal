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

package org.jasig.portal.channels.portlet;


import org.jasig.portal.ICacheable;
import org.jasig.portal.ICharacterChannel;
import org.jasig.portal.IDirectResponse;
import org.jasig.portal.IPrivilegedChannel;
import org.jasig.portal.IResetableChannel;
import org.jasig.portal.PortalException;

/**
 * IChannel interface that describes the required functionality for a portlet. The framework
 * should treat all implementing channels of this type as portlets which includes the following
 * special behaviors:
 * Rendering when minimized
 * Action support
 * 
 * @since uPortal 2.5
 * @deprecated All IChannel implementations should be migrated to portlets
 */
@Deprecated
public interface IPortletAdaptor extends IResetableChannel, IPrivilegedChannel, ICharacterChannel, ICacheable, IDirectResponse {
    /**
     * {@link javax.servlet.http.HttpServletRequest} attribute that the adaptor will store the current
     * {@link org.jasig.portal.ChannelRuntimeData} under.
     */
    public static final String ATTRIBUTE__RUNTIME_DATA = IPortletAdaptor.class.getName() + ".RUNTIME_DATA";
    
    /**
     * {@link javax.servlet.http.HttpServletRequest} attribute that the adaptor will store the title the portlet
     * dynamically sets under.
     */
    public static final String ATTRIBUTE__PORTLET_TITLE = IPortletAdaptor.class.getName() + ".PORTLET_TITLE";
    
    /**
     * Name of the {@link org.jasig.portal.ChannelDefinition} parameter the name of the
     * {@link org.apache.pluto.descriptors.portlet.PortletAppDD} is defined in.
     * 
     * @see org.apache.pluto.spi.optional.PortletRegistryService#getPortletApplicationDescriptor(String)
     */
    public static final String CHANNEL_PARAM__PORTLET_APPLICATION_ID = "portletApplicationId";

    /**
     * Name of the {@link org.jasig.portal.ChannelDefinition} parameter the name of the
     * {@link org.apache.pluto.descriptors.portlet.PortletDD} is defined in.
     * 
     * @see org.apache.pluto.descriptors.portlet.PortletAppDD#getPortlets()
     */
    public static final String CHANNEL_PARAM__PORTLET_NAME = "portletName";
    
    /**
     * Name of the {@link org.jasig.portal.ChannelDefinition} parameter used to determine
     * if the portlet application ID should be set to the context path of the portal.
     */
    public static final String CHANNEL_PARAM__IS_FRAMEWORK_PORTLET = "isFrameworkPortlet";
    
    /**
     * {@link org.jasig.portal.IChannel#setStaticData(org.jasig.portal.ChannelStaticData)},
     * {@link org.jasig.portal.IPrivileged#setPortalControlStructures(org.jasig.portal.PortalControlStructures)}, and 
     * {@link org.jasig.portal.IChannel#setRuntimeData(org.jasig.portal.ChannelRuntimeData)} will be called before
     * this method. Actions are executed before the layout rendering pipeline is initiated and redirects can be sent though
     * content can not be written out to the response. Only one channel will execute a processAction per request.
     * 
     * @throws PortalException If an exception occurs while processing the action.
     */
    public void processAction() throws PortalException;
}