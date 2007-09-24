/* Copyright 2004 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/

package org.jasig.portal.services.persondir.support;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

import org.jasig.portal.services.persondir.IPersonAttributeDao;

/**
 * A stub IPersonAttributeDao to be used for testing.
 * Backed by a single Map which this implementation will always return.
 * 
 * @author andrew.petro@yale.edu
 * @version $Revision$ $Date$
 * @since uPortal 2.5
 */
public class StubPersonAttributeDao implements IPersonAttributeDao {
    private Map backingMap;
    
    public Set getPossibleUserAttributeNames() {
        if (this.backingMap == null) {
            return null;
        }
        
        return Collections.unmodifiableSet(this.backingMap.keySet());
    }
    
    public Map getUserAttributes(final Map seed) {
        if (seed == null) {
            throw new IllegalArgumentException("Illegal to invoke getUserAttributes(Map) with a null argument.");
        }
        return this.backingMap;
    }
    
    public Map getUserAttributes(final String uid) {
        if (uid == null) {
            throw new IllegalArgumentException("Illegal to invoke getUserAttributes(String) with a null argument.");
        }
        return this.backingMap;
    }
    
    
    /**
     * Get the Map which this stub object will return for all legal invocations of
     * attributesForUser()
     * 
     * @return Returns the backingMap.
     */
    public Map getBackingMap() {
        return this.backingMap;
    }
    
    /**
     * Set the Map which this stub object will return for all legal invocations of
     * attributesForUser().
     * 
     * @param backingMap The backingMap to set.
     */
    public void setBackingMap(final Map backingMap) {
        this.backingMap = backingMap;
    }
}
