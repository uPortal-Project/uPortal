/* Copyright 2004 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/

package org.jasig.portal.services.persondir.support;

import java.util.Map;
import java.util.Set;

/**
 * Data access object which, for a given userid, returns a Map
 * from attribute names to attribute values.
 * @author andrew.petro@yale.edu
 * @version $Revision$ $Date$
 */
public interface PersonAttributeDao {
    
    /**
     * Obtain a Map from attribute names to attribute values for the given user.
     * Attribute names must be Strings.  Attribute values may be arbitrary Objects
     * but are expected to typically be either Strings or Lists of Strings.
     * The return value must be a mutable Map and clients of this interface may
     * arbitrarily modify the returned Map.  While mutable entry values are not
     * forbidden, they are strongly discouraged.
     * @param uid - identifier for the user about whom we query
     * @return Map from attribute names to values
     * @throws DataAccessException - on data access failure
     * @throws IllegalArgumentException - if uid is null
     */
    public Map attributesForUser(String uid);
    
    
    /**
     * Gets a {@link Set} of attribute names that this PersonAttributeDao implementation
     * expects will be the keys in the Map it returns for the attributesForUser() method. 
     * No guarantee is made about the relationship of this Set to the set of keys
     * in the map returned by attributesForUser() -- attributesForUser() may map
     * attributes not declared in this Set and it may fail to map attributes declared in
     * this Set.
     * <br>
     * Implementations may return <code>null</code> if they cannot provide
     * a Set of attribute names and they should document at the implementation
     * level the semantics of their implementation of this method.
     * <br>
     * Implementations may return an immutable {@link Set}.
     * 
     * @return A {@link Set} of possible attribute names for user queries.
     */
    public Set getAttributeNames();
}
