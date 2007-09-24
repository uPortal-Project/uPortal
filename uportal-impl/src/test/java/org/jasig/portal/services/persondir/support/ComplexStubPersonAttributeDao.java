/* Copyright 2005 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/

package org.jasig.portal.services.persondir.support;

import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.jasig.portal.services.persondir.IPersonAttributeDao;

/**
 * Implements IPersonAttributeDao by looking up the specified user in a configured
 * Map.  The configured Map is a Map from String userids to Maps.  The Map
 * values are Maps from String user attribute names to user attribute values.
 * @version $Revision$ $Date$
 */
public class ComplexStubPersonAttributeDao 
    implements IPersonAttributeDao {

    /**
     * Map from userids to Maps.  The Map values are maps from
     * attribute names to attribute values.
     */
    private final Map backingMap;
    
    /**
     * Lazily initialized Set of possible all attribute names that map to an attribute
     * value for some user in our backing map.
     */
    private Set possibleAttributeNames = null;
    
    public ComplexStubPersonAttributeDao(Map backingMap) {
        this.backingMap = Collections.unmodifiableMap(backingMap);
    }
    

    
    public Set getPossibleUserAttributeNames() {
        if (this.possibleAttributeNames == null) {
            initializePossibleAttributeNames();
        }
        return this.possibleAttributeNames;
    }
    
    /**
     * Compute the set of attribute names that map to a value for at least one
     * user in our backing map and store it as the instance variable 
     * possibleAttributeNames.
     */
    private synchronized void initializePossibleAttributeNames() {
        // check again whether we need to run because the value may have
        // been initialized after the logic check that decided to call this method
        // but before the lock to enter this method was actually acquired.
        if (this.possibleAttributeNames == null) {
            /*
             * Compute the union of the keysets of all the Sets that are values in our 
             * backing map.
             */
            
            Set possibleAttribNames = new HashSet();
            
            for (Iterator iter = this.backingMap.values().iterator(); iter.hasNext() ; ) {
                Map attributeMapForSomeUser = (Map) iter.next();
                possibleAttribNames.addAll(attributeMapForSomeUser.keySet());
            }
            
            this.possibleAttributeNames = possibleAttribNames;
        }
    }
    
    
    public Map getUserAttributes(final Map seed) {
        if (seed == null) {
            throw new IllegalArgumentException("Illegal to invoke getUserAttributes(Map) with a null argument.");
        }
        return (Map) this.backingMap.get(seed.get("uid"));

    }
    
    public Map getUserAttributes(final String uid) {
        if (uid == null) {
            throw new IllegalArgumentException("Illegal to invoke getUserAttributes(String) with a null argument.");
        }
        
        return (Map) this.backingMap.get(uid);
    }
    
    
    /**
     * Get the Map backing this ComplexStubPersonAttributeDao.
     * 
     * @return Returns the backingMap.
     */
    public Map getBackingMap() {
        return this.backingMap;
    }
    
}
