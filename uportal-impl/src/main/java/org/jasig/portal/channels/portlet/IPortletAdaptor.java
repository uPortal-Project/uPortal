/* Copyright 2005 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/

package org.jasig.portal.channels.portlet;

import javax.portlet.PortletMode;
import javax.portlet.WindowState;

import org.jasig.portal.ICacheable;
import org.jasig.portal.ICharacterChannel;
import org.jasig.portal.IPrivilegedChannel;
import org.jasig.portal.PortalException;

/**
 * IChannel interface that describes the required functionality for a portlet. The framework
 * should treat all implementing channels of this type as portlets which includes the following
 * special behaviors:
 * Rendering when minimized
 * Action support
 * 
 * @since uPortal 2.5
 */
public interface IPortletAdaptor extends IPrivilegedChannel, ICharacterChannel, ICacheable {
    public static final WindowState EXCLUSIVE = new WindowState("EXCLUSIVE");
    
    public static final PortletMode ABOUT = new PortletMode("ABOUT");
    
    /**
     * {@link javax.servlet.http.HttpServletRequest} attribute that the adaptor will store the current
     * {@link org.jasig.portal.ChannelRuntimeData} under.
     */
    public static final String ATTRIBUTE__RUNTIME_DATA = IPortletAdaptor.class.getName() + ".RUNTIME_DATA";
    
    /**
     * {@link javax.servlet.http.HttpServletRequest} attribute that the adaptor will store the title the portlet
     * dynamicly sets under.
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