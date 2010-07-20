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

package org.jasig.portal.portlet.session;

import javax.portlet.PortletContext;
import javax.servlet.http.HttpSession;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.pluto.container.PortletWindow;
import org.apache.pluto.container.impl.PortletSessionImpl;
import org.jasig.portal.portlet.om.IPortletEntityId;

/**
 * Custom portlet session impl, uses the entityId instead of the windowId for the session namespace
 * 
 * @author Eric Dalquist
 * @version $Revision$
 */
public class ScopingPortletSessionImpl extends PortletSessionImpl {
    protected final Log logger = LogFactory.getLog(this.getClass());

    private final IPortletEntityId portletEntityId;

    public ScopingPortletSessionImpl(IPortletEntityId portletEntityId, PortletContext portletContext, PortletWindow portletWindow, HttpSession httpSession) {
        super(portletContext, portletWindow, httpSession);
        this.portletEntityId = portletEntityId;
    }

    @Override
    protected String createPortletScopedId(String name) {
        return PORTLET_SCOPE_NAMESPACE + portletEntityId.getStringId() + ID_NAME_SEPARATOR + name;
    }

    @Override
    protected boolean isInCurrentPortletScope(String name) {
        // Portlet-scoped attribute names MUST start with "javax.portlet.p.",
        //   and contain the ID-name separator '?'.
        if (name.startsWith(PORTLET_SCOPE_NAMESPACE) && name.indexOf(ID_NAME_SEPARATOR) > -1) {
            final String id = name.substring(PORTLET_SCOPE_NAMESPACE.length(), name.indexOf(ID_NAME_SEPARATOR));
            return (id.equals(portletEntityId.getStringId()));
        }

        // Application-scoped attribute names are not in portlet scope.
        return false;
    }
}
