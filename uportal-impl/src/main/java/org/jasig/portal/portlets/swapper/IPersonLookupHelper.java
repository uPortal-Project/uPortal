/**
 * Copyright 2007 The JA-SIG Collaborative.  All rights reserved.
 * See license distributed with this file and
 * available online at http://www.uportal.org/license.html
 */
package org.jasig.portal.portlets.swapper;

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
    public Map<String, IPersonAttributes> doPersonQuery(PersonQuery query);

    /**
     * Performs formatting of strings to display on the query results view.
     * 
     * @param externalContext The {@link ExternalContext} to get the flows's configuration from
     * @param queryResults Results of the query
     * @return Formatted strings as values and user names as keys.
     */
    public Map<String, String> getQueryDisplayResults(ExternalContext externalContext, Map<String, IPersonAttributes> queryResults);
    
    /**
     * Gets the Set of attributes to display in the user details view
     * 
     * @param externalContext The {@link ExternalContext} to get the flows's configuration from
     * @param person The person who's attributes will be displayed
     * @return Set of attributes to display
     */
    public Set<String> getDisplayAttributes(ExternalContext externalContext, IPersonAttributes person);

}