/* Copyright 2001 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/

package  org.jasig.portal.channels.permissionsmanager;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;

import org.jasig.portal.IPermissible;
import org.jasig.portal.RDBMServices;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


/**
 * This class keeps track of IPermissible objects on behalf of CPermissionsManager,
 * and is used to register new classes that generate CPermissionsManagerServants
 *
 * It also include hard coded references to 3 known IPermissibles, but will gracefully
 * ignore them if they are not found
 */
public class RDBMPermissibleRegistry {
    private static final Log log = LogFactory.getLog(RDBMPermissibleRegistry.class);
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
        log.debug("PermissibleRegistryRDBM.init():: setting up registry");
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
                    if (log.isDebugEnabled())
                        log.debug("PermissibleRegistryRDBM(): Could not instantiate IPermissible "
                            + e);
                    unregister(classname);
                }
            }
        } catch (Exception e) {
            log.error(e, e);
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
            if (log.isDebugEnabled())
                log.debug("RDBMPermissibleRegistry.igetAllPermissible(): Unable to instantiate IPermissible "+e);
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
        if (log.isDebugEnabled())
            log.debug("PermissibleRegistryRDBM.registerpermissible():: processing "
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
                    log.error(e, e);
                } finally {
                    RDBMServices.closeStatement(st);
                    releaseConnection(conn);
                }
            } catch (Throwable th) {
                if (log.isDebugEnabled())
                    log.debug("PermissibleRegistryRDBM.registerPermissible(): error while registering "
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
            log.debug(e, e);
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



