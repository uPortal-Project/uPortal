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
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;

import org.jasig.portal.channels.permissionsmanager.IPermissionCommand;
import org.jasig.portal.channels.permissionsmanager.PermissionsSessionData;
import org.jasig.portal.services.LogService;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * An IPermissionCommand implementation that processes target selection
 * for CPermissionsManager
 *
 * @author Alex Vigdor
 * @version $Revision$
 */

public class SelectTargets implements IPermissionCommand {

    /** Creates new SelectTargets */
    public SelectTargets() {
    }

    public void execute(PermissionsSessionData session) throws Exception{
            LogService.log(LogService.DEBUG,"PermissionsManager->Selecttargets processing");
            boolean foundOne = false;
            Element root = session.XML.getDocumentElement();
            Enumeration formkeys = session.runtimeData.getParameterNames();
            HashMap ownerTgts = new HashMap();
            while (formkeys.hasMoreElements()) {
                String key = (String)formkeys.nextElement();
                if (key.indexOf("target//") == 0) {
                    String split1 = key.substring(8);
                    String owner = split1.substring(0, split1.indexOf("|"));
                    String target = split1.substring(split1.indexOf("|") +
                            1);
                    if (!ownerTgts.containsKey(owner)) {
                        ownerTgts.put(owner, new ArrayList());
                    }
                    ((ArrayList)ownerTgts.get(owner)).add(target);
                    foundOne = true;
                }
            }
            if (foundOne) {
                NodeList owners = session.XML.getElementsByTagName("owner");
                for (int i = 0; i < owners.getLength(); i++) {
                    Element owner = (Element)owners.item(i);
                    String ownertoken = owner.getAttribute("ipermissible");
                    if (ownerTgts.containsKey(ownertoken)) {
                        NodeList ownerkids = owner.getElementsByTagName("target");
                        int kids = ownerkids.getLength();
                        for (int j = kids - 1; j >= 0; j--) {
                            Element tgt = (Element)ownerkids.item(j);
                            String tgttoken = tgt.getAttribute("token");
                            if (((ArrayList)ownerTgts.get(ownertoken)).contains(tgttoken)) {
                                tgt.setAttribute("selected","true");
                            }
                            else {
                                tgt.setAttribute("selected","false");
                                //owner.removeChild(tgt);
                            }
                        }
                    }
                    else {
                        //session.XML.getDocumentElement().removeChild(owners.item(i));
                    }
                }
                session.gotTargets=true;
                //sd.setParameter("prmTargets", "finished");
            }
            else {
                session.runtimeData.setParameter("commandResponse", "You must select at least one target to continue");
            }
    }

}
