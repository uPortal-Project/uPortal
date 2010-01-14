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

import java.text.MessageFormat;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import javax.portlet.PortletPreferences;
import javax.portlet.PortletRequest;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jasig.portal.portlets.Attribute;
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
     * @see org.jasig.portal.portlets.lookup.IPersonLookupHelper#doPersonQuery(org.springframework.webflow.context.ExternalContext, org.jasig.portal.portlets.lookup.PersonQuery)
     */
    public Map<String, IPersonAttributes> doPersonQuery(ExternalContext externalContext, PersonQuery query) {
        final Map<String, Attribute> attributes = query.getAttributes();
        
        final Map<String, Object> queryAttributes = new HashMap<String, Object>();
        
        for (final Map.Entry<String, Attribute> attrEntry : attributes.entrySet()) {
            queryAttributes.put(attrEntry.getKey(), attrEntry.getValue().getValue());
        }
        
        final PortletRequest portletRequest = (PortletRequest)externalContext.getNativeRequest();
        final PortletPreferences preferences = portletRequest.getPreferences();
        final String[] configuredAttributes = preferences.getValues(PERSON_LOOKUP_PERSON_DETAILS_DETAILS_ATTRIBUTES, null);
        final String[] excludedAttributes = preferences.getValues(PERSON_LOOKUP_PERSON_DETAILS_DETAILS_ATTRIBUTES_EXCLUDES, null);

        //Calculate the Set of attributes that are OK to be searched with
        final Set<String> allowedAttributes = new LinkedHashSet<String>();
        if (configuredAttributes != null) {
            allowedAttributes.addAll(Arrays.asList(configuredAttributes));
        }
        else {
            allowedAttributes.addAll(attributes.keySet());
        }
        if (excludedAttributes != null) {
            allowedAttributes.removeAll(Arrays.asList(excludedAttributes));
        }
        
        //Filter the attributes map
        for (final Iterator<String> attributeItr = queryAttributes.keySet().iterator(); attributeItr.hasNext(); ) {
            final String attribute = attributeItr.next();
            if (!allowedAttributes.contains(attribute)) {
                attributeItr.remove();
                
                this.logger.warn("User '" + externalContext.getCurrentUser() + "' attempted searching on attribute '" + attribute + "' which is not allowed in the current configuration. The attribute will be ignored.");
            }
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
     * @see org.jasig.portal.portlets.lookup.IPersonLookupHelper#getQueryDisplayResults(org.springframework.webflow.context.ExternalContext, java.util.Collection)
     */
    public Map<IPersonAttributes, String> getQueryDisplayResults(ExternalContext externalContext, Collection<IPersonAttributes> queryResults) {
        final PortletRequest portletRequest = (PortletRequest)externalContext.getNativeRequest();
        final PortletPreferences preferences = portletRequest.getPreferences();
        
        final String[] resultsAttributes = preferences.getValues(PERSON_LOOKUP_PERSON_SEARCH_RESULTS_RESULTS_ATTRIBUTES, null);
        final String resultsMessage = preferences.getValue(PERSON_LOOKUP_PERSON_SEARCH_RESULTS_RESULTS_MESSAGE, null);
        
        final Map<IPersonAttributes, String> displayResults = new LinkedHashMap<IPersonAttributes, String>();

        //No result string attributes or message string, just use the person's name
        if (resultsAttributes == null || resultsMessage == null) {
            for (final IPersonAttributes personAttributes : queryResults) {
                final String name = personAttributes.getName();
                displayResults.put(personAttributes, name);
            }
        }
        //There is configured message info, generate formated strings for each person 
        else {
            for (final IPersonAttributes personAttributes : queryResults) {
                final Object[] resultValues = new Object[resultsAttributes.length];
                for (int index = 0; index < resultsAttributes.length; index++) {
                    final Object attributeValue = personAttributes.getAttributeValue(resultsAttributes[index]);
                    if (attributeValue != null) {
                        resultValues[index] = attributeValue;
                    }
                    else {
                        resultValues[index] = "";
                    }
                }
                
                final String displayResult = MessageFormat.format(resultsMessage, resultValues);
                
                displayResults.put(personAttributes, displayResult);
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
        final String[] excludedAttributes = preferences.getValues(PERSON_LOOKUP_PERSON_DETAILS_DETAILS_ATTRIBUTES_EXCLUDES, null);
        
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
        
        //Remove any excluded attributes
        if (excludedAttributes != null) {
            for (final String excludedAttribute : excludedAttributes) {
                displayAttributes.remove(excludedAttribute);
            }
        }
        
        return displayAttributes;
    }
}
