package org.jasig.portal.hibernate;

import org.hibernate.cfg.Configuration;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.integrator.spi.Integrator;
import org.hibernate.metamodel.source.MetadataImplementor;
import org.hibernate.service.spi.SessionFactoryServiceRegistry;

public class HibernateConfigurationIntegrator implements Integrator {

    @Override
    public void integrate(Configuration configuration, SessionFactoryImplementor sessionFactory,
            SessionFactoryServiceRegistry serviceRegistry) {
        // TODO Auto-generated method stub

    }

    @Override
    public void integrate(MetadataImplementor metadata, SessionFactoryImplementor sessionFactory,
            SessionFactoryServiceRegistry serviceRegistry) {
        // TODO Auto-generated method stub

    }

    @Override
    public void disintegrate(SessionFactoryImplementor sessionFactory, SessionFactoryServiceRegistry serviceRegistry) {
        // TODO Auto-generated method stub

    }

}
