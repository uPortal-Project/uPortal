/* Copyright 2001 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/

package org.jasig.portal.channels.permissionsmanager.commands;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;

import org.jasig.portal.channels.permissionsmanager.IPermissionCommand;
import org.jasig.portal.channels.permissionsmanager.PermissionsSessionData;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
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
    private static final Log log = LogFactory.getLog(SelectTargets.class);
    
    /** Creates new SelectTargets */
    public SelectTargets() {
    }

    public void execute(PermissionsSessionData session) throws Exception{
            log.debug("PermissionsManager->Selecttargets processing");
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
