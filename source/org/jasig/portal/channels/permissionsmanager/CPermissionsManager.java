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

import  java.io.*;
import  java.util.*;
import  org.jasig.portal.*;
import  org.jasig.portal.services.*;
import  org.jasig.portal.utils.*;
import  org.jasig.portal.groups.*;
import  org.jasig.portal.security.*;
import  org.jasig.portal.security.provider.*;
import  org.jasig.portal.channels.groupsmanager.CGroupsManagerServantFactory;
import  org.apache.xerces.dom.DocumentImpl;
import  org.apache.xml.serialize.XMLSerializer;
import  org.apache.xml.serialize.OutputFormat;


/**
 * CPermissionsManager allows graphical administration of permissions for all owners
 * that have a representative implementation of IPermissible recorded in the table
 * UPC_PERM_MGR
 *
 * @author Alex Vigdor
 * @version $Revision$
 */
public class CPermissionsManager
        implements IChannel, ICacheable {
    protected ChannelRuntimeData runtimeData;
    protected ChannelStaticData staticData;
    protected final String sslLocation = "CPermissionsManager.ssl";
    long startRD;
    protected boolean isauthorized = false;

    /**
     * put your documentation comment here
     */
    public CPermissionsManager () {
    }

    /**
     * put your documentation comment here
     * @param rD
     */
    public void setRuntimeData (org.jasig.portal.ChannelRuntimeData rD) {
        startRD = Calendar.getInstance().getTime().getTime();
        this.runtimeData = rD;
        LogService.instance().log(LogService.DEBUG, "PermissionsManager - settting runtime data");
        // test if servant exists and has finished
        if (runtimeData.getParameter("prmCommand") != null) {
            IPermissionCommand pc = CommandFactory.get(runtimeData.getParameter("prmCommand"));
            if (pc != null) {
                pc.execute(runtimeData, staticData);
            }
        }
        if (staticData.get("prmPrincipals") == null) {
            IServant servant = null;
            LogService.instance().log(LogService.DEBUG, "PermissionsManager - Checking Servant");
            try {
                ChannelRuntimeData servantRD = runtimeData;
                if (staticData.get("prmServant") == null) {
                    LogService.instance().log(LogService.DEBUG, "PermissionsManager - creating new Servant");
                    servant = CGroupsManagerServantFactory.getGroupsServantforSelection(staticData,
                            "Select principals you would like to assign permissions to");
                    staticData.put("prmServant", servant);
                    servantRD = (ChannelRuntimeData)runtimeData.clone();
                    Enumeration srd = servantRD.keys();
                    while (srd.hasMoreElements()) {
                        servantRD.remove(srd.nextElement());
                    }
                }
                else {
                    LogService.instance().log(LogService.DEBUG, "PermissionsManager - using existing Servant");
                    servant = (IServant)staticData.get("prmServant");
                }
                ((IChannel)servant).setRuntimeData(servantRD);
                if (servant.isFinished()) {
                  try {
                    LogService.instance().log(LogService.DEBUG, "PermissionsManager - Getting servant results");
                    Object[] results = servant.getResults();
                    if (results != null && results.length > 0) {
                        IAuthorizationPrincipal[] iap = new IAuthorizationPrincipal[results.length];
                        for (int i = 0; i< results.length ; i++){
                          IGroupMember gm = (IGroupMember) results[i];
                          iap[i] = AuthorizationService.instance().newPrincipal(gm);
                        }
                        staticData.put("prmPrincipals", iap);
                        LogService.instance().log(LogService.DEBUG, "PermissionsManager - Getting rid of Servant");
                        staticData.remove("prmServant");
                    }
                    else {
                        LogService.instance().log(LogService.DEBUG, "PermissionsManager - Leaving servant, but setting prmFinished to True");
                        staticData.setParameter("prmFinished", "true");
                        runtimeData.setParameter("commandResponse", "You must select at least once principal to continue");
                    }
                  }
                  catch (Exception e) {
                    LogService.instance().log(LogService.ERROR, e);
                  }
                }
            } catch (Exception e) {
                LogService.instance().log(LogService.ERROR, e);
            }
        }
        if (staticData.get("prmPrincipals") == null) {
            runtimeData.setParameter("prmView", "Select Principals");
        }
        else if (staticData.get("prmOwners") == null) {
            runtimeData.setParameter("prmView", "Select Owners");
        }
        else if (staticData.get("prmActivities") == null) {
            runtimeData.setParameter("prmView", "Select Activities");
        }
        else if (staticData.get("prmTargets") == null) {
            runtimeData.setParameter("prmView", "Select Targets");
        }
        else if (staticData.getParameter("prmView") != null) {
            runtimeData.setParameter("prmView", staticData.getParameter("prmView"));
        }
        else if ((runtimeData.get("prmView") == null) || (runtimeData.get("prmView").equals(""))) {
            runtimeData.setParameter("prmView", "Assign By Principal");
        }
    }

    /**
     * put your documentation comment here
     * @param portalEvent
     */
    public void receiveEvent (org.jasig.portal.PortalEvent portalEvent) {}

    /**
     * put your documentation comment here
     * @return
     */
    public org.jasig.portal.ChannelRuntimeProperties getRuntimeProperties () {
        return  new ChannelRuntimeProperties();
    }

    /**
     * put your documentation comment here
     * @param out
     */
    public void renderXML (org.xml.sax.ContentHandler out) {
        try {
            long time1 = Calendar.getInstance().getTime().getTime();
            if (runtimeData.getParameter("prmView").equals("Select Principals") &&
                    isauthorized) {
                try {
                    LogService.instance().log(LogService.DEBUG, "PermissionsManager - Calling servant renderXML");
                    IChannel servant = (IChannel)staticData.get("prmServant");
                    servant.renderXML(out);
                } catch (Exception e) {
                    LogService.instance().log(LogService.ERROR, "CPermissionsManager: failed to use servant"
                            + e);
                    hackPrincipals();
                }
            }
            if (!runtimeData.getParameter("prmView").equals("Select Principals")
                    || !isauthorized) {
                if (staticData.get("prmViewDoc") == null) {
                    staticData.put("prmViewDoc", PermissionsXML.generateViewDoc(runtimeData,
                            staticData));
                }
                DocumentImpl viewDoc = (DocumentImpl)staticData.get("prmViewDoc");
                /*
                 StringWriter sw = new java.io.StringWriter();
                 XMLSerializer serial = new XMLSerializer(sw, new OutputFormat(viewDoc,"UTF-8",true));
                 serial.serialize(viewDoc);
                 LogService.instance().log(LogService.DEBUG,"CPermissionsManager view XML:\n"+sw.toString());
                 */
                long time2 = Calendar.getInstance().getTime().getTime();
                XSLT xslt = new XSLT(this);
                xslt.setXML(viewDoc);
                xslt.setTarget(out);
                xslt.setStylesheetParameter("baseActionURL", runtimeData.getBaseActionURL());
                xslt.setStylesheetParameter("prmView", runtimeData.getParameter("prmView"));
                if (runtimeData.get("commandResponse") != null) {
                    xslt.setStylesheetParameter("commandResponse", runtimeData.getParameter("commandResponse"));
                }
                xslt.setXSL(sslLocation, "CPermissions", runtimeData.getBrowserInfo());
                transform(xslt);
                long time3 = Calendar.getInstance().getTime().getTime();
                LogService.instance().log(LogService.DEBUG, "CPermissionsManager timer: "
                        + String.valueOf((time3 - time1)) + " ms total, xsl took "
                        + String.valueOf((time3 - time2)) + " ms for view " + runtimeData.getParameter("prmView"));
                LogService.instance().log(LogService.DEBUG, "CPermissionsManager timer: "
                        + String.valueOf((time3 - startRD)) + " since start RD");
            }
        } catch (Exception e) {
            LogService.instance().log(LogService.ERROR, e);
        }
    }

    /**
     * put your documentation comment here
     * @param xslt
     */
    protected void transform (XSLT xslt) {
        try {
            if (isauthorized) {
                xslt.setStylesheetParameter("isAdminUser", "true");
            }
            xslt.transform();
        } catch (Exception e) {
            LogService.instance().log(LogService.ERROR, e);
        }
    }

    /**
     * put your documentation comment here
     * @param sD
     */
    public void setStaticData (org.jasig.portal.ChannelStaticData sD) {
        this.staticData = sD;
        try {
            IEntityGroup admin = GroupService.getDistinguishedGroup(GroupService.PORTAL_ADMINISTRATORS);
            IGroupMember me = AuthorizationService.instance().getGroupMember(staticData.getAuthorizationPrincipal());
            if (admin.deepContains(me)) {
                isauthorized = true;
            }
        } catch (Exception e) {
            LogService.instance().log(LogService.ERROR, e);
        }
        staticData.setParameter("prmFinished", "false");
    }

    /**
     * put your documentation comment here
     */
    private void hackPrincipals () {
        //temporary hack until Groups Manager is ready to provide runtime principal selection
        try {
            ArrayList princs = new ArrayList();
            AuthorizationService as = AuthorizationService.instance();
            IAuthorizationPrincipal ap = as.newPrincipal("1", Class.forName("org.jasig.portal.security.IPerson"));
            princs.add(ap);
            IAuthorizationPrincipal ap2 = as.newPrincipal("2", Class.forName("org.jasig.portal.security.IPerson"));
            princs.add(ap2);
            IAuthorizationPrincipal ap3 = as.newPrincipal("0", Class.forName("org.jasig.portal.groups.IEntityGroup"));
            princs.add(ap3);
            IAuthorizationPrincipal ap4 = as.newPrincipal("4", Class.forName("org.jasig.portal.groups.IEntityGroup"));
            princs.add(ap4);
            staticData.put("prmPrincipals", princs.toArray(new IAuthorizationPrincipal[0]));
            staticData.setParameter("prmView", "Select Owners");
        } catch (Exception e) {
            LogService.instance().log(LogService.ERROR, e);
        }
    }

    /**
     * put your documentation comment here
     * @return
     */
    public ChannelCacheKey generateKey () {
        ChannelCacheKey cck;
        if (staticData.get("prmServant") == null) {
            cck = new ChannelCacheKey();
            cck.setKey(staticData.getChannelPublishId()+"-"+staticData.getChannelSubscribeId() + "-" + String.valueOf(staticData.getPerson().getID()));
            cck.setKeyValidity(runtimeData.getParameter("prmView"));
            LogService.instance().log(LogService.DEBUG, "CPermissionsManager.generateKey() : set validity to "
                    + runtimeData.getParameter("prmView"));
        }
        else {
            cck = ((ICacheable)staticData.get("prmServant")).generateKey();
        }
        return  cck;
    }

    /**
     * put your documentation comment here
     * @param validity
     * @return
     */
    public boolean isCacheValid (Object validity) {
        boolean valid = false;
        if (staticData.get("prmServant") == null) {
            if (validity != null) {
                if (validity.equals(runtimeData.getParameter("prmView")) && runtimeData.get("commandResponse")
                        == null) {
                    valid = true;
                }
            }
            long time3 = Calendar.getInstance().getTime().getTime();
            LogService.instance().log(LogService.DEBUG, "CPermissionsManager.isCacheValid() time since setRD: "
                    + String.valueOf((time3 - startRD)) + ", valid=" + valid);
        }
        else {
            valid = ((ICacheable)staticData.get("prmServant")).isCacheValid(validity);
        }
        return  valid;
    }
}



