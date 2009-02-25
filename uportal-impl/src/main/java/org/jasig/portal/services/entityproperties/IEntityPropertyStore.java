/**
 * Copyright (c) 2000-2009, Jasig, Inc.
 * See license distributed with this file and available online at
 * https://www.ja-sig.org/svn/jasig-parent/tags/rel-10/license-header.txt
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



