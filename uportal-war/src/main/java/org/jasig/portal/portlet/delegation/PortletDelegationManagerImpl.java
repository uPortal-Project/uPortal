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

package org.jasig.portal.portlet.delegation;

import java.util.concurrent.ConcurrentMap;

import javax.servlet.http.HttpServletRequest;

import org.jasig.portal.api.portlet.DelegationRequest;
import org.jasig.portal.portlet.om.IPortletWindowId;
import org.jasig.portal.url.IPortalRequestUtils;
import org.jasig.portal.utils.web.PortalWebUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author Eric Dalquist
 * @version $Revision$
 */
@Service
public class PortletDelegationManagerImpl implements IPortletDelegationManager {
    private static final String DELEGATION_REQUEST_MAP_ATTR = PortletDelegationManagerImpl.class.getName() + ".DELEGATION_REQUEST_MAP";
    
    private IPortalRequestUtils portalRequestUtils;

    @Autowired
    public void setPortalRequestUtils(IPortalRequestUtils portalRequestUtils) {
        this.portalRequestUtils = portalRequestUtils;
    }

    @Override
    public void setDelegationRequest(HttpServletRequest request, IPortletWindowId delegatePortletWindowId, DelegationRequest delegationRequest) {
        final ConcurrentMap<IPortletWindowId, DelegationRequest> delegationRequestMap = getDelegationRequestMap(request);
        delegationRequestMap.put(delegatePortletWindowId, delegationRequest);
    }

    @Override
    public DelegationRequest getDelegationRequest(HttpServletRequest request, IPortletWindowId delegatePortletWindowId) {
        final ConcurrentMap<IPortletWindowId, DelegationRequest> delegationRequestMap = getDelegationRequestMap(request);
        return delegationRequestMap.get(delegatePortletWindowId);
    }

    protected ConcurrentMap<IPortletWindowId, DelegationRequest> getDelegationRequestMap(HttpServletRequest request) {
        request = this.portalRequestUtils.getOriginalPortletOrPortalRequest(request);
        final ConcurrentMap<IPortletWindowId, DelegationRequest> delegationRequestMap = PortalWebUtils.getMapRequestAttribute(request, DELEGATION_REQUEST_MAP_ATTR);
        return delegationRequestMap;
    }
}