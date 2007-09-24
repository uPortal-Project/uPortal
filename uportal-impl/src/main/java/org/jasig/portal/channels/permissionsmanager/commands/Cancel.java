/* Copyright 2001 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/

package org.jasig.portal.channels.permissionsmanager.commands;
import org.jasig.portal.channels.permissionsmanager.IPermissionCommand;
import org.jasig.portal.channels.permissionsmanager.PermissionsSessionData;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * An IPermissionCommand implementation that resets CPermissionsManager
 * and sets the prmFinished flag in staticData for IServant operations
 *
 * @author Alex Vigdor
 * @version $Revision$
 */

public class Cancel implements IPermissionCommand {
    private static final Log log = LogFactory.getLog(Cancel.class);
    
    /** Creates new StartOver */
    public Cancel() {
    }

    public void execute(PermissionsSessionData session) throws Exception{
        session.XML=null;
        //sd.remove("prmViewDoc");
        session.gotOwners = false;
        //session.owners = null;
        //sd.remove("prmOwners");
        session.gotActivities = false;
        //sd.remove("prmActivities");
        // re-instate when this funcion is available
        session.principals=null;
        session.owners = null;
        //sd.remove("prmPrincipals");
        session.servant = null;
        //sd.remove("prmServant");
        session.gotTargets=false;
        //sd.remove("prmTargets");
        session.isFinished=true;
        session.view = null;
        //sd.setParameter("prmFinished","true");
        log.debug("PermissionsManager.Cancel complete");
    }

}
