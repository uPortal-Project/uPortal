/* Copyright 2004 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/

package org.jasig.portal.services.persondir.support;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

/**
 * A mock IPersonAttributeDao to be used for testing.
 * 
 * @author andrew.petro@yale.edu
 * @version $Revision$ $Date$
 */
public class MockPersonAttributeDao implements IPersonAttributeDao {
    private Map backingMap;
    
    /**
     * @see org.jasig.portal.services.persondir.support.IPersonAttributeDao#getPossibleUserAttributeNames()
     */
    public Set getPossibleUserAttributeNames() {
        if (this.backingMap == null) {
            return null;
        }
        else {
            return Collections.unmodifiableSet(this.backingMap.keySet());
        }
    }
    
    /**
     * @see org.jasig.portal.services.persondir.support.IPersonAttributeDao#getUserAttributes(java.util.Map)
     */
    public Map getUserAttributes(final Map seed) {
        return this.backingMap;
    }
    
    /**
     * @see org.jasig.portal.services.persondir.support.IPersonAttributeDao#getUserAttributes(java.lang.String)
     */
    public Map getUserAttributes(final String uid) {
        return this.backingMap;
    }
    
    
    /**
     * Get the Map which this mock object will return for all legal invocations of
     * attributesForUser()
     * 
     * @return Returns the backingMap.
     */
    public Map getBackingMap() {
        return this.backingMap;
    }
    
    /**
     * Set the Map which this mock object will return for all legal invocations of
     * attributesForUser().
     * 
     * @param backingMap The backingMap to set.
     */
    public void setBackingMap(final Map backingMap) {
        this.backingMap = backingMap;
    }
}
