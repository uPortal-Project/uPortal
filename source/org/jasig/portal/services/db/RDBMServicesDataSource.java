/* Copyright 2004 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/

package org.jasig.portal.services.db;

import java.sql.Connection;
import java.sql.SQLException;

import javax.sql.DataSource;

import org.jasig.portal.RDBMServices;

import org.springframework.jdbc.datasource.AbstractDataSource;

/**
 * DataSource backed by RDBMServices.
 * This class allows clients to consume RDBMServices as the DataSource abstraction.
 * I thought about but did not implement returning Connection objects that
 * wrapped actual Connections and performed callback to 
 * RDBMServices.releaseConnection() on connection.close().  This didn't seem
 * to add anything worth having.
 * @author andrew.petro@yale.edu
 * @version $Revision$ $Date$
 */
public class RDBMServicesDataSource extends AbstractDataSource 
         implements DataSource {

    /**
     * The name of the RDBMServices database to which we map.
     */
    private String dbName;
    
    /* (non-Javadoc)
     * @see javax.sql.DataSource#getConnection()
     */
    public Connection getConnection() throws SQLException {
        if (this.dbName == null) {
            return RDBMServices.getConnection();
        }
        return RDBMServices.getConnection(this.dbName);
     }

    /* (non-Javadoc)
     * @see javax.sql.DataSource#getConnection(java.lang.String, java.lang.String)
     */
    public Connection getConnection(String username, String password) throws SQLException {
        return getConnection();
    }

    /**
     * @return Returns the resourceRef.
     */
    public String getDbName() {
        return this.dbName;
    }
    
    /**
     * Set a database name to pass to RDBMServices when we get connections.
     * @param dbName The name of the RDBMServices database to reference.
     */
    public void setDbName(String dbName) {
        this.dbName = dbName;
    }
}
