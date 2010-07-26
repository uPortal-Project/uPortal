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

package org.jasig.portal.services.entityproperties;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
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
         try {
        	 ps.clearParameters();
        	 ps.setInt(1, org.jasig.portal.EntityTypes.getEntityTypeID(entityID.getType()).intValue());
        	 ps.setString(2, entityID.getKey());
        	 ps.setString(3, name);
        	 ps.setString(4, value);
        	 ps.setTimestamp(5, new Timestamp(System.currentTimeMillis()));
        	 ps.executeUpdate();
         } finally {
        	 close(ps);
         }
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
         try {
        	 ps.clearParameters();
        	 ps.setInt(1, org.jasig.portal.EntityTypes.getEntityTypeID(entityID.getType()).intValue());
        	 ps.setString(2, entityID.getKey());
        	 ps.setString(3, name);
        	 ps.executeUpdate();
         } finally {
        	close(ps);
         }
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
            try {
            	ps.clearParameters();
            	ps.setInt(1, org.jasig.portal.EntityTypes.getEntityTypeID(entityID.getType()).intValue());
            	ps.setString(2, entityID.getKey());
            	ResultSet rs = ps.executeQuery();
            	try {
            		while (rs.next()) {
            			ep.setProperty(rs.getString(NAME_COL), rs.getString(VALUE_COL));
            		}
            		addToCache(ep);
            	} finally {
            		close(rs);
            	}
            } finally {
            	close(ps);
            }
         } catch (Exception e) {
            log.error( "RDBMPropertyStore.getCachedProperties: " + ps, e);
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

	private static final void close(final Statement statement) {
		try {
			statement.close();
		} catch (SQLException e) {
			log.warn("problem closing statement", e);
		}
	}

	private static final void close(final ResultSet resultset) {
		try {
			resultset.close();
		} catch (SQLException e) {
			log.warn("problem closing resultset", e);
		}
	}

}
