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

package org.jasig.portal.rdbm;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;

import javax.sql.DataSource;

import org.apache.commons.dbcp.BasicDataSource;
import org.apache.commons.io.IOUtils;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

/**
 * A DataSource implementation backed by an in-memory HSQLDb instance,
 * suitable for implementing testcases for DataSource-consuming DAO impls.
 * @author andrew.petro@yale.edu
 * @version $Revision$ $Date$
 */
public class TransientDatasource implements DataSource {
    
    private DataSource delegate;
    
    public TransientDatasource() {
        
        final Properties dataSourceProperties = new Properties();
        final InputStream dataSourcePropertiesStream = this.getClass().getResourceAsStream("/dataSource.properties");
        try {
            dataSourceProperties.load(dataSourcePropertiesStream);
        }
        catch (IOException e) {
            throw new RuntimeException("Failed to load dataSource.properties", e);
        }
        finally {
            IOUtils.closeQuietly(dataSourcePropertiesStream);
        }

        DriverManagerDataSource dataSource = new DriverManagerDataSource();
        dataSource.setDriverClassName(dataSourceProperties.getProperty("hibernate.connection.driver_class"));
        dataSource.setUrl(dataSourceProperties.getProperty("hibernate.connection.url"));
        dataSource.setUsername(dataSourceProperties.getProperty("hibernate.connection.username"));
        dataSource.setPassword(dataSourceProperties.getProperty("hibernate.connection.password"));
        
        this.delegate = dataSource;

    }

    public PrintWriter getLogWriter() throws SQLException {
        return delegate.getLogWriter();
    }

    public <T> T unwrap(Class<T> iface) throws SQLException {
        return delegate.unwrap(iface);
    }

    public void setLogWriter(PrintWriter out) throws SQLException {
        delegate.setLogWriter(out);
    }

    public boolean isWrapperFor(Class<?> iface) throws SQLException {
        return delegate.isWrapperFor(iface);
    }

    public void setLoginTimeout(int seconds) throws SQLException {
        delegate.setLoginTimeout(seconds);
    }

    public Connection getConnection() throws SQLException {
        return delegate.getConnection();
    }

    public Connection getConnection(String username, String password) throws SQLException {
        return delegate.getConnection(username, password);
    }

    public int getLoginTimeout() throws SQLException {
        return delegate.getLoginTimeout();
    }
}