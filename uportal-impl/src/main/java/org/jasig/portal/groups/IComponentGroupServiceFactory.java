/* Copyright 2002 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/

package org.jasig.portal.groups;

/**
 * Factory interface for creating an <code>IComponentGroupService</code>.
 * @author Dan Ellentuck
 * @version $Revision$
 */
public interface IComponentGroupServiceFactory {
/**
 * 
 */
public IComponentGroupService newGroupService() throws GroupsException;
/**
 * @param descriptor org.jasig.portal.groups.ComponentGroupServiceDescriptor
 */
public IComponentGroupService newGroupService(ComponentGroupServiceDescriptor descriptor)
throws GroupsException;
}
