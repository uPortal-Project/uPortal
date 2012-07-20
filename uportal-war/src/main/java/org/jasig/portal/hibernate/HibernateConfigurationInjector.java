package org.jasig.portal.hibernate;

import java.util.Collection;

import org.hibernate.cfg.Configuration;
import org.hibernate.dialect.Dialect;
import org.hibernate.engine.jdbc.spi.JdbcServices;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.metamodel.source.MetadataImplementor;
import org.hibernate.service.spi.SessionFactoryServiceRegistry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Component;

/**
 * Injects {@link Dialect} and {@link Configuration} objects into other spring beans
 * 
 * @author Eric Dalquist
 */
@Component("hibernateConfigurationInjector")
public class HibernateConfigurationInjector extends SpringHibernateIntegrator {
	private Collection<DialectAware> dialectAwareBeans;
	private Collection<ConfigurationAware> configurationAwareBeans;
	
	@Autowired
    public void setDialectAwareBeans(Collection<DialectAware> dialectAwareBeans) {
		this.dialectAwareBeans = dialectAwareBeans;
	}

	@Autowired
    public void setConfigurationAwareBeans(
			Collection<ConfigurationAware> configurationAwareBeans) {
		this.configurationAwareBeans = configurationAwareBeans;
	}

	@Override
    public void integrate(Configuration configuration, SessionFactoryImplementor sessionFactory,
            SessionFactoryServiceRegistry serviceRegistry) {
    	
    	final String persistenceUnitName = configuration.getProperty("persistenceUnitName");
    	
    	final JdbcServices jdbcServices = sessionFactory.getJdbcServices();
		final Dialect dialect = jdbcServices.getDialect();
		for (final DialectAware dialectAware : this.dialectAwareBeans) {
			if (dialectAware.supports(persistenceUnitName)) {
				dialectAware.setDialect(persistenceUnitName, dialect);
			}
		}

		for (final ConfigurationAware configurationAware : this.configurationAwareBeans) {
			if (configurationAware.supports(persistenceUnitName)) {
				configurationAware.setConfiguration(persistenceUnitName, configuration);
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
    	//ignored
    }
}
