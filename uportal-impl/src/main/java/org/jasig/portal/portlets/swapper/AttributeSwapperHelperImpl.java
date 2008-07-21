/**
 * Copyright 2007 The JA-SIG Collaborative.  All rights reserved.
 * See license distributed with this file and
 * available online at http://www.uportal.org/license.html
 */
package org.jasig.portal.portlets.swapper;

import java.security.Principal;
import java.util.Arrays;
import java.util.Collections;
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
public class AttributeSwapperHelperImpl implements IAttributeSwapperHelper {
    private OverwritingPersonAttributeDao overwritingPersonAttributeDao;
    
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

    /* (non-Javadoc)
     * @see org.jasig.portal.portlets.swapper.IAttributeSwapperHelper#getSwappableAttributes(org.springframework.webflow.context.ExternalContext)
     */
    public Set<String> getSwappableAttributes(ExternalContext externalContext) {
        final PortletRequest portletRequest = (PortletRequest)externalContext.getNativeRequest();
        final PortletPreferences preferences = portletRequest.getPreferences();
        
        final Set<String> swappableAttributes;
        
        //Use prefs configured list if available
        final String[] configuredAttributes = preferences.getValues(ATTRIBUTE_SWAPPER_ATTRIBUTES_FORM_SWAPPABLE_ATTRIBUTES, null);
        if (configuredAttributes != null) {
            swappableAttributes = new LinkedHashSet<String>(Arrays.asList(configuredAttributes));
        }
        else {
            //If no prefs try the 'possibleUserAttributeNames' from the IPersonAttributeDao
            final Set<String> possibleAttributes = this.overwritingPersonAttributeDao.getPossibleUserAttributeNames();
            if (possibleAttributes != null) {
                swappableAttributes = new TreeSet<String>(possibleAttributes);
            }
            //If no possible names try getting the current user's attributes and use the key set
            else {
                final Principal currentUser = externalContext.getCurrentUser();
                final IPersonAttributes baseUserAttributes = this.getOriginalUserAttributes(currentUser.getName());
                
                if (baseUserAttributes != null) {
                    final Map<String, List<Object>> attributes = baseUserAttributes.getAttributes();
                    swappableAttributes = new LinkedHashSet<String>(attributes.keySet());
                }
                else {
                    swappableAttributes = Collections.emptySet();
                }
            }
        }
        
        return swappableAttributes;
    }
    
    /* (non-Javadoc)
     * @see org.jasig.portal.portlets.swapper.IAttributeSwapperHelper#getOriginalUserAttributes(java.lang.String)
     */
    public IPersonAttributes getOriginalUserAttributes(String uid) {
        final IPersonAttributeDao delegatePersonAttributeDao = this.overwritingPersonAttributeDao.getDelegatePersonAttributeDao();
        return delegatePersonAttributeDao.getPerson(uid);
    }
    
    /* (non-Javadoc)
     * @see org.jasig.portal.portlets.swapper.IAttributeSwapperHelper#populateSwapRequest(org.springframework.webflow.context.ExternalContext, org.jasig.portal.portlets.swapper.AttributeSwapRequest)
     */
    public void populateSwapRequest(ExternalContext externalContext, AttributeSwapRequest attributeSwapRequest) {
        final Principal currentUser = externalContext.getCurrentUser();
        final String uid = currentUser.getName();
        final IPersonAttributes person = this.overwritingPersonAttributeDao.getPerson(uid);
        
        final Map<String, Attribute> currentAttributes = attributeSwapRequest.getCurrentAttributes();
        currentAttributes.clear();
        
        final Set<String> swappableAttributes = this.getSwappableAttributes(externalContext);
        for (final String attribute : swappableAttributes) {
            final Object value = person.getAttributeValue(attribute);
            currentAttributes.put(attribute, new Attribute(String.valueOf(value)));
        }
    }
    
    /* (non-Javadoc)
     * @see org.jasig.portal.portlets.swapper.IAttributeSwapperHelper#swapAttributes(java.lang.String, org.jasig.portal.portlets.swapper.AttributeSwapRequest)
     */
    public void swapAttributes(String uid, AttributeSwapRequest attributeSwapRequest) {
        final Map<String, Object> attributes = new HashMap<String, Object>();
        
        final Map<String, Attribute> currentAttributes = attributeSwapRequest.getCurrentAttributes();
        this.copyAttributes(attributes, currentAttributes);
        
        final Map<String, Attribute> attributesToCopy = attributeSwapRequest.getAttributesToCopy();
        this.copyAttributes(attributes, attributesToCopy);
        
        this.overwritingPersonAttributeDao.setUserAttributeOverride(uid, attributes);
    }
    
    /* (non-Javadoc)
     * @see org.jasig.portal.portlets.swapper.IAttributeSwapperHelper#resetAttributes(java.lang.String)
     */
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
