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

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.jasig.portal.hibernate.DelegatingHibernateIntegrator.HibernateConfiguration;

/**
 * Should be extended by classes that are interested in knowing about the configuration
 * of the hibernate instance behind a specific persistence unit.
 * <p/>
 * IMPORTANT: these objects are mutable but SHOULD NOT BE MODIFIED
 * 
 * @author Eric Dalquist
 */
public abstract class HibernateConfigurationAware {

    @PostConstruct
    public final void registerConfigurationAwareBean() {
        DelegatingHibernateIntegrator.registerConfigurationAwareBeans(this);
    }
    
    @PreDestroy
    public final void unregisterConfigurationAwareBean() {
        DelegatingHibernateIntegrator.unregisterConfigurationAwareBeans(this);
    }
    
	/**
	 * @return true if the class cares about the specific persistence unit
	 */
	public abstract boolean supports(String persistenceUnit);
	
	/**
	 * @param persistenceUnit The JPA persistence unit the configuration is fore
	 * @param configuration The configuration
	 */
	public abstract void setConfiguration(String persistenceUnit, HibernateConfiguration hibernateConfiguration);
}
