/* Copyright 2004 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/

package org.jasig.portal.rdbm;

import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.SQLException;

import javax.sql.DataSource;

import org.jasig.portal.RDBMServices;

/**
 * DataSource backed by RDBMServices.
 * This class allows clients to consume RDBMServices as the DataSource abstraction.
 * 
 * Depending on the constructor used either the default portal database or a named
 * portal database will be used.
 * 
 * @author andrew.petro@yale.edu, Eric Dalquist edalquist@unicon.net
 * @version $Revision$ $Date$
 */
public class RDBMServicesDataSource implements DataSource {
    private final String name;
    
    /**
     * Creates a new {@link DataSource} wrapper which will be backed by the
     * {@link DataSource} representing uPortal's default database.
     * 
     * @see RDBMServices#getDatabaseServer()
     */
    public RDBMServicesDataSource() {
        this.name = null;
    }

    /**
     * Creates a new {@link DataSource} wrapper which will be backed by the
     * {@link DataSource} representing the database with the specified name.
     * 
     * @param serverName The name of the database server to use to back this wrapper.
     * @see RDBMServices#getDatabaseServer(String)
     */
    public RDBMServicesDataSource(final String serverName) {
        this.name = serverName;
    }
    
    /**
     * Returns the server name being used. A value of <code>null</code> means the
     * default portal database server is being used.
     * 
     * @return The server name being used.
     */
    public String getServerName() {
        return this.name;
    }

    /**
     * @see javax.sql.DataSource#getLoginTimeout()
     */
    public int getLoginTimeout() throws SQLException {
        final DataSource ds = this.getDataSource();
        return ds.getLoginTimeout();
    }

    /**
     * @see javax.sql.DataSource#setLoginTimeout(int)
     */
    public void setLoginTimeout(final int seconds) throws SQLException {
        final DataSource ds = this.getDataSource();
        ds.setLoginTimeout(seconds);
    }

    /**
     * @see javax.sql.DataSource#getLogWriter()
     */
    public PrintWriter getLogWriter() throws SQLException {
        final DataSource ds = this.getDataSource();
        return ds.getLogWriter();
    }

    /**
     * @see javax.sql.DataSource#setLogWriter(java.io.PrintWriter)
     */
    public void setLogWriter(final PrintWriter out) throws SQLException {
        final DataSource ds = this.getDataSource();
        ds.setLogWriter(out);
    }

    /**
     * @see javax.sql.DataSource#getConnection()
     */
    public Connection getConnection() throws SQLException {
        final DataSource ds = this.getDataSource();
        return ds.getConnection();
    }

    /**
     * @see javax.sql.DataSource#getConnection(java.lang.String, java.lang.String)
     */
    public Connection getConnection(final String username, final String password) throws SQLException {
        final DataSource ds = this.getDataSource();
        return ds.getConnection(username, password);
    }
    
    /**
     * Gets the appropriate {@link IDatabaseServer} for the configured
     * server name then gets a {@link DataSource} from it.
     * 
     * Note that the {@link IDatabaseServer} and {@link DataSource} are not
     * cached on purpose. We are relying on {@link RDBMServices} to perform
     * any caching of these objects that is needed.
     * 
     * @return The appropriate {@link DataSource} for the server name.
     */
    protected DataSource getDataSource() {
        if (this.name != null) {
            final IDatabaseServer server = RDBMServices.getDatabaseServer(this.name);
            
            if (server == null) {
                throw new IllegalStateException("No IDatabaseServer was found for " + this.name);
            }
            
            return server.getDataSource();
        }
        else {
            final IDatabaseServer server = RDBMServices.getDatabaseServer();
            
            if (server == null) {
                throw new IllegalStateException("No default IDatabaseServer was found.");
            }
            
            return server.getDataSource();
        }
    }
}
