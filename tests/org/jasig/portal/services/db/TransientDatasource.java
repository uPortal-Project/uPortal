/* Copyright 2004 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/

package org.jasig.portal.services.db;

import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.SQLException;

import javax.sql.DataSource;

import org.apache.commons.dbcp.BasicDataSource;

/**
 * A DataSource implementation backed by an in-memory HSQLDb instance,
 * suitable for implementing testcases for DataSource-consuming DAO impls.
 * @author andrew.petro@yale.edu
 * @version $Revision$ $Date$
 */
public class TransientDatasource implements DataSource {
    
    private DataSource delegate;
    
    public TransientDatasource() {
        
        BasicDataSource basicDataSource = new BasicDataSource();
        basicDataSource.setMaxActive(1);
        basicDataSource.setInitialSize(1);
        basicDataSource.setDriverClassName("org.hsqldb.jdbcDriver");
        basicDataSource.setUrl("jdbc:hsqldb:mem:adhommemds");
        basicDataSource.setMaxIdle(0);
        
        this.delegate = basicDataSource;

    }
    
    /* (non-Javadoc)
     * @see javax.sql.DataSource#getConnection()
     */
    public Connection getConnection() throws SQLException {
        return this.delegate.getConnection();
    }

    public Connection getConnection(String arg0, String arg1) throws SQLException {
        return this.delegate.getConnection();
    }

    /* (non-Javadoc)
     * @see javax.sql.DataSource#getLogWriter()
     */
    public PrintWriter getLogWriter() throws SQLException {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see javax.sql.DataSource#getLoginTimeout()
     */
    public int getLoginTimeout() throws SQLException {
        // TODO Auto-generated method stub
        return 0;
    }

    /* (non-Javadoc)
     * @see javax.sql.DataSource#setLogWriter(java.io.PrintWriter)
     */
    public void setLogWriter(PrintWriter arg0) throws SQLException {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see javax.sql.DataSource#setLoginTimeout(int)
     */
    public void setLoginTimeout(int arg0) throws SQLException {
        // TODO Auto-generated method stub
        
    }

}