/**
 * Copyright (c) 2000-2009, Jasig, Inc.
 * See license distributed with this file and available online at
 * https://www.ja-sig.org/svn/jasig-parent/tags/rel-10/license-header.txt
 */
 package org.jasig.portal.groups;

 
/**
 * Factory for creating <code>ReferencePersonNameFinders</code>.
 * @author Alex Vigdor
 * @version $Revision$
 */

public class ReferenceChannelNameFinderFactory implements IEntityNameFinderFactory {
  /**
   * ReferenceChannelNameFinderFactory constructor comment.
   */
  public ReferenceChannelNameFinderFactory() {
          super();
  }
  /**
   * Return a finder instance.
   * @return org.jasig.portal.groups.IEntityNameFinder
   * @exception org.jasig.portal.groups.GroupsException
   */
  public IEntityNameFinder newFinder() throws GroupsException
  {
   return ReferenceChannelNameFinder.singleton();
  }
}
