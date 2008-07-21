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
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.Map.Entry;

import javax.portlet.PortletPreferences;
import javax.portlet.PortletRequest;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;
import org.jasig.portal.security.IPerson;
import org.jasig.portal.security.IPersonManager;
import org.jasig.portal.url.IPortalRequestUtils;
import org.jasig.services.persondir.IPersonAttributeDao;
import org.jasig.services.persondir.IPersonAttributes;
import org.jasig.services.persondir.support.MultivaluedPersonAttributeUtils;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.webflow.context.ExternalContext;

/**
 * @author Eric Dalquist
 * @version $Revision$
 */
public class AttributeSwapperHelperImpl implements IAttributeSwapperHelper {
    private static final String OVERRIDDEN_ATTRIBUTES = AttributeSwapperHelperImpl.class.getName() + ".OVERRIDDEN_ATTRIBUTES";

    private OverwritingPersonAttributeDao overwritingPersonAttributeDao;
    private IPersonManager personManager;
    private IPortalRequestUtils portalRequestUtils;
    
    public OverwritingPersonAttributeDao getPersonAttributeDao() {
        return overwritingPersonAttributeDao;
    }
    /**
     * The {@link OverwritingPersonAttributeDao} instance to use to override attributes
     */
    @Required
    public void setPersonAttributeDao(OverwritingPersonAttributeDao personAttributeDao) {
        this.overwritingPersonAttributeDao = personAttributeDao;
    }
    
    public IPersonManager getPersonManager() {
        return personManager;
    }
    /**
     * The {@link IPersonManager} to use to access the current IPerson
     */
    @Required
    public void setPersonManager(IPersonManager personManager) {
        this.personManager = personManager;
    }
    
    public IPortalRequestUtils getPortalRequestUtils() {
        return portalRequestUtils;
    }
    /**
     * Utility class to access the original portal request
     */
    @Required
    public void setPortalRequestUtils(IPortalRequestUtils portalRequestUtils) {
        this.portalRequestUtils = portalRequestUtils;
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
     * @see org.jasig.portal.portlets.swapper.IAttributeSwapperHelper#swapAttributes(org.springframework.webflow.context.ExternalContext, org.jasig.portal.portlets.swapper.AttributeSwapRequest)
     */
    public void swapAttributes(ExternalContext externalContext, AttributeSwapRequest attributeSwapRequest) {
        //Collate the swap request into a single overrides map
        final Map<String, Object> attributes = new HashMap<String, Object>();
        
        final Map<String, Attribute> currentAttributes = attributeSwapRequest.getCurrentAttributes();
        this.copyAttributes(attributes, currentAttributes);
        
        final Map<String, Attribute> attributesToCopy = attributeSwapRequest.getAttributesToCopy();
        this.copyAttributes(attributes, attributesToCopy);
        
        final Principal currentUser = externalContext.getCurrentUser();
        final String uid = currentUser.getName();
        final IPersonAttributes originalUserAttributes = this.getOriginalUserAttributes(uid);
        
        //Filter out unchanged attributes
        for (final Iterator<Map.Entry<String, Object>> overrideAttrEntryItr = attributes.entrySet().iterator(); overrideAttrEntryItr.hasNext();) {
            final Entry<String, Object> overrideAttrEntry = overrideAttrEntryItr.next();
            final String attribute = overrideAttrEntry.getKey();
            
            final Object originalValue = originalUserAttributes.getAttributeValue(attribute);
            final Object overrideValue = overrideAttrEntry.getValue();
            if (originalValue == overrideValue || (originalValue != null && originalValue.equals(overrideValue))) {
                overrideAttrEntryItr.remove();
            }
        }
        
        
        //Override attributes retrieved the person directory
        this.overwritingPersonAttributeDao.setUserAttributeOverride(uid, attributes);

        //Update the IPerson, setting the overridden attributes
        final PortletRequest portletRequest = (PortletRequest)externalContext.getNativeRequest();
        final HttpServletRequest portalRequest = this.portalRequestUtils.getOriginalPortalRequest(portletRequest);
        
        final IPerson person = this.personManager.getPerson(portalRequest);
        final Map<String, List<Object>> multivaluedAttributes = MultivaluedPersonAttributeUtils.toMultivaluedMap(attributes);
        person.setAttributes(multivaluedAttributes);
        person.setAttribute(OVERRIDDEN_ATTRIBUTES, multivaluedAttributes.keySet());
    }
    
    /* (non-Javadoc)
     * @see org.jasig.portal.portlets.swapper.IAttributeSwapperHelper#resetAttributes(java.lang.String)
     */
    public void resetAttributes(ExternalContext externalContext) {
        final Principal currentUser = externalContext.getCurrentUser();
        final String uid = currentUser.getName();
        
        //Remove the person directory override
        this.overwritingPersonAttributeDao.removeUserAttributeOverride(uid);
        
        //Remove the IPerson attribute override, bit of a hack as we really just remove all overrides
        //then re-add all attributes from person directory
        final PortletRequest portletRequest = (PortletRequest)externalContext.getNativeRequest();
        final HttpServletRequest portalRequest = this.portalRequestUtils.getOriginalPortalRequest(portletRequest);
        
        final IPerson person = this.personManager.getPerson(portalRequest);

        final Set<String> overriddenAttributes = (Set)person.getAttribute(OVERRIDDEN_ATTRIBUTES);
        if (overriddenAttributes != null) {
            person.setAttribute(OVERRIDDEN_ATTRIBUTES, null);
            
            for (final String attribute : overriddenAttributes) {
                person.setAttribute(attribute, null);
            }
        }
        
        final IPersonAttributes originalUserAttributes = this.getOriginalUserAttributes(uid);
        final Map<String, List<Object>> attributes = originalUserAttributes.getAttributes();
        person.setAttributes(attributes);
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
