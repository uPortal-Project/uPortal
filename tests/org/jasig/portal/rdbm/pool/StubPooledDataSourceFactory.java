/* Copyright 2005 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/

package org.jasig.portal.rdbm.pool;

import javax.sql.DataSource;


/**
 * A stub IPooledDataSourceFactory implementation we use to test ability
 * to instantiate and use the implementation configured via PropertiesManager.
 * @version $Revision$ $Date$
 */
public class StubPooledDataSourceFactory 
    implements IPooledDataSourceFactory {

    public StubPooledDataSourceFactory() {
        // do nothing
    }
    
    public DataSource createPooledDataSource(String driverClassName, String userName, String password, String url) {
        return null;
    }
    
}

