/* Copyright 2004 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/

package org.jasig.portal.services.persondir;

import java.util.Map;
import java.util.Set;

import org.jasig.portal.security.IPerson;
import org.jasig.portal.security.provider.RestrictedPerson;

/**
 * Interface for services to be provided by PersonDirectory implementations.
 * 
 * @author andrew.petro@yale.edu
 * @version $Revision$ $Date$
 */
public interface IPersonDirectory {

    /**
     * For a given user, return a Map from attribute names to values.
     * 
     * The expected implementation is that values will be Strings or Lists of
     * Strings, but there is nothing to prevent other implementations -- except
     * ensuring that your clients of this class will understand these other
     * typed values.
     * 
     * While clients of this method may not understand attribute values other
     * than Strings, they should treat attribute values they cannot understand
     * as equivalent to the attributes not being present at all. That is, do
     * *not* iterate over this Map casting it to String -- by doing so you will
     * be forclosing some other client which would like to obtain some Object as
     * an attribute.
     * 
     * @param username -
     *                name of user for whom information is desired
     * @return Map from String attribute names to values
     */
    public Map getUserDirectoryInformation(String username);

    /**
     * For a given user, obtain attributes for the user and then insert those
     * attributes into the given IPerson.  SIDE EFFECT: modifies the person
     * argument, adding the attributes for the given uid.
     * 
     * @param uid -
     *                unique identifier for user.
     * @param person -
     *                IPerson representing the user
     */
    public void getUserDirectoryInformation(String uid, IPerson person);
    
    /**
     * Gets a {@link Set} of attribute names that may be returned for 
     * a user query. No guarantee is made that all the attributes in the
     * {@link Set} will be avalable for every user.
     * <br>
     * Implementations may return <code>null</code> if they cannot provide
     * a list of attribute names.
     * <br>
     * It is recommended that implementations return an immutable
     * {@link Set}.
     * 
     * @return A {@link Set} of possible attribute names for user queries.
     */
    public Set getAttributeNames();

    /**
     * Returns a reference to a restricted IPerson represented by the supplied
     * user ID. The restricted IPerson allows access to person attributes
     * (which will be populated by this class), but not the security context.
     * 
     * @param uid
     *                the user ID
     * @return the corresponding person, restricted so that its security context
     *            is inaccessible
     */
    public RestrictedPerson getRestrictedPerson(String uid);

    /**
     * Suggests to the IPersonDirectory implementation that it update its cache
     * of IPersons with the uid->IPerson mapping supplied.
     * @param uid - user identifier
     * @param person - a fresh IPerson to be cached.
     */
    public void cachePerson(String uid, IPerson person);

}
