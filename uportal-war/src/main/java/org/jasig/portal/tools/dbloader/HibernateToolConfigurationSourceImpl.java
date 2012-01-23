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

package org.jasig.portal.tools.dbloader;

import java.util.Properties;

import javax.persistence.spi.PersistenceUnitInfo;
import javax.sql.DataSource;

import org.hibernate.cfg.Configuration;
import org.hibernate.cfg.Environment;
import org.hibernate.ejb.Ejb3Configuration;
import org.hibernate.internal.util.config.ConfigurationHelper;
import org.hibernate.service.ServiceRegistry;
import org.hibernate.service.ServiceRegistryBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.jdbc.datasource.lookup.SingleDataSourceLookup;
import org.springframework.orm.jpa.persistenceunit.DefaultPersistenceUnitManager;

import com.google.common.collect.ImmutableMap;

/**
 * Runs the Hibernate Schema Export tool using the specified DataSource for the target DB.
 * 
 * @author Eric Dalquist
 * @version $Revision$
 */
public class HibernateToolConfigurationSourceImpl implements InitializingBean, HibernateToolConfigurationSource {
    protected final Logger logger = LoggerFactory.getLogger(getClass());
    
    private DataSource dataSource;
    
    private Configuration configuration;
    private ServiceRegistry serviceRegistry;
    private String persistenceUnitName;


    /**
     * Specify the name of the EntityManagerFactory configuration.
     * <p>Default is none, indicating the default EntityManagerFactory
     * configuration. The persistence provider will throw an exception if
     * ambiguous EntityManager configurations are found.
     * @see javax.persistence.Persistence#createEntityManagerFactory(String)
     */
    public void setPersistenceUnitName(String persistenceUnitName) {
        this.persistenceUnitName = persistenceUnitName;
    }

    /**
     * @param dataSource the dataSource to use
     */
    public void setDataSource(DataSource dataSource) {
        this.dataSource = dataSource;
    }
    
    @Override
    public void afterPropertiesSet() throws Exception {
        this.loadConfiguration();
        this.loadServiceRegistry();
    }

    @Override
    public Configuration getConfiguration() {
        return this.configuration;
    }

    @Override
    public ServiceRegistry getServiceRegistry() {
        return this.serviceRegistry;
    }

    private final void loadConfiguration() {
        //Use DefaultPersistenceUnitManager and Ejb3Configuration to build hibernate configuration object
        final DefaultPersistenceUnitManager defaultPersistenceUnitManager = new DefaultPersistenceUnitManager();
        defaultPersistenceUnitManager.setDataSourceLookup(new SingleDataSourceLookup(dataSource));
        defaultPersistenceUnitManager.setDefaultDataSource(dataSource);
        defaultPersistenceUnitManager.afterPropertiesSet();
        final PersistenceUnitInfo persistenceUnitInfo = defaultPersistenceUnitManager.obtainPersistenceUnitInfo(this.persistenceUnitName);
        
        final Ejb3Configuration configBuilder = new Ejb3Configuration();
        final Ejb3Configuration jpaConfiguration = configBuilder.configure(
                persistenceUnitInfo,
                ImmutableMap.of(
                        "hibernate.cache.use_query_cache", "false",
                        "hibernate.cache.use_second_level_cache", "false"));
        
        jpaConfiguration.setDataSource(dataSource);
        
        this.configuration = jpaConfiguration.getHibernateConfiguration();

        //Build the entity mappings
        this.configuration.buildMappings();
    }

    private void loadServiceRegistry() {
        final Properties properties = this.configuration.getProperties();
        Environment.verifyProperties(properties);
        ConfigurationHelper.resolvePlaceHolders(properties);
        this.serviceRegistry = new ServiceRegistryBuilder()
                .applySettings(properties)
                .buildServiceRegistry();
    }

}
