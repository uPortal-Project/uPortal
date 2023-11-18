/**
 * Licensed to Apereo under one or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information regarding copyright ownership. Apereo
 * licenses this file to you under the Apache License, Version 2.0 (the "License"); you may not use
 * this file except in compliance with the License. You may obtain a copy of the License at the
 * following location:
 *
 * <p>http://www.apache.org/licenses/LICENSE-2.0
 *
 * <p>Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apereo.portal.url;

import java.security.Principal;
import java.util.Collections;
import java.util.Enumeration;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.lang.Validate;
import org.apereo.portal.EntityIdentifier;
import org.apereo.portal.groups.IEntityGroup;
import org.apereo.portal.groups.IGroupMember;
import org.apereo.portal.i18n.LocaleManager;
import org.apereo.portal.security.IPerson;
import org.apereo.portal.services.GroupService;
import org.apereo.portal.user.IUserInstance;
import org.apereo.portal.user.IUserInstanceManager;
import org.apereo.portal.utils.web.AbstractHttpServletRequestWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Portal wide request wrapper. Provides portal specific information for request parameters,
 * attributes, user and role.
 */
public class PortalHttpServletRequestWrapper extends AbstractHttpServletRequestWrapper {
    /**
     * {@link javax.servlet.http.HttpServletRequest} attribute that this {@link HttpServletRequest}
     * object will be available.
     */
    public static final String ATTRIBUTE__HTTP_SERVLET_REQUEST =
            PortalHttpServletRequestWrapper.class.getName() + ".PORTAL_HTTP_SERVLET_REQUEST";
    /**
     * {@link javax.servlet.http.HttpServletRequest} attribute that the {@link HttpServletResponse}
     * object will be available.
     */
    public static final String ATTRIBUTE__HTTP_SERVLET_RESPONSE =
            PortalHttpServletRequestWrapper.class.getName() + ".PORTAL_HTTP_SERVLET_RESPONSE";

    protected final Logger logger = LoggerFactory.getLogger(getClass());

    private final Map<String, String> additionalHeaders = new LinkedHashMap<>();
    private final HttpServletResponse httpServletResponse;
    private final IUserInstanceManager userInstanceManager;

    /**
     * Construct a writable this.request wrapping a real this.request
     *
     * @param request Request to wrap, can not be null.
     */
    public PortalHttpServletRequestWrapper(
            HttpServletRequest request,
            HttpServletResponse response,
            IUserInstanceManager userInstanceManager) {
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
        final String value = this.additionalHeaders.get(name);
        if (value == null) {
            return super.getDateHeader(name);
        }
        try {
            final long longValue = Long.parseLong(value);
            return longValue;
        } catch (NumberFormatException nfe) {
            throw new IllegalArgumentException(
                    "Header '" + name + "' cannot be converted to long: '" + value + "'", nfe);
        }
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
    public Enumeration<String> getHeaders(String name) {
        final String value = this.additionalHeaders.get(name);
        if (value == null) {
            return super.getHeaders(name);
        }

        return Collections.enumeration(Collections.singleton(value));
    }

    @Override
    public Enumeration<String> getHeaderNames() {
        final Set<String> headerNames = new LinkedHashSet<>();

        for (final Enumeration<String> headerNamesEnum = super.getHeaderNames();
                headerNamesEnum.hasMoreElements(); ) {
            final String name = headerNamesEnum.nextElement();
            headerNames.add(name);
        }

        headerNames.addAll(this.additionalHeaders.keySet());

        return Collections.enumeration(headerNames);
    }

    @Override
    public int getIntHeader(String name) {
        final String value = this.additionalHeaders.get(name);
        if (value == null) {
            return super.getIntHeader(name);
        }
        try {
            final int intValue = Integer.parseInt(value);
            return intValue;
        } catch (NumberFormatException nfe) {
            throw new IllegalArgumentException(
                    "Header '" + name + "' cannot be converted to int: '" + value + "'", nfe);
        }
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

        final IUserInstance userInstance =
                this.userInstanceManager.getUserInstance(this.getWrappedRequest());
        final IPerson person = userInstance.getPerson();
        if (person == null || person.isGuest()) {
            return null;
        }

        return person;
    }

    /**
     * Determines whether or not the user is in the given role. The wrapped request is consulted
     * first then the {@link GroupService} is used to determine if a group exists for the specified
     * role and if the user is a member of it.
     *
     * <p>Role is case sensitive.
     *
     * @see
     *     org.apereo.portal.utils.web.AbstractHttpServletRequestWrapper#isUserInRole(java.lang.String)
     */
    @Override
    public boolean isUserInRole(String role) {
        if (super.getSession(false) == null) {
            return super.isUserInRole(role);
        }

        // Check the wrapped request first
        final boolean isUserInRole = super.isUserInRole(role);
        if (isUserInRole) {
            return true;
        }

        // Find the group for the role, if not found return false
        IEntityGroup groupForRole = GroupService.findGroup(role);
        if (groupForRole == null) {
            final EntityIdentifier[] results =
                    GroupService.searchForGroups(
                            role, GroupService.SearchMethod.DISCRETE, IPerson.class);
            if (results == null || results.length == 0) {
                return false;
            }

            if (results.length > 1) {
                this.logger.warn(
                        results.length
                                + " groups were found for role '"
                                + role
                                + "'. The first result will be used.");
            }

            IGroupMember member = GroupService.getGroupMember(results[0]);
            if (member == null || !member.isGroup()) {
                return false;
            }
            groupForRole = member.asGroup();
        }

        // Load the group information about the current user
        final IUserInstance userInstance =
                this.userInstanceManager.getUserInstance(this.getWrappedRequest());
        final IPerson person = userInstance.getPerson();
        final EntityIdentifier personEntityId = person.getEntityIdentifier();
        final IGroupMember personGroupMember = GroupService.getGroupMember(personEntityId);

        final boolean result = personGroupMember.isDeepMemberOf(groupForRole);
        logger.trace(
                "Answering {} for isUserInRole where user='{}', role='{}', and groupForRole='{}'",
                result,
                person.getUserName(),
                role,
                groupForRole.getName());
        return result;
    }

    /* (non-Javadoc)
     * @see org.apereo.portal.url.AbstractHttpServletRequestWrapper#getLocale()
     */
    @Override
    public Locale getLocale() {
        if (super.getSession(false) == null) {
            return super.getLocale();
        }

        final IUserInstance userInstance =
                this.userInstanceManager.getUserInstance(this.getWrappedRequest());
        final LocaleManager localeManager = userInstance.getLocaleManager();
        final List<Locale> locales = localeManager.getLocales();
        return locales.get(0);
    }

    /* (non-Javadoc)
     * @see org.apereo.portal.url.AbstractHttpServletRequestWrapper#getLocales()
     */
    @Override
    public Enumeration<Locale> getLocales() {
        if (super.getSession(false) == null) {
            return super.getLocales();
        }

        final IUserInstance userInstance =
                this.userInstanceManager.getUserInstance(this.getWrappedRequest());
        final LocaleManager localeManager = userInstance.getLocaleManager();
        final List<Locale> locales = localeManager.getLocales();
        return Collections.enumeration(locales);
    }
}
