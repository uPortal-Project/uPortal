/**
 * Copyright © 2004 The JA-SIG Collaborative.  All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. Redistributions of any form whatsoever must retain the following
 *    acknowledgment:
 *    "This product includes software developed by the JA-SIG Collaborative
 *    (http://www.jasig.org/)."
 *
 * THIS SOFTWARE IS PROVIDED BY THE JA-SIG COLLABORATIVE "AS IS" AND ANY
 * EXPRESSED OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL THE JA-SIG COLLABORATIVE OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
 * NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 * OF THE POSSIBILITY OF SUCH DAMAGE.
 *
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
