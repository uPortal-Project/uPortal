/* Copyright 2004 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/

package org.jasig.portal.container.services;

import java.util.HashMap;
import java.util.Map;

import org.apache.pluto.services.ContainerService;
import org.apache.pluto.services.PortletContainerEnvironment;

/**
 * Implementation of Apache Pluto PortletContainerEnvironment.
 * @author Ken Weiner, kweiner@unicon.net
 * @version $Revision$
 */
public class PortletContainerEnvironmentImpl implements PortletContainerEnvironment {

    private Map containerServices = new HashMap();

    public ContainerService getContainerService(Class service) {
        return(ContainerService)containerServices.get(service);
    }
    
    // Additional methods.

    public void addContainerService(ContainerService service) {
        Class serviceClass = service.getClass();
        while (serviceClass != null) {
            Class[] interfaces = serviceClass.getInterfaces();
            for (int i = 0; i < interfaces.length; i++) {
                Class[] interfaces2 = interfaces[i].getInterfaces();
                for (int j = 0; j < interfaces2.length; j++) {
                    if (interfaces2[j].equals(ContainerService.class)) {
                        containerServices.put(interfaces[i], service);
                    }
                }
            }
            serviceClass = serviceClass.getSuperclass();
        }
    }

}
