/* Copyright 2004 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/

package org.jasig.portal.services.persondir.support;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;


/**
 * Simply returns the seed it is passed.
 * 
 * @author Eric Dalquist <a href="mailto:edalquist@unicon.net">edalquist@unicon.net</a>
 * @version $Revision$ $Date$
 */
public class EchoPersonAttributeDaoImpl extends AbstractDefaultQueryPersonAttributeDao {

    /**
     * Returns a duplicate of the seed it is passed.
     * @return a Map equal to but not the same reference as the seed.
     * @see org.jasig.portal.services.persondir.IPersonAttributeDao#getUserAttributes(java.util.Map)
     */
    public Map getUserAttributes(final Map seed) {
        return new HashMap(seed);
    }

    /**
     * Possible attributes are unknown; will always return <code>null</code>.
     * @return null
     * @see org.jasig.portal.services.persondir.IPersonAttributeDao#getPossibleUserAttributeNames()
     */
    public Set getPossibleUserAttributeNames() {
        return null;
    }

}
