/**
 * Copyright (c) 2000-2009, Jasig, Inc.
 * See license distributed with this file and available online at
 * https://www.ja-sig.org/svn/jasig-parent/tags/rel-10/license-header.txt
 */
package org.jasig.portal.groups;

/**
 * Factory interface for creating an <code>ICompositeGroupService</code>.
 * @author Dan Ellentuck
 * @version $Revision$
 */
public interface ICompositeGroupServiceFactory {
/**
 * 
 */
public ICompositeGroupService newGroupService() throws GroupsException;
}
