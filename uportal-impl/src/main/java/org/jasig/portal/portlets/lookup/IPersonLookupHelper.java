/**
 * Copyright (c) 2000-2009, Jasig, Inc.
 * See license distributed with this file and available online at
 * https://www.ja-sig.org/svn/jasig-parent/tags/rel-10/license-header.txt
 */
package org.jasig.portal.portlets.lookup;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

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
    
    /**
     * Gets the Set of attributes to allow the user to query with.
     * 
     * @param externalContext The {@link ExternalContext} to get the flows's configuration from
     * @return Set of attributes that can be used in a query.
     */
    public Set<String> getQueryAttributes(ExternalContext externalContext);

    /**
     * Execute a query for users using the attributes in the passed {@link PersonQuery} object.
     * 
     * @param query Query to run for users.
     * @return Map of users with their name attribute as the key.
     */
    public Map<String, IPersonAttributes> doPersonQuery(ExternalContext externalContext, PersonQuery query);

    /**
     * Performs formatting of strings to display on the query results view.
     * 
     * @param externalContext The {@link ExternalContext} to get the flows's configuration from
     * @param queryResults Results of the query
     * @return Formatted strings as values and user names as keys.
     */
    public Map<IPersonAttributes, String> getQueryDisplayResults(ExternalContext externalContext, Collection<IPersonAttributes> queryResults);
    
    /**
     * Gets the Set of attributes to display in the user details view
     * 
     * @param externalContext The {@link ExternalContext} to get the flows's configuration from
     * @param person The person who's attributes will be displayed
     * @return Set of attributes to display
     */
    public Set<String> getDisplayAttributes(ExternalContext externalContext, IPersonAttributes person);

}