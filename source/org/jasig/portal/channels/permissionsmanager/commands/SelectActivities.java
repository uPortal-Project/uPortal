/* Copyright 2001 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/

package  org.jasig.portal.channels.permissionsmanager.commands;

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
 * An IPermissionCommand implementation that processes activity selection
 * for CPermissionsManager
 *
 * @author Alex Vigdor
 * @version $Revision$ $Date$
 */

public class SelectActivities
        implements IPermissionCommand {
    private static final Log log = LogFactory.getLog(SelectActivities.class);
    
    /** Creates new SelectActivities */
    public SelectActivities () {
    }

    public void execute (PermissionsSessionData session) throws Exception{
            log.debug("PermissionsManager->SelectActivities processing");
            boolean foundOne = false;
            Element root = session.XML.getDocumentElement();
            Enumeration formkeys = session.runtimeData.getParameterNames();
            HashMap ownerActs = new HashMap();
            while (formkeys.hasMoreElements()) {
                String key = (String)formkeys.nextElement();
                if (log.isInfoEnabled())
                    log.info("checking key " + key);
                if (key.indexOf("activity//") == 0) {
                    String split1 = key.substring(10);
                    String owner = split1.substring(0, split1.indexOf("|"));
                    String activity = split1.substring(split1.indexOf("|") +
                            1);
                    if (!ownerActs.containsKey(owner)) {
                        ownerActs.put(owner, new ArrayList());
                    }
                    ((ArrayList)ownerActs.get(owner)).add(activity);
                    foundOne = true;
                }
            }
            if (foundOne) {
                NodeList owners = session.XML.getElementsByTagName("owner");
                for (int i = 0; i < owners.getLength(); i++) {
                    Element owner = (Element)owners.item(i);
                    String ownertoken = owner.getAttribute("ipermissible");
                    if (ownerActs.containsKey(ownertoken)) {
                        NodeList ownerkids = owner.getElementsByTagName("activity");
                        int kids = ownerkids.getLength();
                        for (int j = kids - 1; j >= 0; j--) {
                            Element act = (Element)ownerkids.item(j);
                            String acttoken = act.getAttribute("token");
                            if (((ArrayList)ownerActs.get(ownertoken)).contains(acttoken)) {
                               act.setAttribute("selected","true");
                            }
                            else {
                                act.setAttribute("selected","false");
                                //owner.removeChild(act);
                            }
                        }
                    }
                    else {
                        //session.XML.getDocumentElement().removeChild(owners.item(i));
                    }
                }
                session.gotActivities=true;
            }
            else {
                session.runtimeData.setParameter("commandResponse", "You must select at least one activity to continue");
            }
    }
}



