package org.jasig.portal.hibernate;

import org.hibernate.cfg.Configuration;
import org.springframework.beans.factory.Aware;

/**
 * Should be implemented by classes that are interested in knowing which {@link Configuration} is in use
 * 
 * @author Eric Dalquist
 */
public interface ConfigurationAware extends Aware {
	/**
	 * @return true if the class cares about the specific persistence unit
	 */
	boolean supports(String persistenceUnit);
	
	/**
	 * @param persistenceUnit The JPA persistence unit the configuration is fore
	 * @param configuration The configuration
	 */
	void setConfiguration(String persistenceUnit, Configuration configuration);
}
