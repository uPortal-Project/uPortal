package org.jasig.portal.hibernate;

import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

import org.hibernate.cfg.Configuration;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.integrator.spi.Integrator;
import org.hibernate.metamodel.source.MetadataImplementor;
import org.hibernate.service.spi.SessionFactoryServiceRegistry;

/**
 * Delegates to other integrators that are registered via {@link #registerIntegrator(Integrator)}
 * 
 * @author Eric Dalquist
 */
public class DelegatingHibernateIntegrator implements Integrator {
	private static final Set<Integrator> integrators = new CopyOnWriteArraySet<Integrator>();
	
	public static void registerIntegrator(Integrator integrator) {
		integrators.add(integrator);
	}
	
	public static void unregisterIntegrator(Integrator integrator) {
		integrators.remove(integrator);
	}

    @Override
    public void integrate(Configuration configuration, SessionFactoryImplementor sessionFactory,
            SessionFactoryServiceRegistry serviceRegistry) {
    	
    	for (final Integrator integrator : integrators) {
			integrator.integrate(configuration, sessionFactory, serviceRegistry);
		}
    }

    @Override
    public void integrate(MetadataImplementor metadata, SessionFactoryImplementor sessionFactory,
            SessionFactoryServiceRegistry serviceRegistry) {
    	
    	for (final Integrator integrator : integrators) {
			integrator.integrate(metadata, sessionFactory, serviceRegistry);
		}
    }

    @Override
    public void disintegrate(SessionFactoryImplementor sessionFactory, SessionFactoryServiceRegistry serviceRegistry) {
    	for (final Integrator integrator : integrators) {
			integrator.disintegrate(sessionFactory, serviceRegistry);
		}
    }
}
