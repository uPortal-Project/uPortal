/* Copyright 2004 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/

package org.jasig.portal.services.persondir.support;

import java.util.Map;

import org.springframework.dao.DataAccessException;

/**
 * Interface for classes that are able to take a Map from currrently known
 * attribute names to currently known attribute values and return a Map
 * from attribute names to values representing additional attributes the class would
 * like to contribute.
 * @author andrew.petro@yale.edu
 * @version $Revision$ $Date$
 */
public interface AttributesFromAttributesDao {

    /**
     * Obtain a Map from attribute names to attribute values for the given user.
     * Attribute names must be Strings.  Attribute values may be arbitrary Objects
     * but are expected to typically be either Strings or Lists of Strings.
     * The attributes argument is also a Map from attribute names to arbitrary values.
     * The implementation may use this map to seed queries, determine whether it
     * has anything intelligent to say about this user, read from it a CAS proxy ticket
     * to authenticate to a remote service, or whatever.
     * Typically this Map will include a map from the attribute "uid" to the unique
     * identifier (username) of the user about whom we are querying.
     * The Map that this method returns must be a Map from attribute names to
     * values for which the implementation is in some sense claiming responsibility.
     * Particularly, the returned Map should *not* be the union of what the
     * implementation is able to determine from the Map it was given and that Map
     * argument it was given.  If you are given a Map that includes the attribute 
     * "phone" and value "915-7333" and you return a Map that includes the
     * attribute "phone" and value "915-7333", this means that your implementation
     * also believes that the "phone" attribute should have this value.
     * Implementations may modify the "attributes" argument and may return it
     * or another Map.
     * The return value must be a mutable Map.  Map entry values are recommended
     * to be Strings, strongly encouraged to be only immutable objects, but may be
     * any Objects.  Therefore, while the client of this method may not understand
     * any values other than Strings, it should cope with (be able to ignore) other
     * value types.
     * @param attributes - currently known attributes
     * @return Map from attribute names to values
     * @throws DataAccessException - on data access failure
     * @throws IllegalArgumentException - if attributes is null
     */
    public Map attributesFromAttributes(Map attributes);
    
}

