/* Copyright 2004 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/

package org.jasig.portal.services.persondir.support;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * A thin adaptor from PersonAttributeDao 
 * to AttributesFromAttributesDao.
 * Does so by creating a Map from a configurable attribute name to the
 * uid provided in attributesForUser() and invoking the delegate's
 * attributesFromAttributes(Map).
 * @author andrew.petro@yale.edu
 * @version $Revision$ $Date$
 */
public class PADaoToAFADaoDelegator implements PersonAttributeDao {

    /**
     * A delegate which we will invoke to do our actual work.  This class merely
     * adapts from the interface that receives only a String uid to this instance
     * of an interface that takes a seed Map.
     */
    private AttributesFromAttributesDao attributesFromAttributesDao;
    
    /**
     * The attribute name which will be mapped to the uid given in
     * attributesForUser(uid).  The map is then used to invoke the
     * AttributesFromAttributes DAO delegate to perform the actual work
     * of going and getting attributes from wherever it gets them.
     */
    private String seedAttributeName = "uid";
    
    /**
     * This implementation adapts from the PersonAttributeDao interface to
     * an AttributesFromAttributesDao delegate by creating a Map from
     * the configurable seed attribute name to the provided uid value.
     * @param uid - unique identifier for user
     * @return Map from String attribute names to values
     */
    public Map attributesForUser(String uid) {
        Map seedMap = new HashMap();
        seedMap.put(this.seedAttributeName, uid);
        return this.attributesFromAttributesDao.attributesFromAttributes(seedMap);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.jasig.portal.services.persondir.support.PersonAttributeDao#getAttributeNames()
     */
    public Set getAttributeNames() {
        //TODO Could some of the AttributesFromAttributesDao impls provide this information?
        return null;
    }
    
    /**
     * Get the delegate DAO which this implementation uses to do its actual work.
     * @return Returns the attributesFromAttributesDao.
     */
    public AttributesFromAttributesDao getAttributesFromAttributesDao() {
        return this.attributesFromAttributesDao;
    }
    
    /**
     * @param attributesFromAttributesDao The attributesFromAttributesDao to set.
     */
    public void setAttributesFromAttributesDao(
            AttributesFromAttributesDao attributesFromAttributesDao) {
        this.attributesFromAttributesDao = attributesFromAttributesDao;
    }
    
    /**
     * Get the name of the attribute which will be seeded with the uid provided
     * in getAttributesForUser(uid) before invoking 
     * the delegate getAttributesFromAttributes(Map).
     * @return Returns the seedAttributeName.
     */
    public String getSeedAttributeName() {
        return this.seedAttributeName;
    }
    
    /**
     * @param seedAttributeName The seedAttributeName to set.
     */
    public void setSeedAttributeName(String seedAttributeName) {
        this.seedAttributeName = seedAttributeName;
    }

}