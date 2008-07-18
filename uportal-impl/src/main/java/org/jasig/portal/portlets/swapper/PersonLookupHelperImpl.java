/**
 * Copyright 2007 The JA-SIG Collaborative.  All rights reserved.
 * See license distributed with this file and
 * available online at http://www.uportal.org/license.html
 */
package org.jasig.portal.portlets.swapper;

import java.io.Serializable;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import javax.portlet.PortletPreferences;
import javax.portlet.PortletRequest;

import org.apache.commons.lang.builder.CompareToBuilder;
import org.jasig.services.persondir.IPersonAttributeDao;
import org.jasig.services.persondir.IPersonAttributes;
import org.springframework.webflow.context.ExternalContext;

/**
 * @author Eric Dalquist
 * @version $Revision$
 */
public class PersonLookupHelperImpl {
    private static final Comparator<IPersonAttributes> PERSON_ATTRIBUTES_COMPARATOR = new PersonAttributesNameComparator();
    
    private IPersonAttributeDao personAttributeDao;
    private boolean onlyUseConfiguredAttributes = false;
    
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
    /**
     * @return the onlyUseConfiguredAttributes
     */
    public boolean isOnlyUseConfiguredAttributes() {
        return onlyUseConfiguredAttributes;
    }
    /**
     * @param onlyUseConfiguredAttributes the onlyUseConfiguredAttributes to set
     */
    public void setOnlyUseConfiguredAttributes(boolean onlyUseConfiguredAttributes) {
        this.onlyUseConfiguredAttributes = onlyUseConfiguredAttributes;
    }


    public Set<String> getQueryAttributes(ExternalContext externalContext) {
        final PortletRequest portletRequest = (PortletRequest)externalContext.getNativeRequest();
        final PortletPreferences preferences = portletRequest.getPreferences();
        
        final Set<String> queryAttributes = new LinkedHashSet<String>();
        
        final String[] configuredAttributes = preferences.getValues("personLookupAttributes", new String[0]);
        for (final String configuredAttribute : configuredAttributes) {
            queryAttributes.add(configuredAttribute);
        }
        
        if (queryAttributes.size() == 0 || !this.onlyUseConfiguredAttributes) {
            final Set<String> availableAttributes = this.personAttributeDao.getAvailableQueryAttributes();
            
            if (availableAttributes != null) {
                for (final String availableAttribute : new TreeSet<String>(availableAttributes)) {
                    queryAttributes.add(availableAttribute);
                }
            }
        }
        
        return queryAttributes;
    }

    public Set<IPersonAttributes> doPersonQuery(PersonQuery query) {
        final Map<String, Attribute> attributes = query.getAttributes();
        
        final Map<String, Object> queryAttributes = new HashMap<String, Object>();
        
        for (final Map.Entry<String, Attribute> attrEntry : attributes.entrySet()) {
            queryAttributes.put(attrEntry.getKey(), attrEntry.getValue().getValue());
        }
        
        final Set<IPersonAttributes> people = this.personAttributeDao.getPeople(queryAttributes);
        if (people == null) {
            return null;
        }

        final TreeSet<IPersonAttributes> sortedPeople = new TreeSet<IPersonAttributes>(PERSON_ATTRIBUTES_COMPARATOR);
        sortedPeople.addAll(people);
        return sortedPeople;
    }
    
    
    private static class PersonAttributesNameComparator implements Comparator<IPersonAttributes>, Serializable {
        private static final long serialVersionUID = 1L;

        /* (non-Javadoc)
         * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
         */
        public int compare(IPersonAttributes o1, IPersonAttributes o2) {
            return new CompareToBuilder().append(o1.getName(), o2.getName()).toComparison();
        }
    }
}
