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


package  org.jasig.portal.services.entityproperties;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.util.ArrayList;

import org.jasig.portal.EntityIdentifier;
import org.jasig.portal.RDBMServices;
import org.jasig.portal.services.LogService;


/**
 * A portal RDBM based entity property store implementation
 *
 * @author Alex Vigdor av317@columbia.edu
 * @version $Revision$
 */
public class RDBMPropertyStore
        implements IEntityPropertyStore {
    protected final static String TABLE_NAME = "UP_ENTITY_PROP";
    protected final static String TYPE_COL = "ENTITY_TYPE_ID";
    protected final static String KEY_COL = "ENTITY_KEY";
    protected final static String NAME_COL = "PROPERTY_NAME";
    protected final static String VALUE_COL = "PROPERTY_VALUE";
    protected final static String DATE_COL = "LAST_MODIFIED";
    protected final static String selectPropertyNames = "SELECT " + NAME_COL
            + " FROM " + TABLE_NAME + " WHERE " + TYPE_COL + "=? AND " + KEY_COL
            + "=?";
    protected final static String selectProperty = "SELECT " + VALUE_COL +
            " FROM " + TABLE_NAME + " WHERE " + TYPE_COL + "=? AND " + KEY_COL
            + "=? AND " + NAME_COL + "=?";
    protected final static String deleteProperty = "DELETE FROM " + TABLE_NAME
            + " WHERE " + TYPE_COL + "=? AND " + KEY_COL + "=? AND " + NAME_COL
            + "=?";
    protected final static String insertProperty = "INSERT INTO " + TABLE_NAME
            + " VALUES (?,?,?,?,?)";

    public RDBMPropertyStore() {
    }

    public String[] getPropertyNames(EntityIdentifier entityID) {
        String[] rn = new String[0];
        ArrayList ar = new ArrayList();
        Connection conn = null;
        RDBMServices.PreparedStatement ps = null;
        try {
            conn = this.getConnection();
            ps = new RDBMServices.PreparedStatement(conn, selectPropertyNames);
            ps.clearParameters();
            ps.setInt(1, org.jasig.portal.EntityTypes.getEntityTypeID(entityID.getType()).intValue());
            ps.setString(2, entityID.getKey());
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                ar.add(rs.getString(NAME_COL));
            }
            rs.close();
            ps.close();
        } catch (Exception e) {
            LogService.log(LogService.ERROR, "RDBMPropertyStore.getPropertyNames: "
                    + ps);
            LogService.log(LogService.ERROR, e);
        } finally {
            this.releaseConnection(conn);
        }
        return  (String[])ar.toArray(rn);
    }

    public String getProperty(EntityIdentifier entityID, String name) {
        String r = null;
        Connection conn = null;
        RDBMServices.PreparedStatement ps = null;
        try {
            conn = this.getConnection();
            ps = new RDBMServices.PreparedStatement(conn, selectProperty);
            ps.clearParameters();
            ps.setInt(1, org.jasig.portal.EntityTypes.getEntityTypeID(entityID.getType()).intValue());
            ps.setString(2, entityID.getKey());
            ps.setString(3, name);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                r = rs.getString(VALUE_COL);
            }
            rs.close();
            ps.close();
        } catch (Exception e) {
            LogService.log(LogService.ERROR, "RDBMPropertyStore.getProperty "
                    + ps);
            LogService.log(LogService.ERROR, e);
        } finally {
            this.releaseConnection(conn);
        }
        return  r;
    }

    public void storeProperty(EntityIdentifier entityID, String name, String value) {
        this.unStoreProperty(entityID, name);
        Connection conn = null;
        RDBMServices.PreparedStatement ps = null;
        try {
            conn = this.getConnection();
            ps = new RDBMServices.PreparedStatement(conn, insertProperty);
            ps.clearParameters();
            ps.setInt(1, org.jasig.portal.EntityTypes.getEntityTypeID(entityID.getType()).intValue());
            ps.setString(2, entityID.getKey());
            ps.setString(3, name);
            ps.setString(4, value);
            ps.setTimestamp(5, new Timestamp(System.currentTimeMillis()));
            int i = ps.executeUpdate();
            //System.out.println(i+" "+ps.toString());
            ps.close();
        } catch (Exception e) {
            LogService.log(LogService.ERROR, "RDBMPropertyStore.storeProperty "
                    + ps);
            LogService.log(LogService.ERROR, e);
        } finally {
            this.releaseConnection(conn);
        }
    }

    public void unStoreProperty(EntityIdentifier entityID, String name) {
        Connection conn = null;
        RDBMServices.PreparedStatement ps = null;
        try {
            conn = this.getConnection();
            ps = new RDBMServices.PreparedStatement(conn, deleteProperty);
            ps.clearParameters();
            ps.setInt(1, org.jasig.portal.EntityTypes.getEntityTypeID(entityID.getType()).intValue());
            ps.setString(2, entityID.getKey());
            ps.setString(3, name);
            int i = ps.executeUpdate();
            //System.out.println(i+" "+ps.toString());
            ps.close();
        } catch (Exception e) {
            LogService.log(LogService.ERROR, "RDBMPropertyStore.unStoreProperty "
                    + ps);
            LogService.log(LogService.ERROR, e);
        } finally {
            this.releaseConnection(conn);
        }
    }

    protected Connection getConnection() {
        return  RDBMServices.getConnection();
    }

    protected void releaseConnection(Connection conn) {
        RDBMServices.releaseConnection(conn);
    }
}



