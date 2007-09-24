/* Copyright 2002 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/

 package org.jasig.portal.groups;

/**
 * Factory interface for creating <code>IEntityNameFinders</code>.
 * @author Dan Ellentuck
 * @version $Revision$
 */

public interface IEntityNameFinderFactory {
/**
 * Return a finder instance.
 * @return org.jasig.portal.groups.IEntityNameFinder
 * @exception org.jasig.portal.groups.GroupsException
 */
public IEntityNameFinder newFinder() throws GroupsException;
}
