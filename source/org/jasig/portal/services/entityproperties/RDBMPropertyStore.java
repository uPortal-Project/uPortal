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
         int i = ps.executeUpdate();
         //System.out.println(i+" "+ps.toString());
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
         int i = ps.executeUpdate();
         //System.out.println(i+" "+ps.toString());
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
