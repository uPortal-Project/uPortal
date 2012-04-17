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

package org.jasig.portal.portlets.lookup;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import javax.portlet.PortletPreferences;
import javax.portlet.PortletRequest;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jasig.portal.EntityIdentifier;
import org.jasig.portal.portlets.search.DisplayNameComparator;
import org.jasig.portal.security.IAuthorizationPrincipal;
import org.jasig.portal.security.IPerson;
import org.jasig.portal.services.AuthorizationService;
import org.jasig.services.persondir.IPersonAttributeDao;
import org.jasig.services.persondir.IPersonAttributes;
import org.jasig.services.persondir.support.NamedPersonImpl;
import org.springframework.webflow.context.ExternalContext;

/**
 * Implements logic and helper methods for the person-lookup web flow.
 * 
 * @author Eric Dalquist
 * @version $Revision$
 */
public class PersonLookupHelperImpl implements IPersonLookupHelper {
    protected final Log logger = LogFactory.getLog(this.getClass());

    private IPersonAttributeDao personAttributeDao;
    
    public IPersonAttributeDao getPersonAttributeDao() {
        return personAttributeDao;
    }
    
    /**
     * The {@link IPersonAttributeDao} used to perform lookups.
     */
    public void setPersonAttributeDao(IPersonAttributeDao personLookupDao) {
        this.personAttributeDao = personLookupDao;
    }


    /* (non-Javadoc)
     * @see org.jasig.portal.portlets.swapper.IPersonLookupHelper#getQueryAttributes(org.springframework.webflow.context.ExternalContext)
     */
    public Set<String> getQueryAttributes(ExternalContext externalContext) {
        final PortletRequest portletRequest = (PortletRequest)externalContext.getNativeRequest();
        final PortletPreferences preferences = portletRequest.getPreferences();
        
        final Set<String> queryAttributes;
        final String[] configuredAttributes = preferences.getValues(PERSON_LOOKUP_PERSON_LOOKUP_QUERY_ATTRIBUTES, null);
        final String[] excludedAttributes = preferences.getValues(PERSON_LOOKUP_PERSON_LOOKUP_QUERY_ATTRIBUTES_EXCLUDES, null);

        //If attributes are configured in portlet prefs just use them 
        if (configuredAttributes != null) {
            queryAttributes = new LinkedHashSet<String>(Arrays.asList(configuredAttributes));
        }
        //Otherwise provide all available attributes from the IPersonAttributeDao
        else {
            final Set<String> availableAttributes = this.personAttributeDao.getAvailableQueryAttributes();
            queryAttributes = new TreeSet<String>(availableAttributes);
        }
        
        //Remove excluded attributes
        if (excludedAttributes != null) {
            for (final String excludedAttribute : excludedAttributes) {
                queryAttributes.remove(excludedAttribute);
            }
        }
        
        return queryAttributes;
    }
    
    /* (non-Javadoc)
     * @see org.jasig.portal.portlets.swapper.IPersonLookupHelper#getDisplayAttributes(org.springframework.webflow.context.ExternalContext)
     */
    public Set<String> getDisplayAttributes(ExternalContext externalContext) {
        final PortletRequest portletRequest = (PortletRequest)externalContext.getNativeRequest();
        final PortletPreferences preferences = portletRequest.getPreferences();
        
        final Set<String> displayAttributes;
        final String[] configuredAttributes = preferences.getValues(PERSON_LOOKUP_PERSON_DETAILS_DETAILS_ATTRIBUTES, null);
        final String[] excludedAttributes = preferences.getValues(PERSON_LOOKUP_PERSON_DETAILS_DETAILS_ATTRIBUTES_EXCLUDES, null);
        
        //If attributes are configured in portlet prefs use those the user has 
        if (configuredAttributes != null) {
            displayAttributes = new LinkedHashSet<String>();
            displayAttributes.addAll(Arrays.asList(configuredAttributes));
        }
        //Otherwise provide all available attributes from the IPersonAttributes
        else {
            displayAttributes = new TreeSet<String>(personAttributeDao.getPossibleUserAttributeNames());
        }
        
        //Remove any excluded attributes
        if (excludedAttributes != null) {
            for (final String excludedAttribute : excludedAttributes) {
                displayAttributes.remove(excludedAttribute);
            }
        }
        
        return displayAttributes;
    }
    
    /* (non-Javadoc)
     * @see org.jasig.portal.portlets.lookup.IPersonLookupHelper#getSelf(org.springframework.webflow.context.ExternalContext)
     */
    public IPersonAttributes getSelf(ExternalContext externalContext) {
        
        final PortletRequest portletRequest = (PortletRequest)externalContext.getNativeRequest();
        final String username = portletRequest.getRemoteUser();
        
        return this.personAttributeDao.getPerson(username);
        
    }

