/* Copyright 2004 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
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