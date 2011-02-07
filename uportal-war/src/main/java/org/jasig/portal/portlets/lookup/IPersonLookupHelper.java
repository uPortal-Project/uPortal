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

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.jasig.portal.security.IPerson;
import org.jasig.services.persondir.IPersonAttributes;
import org.springframework.webflow.context.ExternalContext;

/**
 * @author Eric Dalquist
 * @version $Revision$
 */
public interface IPersonLookupHelper {
    /**
     * Portlet preference name to use to specify a List of attributes displayed in query UI
     */
    public static final String PERSON_LOOKUP_PERSON_LOOKUP_QUERY_ATTRIBUTES = "person-lookup.personLookup.queryAttributes";
    /**
     * Portlet preference name to use to specify a List of attributes displayed in query UI
     */
    public static final String PERSON_LOOKUP_PERSON_LOOKUP_QUERY_ATTRIBUTES_EXCLUDES = "person-lookup.personLookup.queryAttributes.exclude";
    /**
     * Portlet preference name to use to specify a MessageFormat string for search results list
     */
    public static final String PERSON_LOOKUP_PERSON_SEARCH_RESULTS_RESULTS_MESSAGE = "person-lookup.personSearchResults.resultsMessage";
    /**
     * Portlet preference name to use to specify a list of attributes who's values should be passed to the MessageFormat for the resultsMessage string
     */
    public static final String PERSON_LOOKUP_PERSON_SEARCH_RESULTS_RESULTS_ATTRIBUTES = "person-lookup.personSearchResults.resultsAttributes";
    /**
     * Portlet preference name to use to specify a attributes to display in the user details view
     */
    public static final String PERSON_LOOKUP_PERSON_DETAILS_DETAILS_ATTRIBUTES = "person-lookup.personDetails.detailsAttributes";
    /**
     * Portlet preference name to use to specify a attributes to exclude in the user details view
     */
    public static final String PERSON_LOOKUP_PERSON_DETAILS_DETAILS_ATTRIBUTES_EXCLUDES = "person-lookup.personDetails.detailsAttributes.exclude";

    public static final String USERS_OWNER = "UP_USERS";
    
    public static final String VIEW_USER_PERMISSION = "VIEW_USER";

    public static final String VIEW_ATTRIBUTE_PERMISSION = "VIEW_USER_ATTRIBUTE";
    

    /**
     * Gets the Set of attributes to allow the user to query with.
     * 
     * @param externalContext The {@link ExternalContext} to get the flows's configuration from
     * @return Set of attributes that can be used in a query.
     */
    public Set<String> getQueryAttributes(ExternalContext externalContext);
    
    /**
     * Gets the Set of attributes to display in the user details view
     * 
     * @param externalContext The {@link ExternalContext} to get the flows's configuration from
     * @param person The person who's attributes will be displayed
     * @return Set of attributes to display
     */
    public Set<String> getDisplayAttributes(ExternalContext externalContext);

    /**
     * Obtain the person ({@link IPersonAttributes}) representing oneself.
     * 
     * @param externalContext The {@link ExternalContext} to get the flows's configuration from
     * @return The {@link IPersonAttributes} for the current user
     */
    public IPersonAttributes getSelf(ExternalContext externalContext);

    /**
     * Search for people matching the specified query, limited to the permissions
     * of the searching user.  Both the returned person list and the attributes
     * of the returned people will be filtered according to the searching user's
     * permissions.
     * 
     * @param searcher
     * @param query
     * @return
     */
    public List<IPersonAttributes> searchForPeople(final IPerson searcher, final Map<String, Object> query);

    /**
     * Find an individual person matching the provided username.  The returned
     * result (if any), will be limited by the permissions of the searching user.
     * 
     * @param searcher
     * @param username
     * @return
     */
    public IPersonAttributes findPerson(final IPerson searcher, final String username);

}