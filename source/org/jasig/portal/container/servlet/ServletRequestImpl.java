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

package org.jasig.portal.container.servlet;

import java.security.Principal;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;

import org.apache.pluto.om.common.SecurityRoleRef;
import org.apache.pluto.om.common.SecurityRoleRefSet;
import org.jasig.portal.groups.IEntityGroup;
import org.jasig.portal.groups.IGroupMember;
import org.jasig.portal.security.IPerson;
import org.jasig.portal.services.GroupService;

import sun.security.acl.PrincipalImpl;


/**
 * A wrapper of the real HttpServletRequest that allows
 * modification of the request parameters and a uPortal
 * implementation of security methods.
 * <p>
 * uPortal's {@link IPerson} and {@link GroupService}
 * are used to determine the remote user its role
 * memberships in the case that the container does not know.
 * @author Ken Weiner, kweiner@unicon.net
 * @version $Revision$
 */
public class ServletRequestImpl extends HttpServletRequestWrapper {
    
    protected Hashtable parameters;
    protected IPerson person;
    protected SecurityRoleRefSet securityRoleRefs;
    
    public ServletRequestImpl(HttpServletRequest request) {
        super(request);
        this.parameters = new Hashtable(request.getParameterMap());
    }    

    public ServletRequestImpl(HttpServletRequest request, IPerson person, SecurityRoleRefSet securityRoleRefs) {
        super(request);
        this.parameters = new Hashtable(request.getParameterMap());
        this.person = person;
        this.securityRoleRefs = securityRoleRefs;
    }
    
    public String getParameter(String name) {
        String[] values = (String[])parameters.get(name);

        if (values == null || values.length <= 0)
            return null;
        else
            return values[0];
    }

    public Map getParameterMap() {
        return parameters;
    }

    public Enumeration getParameterNames() {
        return parameters.keys();
    }

    public String[] getParameterValues(String name) {
        return (String[])parameters.get(name);
    }

    /**
     * Replaces the existing request parameters with a new set
     * of parameters.
     * @param parameters the new parameters
     */
    public void setParameters(Map parameters) {
        this.parameters.clear();
        this.parameters.putAll(parameters);
    }

    /**
     * Returns the remote user from the real HttpServletRequest
     * if it is available.  If it is not available, the username
     * of the user will be returned provided that the user is
     * authenticated.  If not authenticated, then <code>null</code>
     * will be returned.
     * @return the name of the remote user or <code>null</code>
     */
    public String getRemoteUser() {
        String userName = super.getRemoteUser();
        if (userName == null && person != null && person.getSecurityContext().isAuthenticated()) {
            userName = (String)person.getAttribute(IPerson.USERNAME);
        }
        return userName;
    }
    
    /**
     * Returns the user principal from the real HttpServletRequest
     * if it is available.  If it is not available, the principal
     * representing the user will be returned provided that the user is
     * authenticated.  If not authenticated, then <code>null</code>
     * will be returned.
     * @return the user principal or <code>null</code>
     */
    public Principal getUserPrincipal() {
        Principal principal = super.getUserPrincipal();
        if (principal == null && person != null) {
            principal = new PrincipalImpl(getRemoteUser());
        }
        return principal;
    }
    
    /**
     * Determines whether or not the user is in the given role.
     * The uPortal <code>GroupService</code> shall be used to
     * represent the given role as a uPortal <code>IGroupMember</code>.
     * Therefore, the role must be in the form of a uPortal group key such as
     * <code>local.0</code> or <code>pags.students</code>.
     * @param role the role of the user
     * @return <code>true</code> is the user is in the given role, otherwise <code>false</code>
     */
    public boolean isUserInRole(String role) {        
        boolean isUserInRole = super.isUserInRole(role);
        try {
            if (!isUserInRole && person != null) {
                IGroupMember user = GroupService.getGroupMember(person.getEntityIdentifier());
                IGroupMember groupForRole = GroupService.getGroupMember(role, IEntityGroup.class);
                if (groupForRole != null) {
                    isUserInRole = user.isDeepMemberOf(groupForRole);
                    if (!isUserInRole) {
                        SecurityRoleRef securityRoleRef = securityRoleRefs.get(role);
                        if (securityRoleRef != null) {
                            String roleLink = securityRoleRef.getRoleLink();
                            IGroupMember groupForRoleLink = GroupService.getGroupMember(roleLink, IEntityGroup.class);
                            if (groupForRoleLink != null) {
                                isUserInRole = user.isDeepMemberOf(groupForRoleLink);
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            isUserInRole = false;
        }
        return isUserInRole;
    }
}