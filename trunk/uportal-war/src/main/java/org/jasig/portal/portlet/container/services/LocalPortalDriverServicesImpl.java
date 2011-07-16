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
