/* Copyright 2004 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/

package org.jasig.portal.rdbm.pool;

import javax.sql.DataSource;

import org.apache.commons.dbcp.BasicDataSource;


/**
 * @author Eric Dalquist <a href="mailto:edalquist@unicon.net">edalquist@unicon.net</a>
 * @version $Revision$ $Date$
 */
public class DBCPDataSourceFactory implements IPooledDataSourceFactory {

    /**
     * @see org.jasig.portal.rdbm.pool.IPooledDataSourceFactory#createPooledDataSource(java.lang.String, java.lang.String, java.lang.String, java.lang.String, int, int, int)
     */
    public DataSource createPooledDataSource(final String driverClassName,
                                             final String userName,
                                             final String password,
                                             final String url) {
        
        final BasicDataSource ds = new BasicDataSource();
        
        ds.setDriverClassName(driverClassName);
        ds.setUsername(userName);
        ds.setPassword(password);
        ds.setUrl(url);
        
        //TODO Create a properties file to for DBCP
        ds.setMaxActive(100);
        ds.setMaxIdle(30);
        ds.setMaxWait(10000);
        
        return ds;
    }
}
