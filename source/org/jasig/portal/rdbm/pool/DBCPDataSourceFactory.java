/* Copyright 2004 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/

package org.jasig.portal.rdbm.pool;

import javax.sql.DataSource;

import org.apache.commons.dbcp.BasicDataSource;


/**
 * DBCPDataSourceFactory produces an instance of {@link BasicDataSource} 
 * for the given driver class name, username, password, and URL, defaulting
 * the maxActive, maxIdle, and maxWait properties.
 * 
 * This class is final because it is not designed to be subclassed.
 * 
 * @author Eric Dalquist <a href="mailto:edalquist@unicon.net">edalquist@unicon.net</a>
 * @version $Revision$ $Date$
 * @since uPortal 2.5
 */
public final class DBCPDataSourceFactory implements IPooledDataSourceFactory {

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
        
        // I'd like to see us move to using Spring to produce and manage our
        // DataSource singletons rather than creating another properties file for
        // the configuration of this use of DBCP.  
        //
        // Think of Spring as the ultimate DataSource factory.  Want some particular
        // configuration of one of the standard dbcp DataSource implementations?
        // It can do that.  Want to get your DataSource from JNDI?  It can do that.
        // Want to wire together our own custom DataSource implementation?
        // It can do that too.  My personal viewpoint is that the factory API
        // in this package isn't buying us much beyond what we'd get if we wired
        // together singleton DataSources directly in our Spring configuration.
        // -Andrew Petro
        
        ds.setMaxActive(100);
        ds.setMaxIdle(30);
        ds.setMaxWait(10000);
        
        return ds;
    }
}
