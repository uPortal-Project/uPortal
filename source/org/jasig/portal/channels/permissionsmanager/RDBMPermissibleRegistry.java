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

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;

import org.jasig.portal.IPermissible;
import org.jasig.portal.RDBMServices;
import org.jasig.portal.services.LogService;


/**
 * This class keeps track of IPermissible objects on behalf of CPermissionsManager,
 * and is used to register new classes that generate CPermissionsManagerServants
 *
 * It also include hard coded references to 3 known IPermissibles, but will gracefully
 * ignore them if they are not found
 */
public class RDBMPermissibleRegistry {
    private static RDBMPermissibleRegistry _instance;
    private HashMap owners = new HashMap();
    private static final String findPermissibles = "SELECT IPERMISSIBLE_CLASS FROM UPC_PERM_MGR";

    /**
     * put your documentation comment here
     */
    protected RDBMPermissibleRegistry () {
        init();
    }

    /**
     * put your documentation comment here
     */
    private void init () {
        LogService.log(LogService.DEBUG, "PermissibleRegistryRDBM.init():: setting up registry");
        Connection conn = null;
        Statement st = null;
        ResultSet rs = null;
        try {
            conn = getConnection();
            st = conn.createStatement();
            rs = st.executeQuery(findPermissibles);
            while (rs.next()) {
                String classname = rs.getString(1);
                try {
                    Class newowner = Class.forName(classname);
                    owners.put(classname, newowner);
                } catch (Exception e) {
                    LogService.log(LogService.DEBUG, "PermissibleRegistryRDBM(): Could not instantiate IPermissible "
                            + e);
                    unregister(classname);
                }
            }
        } catch (Exception e) {
            LogService.log(LogService.ERROR, e);
        } finally {
            RDBMServices.closeResultSet(rs); 
            RDBMServices.closeStatement(st); 
            releaseConnection(conn);
        }
        registerKnownPermissibles();
    }

    /**
     * put your documentation comment here
     * 
     * @return a <code>RDBMPermissibleRegistry</code> singleton
     */
    public static synchronized RDBMPermissibleRegistry instance () {
        if (_instance == null) {
            _instance = new RDBMPermissibleRegistry();
        }
        return  _instance;
    }

    /**
     * put your documentation comment here
     * @return an array of <code>IPermissible</code>
     */
    public static IPermissible[] getAllPermissible () {
        return  instance().igetAllPermissible();
    }

    /**
     * put your documentation comment here
     * @return an array of <code>IPermissible</code>
     */
    private IPermissible[] igetAllPermissible () {
        //IPermissible[] ips = new IPermissible[owners.size()];
        ArrayList prms = new ArrayList();
        Class[] pclasses = (Class[]) owners.values().toArray(new Class[owners.size()]);
        for (int i=0; i<owners.size();i++){
          try{
            IPermissible ip = (IPermissible) pclasses[i].newInstance();
            prms.add(ip);
          }
          catch (Exception e){
            LogService.log(LogService.DEBUG,"RDBMPermissibleRegistry.igetAllPermissible(): Unable to instantiate IPermissible "+e);
          }
        }
        return  (IPermissible[])prms.toArray(new IPermissible[prms.size()]);
    }

    /*
     * hard coded known included Permissibles
     */
    private void registerKnownPermissibles () {
        iregisterPermissible("org.jasig.portal.channels.permissionsmanager.PermissiblePublishChannels");
        iregisterPermissible("org.jasig.portal.channels.permissionsmanager.PermissibleSubscribeChannels");
        iregisterPermissible("org.jasig.portal.channels.groupsmanager.CGroupsManager");
    }

    /**
     * put your documentation comment here
     * @param classname
     */
    public static void registerPermissible (String classname) {
        instance().iregisterPermissible(classname);
    }

    /**
     * put your documentation comment here
     * @param classname
     */
    protected void iregisterPermissible (String classname) {
        LogService.log(LogService.DEBUG, "PermissibleRegistryRDBM.registerpermissible():: processing "
                + classname);
        if (!owners.containsKey(classname)) {
            try {
                IPermissible ip = (IPermissible)Class.forName(classname).newInstance();
                Connection conn = null;
                Statement st = null;
                try {
                    conn = getConnection();
                    st = conn.createStatement();
                    st.executeUpdate("INSERT INTO UPC_PERM_MGR VALUES('" + classname
                            + "')");
                    owners.put(classname, Class.forName(classname));
                } catch (Exception e) {
                    LogService.log(LogService.ERROR, e);
                } finally {
                    RDBMServices.closeStatement(st);
                    releaseConnection(conn);
                }
            } catch (Throwable th) {
                LogService.log(LogService.DEBUG, "PermissibleRegistryRDBM.registerPermissible(): error while registering "
                        + classname + " : " + th);
            }
        }
    }

    /**
     * put your documentation comment here
     * @param permissibleClass
     */
    private void unregister (String permissibleClass) {
        Connection conn = null;
        Statement st = null;
        try {
            conn = getConnection();
            st = conn.createStatement();
            st.executeUpdate("DELETE FROM UPC_PERM_MGR WHERE IPERMISSIBLE_CLASS like '"
                    + permissibleClass + "'");
            owners.remove(permissibleClass);
        } catch (Exception e) {
            LogService.log(LogService.DEBUG, e);
        } finally {
            RDBMServices.closeStatement(st);
            releaseConnection(conn);
        }
    }

    /**
     * put your documentation comment here
     * @return
     */
    protected Connection getConnection () {
        return  RDBMServices.getConnection();
    }

    /**
     * put your documentation comment here
     * @param conn
     */
    protected void releaseConnection (Connection conn) {
        RDBMServices.releaseConnection(conn);
    }
}



