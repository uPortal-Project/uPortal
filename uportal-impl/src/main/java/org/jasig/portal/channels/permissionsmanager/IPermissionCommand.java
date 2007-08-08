/* Copyright 2001 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/

package  org.jasig.portal.channels.permissionsmanager;

/**
 *  Defines the interface for a command object to be used by CPermissionsManager
 *
 * @author Alex Vigdor
 * @version $Revision$
 */
public interface IPermissionCommand {
    public void execute (PermissionsSessionData session) throws Exception;
}



