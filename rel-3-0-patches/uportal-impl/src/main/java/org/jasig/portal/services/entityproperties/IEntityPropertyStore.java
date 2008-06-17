/* Copyright 2001 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/

package  org.jasig.portal.services.entityproperties;

import org.jasig.portal.EntityIdentifier;

/**
 * The interface for an entity property store; the EntityChannelRegistry
 * must be configured with exactly one store, but can have multiple finders.
 *
 * @author Alex Vigdor av317@columbia.edu
 * @version $Revision$
 */
public interface IEntityPropertyStore extends IEntityPropertyFinder{

    // will add property to store without affecting finder results
    void storeProperty(EntityIdentifier entityID, String name, String value);

    // will remove property from the local store, but not from finders
    void unStoreProperty(EntityIdentifier entityID, String name);
}



