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

import java.util.Collection;
import java.util.Collections;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.jasig.portal.hibernate.DelegatingHibernateIntegrator.HibernateConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Injects the {@link HibernateConfiguration} into beans that want to know about it
 * 
 * @author Eric Dalquist
 */
@Service("hibernateConfigurationAwareInjector")
public final class HibernateConfigurationAwareInjector {
    private Collection<HibernateConfigurationAware> hibernateConfigurationAwareBeans = Collections.emptySet();
    
    @Autowired
    public void setHibernateConfigurationAwareBeans(Collection<HibernateConfigurationAware> hibernateConfigurationAwareBeans) {
        this.hibernateConfigurationAwareBeans = hibernateConfigurationAwareBeans;
    }

    @PostConstruct
    public final void registerConfigurationAwareBean() {
        DelegatingHibernateIntegrator.registerConfigurationAwareBeans(this);
    }
    
    @PreDestroy
    public final void unregisterConfigurationAwareBean() {
        DelegatingHibernateIntegrator.unregisterConfigurationAwareBeans(this);
    }

    /**
	 * @param persistenceUnit The JPA persistence unit the configuration is fore
	 * @param configuration The configuration
	 */
	public void setConfiguration(String persistenceUnit, HibernateConfiguration hibernateConfiguration) {
	    for (final HibernateConfigurationAware hibernateConfigurationAware : hibernateConfigurationAwareBeans) {
	        if (hibernateConfigurationAware.supports(persistenceUnit)) {
	            hibernateConfigurationAware.setConfiguration(persistenceUnit, hibernateConfiguration);
	        }
	    }
	}
}
