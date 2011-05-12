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

package org.jasig.portal.portlet.container;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.pluto.container.ContainerServices;
import org.apache.pluto.container.PortletContainer;
import org.apache.pluto.container.impl.PortletContainerImpl;

/**
 * uPortal specific extension to the Pluto {@link PortletContainer}
 * 
 * @author Eric Dalquist
 * @version $Revision$
 */
public class UportalPortletContainerImpl extends PortletContainerImpl {

    /**
     * @see PortletContainerImpl#PortletContainerImpl(String, ContainerServices)
     */
    public UportalPortletContainerImpl(String name, ContainerServices requiredServices) {
        super(name, requiredServices);
    }

    @Override
    protected void redirect(HttpServletRequest request, HttpServletResponse response, String location) throws IOException {
        //uPortal handles redirects itself, no need for pluto to do it
    }
}
