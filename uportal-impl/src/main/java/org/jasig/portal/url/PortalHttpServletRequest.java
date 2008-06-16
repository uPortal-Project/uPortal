/* Copyright 2004 The JA-SIG Collaborative.  All rights reserved.
 * See license distributed with this file and
 * available online at http://www.uportal.org/license.html
 */

package org.jasig.portal.url;

import java.security.Principal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.Validate;
import org.jasig.portal.EntityIdentifier;
import org.jasig.portal.groups.IEntityGroup;
import org.jasig.portal.groups.IGroupMember;
import org.jasig.portal.i18n.LocaleManager;
import org.jasig.portal.security.IPerson;
import org.jasig.portal.services.GroupService;
import org.jasig.portal.user.IUserInstance;
import org.jasig.portal.user.IUserInstanceManager;
import org.jasig.portal.utils.ArrayEnumerator;

/**
 * Portal wide request wrapper. Provides portal specific information for request parameters,
 * attributes, user and role.
 *
 * @author Peter Kharchenko: pkharchenko at unicon.net
 * @version $Revision: 11911 $
 */
public class PortalHttpServletRequest extends AbstractHttpServletRequestWrapper implements IWritableHttpServletRequest {
    /**
     * {@link javax.servlet.http.HttpServletRequest} attribute that this {@link HttpServletRequest} object
     * will be available.
     */
    public static final String ATTRIBUTE__HTTP_SERVLET_REQUEST = PortalHttpServletRequest.class.getName() + ".PORTAL_HTTP_SERVLET_REQUEST";
    
    
    private final IUserInstanceManager userInstanceManager;
    private final Map<String, String[]> parameterMap = new HashMap<String, String[]>();

    /**
     * Construct a writable this.request wrapping a real this.request
     * @param this.request Request to wrap, can not be null.
     */
    @SuppressWarnings("unchecked")
    public PortalHttpServletRequest(HttpServletRequest request, IUserInstanceManager userInstanceManager) {
        super(request);
        Validate.notNull(userInstanceManager);

        // place all parameters into the map, saves run-time merging
        this.parameterMap.putAll(request.getParameterMap());
        
        this.userInstanceManager = userInstanceManager;
    }
    
    @Override
    public Object getAttribute(String name) {
        if (ATTRIBUTE__HTTP_SERVLET_REQUEST.equals(name)) {
            return this;
        }
        
        return super.getAttribute(name);
    }

    public boolean addParameterValue(String name, String value) {
        Validate.notNull(name, "name can not be null");
        Validate.notNull(value, "value can not be null");
        
        final String[] currentValues = this.parameterMap.get(name);
        final String[] newValues;
        
        if (currentValues == null) {
            newValues = new String[] { value };
        }
        else {
            newValues = (String[])ArrayUtils.add(currentValues,  value);
        }
        
        this.parameterMap.put(name, newValues);
        return currentValues != null;
    }

    public boolean setParameterValue(String name, String value) {
        Validate.notNull(name, "name can not be null");
        Validate.notNull(value, "value can not be null");
        
        return this.parameterMap.put(name, new String[] { value }) != null;
    }

    public boolean setParameterValues(String name, String[] values) {
        Validate.notNull(name, "name can not be null");
        Validate.notNull(values, "values can not be null");
        Validate.noNullElements(values, "values can not contain null elements");
        
        return this.parameterMap.put(name, values) != null;
    }

    public boolean deleteParameter(String name) {
        Validate.notNull(name, "name can not be null");
        
        return this.parameterMap.remove(name) != null;
    }

    public boolean deleteParameterValue(String name, String value) {
        Validate.notNull(name, "name can not be null");
        Validate.notNull(value, "value can not be null");
        
        final String[] currentValues = this.parameterMap.get(name);
        if (currentValues == null) {
            return false;
        }

        final List<String> newValues = new ArrayList<String>(currentValues.length);
        for (final String currentValue : currentValues) {
            if (value != currentValue && !value.equals(currentValue)) {
                newValues.add(currentValue);
            }
        }
        
        this.parameterMap.put(name, newValues.toArray(new String[newValues.size()]));

        return currentValues.length != newValues.size();
    }

    @Override
    public String getParameter(String name) {
        Validate.notNull(name, "name can not be null");
        
        final String[] pValues = this.parameterMap.get(name);
        
        if (pValues != null && pValues.length > 0) {
            return pValues[0];
        }

        return null;
    }

    @Override
    public Map<String, String[]> getParameterMap() {
        return this.parameterMap;
    }

    @Override
    public Enumeration<String> getParameterNames() {
        return Collections.enumeration(this.parameterMap.keySet());
    }

    @Override
    public String[] getParameterValues(String name) {
        return this.parameterMap.get(name);
    }

    /* (non-Javadoc)
     * @see org.jasig.portal.url.AbstractHttpServletRequestWrapper#getRemoteUser()
     */
    @Override
    public String getRemoteUser() {
        final Principal userPrincipal = this.getUserPrincipal();
        if (userPrincipal == null) {
            return null;
        }

        return userPrincipal.getName();
    }

    /* (non-Javadoc)
     * @see org.jasig.portal.url.AbstractHttpServletRequestWrapper#getUserPrincipal()
     */
    @Override
    public Principal getUserPrincipal() {
        final IUserInstance userInstance = this.userInstanceManager.getUserInstance(this);
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
     * @see org.jasig.portal.url.AbstractHttpServletRequestWrapper#isUserInRole(java.lang.String)
     */
    @Override
    public boolean isUserInRole(String role) {
        //Check the wrapped request first
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
        final IUserInstance userInstance = this.userInstanceManager.getUserInstance(this);
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
        final IUserInstance userInstance = this.userInstanceManager.getUserInstance(this);
        final LocaleManager localeManager = userInstance.getLocaleManager();
        final Locale[] locales = localeManager.getLocales();
        return locales[0];
    }

    /* (non-Javadoc)
     * @see org.jasig.portal.url.AbstractHttpServletRequestWrapper#getLocales()
     */
    @Override
    public Enumeration<Locale> getLocales() {
        final IUserInstance userInstance = this.userInstanceManager.getUserInstance(this);
        final LocaleManager localeManager = userInstance.getLocaleManager();
        final Locale[] locales = localeManager.getLocales();
        return new ArrayEnumerator<Locale>(locales);
    }
}