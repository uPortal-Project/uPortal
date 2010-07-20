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

package org.jasig.portal.channels.portlet;

import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.Validate;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import org.apache.pluto.container.om.portlet.SecurityRoleRef;
import org.jasig.portal.EntityIdentifier;
import org.jasig.portal.groups.IEntityGroup;
import org.jasig.portal.groups.IGroupMember;
import org.jasig.portal.security.IPerson;
import org.jasig.portal.services.GroupService;
import org.jasig.portal.url.AbstractHttpServletRequestWrapper;

/**
 * Scopes set request attributes to just this request. Attribute retrieval methods fall through
 * to the parent request on a miss. Only the scoped attribute names are enumerated by {@link #getAttributeNames()}
 * 
 * @author Eric Dalquist
 * @version $Revision$
 */
public class PortletHttpServletRequestWrapper extends AbstractHttpServletRequestWrapper {
    /**
     * {@link javax.servlet.http.HttpServletRequest} attribute that this {@link HttpServletRequest} object
     * will be available.
     */
    public static final String ATTRIBUTE__HTTP_SERVLET_REQUEST = PortletHttpServletRequestWrapper.class.getName() + ".PORTLET_HTTP_SERVLET_REQUEST";
    
    private final Map<String, Object> attributes = new LinkedHashMap<String, Object>();
    private final Map<String, String[]> parameters;
    private final IPerson person;
    private final List<? extends SecurityRoleRef> securityRoleRefs;
    
    public PortletHttpServletRequestWrapper(HttpServletRequest httpServletRequest, Map<String, String[]> parameters, IPerson person, List<? extends SecurityRoleRef> securityRoleRefs) {
        super(httpServletRequest);
        Validate.notNull(parameters, "parameters can not be null");
        Validate.notNull(person, "person can not be null");
        Validate.notNull(securityRoleRefs, "securityRoleRefs can not be null");
        
        this.parameters = new LinkedHashMap<String, String[]>();
        for (final Map.Entry<String, String[]> parameterEntry : parameters.entrySet()) {
            final String name = parameterEntry.getKey();
            final String[] values = parameterEntry.getValue();
            
            if (values == null) {
                this.parameters.put(name, null);
            }
            else {
                this.parameters.put(name, Arrays.copyOf(values, values.length));
            }
        }
        
        this.person = person;
        this.securityRoleRefs = securityRoleRefs;
    }

    @Override
    public Object getAttribute(String name) {
        if (ATTRIBUTE__HTTP_SERVLET_REQUEST.equals(name)) {
            return this;
        }
        
        final Object attribute = this.attributes.get(name);
        if (attribute != null) {
            return attribute;
        }
        
        if (name.startsWith(PORTAL_ATTRIBUTE_PREFIX)) {
            return super.getAttribute(name);
        }
        
        return null;
    }

    @Override
    public Enumeration<String> getAttributeNames() {
        final Set<String> attributeNames = this.attributes.keySet();
        return Collections.enumeration(attributeNames);
    }

    @Override
    public void removeAttribute(String name) {
        this.attributes.remove(name);
    }

    @Override
    public void setAttribute(String name, Object o) {
        this.attributes.put(name, o);
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
        final SecurityRoleRef securityRoleRef = getSecurityRoleRef(role);
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
    private SecurityRoleRef getSecurityRoleRef(String role) {
        for (final SecurityRoleRef securityRoleRef : this.securityRoleRefs) {
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
        if (!(object instanceof PortletHttpServletRequestWrapper)) {
            return false;
        }
        PortletHttpServletRequestWrapper rhs = (PortletHttpServletRequestWrapper) object;
        return new EqualsBuilder()
            .append(this.getWrappedRequest(), rhs.getWrappedRequest())
            .append(this.parameters, rhs.parameters)
            .isEquals();
    }

    /**
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        return new HashCodeBuilder(500546767, -122181035)
            .append(this.getWrappedRequest())
            .append(this.parameters)
            .toHashCode();
    }

    /**
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE)
            .append("wrappedRequest", this.getWrappedRequest())
            .append("parameters", this.parameters)
            .toString();
    }
}
