/* Copyright 2004 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/

package org.jasig.portal.rdbm.pool;

import javax.sql.DataSource;


/**
 * @author Eric Dalquist <a href="mailto:edalquist@unicon.net">edalquist@unicon.net</a>
 * @version $Revision$ $Date$
 */
public interface IPooledDataSourceFactory {

    /**
     * Creates a {@link DataSource} that is backed by a connection pool.
     *
     * @param driverClassName The class name of the jdbc driver to use.
     * @param userName The username to connect to the database with.
     * @param password The password to connect to the database with.
     * @param url The url to locate the database with.
     * @param poolPreparedStatements Should prepared statements be pooled
     * @return A {@link DataSource} that is backed by a connection pool.
     */
    public DataSource createPooledDataSource(String driverClassName, String userName, String password, String url, boolean poolPreparedStatements);
}
