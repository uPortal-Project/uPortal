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


package  org.jasig.portal.channels.permissionsmanager;

import  org.jasig.portal.*;
import  org.jasig.portal.security.*;
import  org.jasig.portal.security.provider.*;
import  org.jasig.portal.services.*;
import  org.jasig.portal.groups.*;
import  org.w3c.dom.Node;
import  org.w3c.dom.NodeList;
import  org.w3c.dom.Element;
import  org.w3c.dom.Text;
import  java.util.*;
import  java.sql.*;
import  org.apache.xml.serialize.XMLSerializer;
import  org.apache.xml.serialize.OutputFormat;
import  java.io.StringReader;
import  java.io.StringWriter;
import  org.apache.xerces.parsers.DOMParser;
import  org.apache.xerces.parsers.SAXParser;
import  org.apache.xerces.dom.DocumentImpl;


/**
 * The class responsible for generating the monolithic XML file for CPermissionsManager
 *
 * @author Alex Vigdor
 * @version $Revision$
 */
public class PermissionsXML {
    private static final String findPermissibles = "SELECT IPERMISSIBLE_CLASS FROM UPC_PERM_MGR";

    /** Creates new PermissionsXML */
    public PermissionsXML () {
    }

    /**
     * put your documentation comment here
     * @param rd
     * @param sd
     * @return
     */
    public static DocumentImpl generateViewDoc (ChannelRuntimeData rd, ChannelStaticData sd) {
        DocumentImpl rDoc = new DocumentImpl();
        IPermissible[] owners = new IPermissible[0];
        IAuthorizationPrincipal[] principals;
        try {
            Element root = rDoc.createElement("CPermissionsManager");
            rDoc.appendChild(root);
            if (sd.get("prmOwners") != null) {
                // use specified set of owners
                owners = (IPermissible[])sd.get("prmOwners");
            }
            else {
                // use owners found in DB
                owners = RDBMPermissibleRegistry.getAllPermissible();
            }
            for (int i = 0; i < owners.length; i++) {
                Element owner = rDoc.createElement("owner");
                owner.setAttribute("name", owners[i].getOwnerName());
                owner.setAttribute("token", owners[i].getOwnerToken());
                owner.setAttribute("ipermissible", owners[i].getClass().getName());
                String[] activities = owners[i].getActivityTokens();
                if (sd.get("prmActivities") != null) {
                    if (((HashMap)sd.get("prmActivities")).get(owners[i].getOwnerToken())
                            != null) {
                        activities = (String[])((HashMap)sd.get("prmActivities")).get(owners[i].getOwnerToken());
                    }
                }
                for (int j = 0; j < activities.length; j++) {
                    Element act = rDoc.createElement("activity");
                    act.setAttribute("token", activities[j]);
                    act.setAttribute("name", owners[i].getActivityName(activities[j]));
                    owner.appendChild(act);
                }
                String[] targets = owners[i].getTargetTokens();
                if (sd.get("prmTargets") != null) {
                    if (((HashMap)sd.get("prmTargets")).get(owners[i].getOwnerToken())
                            != null) {
                        targets = (String[])((HashMap)sd.get("prmTargets")).get(owners[i].getOwnerToken());
                    }
                }
                for (int k = 0; k < targets.length; k++) {
                    Element tgt = rDoc.createElement("target");
                    tgt.setAttribute("token", targets[k]);
                    tgt.setAttribute("name", owners[i].getTargetName(targets[k]));
                    owner.appendChild(tgt);
                }
                root.appendChild(owner);
            }
            //LogService.instance().log(LogService.DEBUG,"PermissionsManager:: checking for principals");
            if (sd.get("prmPrincipals") != null) {
                ArrayList ownerKeys = new ArrayList();
                for (int x = 0; x < owners.length; x++) {
                    ownerKeys.add(owners[x].getOwnerToken());
                }
                //LogService.instance().log(LogService.DEBUG,"PermissionsManager:: getting principals");
                IAuthorizationPrincipal[] aps = (IAuthorizationPrincipal[])sd.get("prmPrincipals");
                for (int m = 0; m < aps.length; m++) {
                    //LogService.instance().log(LogService.DEBUG,"PermissionsManager:: iterating over principals");
                    Element ppl = rDoc.createElement("principal");
                    ppl.setAttribute("token", aps[m].getPrincipalString());
                    ppl.setAttribute("type", aps[m].getType().getName());
                    String name = aps[m].getKey();
                    try {
                        name = EntityNameFinderService.instance().getNameFinder(aps[m].getType()).getName(name);
                    } catch (Exception e) {
                        LogService.instance().log(LogService.ERROR, e);
                    }
                    ppl.setAttribute("name", name);
                    IPermission[] pms = aps[m].getAllPermissions();
                    for (int n = 0; n < pms.length; n++) {
                        if (ownerKeys.contains(pms[n].getOwner())) {
                            Element perm = rDoc.createElement("permission");
                            perm.setAttribute("owner", pms[n].getOwner());
                            perm.setAttribute("principal", pms[n].getPrincipal());
                            perm.setAttribute("activity", pms[n].getActivity());
                            perm.setAttribute("type", pms[n].getType());
                            perm.setAttribute("target", pms[n].getTarget());
                            if (pms[n].getEffective() != null) {
                                perm.setAttribute("effective", pms[n].getEffective().toString());
                            }
                            if (pms[n].getExpires() != null) {
                                perm.setAttribute("expires", pms[n].getExpires().toString());
                            }
                            ppl.appendChild(perm);
                        }
                    }
                    root.appendChild(ppl);
                }
            }            /*
             StringWriter sw = new java.io.StringWriter();
             XMLSerializer serial = new XMLSerializer(sw, new OutputFormat(rDoc,"UTF-8",true));
             serial.serialize(rDoc);
             LogService.instance().log(LogService.DEBUG,"Permissions viewXMl ready:\n"+sw.toString());
             */

        } catch (Exception e) {
            LogService.instance().log(LogService.ERROR, e);
        }
        return  rDoc;
    }
}



