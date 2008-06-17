/* Copyright 2002 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/

package org.jasig.portal.groups;

import java.util.Map;

/**
 * Interface for finding the names of portal entities of a given type.
 * @author Dan Ellentuck
 * @version $Revision$
 */

public interface IEntityNameFinder {
/**
 * Given the key, returns the entity's name.
 * @param key java.lang.String
 */
public String getName(String key) throws Exception;
/**
 * Given an array of keys, returns the names of the entities.
 * @param keys java.lang.String[]
 */
public Map getNames(String[] keys) throws Exception;
/**
 * Returns the entity type for this <code>IEntityFinder</code>.
 * @return java.lang.Class
 */
public Class getType();
}
