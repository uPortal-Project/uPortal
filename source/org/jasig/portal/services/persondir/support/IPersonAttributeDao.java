/* Copyright 2004 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/

package org.jasig.portal.services.persondir.support;

import java.util.Map;
import java.util.Set;

/**
 * Data access object which, for a given {@link Map} of query
 * data, returns a {@link Map} from attribute names to attribute
 * values.
 * 
 * @author andrew.petro@yale.edu
 * @author Eric Dalquist <a href="mailto:edalquist@unicon.net">edalquist@unicon.net</a>
 * @version $Revision$ $Date$
 */
public interface IPersonAttributeDao {

    /**
     * Obtains a mutable {@link Map} from attribute names to values for
     * the given query seed. The values may be mutable objects but it is
     * recommended that they be immutable.<br>
     * 
     * For the returned {@link Map}; Keys must be {@link String}, Values
     * can be any {@link Object}, they are typically {@link String}s.<br>
     * 
     * Values may also be multi-valued, in this case they are of type
     * {@link java.util.List} and the list contents are the values of the
     * attribute.<br>
     * 
     * This method returns according to the following rules:<br>
     * <ul>
     *  <li>If the user exists and has attributes a populated {@link Map} is returned.</li>
     *  <li>If the user exists and has no attributes an empty {@link Map} is returned.</li>
     *  <li>If the user doesn't exist <code>null</code> is returned.</li>
     *  <li>If an error occurs while getting the attributes the appropriate exception will be propagated.</li>
     * </ul>
     * 
     * @param seed Map of attributes to values to seed the query
     * @return Map from attribute names to values
     * @throws IllegalArgumentException If <code>seed</code> is <code>null.</code>
     */
    public Map getUserAttributes(final Map seed);


    /**
     * This method uses the default attribute to construct a seed
     * {@link Map} and call the {@link #getUserAttributes(Map)} method
     * with.
     * 
     * @param uid The string to use as the value in the seed
     * @return The same value as returned by {@link #getUserAttributes(Map)}
     * @see #getUserAttributes(Map)
     */
    public Map getUserAttributes(final String uid);


    /**
     * Gets a {@link Set} of attribute names that may be returned by the
     * {@link #getUserAttributes(Map)}. The names returned represent all
     * possible names {@link #getUserAttributes(Map)} could return. If the
     * dao doesn't have a way to know all possible attribute names this
     * method should return <code>null</code>.
     * <br>
     * An immutable {@link Set} is returned.
     * 
     * @return A {link Set} of possible attribute names for user queries.
     */
    public Set getPossibleUserAttributeNames();
}
