package org.jasig.portal.hibernate;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.hibernate.integrator.spi.Integrator;

/**
 * Base class for spring managed hibernate {@link Integrator} impls. This class
 * registers itself with the {@link DelegatingHibernateIntegrator} after construction
 * and deregisteres itself pre destruction
 * 
 * @author Eric Dalquist
 */
public abstract class SpringHibernateIntegrator implements Integrator {

	@PostConstruct
	public final void registerIntegrator() {
		DelegatingHibernateIntegrator.registerIntegrator(this);
	}
	
	@PreDestroy
	public void unregisterIntegrator() {
		DelegatingHibernateIntegrator.unregisterIntegrator(this);
	}
}
