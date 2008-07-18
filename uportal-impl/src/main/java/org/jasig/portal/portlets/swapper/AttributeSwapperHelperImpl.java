/**
 * Copyright 2007 The JA-SIG Collaborative.  All rights reserved.
 * See license distributed with this file and
 * available online at http://www.uportal.org/license.html
 */
package org.jasig.portal.portlets.swapper;

import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import javax.portlet.PortletPreferences;
import javax.portlet.PortletRequest;

import org.apache.commons.lang.StringUtils;
import org.jasig.services.persondir.IPersonAttributeDao;
import org.jasig.services.persondir.IPersonAttributes;
import org.springframework.webflow.context.ExternalContext;

/**
 * @author Eric Dalquist
 * @version $Revision$
 */
public class AttributeSwapperHelperImpl {
    private OverwritingPersonAttributeDao overwritingPersonAttributeDao;
    private boolean onlyUseConfiguredAttributes = false;
    
    /**
     * @return the personAttributeDao
     */
    public OverwritingPersonAttributeDao getPersonAttributeDao() {
        return overwritingPersonAttributeDao;
    }
    /**
     * @param personAttributeDao the personAttributeDao to set
     */
    public void setPersonAttributeDao(OverwritingPersonAttributeDao personAttributeDao) {
        this.overwritingPersonAttributeDao = personAttributeDao;
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

    public Set<String> getSwappableAttributes(ExternalContext externalContext) {
        final PortletRequest portletRequest = (PortletRequest)externalContext.getNativeRequest();
        final PortletPreferences preferences = portletRequest.getPreferences();
        
        final Set<String> swappableAttributes = new LinkedHashSet<String>();
        
        final String[] configuredAttributes = preferences.getValues("swappableAttributes", new String[0]);
        for (final String configuredAttribute : configuredAttributes) {
            swappableAttributes.add(configuredAttribute);
        }
        
        if (swappableAttributes.size() == 0 || !this.onlyUseConfiguredAttributes) {
            final Set<String> possibleAttributes = this.overwritingPersonAttributeDao.getPossibleUserAttributeNames();
            
            if (possibleAttributes != null) {
                for (final String availableAttribute : new TreeSet<String>(possibleAttributes)) {
                    swappableAttributes.add(availableAttribute);
                }
            }
        }
        
        return swappableAttributes;
    }
    
    public IPersonAttributes getOriginalUserAttributes(String uid) {
        final IPersonAttributeDao delegatePersonAttributeDao = this.overwritingPersonAttributeDao.getDelegatePersonAttributeDao();
        final IPersonAttributes person = delegatePersonAttributeDao.getPerson(uid);
        return person;
    }
    
    public void populateSwapRequest(String uid, AttributeSwapRequest attributeSwapRequest) {
        final IPersonAttributes person = this.overwritingPersonAttributeDao.getPerson(uid);
        
        final Map<String, Attribute> currentAttributes = attributeSwapRequest.getCurrentAttributes();
        currentAttributes.clear();
        for (final Map.Entry<String, List<Object>> userAttributeEntry : person.getAttributes().entrySet()) {
            final String attribute = userAttributeEntry.getKey();
            final List<Object> values = userAttributeEntry.getValue();
            final Object value = (values != null && values.size() > 0 ? values.get(0) : null);
            currentAttributes.put(attribute, new Attribute(String.valueOf(value)));
        }
    }
    
    public void swapAttributes(String uid, AttributeSwapRequest attributeSwapRequest) {
        final Map<String, Object> attributes = new HashMap<String, Object>();
        
        final Map<String, Attribute> currentAttributes = attributeSwapRequest.getCurrentAttributes();
        this.copyAttributes(attributes, currentAttributes);
        
        final Map<String, Attribute> attributesToCopy = attributeSwapRequest.getAttributesToCopy();
        this.copyAttributes(attributes, attributesToCopy);
        
        this.overwritingPersonAttributeDao.setUserAttributeOverride(uid, attributes);
    }
    
    public void resetAttributes(String uid) {
        this.overwritingPersonAttributeDao.removeUserAttributeOverride(uid);
    }

    protected void copyAttributes(final Map<String, Object> destination, final Map<String, Attribute> source) {
        for (final Map.Entry<String, Attribute> sourceEntry : source.entrySet()) {
            final Attribute attribute = sourceEntry.getValue();
            if (attribute != null && StringUtils.isNotEmpty(attribute.getValue())) {
                final String name = sourceEntry.getKey();
                destination.put(name, attribute.getValue());
            }
        }
    }
}
