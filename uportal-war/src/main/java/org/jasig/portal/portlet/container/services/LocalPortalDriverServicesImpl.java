/**
 * Copyright (c) 2000-2009, Jasig, Inc.
 * See license distributed with this file and available online at
 * https://www.ja-sig.org/svn/jasig-parent/tags/rel-10/license-header.txt
 */

package org.jasig.portal.portlet.container.services;

import org.apache.pluto.container.driver.OptionalContainerServices;
import org.apache.pluto.container.driver.PortalDriverContainerServices;
import org.apache.pluto.container.driver.RequiredContainerServices;
import org.apache.pluto.driver.container.PortalDriverServicesImpl;

/**
 * Simple extension of pluto to allow injection of PortalDriverContainerServices as well.
 * 
 * @author Eric Dalquist
 * @version $Revision$
 */
public class LocalPortalDriverServicesImpl extends PortalDriverServicesImpl {

    public LocalPortalDriverServicesImpl(
            RequiredContainerServices required, 
            OptionalContainerServices optional,
            PortalDriverContainerServices driver) {
        
        super(required.getPortalContext(), 
                required.getPortletRequestContextService(),
                required.getEventCoordinationService(), 
                required.getFilterManagerService(), 
                required.getPortletURLListenerService(), 
                optional, 
                driver.getPortletContextService(), 
                driver.getPortletRegistryService(), 
                driver.getPortalAdministrationService());
    }

}
