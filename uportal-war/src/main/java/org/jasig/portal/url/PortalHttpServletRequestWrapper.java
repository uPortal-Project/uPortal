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

package org.jasig.portal.url;

import java.security.Principal;
import java.util.Collections;
import java.util.Enumeration;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.Validate;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jasig.portal.EntityIdentifier;
import org.jasig.portal.groups.IGroupMember;
import org.jasig.portal.i18n.LocaleManager;
import org.jasig.portal.security.IPerson;
import org.jasig.portal.services.GroupService;
import org.jasig.portal.user.IUserInstance;
import org.jasig.portal.user.IUserInstanceManager;
import org.jasig.portal.utils.ArrayEnumerator;
import org.jasig.portal.utils.web.AbstractHttpServletRequestWrapper;

/**
 * Portal wide request wrapper. Provides portal specific information for request parameters,
 * attributes, user and role.
 *
 * @author Peter Kharchenko: pkharchenko at unicon.net
 * @version $Revision: 11911 $
 */
public class PortalHttpServletRequestWrapper extends AbstractHttpServletRequestWrapper {
    /**
     * {@link javax.servlet.http.HttpServletRequest} attribute that this {@link HttpServletRequest} object
     * will be available.
     */
    public static final String ATTRIBUTE__HTTP_SERVLET_REQUEST = PortalHttpServletRequestWrapper.class.getName() + ".PORTAL_HTTP_SERVLET_REQUEST";
    /**
     * {@link javax.servlet.http.HttpServletRequest} attribute that the {@link HttpServletResponse} object
     * will be available.
     */
    public static final String ATTRIBUTE__HTTP_SERVLET_RESPONSE = PortalHttpServletRequestWrapper.class.getName() + ".PORTAL_HTTP_SERVLET_RESPONSE";
    
    protected final Log logger = LogFactory.getLog(this.getClass());
    
    private final Map<String, Object> additionalHeaders = new LinkedHashMap<String, Object>();
    private final HttpServletResponse httpServletResponse;
    private final IUserInstanceManager userInstanceManager;

    /**
     * Construct a writable this.request wrapping a real this.request
     * @param this.request Request to wrap, can not be null.
     */
    public PortalHttpServletRequestWrapper(HttpServletRequest request, HttpServletResponse response, IUserInstanceManager userInstanceManager) {
        super(request);
        Validate.notNull(response);
        Validate.notNull(userInstanceManager);

        this.httpServletResponse = response;
        this.userInstanceManager = userInstanceManager;
    }

    public void setHeader(String name, String value) {
        this.additionalHeaders.put(name, value);
    }
    public void setDateHeader(String name, String value) {
        this.additionalHeaders.put(name, value);
    }
    public void setIntHeader(String name, String value) {
        this.additionalHeaders.put(name, value);
    }

    @Override
    public long getDateHeader(String name) {
        final Object value = this.additionalHeaders.get(name);
        if (value == null) {
            return super.getDateHeader(name);
        }
        if (value instanceof Long) {
            return (Long)value;
        }
        
        throw new IllegalArgumentException("Header '" + name + "' cannot be converted to long: '" + value + "' is of type " + value.getClass().getName());
    }

    @Override
    public String getHeader(String name) {
        final Object value = this.additionalHeaders.get(name);
        if (value == null) {
            return super.getHeader(name);
        }
        
        return String.valueOf(value);
    }

    @Override
    public Enumeration<?> getHeaders(String name) {
        final Object value = this.additionalHeaders.get(name);
        if (value == null) {
            return super.getHeaders(name);
        }
        
        return Collections.enumeration(Collections.singleton(value));
    }

    @Override
    public Enumeration<?> getHeaderNames() {
        final Set<Object> headerNames = new LinkedHashSet<Object>();
        
        for (final Enumeration<?> headerNamesEnum = super.getHeaderNames(); headerNamesEnum.hasMoreElements();) {
            final Object name = headerNamesEnum.nextElement();
            headerNames.add(name);
        }
        
        headerNames.addAll(this.additionalHeaders.keySet());
        
        return Collections.enumeration(headerNames);
    }

