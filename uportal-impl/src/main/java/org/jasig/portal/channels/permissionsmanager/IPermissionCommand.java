/**
 * Copyright (c) 2000-2009, Jasig, Inc.
 * See license distributed with this file and available online at
 * https://www.ja-sig.org/svn/jasig-parent/tags/rel-10/license-header.txt
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



