/**
 * Licensed to Jasig under one or more contributor license
 * agreements. See the NOTICE file distributed with this work
 * for additional information regarding copyright ownership.
 * Jasig licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a
 * copy of the License at:
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
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
 * @deprecated All IChannel implementations should be migrated to portlets
 */
@Deprecated
public class SelectOwners implements IPermissionCommand {
    private static final Log log = LogFactory.getLog(SelectOwners.class);
    
    
    /** Creates new SelectOwners */
    public SelectOwners() {
    }

    public void execute(PermissionsSessionData session) {
            log.debug("PermissionsManager->SelectOwners processing");
            boolean foundOne = false;
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
