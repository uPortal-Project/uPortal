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
package org.jasig.portal.hibernate;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.Set;

import org.hibernate.cfg.Configuration;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.integrator.spi.Integrator;
import org.hibernate.metamodel.source.MetadataImplementor;
import org.hibernate.service.spi.SessionFactoryServiceRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Works with {@link HibernateConfigurationAware} beans to provide configuration information from hibernate
 * to other beans. 
 * <p/>
 * Configured via the {@link ServiceLoader} mechanism.
 * 
 * @author Eric Dalquist
 */
public class DelegatingHibernateIntegrator implements Integrator {
	private static final Set<HibernateConfigurationAwareInjector> configurationAwareBeans = new HashSet<HibernateConfigurationAwareInjector>();
	private static final Map<String, HibernateConfiguration> hibernateInfoMap = new HashMap<String, DelegatingHibernateIntegrator.HibernateConfiguration>();
	
	protected final Logger logger = LoggerFactory.getLogger(getClass());
	
	public static void registerConfigurationAwareBeans(HibernateConfigurationAwareInjector configurationAwareBean) {
	    synchronized (configurationAwareBeans) {
	        //make sure any hibernate configs already loaded get injected
	        for (final Map.Entry<String, HibernateConfiguration> configEntry : hibernateInfoMap.entrySet()) {
                configurationAwareBean.setConfiguration(configEntry.getKey(), configEntry.getValue());
	        }
	        
	        configurationAwareBeans.add(configurationAwareBean);
	    }
	}
	
	public static void unregisterConfigurationAwareBeans(HibernateConfigurationAwareInjector configurationAwareBean) {
		configurationAwareBeans.remove(configurationAwareBean);
	}

    @Override
    public void integrate(Configuration configuration, SessionFactoryImplementor sessionFactory,
            SessionFactoryServiceRegistry serviceRegistry) {
        
        final String persistenceUnitName = configuration.getProperty("persistenceUnitName");
        if (persistenceUnitName == null) {
            logger.warn("Hibernate Configuration does not have '" + persistenceUnitName + "' set. It will not be considered for injecting into HibernateConfigurationAware beans: " + configuration);
            return;
        }
        
        final HibernateConfiguration hibernateConfiguration = new HibernateConfiguration(configuration, sessionFactory, serviceRegistry);
        
        synchronized (configurationAwareBeans) {
            hibernateInfoMap.put(persistenceUnitName, hibernateConfiguration);
        	
        	for (final HibernateConfigurationAwareInjector configurationAwareBean : configurationAwareBeans) {
    	        configurationAwareBean.setConfiguration(persistenceUnitName, hibernateConfiguration);
    		}
        }
    }

    @Override
    public void integrate(MetadataImplementor metadata, SessionFactoryImplementor sessionFactory,
            SessionFactoryServiceRegistry serviceRegistry) {
        throw new UnsupportedOperationException("TODO REWRITE CODE AFTER HIBERNATE SWITCHES TO METAMODEL");
    }

    @Override
    public void disintegrate(SessionFactoryImplementor sessionFactory, SessionFactoryServiceRegistry serviceRegistry) {
    	//ignore
    }
    
    public static final class HibernateConfiguration {
        private final Configuration configuration;
        private final SessionFactoryImplementor sessionFactory;
        private final SessionFactoryServiceRegistry serviceRegistry;
        
        public HibernateConfiguration(Configuration configuration, SessionFactoryImplementor sessionFactory,
                SessionFactoryServiceRegistry serviceRegistry) {
            this.configuration = configuration;
            this.sessionFactory = sessionFactory;
            this.serviceRegistry = serviceRegistry;
        }
        public Configuration getConfiguration() {
            return configuration;
        }
        public SessionFactoryImplementor getSessionFactory() {
            return sessionFactory;
        }
        public SessionFactoryServiceRegistry getServiceRegistry() {
            return serviceRegistry;
        }
    }
}