    /* (non-Javadoc)
     * @see org.jasig.portal.portlets.lookup.IPersonLookupHelper#searchForPeople(org.jasig.portal.security.IPerson, java.util.Map)
     */
    public List<IPersonAttributes> searchForPeople(final IPerson searcher, final Map<String, Object> query) {

        // get the IAuthorizationPrincipal for the searching user
        final IAuthorizationPrincipal principal = getPrincipalForUser(searcher);

        // build a set of all possible user attributes the current user has 
        // permission to view
        final Set<String> permittedAttributes = getAvailableAttributes(principal);
        
        // remove any query attributes that the user does not have permission
        // to view
        final Map<String, Object> inUseQuery = new HashMap<String, Object>();
        for (Map.Entry<String, Object> queryEntry : query.entrySet()) {
            final String attr = queryEntry.getKey();
            if (permittedAttributes.contains(attr)) {
                inUseQuery.put(attr, queryEntry.getValue());
            } else {
                this.logger.warn("User '" + searcher.getName() + "' attempted searching on attribute '" + attr + "' which is not allowed in the current configuration. The attribute will be ignored.");
            }
        }
        
        // ensure the query has at least one search attribute defined
        if (inUseQuery.keySet().size() == 0) {
            throw new IllegalArgumentException("Search query is empty");
        }
        
        // get the set of people matching the search query
        final Set<IPersonAttributes> people = this.personAttributeDao.getPeople(inUseQuery);
        if (people == null) {
            return Collections.emptyList();
        }

        // for each returned match, check to see if the current user has 
        // permissions to view this user
        List<IPersonAttributes> list = new ArrayList<IPersonAttributes>();
        for (IPersonAttributes person : people) {
            // if the current user has permission to view this person, construct
            // a new representation of the person limited to attributes the
            // searcher has permissions to view
            final IPersonAttributes visiblePerson = getVisiblePerson(principal, person, permittedAttributes);
            if (visiblePerson != null) {
                list.add(visiblePerson);
            }
        }
        
        // sort the list by display name
        Collections.sort(list, new DisplayNameComparator());
        
        // limit the list to a maximum of 10 returned results
        // TODO: make this limit configurable
        if (list.size() > 10) {
            list = list.subList(0, 9);
        }
        
        return list;
    }

    /* (non-Javadoc)
     * @see org.jasig.portal.portlets.lookup.IPersonLookupHelper#findPerson(org.jasig.portal.security.IPerson, java.lang.String)
     */
    public IPersonAttributes findPerson(final IPerson searcher, final String username) {

        // get the IAuthorizationPrincipal for the searching user
        final IAuthorizationPrincipal principal = getPrincipalForUser(searcher);

        // build a set of all possible user attributes the current user has 
        // permission to view
        final Set<String> permittedAttributes = getAvailableAttributes(principal);
        
        // get the set of people matching the search query
        final IPersonAttributes person = this.personAttributeDao.getPerson(username);
        
        if (person == null) {
            logger.info("No user found with username matching " + username);
            return null;
        }
        
        // if the current user has permission to view this person, construct
        // a new representation of the person limited to attributes the
        // searcher has permissions to view
        return getVisiblePerson(principal, person, permittedAttributes);
        
    }

    /**
     * Get the authoriztaion principal matching the supplied IPerson.
     * 
     * @param person
     * @return
     */
    protected IAuthorizationPrincipal getPrincipalForUser(final IPerson person) {
        final EntityIdentifier ei = person.getEntityIdentifier();
        return AuthorizationService.instance().newPrincipal(ei.getKey(), ei.getType());
    }

    /**
     * Get the set of all user attribute names defined in the portal for which
     * the specified principal has the attribute viewing permission.
     * 
     * @param principal
     * @return
     */
    protected Set<String> getAvailableAttributes(final IAuthorizationPrincipal principal) {
        final Set<String> attributeNames = this.personAttributeDao.getPossibleUserAttributeNames();
        return getAvailableAttributes(principal, attributeNames);
    }

    /**
     * Filter the provided set of user attribute names to contain only those
     * the specified principal has permissions to view.
     * 
     * @param principal
     * @param attributeNames
     * @return
     */
    protected Set<String> getAvailableAttributes(final IAuthorizationPrincipal principal, final Set<String> attributeNames) {
        final Set<String> permittedAttributes = new HashSet<String>();
        for (String attr : attributeNames) {
            if (principal.hasPermission(USERS_OWNER, VIEW_ATTRIBUTE_PERMISSION, attr)) {
                permittedAttributes.add(attr);
            }
        }
        return permittedAttributes;
    }

    /**
     * Filter an IPersonAttributes for a specified viewing principal.  The returned 
     * person will contain only the attributes provided in the permitted attributes
     * list.  <code>null</code> if the principal does not have permission to
     * view the user.
     * 
     * @param principal
     * @param person
     * @param permittedAttributes
     * @return
     */
    protected IPersonAttributes getVisiblePerson(final IAuthorizationPrincipal principal,
            final IPersonAttributes person, final Set<String> permittedAttributes) {

        // first check to see if the principal has permission to view this user
        if (person.getName() != null && principal.hasPermission(USERS_OWNER, VIEW_USER_PERMISSION, person.getName())) {
            
            // if the user has permission, filter the person attributes according
            // to the specified permitted attributes
            final Map<String,List<Object>> visibleAttributes = new HashMap<String,List<Object>>();
            for (String attr : person.getAttributes().keySet()) {
                if (permittedAttributes.contains(attr)) {
                    visibleAttributes.put(attr, person.getAttributeValues(attr));
                }
            }
            
            // use the filtered attribute list to create and return a new
            // person object
            final IPersonAttributes visiblePerson = new NamedPersonImpl(person.getName(), visibleAttributes);
            return visiblePerson;
            
        } else {
            logger.debug("Principal " + principal.getKey() + " does not have permissions to view user " + person.getName());
            return null;
        }

    }
    
}
