/* Copyright 2004 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/

package org.jasig.portal.services.persondir.support;

import java.util.Map;

import org.springframework.dao.DataAccessException;

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

}
