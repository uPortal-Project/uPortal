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
 * @author Eric Dalquist
 * @version $Revision$
 */
public class PersonLookupHelperImpl {
    private IPersonAttributeDao personAttributeDao;
    
    /**
     * @return the personAttributeDao
     */
    public IPersonAttributeDao getPersonAttributeDao() {
        return personAttributeDao;
    }
    /**
     * @param personAttributeDao the personAttributeDao to set
     */
    public void setPersonAttributeDao(IPersonAttributeDao personLookupDao) {
        this.personAttributeDao = personLookupDao;
    }


    public Set<String> getQueryAttributes(ExternalContext externalContext) {
        final PortletRequest portletRequest = (PortletRequest)externalContext.getNativeRequest();
        final PortletPreferences preferences = portletRequest.getPreferences();
        
        final Set<String> queryAttributes;
        
        final String[] configuredAttributes = preferences.getValues("person-lookup.personLookup.queryAttributes", null);
        if (configuredAttributes != null) {
            queryAttributes = new LinkedHashSet<String>(Arrays.asList(configuredAttributes));
        }
        else {
            final Set<String> availableAttributes = this.personAttributeDao.getAvailableQueryAttributes();
            queryAttributes = new TreeSet<String>(availableAttributes);
        }
        
        return queryAttributes;
    }

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
    
    public Map<String, String> getQueryDisplayResults(ExternalContext externalContext, Map<String, IPersonAttributes> queryResults) {
        final PortletRequest portletRequest = (PortletRequest)externalContext.getNativeRequest();
        final PortletPreferences preferences = portletRequest.getPreferences();
        
        final String[] resultsAttributes = preferences.getValues("person-lookup.personSearchResults.resultsAttributes", null);
        final String resultsMessage = preferences.getValue("person-lookup.personSearchResults.resultsMessage", null);
        
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
}
