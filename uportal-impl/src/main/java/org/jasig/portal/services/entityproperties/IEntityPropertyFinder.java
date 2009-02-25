/**
 * Copyright (c) 2000-2009, Jasig, Inc.
 * See license distributed with this file and available online at
 * https://www.ja-sig.org/svn/jasig-parent/tags/rel-10/license-header.txt
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



