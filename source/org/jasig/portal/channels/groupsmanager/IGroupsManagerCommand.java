/* Copyright 2001 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
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



