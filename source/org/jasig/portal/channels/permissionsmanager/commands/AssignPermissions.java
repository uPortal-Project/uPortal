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
 *
 * formatted with JxBeauty (c) johann.langhofer@nextra.at
 */


package  org.jasig.portal.channels.permissionsmanager.commands;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;

import org.jasig.portal.channels.permissionsmanager.CommandFactory;
import org.jasig.portal.channels.permissionsmanager.IPermissionCommand;
import org.jasig.portal.channels.permissionsmanager.PermissionsSessionData;
import org.jasig.portal.security.IPermission;
import org.jasig.portal.security.IUpdatingPermissionManager;
import org.jasig.portal.services.AuthorizationService;
import org.jasig.portal.services.LogService;
import org.w3c.dom.Element;


/**
 * An IPermissionCommand implementation that processes form data from the
 * CPermissionsManager matrix screen and records all permissions
 *
 * @author Alex Vigdor
 * @version $Revision$
 */
public class AssignPermissions
        implements IPermissionCommand {

    /** Creates new AssignPermissions */
    public AssignPermissions () {
    }

    /*
     *  expects to receive all permissions in form
     *  key['permission//{owner}|{principal}|{activity}|{target}'] = value['INHERIT','GRANT','DENY']
     */
    public void execute (PermissionsSessionData session) throws Exception {
        
            LogService.log(LogService.DEBUG, "PermissionsManager->AssignPermissions processing");
            Element root = session.XML.getDocumentElement();
            Enumeration formkeys = session.runtimeData.getParameterNames();
            HashMap owners = new HashMap();
            while (formkeys.hasMoreElements()) {
                String key = (String)formkeys.nextElement();
                if (key.indexOf("permission//") == 0) {
                    PermissionHolder ph = new PermissionHolder();
                    String split1 = key.substring(12);
                    ph.owner = split1.substring(0, split1.indexOf("|"));
                    String split2 = split1.substring(split1.indexOf("|") +
                            1);
                    ph.principal = split2.substring(0, split2.indexOf("|"));
                    String split3 = split2.substring(split2.indexOf("|") +
                            1);
                    ph.activity = split3.substring(0, split3.indexOf("|"));
                    ph.target = split3.substring(split3.indexOf("|") + 1);
                    ph.type = session.runtimeData.getParameter(key);
                    LogService.log(LogService.DEBUG, "Processing "
                            + ph.type + " permission o=" + ph.owner + " p="
                            + ph.principal + " a=" + ph.activity + " t=" +
                            ph.target);
                    if (!owners.containsKey(ph.owner)) {
                        owners.put(ph.owner, new ArrayList());
                    }
                    ((ArrayList)owners.get(ph.owner)).add(ph);
                }
            }
            String[] ownerkeys = (String[])owners.keySet().toArray(new String[0]);
            for (int i = 0; i < ownerkeys.length; i++) {
                String owner = ownerkeys[i];
                IUpdatingPermissionManager upm = AuthorizationService.instance().newUpdatingPermissionManager(owner);
                ArrayList phs = (ArrayList)owners.get(owner);
                IPermission[] ipsd = pHolder2DeleteArray(upm, phs);
                LogService.log(LogService.DEBUG, "removing " + String.valueOf(ipsd.length)
                        + " old permissions");
                upm.removePermissions(ipsd);
                IPermission[] ipsa = pHolder2AddArray(upm, phs);
                LogService.log(LogService.DEBUG, "adding " + String.valueOf(ipsa.length)
                        + " new permissions");
                upm.addPermissions(ipsa);
            }
            IPermissionCommand wrapit = CommandFactory.get("Cancel");
            wrapit.execute(session);
        
    }

    private class PermissionHolder {
        String owner;
        String principal;
        String activity;
        String target;
        String type;
    }

    private IPermission[] pHolder2DeleteArray (IUpdatingPermissionManager upm,
            ArrayList holders) {
        ArrayList rlist = new ArrayList();
        for (int i = 0; i < holders.size(); i++) {
            try {
                PermissionHolder ph = (PermissionHolder)holders.get(i);
                IPermission p = upm.newPermission(null);
                p.setPrincipal(ph.principal);
                p.setActivity(ph.activity);
                p.setTarget(ph.target);
                rlist.add(p);
            } catch (Exception e) {
                LogService.log(LogService.ERROR,e);
            }
        }
        return  (IPermission[])rlist.toArray(new IPermission[0]);
    }

    private IPermission[] pHolder2AddArray (IUpdatingPermissionManager upm,
            ArrayList holders) {
        ArrayList rlist = new ArrayList();
        for (int i = 0; i < holders.size(); i++) {
            try {
                PermissionHolder ph = (PermissionHolder)holders.get(i);
                IPermission p = upm.newPermission(null);
                p.setPrincipal(ph.principal);
                p.setActivity(ph.activity);
                p.setTarget(ph.target);
                if (ph.type.equals("GRANT")) {
                    p.setType("GRANT");
                    rlist.add(p);
                }
                else if (ph.type.equals("DENY")) {
                    p.setType("DENY");
                    rlist.add(p);
                }
            } catch (Exception e) {
                LogService.log(LogService.ERROR,e);
            }
        }
        return  (IPermission[])rlist.toArray(new IPermission[0]);
    }
}



