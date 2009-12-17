/**
 * Copyright (c) 2000-2009, Jasig, Inc.
 * See license distributed with this file and available online at
 * https://www.ja-sig.org/svn/jasig-parent/tags/rel-10/license-header.txt
 */
package org.jasig.portal.channels.portlet;

import java.util.Collections;
import java.util.Enumeration;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;

import org.apache.commons.lang.Validate;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import org.apache.pluto.descriptors.common.SecurityRoleRefDD;
import org.jasig.portal.EntityIdentifier;
import org.jasig.portal.groups.IEntityGroup;
import org.jasig.portal.groups.IGroupMember;
import org.jasig.portal.security.IPerson;
import org.jasig.portal.services.GroupService;

/**
 * Portlet specific request wrapper. Provides a specific set of parameters to the portlet, adds isUserInRole
 * logic that understands portlet role-refs and calls back into {@link GroupService}
 * 
 * @author Eric Dalquist
 * @version $Revision$
 */
public class PortletHttpRequestWrapper extends HttpServletRequestWrapper {
    private final Map<String, String[]> parameters;
    private final IPerson person;
    private final List<SecurityRoleRefDD> securityRoleRefs;
    
    public PortletHttpRequestWrapper(HttpServletRequest request, Map<String, List<String>> parameters, IPerson person, List<SecurityRoleRefDD> securityRoleRefs) {
        super(request);
        Validate.notNull(parameters, "parameters can not be null");
        Validate.notNull(person, "person can not be null");
        Validate.notNull(securityRoleRefs, "securityRoleRefs can not be null");
        
        this.parameters = new LinkedHashMap<String, String[]>();
        for (final Map.Entry<String, List<String>> parameterEntry : parameters.entrySet()) {
            final String name = parameterEntry.getKey();
            final List<String> values = parameterEntry.getValue();
            
            if (values == null) {
                this.parameters.put(name, null);
            }
            else {
                this.parameters.put(name, values.toArray(new String[values.size()]));
            }
        }
        
        this.person = person;
        this.securityRoleRefs = securityRoleRefs;
    }

    /* (non-Javadoc)
     * @see javax.servlet.ServletRequestWrapper#getParameter(java.lang.String)
     */
    @Override
    public String getParameter(String name) {
        final String[] values = this.getParameterValues(name);
        if (values != null && values.length > 0) {
            return values[0];
        }

        return null;
    }

    /* (non-Javadoc)
     * @see javax.servlet.ServletRequestWrapper#getParameterMap()
     */
    @Override
    public Map<String, String[]> getParameterMap() {
        return this.parameters;
    }

    /* (non-Javadoc)
     * @see javax.servlet.ServletRequestWrapper#getParameterNames()
     */
    @Override
    public Enumeration<String> getParameterNames() {
        return Collections.enumeration(this.parameters.keySet());
    }

    /* (non-Javadoc)
     * @see javax.servlet.ServletRequestWrapper#getParameterValues(java.lang.String)
     */
    @Override
    public String[] getParameterValues(String name) {
        return this.parameters.get(name);
    }
    
    /**
     * Determines whether or not the user is in the given role. The wrapped request
     * is consulted first then the {@link GroupService} is used to determine if a
     * group exists for the specified role link and if the user is a member of it.
     * 
     * This assumes the parent request will check the role directly
     *
     * @see javax.servlet.http.HttpServletRequestWrapper#isUserInRole(java.lang.String)
     */
    @Override
    public boolean isUserInRole(String role) {
        //Check the wrapped request first, assume this checks the role directly
        final boolean isUserInRole = super.isUserInRole(role);
        if (isUserInRole) {
            return true;
        }
        
        //Find the group for the role, if not found return false
        final IGroupMember groupForRole = GroupService.getGroupMember(role, IEntityGroup.class);
        if (groupForRole == null) {
            return false;
        }

        //Load the group information about the current user
        final EntityIdentifier personEntityId = this.person.getEntityIdentifier();
        final IGroupMember personGroupMember = GroupService.getGroupMember(personEntityId);

        //Find the role link for the role
        final SecurityRoleRefDD securityRoleRef = getSecurityRoleRef(role);
        if (securityRoleRef == null) {
            return false;
        }
        
        //Find the group specified by the role link
        final String roleLink = securityRoleRef.getRoleLink();
        final IGroupMember groupForRoleLink = GroupService.getGroupMember(roleLink, IEntityGroup.class);
        if (groupForRoleLink == null) {
            return false;
        }
        
        //Preform the group memebership check
        return personGroupMember.isDeepMemberOf(groupForRoleLink);
    }

    /**
     * Gets a SecurityRoleRefDD for the specified role name;
     */
    private SecurityRoleRefDD getSecurityRoleRef(String role) {
        for (final SecurityRoleRefDD securityRoleRef : this.securityRoleRefs) {
            final String roleRefName = securityRoleRef.getRoleName();
            if (role.equals(roleRefName)) {
                return securityRoleRef;
            }
        }

        return null;
    }
    
    /**
     * @see java.lang.Object#equals(Object)
     */
    @Override
    public boolean equals(Object object) {
        if (object == this) {
            return true;
        }
        if (!(object instanceof PortletHttpRequestWrapper)) {
            return false;
        }
        PortletHttpRequestWrapper rhs = (PortletHttpRequestWrapper) object;
        return new EqualsBuilder()
            .append(this.getRequest(), rhs.getRequest())
            .append(this.parameters, rhs.parameters)
            .isEquals();
    }

    /**
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        return new HashCodeBuilder(500546767, -122181035)
            .append(this.getRequest())
            .append(this.parameters)
            .toHashCode();
    }

    /**
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE)
            .append("wrappedRequest", this.getRequest())
            .append("parameters", this.parameters)
            .toString();
    }
}
