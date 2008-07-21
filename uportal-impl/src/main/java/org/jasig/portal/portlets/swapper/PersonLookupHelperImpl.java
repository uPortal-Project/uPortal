/**
 * Copyright 2007 The JA-SIG Collaborative.  All rights reserved.
 * See license distributed with this file and
 * available online at http://www.uportal.org/license.html
 */
package org.jasig.portal.portlets.swapper;

import java.text.MessageFormat;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import javax.portlet.PortletPreferences;
import javax.portlet.PortletRequest;

import org.jasig.services.persondir.IPersonAttributeDao;
import org.jasig.services.persondir.IPersonAttributes;
import org.springframework.webflow.context.ExternalContext;

/**
 * Implements logic and helper methods for the person-lookup web flow.
 * 
 * @author Eric Dalquist
 * @version $Revision$
 */
public class PersonLookupHelperImpl implements IPersonLookupHelper {
    public static final String PERSON_LOOKUP_PERSON_LOOKUP_QUERY_ATTRIBUTES = "person-lookup.personLookup.queryAttributes";
    public static final String PERSON_LOOKUP_PERSON_SEARCH_RESULTS_RESULTS_MESSAGE = "person-lookup.personSearchResults.resultsMessage";
    public static final String PERSON_LOOKUP_PERSON_SEARCH_RESULTS_RESULTS_ATTRIBUTES = "person-lookup.personSearchResults.resultsAttributes";
    public static final String PERSON_LOOKUP_PERSON_DETAILS_DETAILS_ATTRIBUTES = "person-lookup.personDetails.detailsAttributes";
    
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

        //If attributes are configured in portlet prefs just use them 
        if (configuredAttributes != null) {
            queryAttributes = new LinkedHashSet<String>(Arrays.asList(configuredAttributes));
        }
        //Otherwise provide all available attributes from the IPersonAttributeDao
        else {
            final Set<String> availableAttributes = this.personAttributeDao.getAvailableQueryAttributes();
            queryAttributes = new TreeSet<String>(availableAttributes);
        }
        
        return queryAttributes;
    }

    /* (non-Javadoc)
     * @see org.jasig.portal.portlets.swapper.IPersonLookupHelper#doPersonQuery(org.jasig.portal.portlets.swapper.PersonQuery)
     */
    public Map<String, IPersonAttributes> doPersonQuery(PersonQuery query) {
        final Map<String, Attribute> attributes = query.getAttributes();
        
        final Map<String, Object> queryAttributes = new HashMap<String, Object>();
        
        for (final Map.Entry<String, Attribute> attrEntry : attributes.entrySet()) {
            queryAttributes.put(attrEntry.getKey(), attrEntry.getValue().getValue());
        }
        
        final Set<IPersonAttributes> people = this.personAttributeDao.getPeople(queryAttributes);
        if (people == null) {
            return null;
        }
        
        final Map<String, IPersonAttributes> sortedPeople = new TreeMap<String, IPersonAttributes>();
        for (final IPersonAttributes personAttributes : people) {
            sortedPeople.put(personAttributes.getName(), personAttributes);
        }
        return sortedPeople;
    }
    
    /* (non-Javadoc)
     * @see org.jasig.portal.portlets.swapper.IPersonLookupHelper#getQueryDisplayResults(org.springframework.webflow.context.ExternalContext, java.util.Map)
     */
    public Map<String, String> getQueryDisplayResults(ExternalContext externalContext, Map<String, IPersonAttributes> queryResults) {
        final PortletRequest portletRequest = (PortletRequest)externalContext.getNativeRequest();
        final PortletPreferences preferences = portletRequest.getPreferences();
        
        final String[] resultsAttributes = preferences.getValues(PERSON_LOOKUP_PERSON_SEARCH_RESULTS_RESULTS_ATTRIBUTES, null);
        final String resultsMessage = preferences.getValue(PERSON_LOOKUP_PERSON_SEARCH_RESULTS_RESULTS_MESSAGE, null);
        
        final Map<String, String> displayResults = new LinkedHashMap<String, String>();

        //No result string attributes or message string, just use the person's name
        if (resultsAttributes == null || resultsMessage == null) {
            for (final IPersonAttributes personAttributes : queryResults.values()) {
                final String name = personAttributes.getName();
                displayResults.put(name, name);
            }
        }
        //There is configured message info, generate formated strings for each person 
        else {
            for (final IPersonAttributes personAttributes : queryResults.values()) {
                final Object[] resultValues = new Object[resultsAttributes.length];
                for (int index = 0; index < resultsAttributes.length; index++) {
                    final Object attributeValue = personAttributes.getAttributeValue(resultsAttributes[index]);
                    resultValues[index] = attributeValue;
                }
                
                final String name = personAttributes.getName();
                final String displayResult = MessageFormat.format(resultsMessage, resultValues);
                
                displayResults.put(name, displayResult);
            }
        }
        
        return displayResults;
    }


    /* (non-Javadoc)
     * @see org.jasig.portal.portlets.swapper.IPersonLookupHelper#getDisplayAttributes(org.springframework.webflow.context.ExternalContext)
     */
    public Set<String> getDisplayAttributes(ExternalContext externalContext, IPersonAttributes person) {
        final PortletRequest portletRequest = (PortletRequest)externalContext.getNativeRequest();
        final PortletPreferences preferences = portletRequest.getPreferences();
        
        final Set<String> displayAttributes;
        final String[] configuredAttributes = preferences.getValues(PERSON_LOOKUP_PERSON_DETAILS_DETAILS_ATTRIBUTES, null);
        final Map<String, List<Object>> attributes = person.getAttributes();
        
        //If attributes are configured in portlet prefs use those the user has 
        if (configuredAttributes != null) {
            displayAttributes = new LinkedHashSet<String>();

            for (final String configuredAttribute : configuredAttributes) {
                if (attributes.containsKey(configuredAttribute)) {
                    displayAttributes.add(configuredAttribute);
                }
            }
        }
        //Otherwise provide all available attributes from the IPersonAttributes
        else {
            displayAttributes = new TreeSet<String>(attributes.keySet());
        }
        
        return displayAttributes;
    }
}
