/* Copyright 2004 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/

package org.jasig.portal.services.persondir.support;

import java.util.Map;

/**
 * A mock PersonAttributeDao to be used for testing.
 * @author andrew.petro@yale.edu
 * @version $Revision$ $Date$
 */
public class MockPersonAttributeDao 
    implements PersonAttributeDao {

    private Map backingMap;
    
    /* (non-Javadoc)
     * @see org.jasig.portal.services.persondir.support.PersonAttributeDao#attributesForUser(java.lang.String)
     */
    public Map attributesForUser(String uid) {
        return this.backingMap;
    }

    /**
     * Get the Map which this mock object will return for all legal invocations of
     * attributesForUser()
     * @return Returns the backingMap.
     */
    public Map getBackingMap() {
        return this.backingMap;
    }
    
    /**
     * Set the Map which this mock object will return for all legal invocations of
     * attributesForUser().
     * @param backingMap The backingMap to set.
     */
    public void setBackingMap(Map backingMap) {
        this.backingMap = backingMap;
    }
}
