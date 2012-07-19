package org.jasig.portal.jpa;

import java.util.Map;

import org.hibernate.cfg.Configuration;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.integrator.spi.Integrator;
import org.hibernate.metamodel.source.MetadataImplementor;
import org.hibernate.service.spi.SessionFactoryServiceRegistry;

public class TestIntegrator implements Integrator {

    @Override
    public void integrate(Configuration configuration, SessionFactoryImplementor sessionFactory,
            SessionFactoryServiceRegistry serviceRegistry) {
        
        for (final Map.Entry<Object, Object> e : configuration.getProperties().entrySet()) {
            System.out.println(e.getKey() + ":\t" + e.getValue());
        }
        //persistenceUnitName
        
//        sessionFactory.getDialect()
        System.out.println("test");
    }

    @Override
    public void integrate(MetadataImplementor metadata, SessionFactoryImplementor sessionFactory,
            SessionFactoryServiceRegistry serviceRegistry) {
        System.out.println("test");
    }

    @Override
    public void disintegrate(SessionFactoryImplementor sessionFactory, SessionFactoryServiceRegistry serviceRegistry) {
        System.out.println("test");
    }

}
