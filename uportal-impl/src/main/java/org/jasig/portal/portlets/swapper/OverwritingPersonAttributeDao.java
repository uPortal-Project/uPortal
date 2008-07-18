/**
 * Copyright 2007 The JA-SIG Collaborative.  All rights reserved.
 * See license distributed with this file and
 * available online at http://www.uportal.org/license.html
 */
package org.jasig.portal.portlets.swapper;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.lang.Validate;
import org.jasig.services.persondir.IPersonAttributeDao;
import org.jasig.services.persondir.IPersonAttributes;
import org.jasig.services.persondir.support.AbstractDefaultAttributePersonAttributeDao;
import org.jasig.services.persondir.support.MultivaluedPersonAttributeUtils;
import org.jasig.services.persondir.support.NamedPersonImpl;
import org.jasig.services.persondir.support.merger.IAttributeMerger;
import org.jasig.services.persondir.support.merger.ReplacingAttributeAdder;
import org.springframework.beans.factory.annotation.Required;

/**
 * Provides for overriding certain attributes for certain users
 * 
 * TODO attribute overrides need to be scoped to the current user session
 * TODO implement logout portal event listener to clear overrides map 
 * 
 * @author Eric Dalquist
 * @version $Revision$
 */
public class OverwritingPersonAttributeDao extends AbstractDefaultAttributePersonAttributeDao {
    private final IAttributeMerger attributeMerger = new ReplacingAttributeAdder();
    private Map<String, Map<String, List<Object>>> attributeOverridesMap = new ConcurrentHashMap<String, Map<String,List<Object>>>();
    
    private IPersonAttributeDao delegatePersonAttributeDao;

    /**
     * @return the delegatePersonAttributeDao
     */
    public IPersonAttributeDao getDelegatePersonAttributeDao() {
        return delegatePersonAttributeDao;
    }
    /**
     * @param delegatePersonAttributeDao the delegatePersonAttributeDao to set
     */
    @Required
    public void setDelegatePersonAttributeDao(IPersonAttributeDao delegatePersonAttributeDao) {
        Validate.notNull(delegatePersonAttributeDao, "delegatePersonAttributeDao can not be null");
        this.delegatePersonAttributeDao = delegatePersonAttributeDao;
    }
    
    /**
     * @return the attributeOverridesMap
     */
    public Map<String, Map<String, List<Object>>> getAttributeOverridesMap() {
        return attributeOverridesMap;
    }
    /**
     * @param attributeOverridesMap the attributeOverridesMap to set
     */
    public void setAttributeOverridesMap(Map<String, Map<String, List<Object>>> attributeOverridesMap) {
        Validate.notNull(attributeOverridesMap, "attributeOverridesMap can not be null");
        this.attributeOverridesMap = attributeOverridesMap;
    }
    

    public void setUserAttributeOverride(String uid, Map<String, Object> attributes) {
        //Not really a seed but the function still works
        final Map<String, List<Object>> multivaluedAttributes = MultivaluedPersonAttributeUtils.toMultivaluedMap(attributes);
        
        //Update the overrides map
        this.attributeOverridesMap.put(uid, multivaluedAttributes);
    }
    
    public void removeUserAttributeOverride(String uid) {
        //Remove the uid from the overrides map
        this.attributeOverridesMap.remove(uid);
    }

    
    /* (non-Javadoc)
     * @see org.jasig.services.persondir.IPersonAttributeDao#getPeopleWithMultivaluedAttributes(java.util.Map)
     */
    public Set<IPersonAttributes> getPeopleWithMultivaluedAttributes(Map<String, List<Object>> query) {
        final Set<IPersonAttributes> people = this.delegatePersonAttributeDao.getPeopleWithMultivaluedAttributes(query);
        
        final Set<IPersonAttributes> modifiedPeople = new LinkedHashSet<IPersonAttributes>();
        
        for (final IPersonAttributes person : people) {
            final String name = person.getName();
            
            if (name == null) {
                this.logger.warn("IPerson '" + person + "' has no name and cannot have attributes overriden");
                modifiedPeople.add(person);
            }
            else {
                final Map<String, List<Object>> attributeOverrides = this.attributeOverridesMap.get(name);
                
                if (attributeOverrides == null) {
                    modifiedPeople.add(person);
                }
                else {
                    final Map<String, List<Object>> mergedAttributes = this.attributeMerger.mergeAttributes(new LinkedHashMap<String, List<Object>>(person.getAttributes()), attributeOverrides);
                    final IPersonAttributes mergedPerson = new NamedPersonImpl(name, mergedAttributes);
                    modifiedPeople.add(mergedPerson);
                }
            }
        }
        
        return modifiedPeople;
    }

    /* (non-Javadoc)
     * @see org.jasig.services.persondir.IPersonAttributeDao#getPossibleUserAttributeNames()
     */
    public Set<String> getPossibleUserAttributeNames() {
        return this.delegatePersonAttributeDao.getPossibleUserAttributeNames();
    }

    /* (non-Javadoc)
     * @see org.jasig.services.persondir.IPersonAttributeDao#getAvailableQueryAttributes()
     */
    public Set<String> getAvailableQueryAttributes() {
        return this.delegatePersonAttributeDao.getAvailableQueryAttributes();
    }
}
