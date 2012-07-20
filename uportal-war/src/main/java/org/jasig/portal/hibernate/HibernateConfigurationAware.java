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
