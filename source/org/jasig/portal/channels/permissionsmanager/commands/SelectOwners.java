/* Copyright 2001 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/

package org.jasig.portal.channels.permissionsmanager.commands;
import java.util.ArrayList;
import java.util.Enumeration;

import org.jasig.portal.channels.permissionsmanager.IPermissionCommand;
import org.jasig.portal.channels.permissionsmanager.PermissionsSessionData;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * An IPermissionCommand implementation that processes owner selection
 * for CPermissionsManager
 *
 * @author Alex Vigdor
 * @version $Revision$
 */

public class SelectOwners implements IPermissionCommand {
    private static final Log log = LogFactory.getLog(SelectOwners.class);
    
    
    /** Creates new SelectOwners */
    public SelectOwners() {
    }

    public void execute(PermissionsSessionData session) {
            log.debug("PermissionsManager->SelectOwners processing");
            boolean foundOne = false;
            Element root = session.XML.getDocumentElement();
            Enumeration formkeys = session.runtimeData.getParameterNames();
            ArrayList ownerkeys = new ArrayList();
            while (formkeys.hasMoreElements()) {
                String key = (String)formkeys.nextElement();
                if (log.isInfoEnabled())
                    log.info("checking key " + key);
                if (key.indexOf("owner//") == 0) {
                    String owner = key.substring(7);
                    ownerkeys.add(owner);
                    foundOne = true;
                }
            }
            if (foundOne) {
                NodeList owners = session.XML.getElementsByTagName("owner");
                for (int i = owners.getLength()-1; i >=0; i--) {
                    Element owner = (Element)owners.item(i);
                    String ownertoken = owner.getAttribute("ipermissible");
                    if (ownerkeys.contains(ownertoken)) {
                        owner.setAttribute("selected","true");
                    }
                    else {
                        owner.setAttribute("selected","false");
                        //session.XML.getDocumentElement().removeChild(owner);
                    }
                }
                session.gotOwners = true;
                //sd.setParameter("prmOwners", "finished");
            }
            else {
                session.runtimeData.setParameter("commandResponse", "You must select at least one owner to continue");
            }
    }

}
