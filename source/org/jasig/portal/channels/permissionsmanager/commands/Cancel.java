/**
 * Copyright © 2001 The JA-SIG Collaborative.  All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. Redistributions of any form whatsoever must retain the following
 *    acknowledgment:
 *    "This product includes software developed by the JA-SIG Collaborative
 *    (http://www.jasig.org/)."
 *
 * THIS SOFTWARE IS PROVIDED BY THE JA-SIG COLLABORATIVE "AS IS" AND ANY
 * EXPRESSED OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL THE JA-SIG COLLABORATIVE OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
 * NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 * OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 */

package org.jasig.portal.channels.permissionsmanager.commands;
import org.jasig.portal.channels.permissionsmanager.IPermissionCommand;
import org.jasig.portal.channels.permissionsmanager.PermissionsSessionData;
import org.jasig.portal.services.LogService;

/**
 * An IPermissionCommand implementation that resets CPermissionsManager
 * and sets the prmFinished flag in staticData for IServant operations
 *
 * @author Alex Vigdor
 * @version $Revision$
 */

public class Cancel implements IPermissionCommand {

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
        LogService.log(LogService.DEBUG,"PermissionsManager.Cancel complete");
    }

}
