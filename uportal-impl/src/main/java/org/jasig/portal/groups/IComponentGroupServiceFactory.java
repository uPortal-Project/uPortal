/**
 * Copyright (c) 2000-2009, Jasig, Inc.
 * See license distributed with this file and available online at
 * https://www.ja-sig.org/svn/jasig-parent/tags/rel-10/license-header.txt
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