    @Override
    public int getIntHeader(String name) {
        final Object value = this.additionalHeaders.get(name);
        if (value == null) {
            return super.getIntHeader(name);
        }
        if (value instanceof Integer) {
            return (Integer)value;
        }
        
        throw new IllegalArgumentException("Header '" + name + "' cannot be converted to int: '" + value + "' is of type " + value.getClass().getName());
    }
    
    @Override
    public Object getAttribute(String name) {
        if (ATTRIBUTE__HTTP_SERVLET_REQUEST.equals(name)) {
            return this;
        }
        if (ATTRIBUTE__HTTP_SERVLET_RESPONSE.equals(name)) {
            return this.httpServletResponse;
        }
        
        return super.getAttribute(name);
    }

    @Override
    public String getRemoteUser() {
        final Principal userPrincipal = this.getUserPrincipal();
        if (userPrincipal == null) {
            return null;
        }

        return userPrincipal.getName();
    }

    @Override
    public Principal getUserPrincipal() {
        if (super.getSession(false) == null) {
            return super.getUserPrincipal();
        }
        
        final IUserInstance userInstance = this.userInstanceManager.getUserInstance(this.getWrappedRequest());
        final IPerson person = userInstance.getPerson();
        if (person == null || person.isGuest()) {
            return null;
        }
        
        return person;
    }

    /**
     * Determines whether or not the user is in the given role. The wrapped request
     * is consulted first then the {@link GroupService} is used to determine if a
     * group exists for the specified role and if the user is a member of it.
     *
     * @see org.jasig.portal.utils.web.AbstractHttpServletRequestWrapper#isUserInRole(java.lang.String)
     */
    @Override
    public boolean isUserInRole(String role) {
        if (super.getSession(false) == null) {
            return super.isUserInRole(role);
        }
        
        //Check the wrapped request first
        final boolean isUserInRole = super.isUserInRole(role);
        if (isUserInRole) {
            return true;
        }
        
        //Find the group for the role, if not found return false
        IGroupMember groupForRole = GroupService.findGroup(role);
        if (groupForRole == null) {
            final EntityIdentifier[] results = GroupService.searchForGroups(role, GroupService.IS, IPerson.class);
            if (results == null || results.length == 0) {
                return false;
            }
            
            if (results.length > 1) {
                this.logger.warn(results.length + " groups were found for role '" + role + "'. The first result will be used.");
            }
            
            groupForRole = GroupService.getGroupMember(results[0]);
        }

        //Load the group information about the current user
        final IUserInstance userInstance = this.userInstanceManager.getUserInstance(this.getWrappedRequest());
        final IPerson person = userInstance.getPerson();
        final EntityIdentifier personEntityId = person.getEntityIdentifier();
        final IGroupMember personGroupMember = GroupService.getGroupMember(personEntityId);
        
        return personGroupMember.isDeepMemberOf(groupForRole);
    }

    /* (non-Javadoc)
     * @see org.jasig.portal.url.AbstractHttpServletRequestWrapper#getLocale()
     */
    @Override
    public Locale getLocale() {
        if (super.getSession(false) == null) {
            return super.getLocale();
        }
        
        final IUserInstance userInstance = this.userInstanceManager.getUserInstance(this.getWrappedRequest());
        final LocaleManager localeManager = userInstance.getLocaleManager();
        final Locale[] locales = localeManager.getLocales();
        return locales[0];
    }

    /* (non-Javadoc)
     * @see org.jasig.portal.url.AbstractHttpServletRequestWrapper#getLocales()
     */
    @SuppressWarnings("unchecked")
    @Override
    public Enumeration<Locale> getLocales() {
        if (super.getSession(false) == null) {
            return super.getLocales();
        }
        
        final IUserInstance userInstance = this.userInstanceManager.getUserInstance(this.getWrappedRequest());
        final LocaleManager localeManager = userInstance.getLocaleManager();
        final Locale[] locales = localeManager.getLocales();
        return new ArrayEnumerator<Locale>(locales);
    }
}