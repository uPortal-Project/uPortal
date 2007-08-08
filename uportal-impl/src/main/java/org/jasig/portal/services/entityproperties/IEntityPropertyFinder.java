/* Copyright 2001 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/

package  org.jasig.portal.services.entityproperties;

import org.jasig.portal.EntityIdentifier;

/**
 * An interface describing entity property finders that can be
 * added to the EntityPropertyRegistry
 *
 * @author Alex Vigdor av317@columbia.edu
 * @version $Revision$
 */
public interface IEntityPropertyFinder {
    String[] getPropertyNames(EntityIdentifier entityID);
    String getProperty(EntityIdentifier entityID, String name);
}



