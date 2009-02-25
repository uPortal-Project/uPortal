/**
 * Copyright (c) 2000-2009, Jasig, Inc.
 * See license distributed with this file and available online at
 * https://www.ja-sig.org/svn/jasig-parent/tags/rel-10/license-header.txt
 */
package  org.jasig.portal.channels.groupsmanager;


/**
 * Defines the interface for a command object to be used by CGroupssManager
 * @author Don Fracapane
 * @version $Revision$
 */

public interface IGroupsManagerCommand {

   /**
    * Execute the command
    * @param sessionData
    */
   public void execute (CGroupsManagerSessionData sessionData) throws Exception;
}



