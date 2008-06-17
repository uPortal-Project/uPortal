/* Copyright 2001, 2002 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/

package org.jasig.portal.groups;

/**
 * Factory interface for creating an <code>IGroupService</code>.
 * @author Dan Ellentuck
 * @version $Revision$
 */

public interface IGroupServiceFactory {
/**
 * Return an IGroupService instance.
 * @return org.jasig.portal.groups.IGroupService
 * @exception org.jasig.portal.groups.GroupsException
 */
public IGroupService newGroupService() throws GroupsException;
}
