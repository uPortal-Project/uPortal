/* Copyright 2001 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/

package org.jasig.portal.services.entityproperties;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jasig.portal.EntityIdentifier;
import org.jasig.portal.RDBMServices;
import org.jasig.portal.services.EntityPropertyRegistry;


/**
 * A portal RDBM based entity property store implementation
 *
 * @author Alex Vigdor av317@columbia.edu
 * @version $Revision$
 */
public class RDBMPropertyStore
      implements IEntityPropertyStore {

    private static final Log log = LogFactory.getLog(RDBMPropertyStore.class);

   protected static Class propsType = null;
   protected final static String TABLE_NAME = "UP_ENTITY_PROP";
   protected final static String TYPE_COL = "ENTITY_TYPE_ID";
   protected final static String KEY_COL = "ENTITY_KEY";
   protected final static String NAME_COL = "PROPERTY_NAME";
   protected final static String VALUE_COL = "PROPERTY_VALUE";
   protected final static String DATE_COL = "LAST_MODIFIED";
   protected final static String selectProperties = "SELECT " + NAME_COL + ", " + VALUE_COL
         + " FROM " + TABLE_NAME + " WHERE " + TYPE_COL + "=? AND " + KEY_COL
         + "=?";
   protected final static String deleteProperty = "DELETE FROM " + TABLE_NAME
         + " WHERE " + TYPE_COL + "=? AND " + KEY_COL + "=? AND " + NAME_COL
         + "=?";
   protected final static String insertProperty = "INSERT INTO " + TABLE_NAME
         + " VALUES (?,?,?,?,?)";

   public RDBMPropertyStore() {
      try{
         if (propsType == null){
            propsType = Class.forName("org.jasig.portal.services.entityproperties.EntityProperties");
         }
      } catch (Exception e) {
         log.error( "RDBMPropertyStore.Constructor Unable to create propstype", e);
      }
   }

   public String[] getPropertyNames(EntityIdentifier entityID) {
      String[] propNames = null;
      EntityProperties ep = getCachedProperties(entityID);
      if (ep != null) {
         propNames = ep.getPropertyNames();
      }
      return propNames;
   }

   public String getProperty(EntityIdentifier entityID, String name) {
      String propVal = null;
      EntityProperties ep = getCachedProperties(entityID);
      if (ep != null) {
         propVal = ep.getProperty(name);
      }
      return propVal;
   }

   public void storeProperty(EntityIdentifier entityID, String name, String value) {
      this.unStoreProperty(entityID, name);
      Connection conn = null;
      PreparedStatement ps = null;
      try {
         conn = this.getConnection();
         ps = conn.prepareStatement(insertProperty);
         ps.clearParameters();
         ps.setInt(1, org.jasig.portal.EntityTypes.getEntityTypeID(entityID.getType()).intValue());
         ps.setString(2, entityID.getKey());
         ps.setString(3, name);
         ps.setString(4, value);
         ps.setTimestamp(5, new Timestamp(System.currentTimeMillis()));
         ps.executeUpdate();
         ps.close();
         clearCache(entityID);
      } catch (Exception e) {
         log.error( "RDBMPropertyStore.storeProperty "
                                          + ps, e);
      } finally {
         this.releaseConnection(conn);
      }
   }

   public void unStoreProperty(EntityIdentifier entityID, String name) {
      Connection conn = null;
      PreparedStatement ps = null;
      try {
         conn = this.getConnection();
         ps = conn.prepareStatement(deleteProperty);
         ps.clearParameters();
         ps.setInt(1, org.jasig.portal.EntityTypes.getEntityTypeID(entityID.getType()).intValue());
         ps.setString(2, entityID.getKey());
         ps.setString(3, name);
         ps.executeUpdate();
         ps.close();
         clearCache(entityID);
      } catch (Exception e) {
         log.error( "RDBMPropertyStore.unStoreProperty "
                                          + ps, e);
      } finally {
         this.releaseConnection(conn);
      }
   }

   protected Connection getConnection() {
      return RDBMServices.getConnection();
   }

   protected void releaseConnection(Connection conn) {
      RDBMServices.releaseConnection(conn);
   }

   protected EntityProperties getCachedProperties(EntityIdentifier entityID) {
      EntityProperties ep = EntityPropertyRegistry.instance().getCachedProperties(entityID);
      if (ep == null) {
         ep = new EntityProperties(entityID.getKey());
         Connection conn = null;
         PreparedStatement ps = null;
         try {
            conn = this.getConnection();
            ps = conn.prepareStatement(selectProperties);
            ps.clearParameters();
            ps.setInt(1, org.jasig.portal.EntityTypes.getEntityTypeID(entityID.getType()).intValue());
            ps.setString(2, entityID.getKey());
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
               ep.setProperty(rs.getString(NAME_COL), rs.getString(VALUE_COL));
            }
            addToCache(ep);
            rs.close();
            ps.close();
         } catch (Exception e) {
            log.error( "RDBMPropertyStore.getPropertyNames: " + ps, e);
         } finally {
            this.releaseConnection(conn);
         }
      }
      return ep;
   }

   protected void clearCache(EntityIdentifier entityID) {
      EntityPropertyRegistry.instance().clearCache(entityID);
   }

   protected void addToCache(EntityProperties ep) {
      EntityPropertyRegistry.instance().addToCache(ep);
   }

}
