/* Copyright 2001 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/

package org.jasig.portal.groups;
 
 /**
 * An interface for retrieving <code>IEntities</code>.
 * @author Dan Ellentuck
 * @version 1.0, 11/29/01
 */
public interface IEntityStore {
/**
 * @return org.jasig.portal.groups.IEntity
 * @param key java.lang.String
 */
IEntity newInstance(String key) throws GroupsException;
/**
 * @return org.jasig.portal.groups.IEntity
 * @param key java.lang.String - the entity's key
 * @param type java.lang.Class - the entity's Type
 */
IEntity newInstance(String key, Class type) throws GroupsException;
}
