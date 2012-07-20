package org.jasig.portal.hibernate;

import org.hibernate.dialect.Dialect;
import org.springframework.beans.factory.Aware;

/**
 * Should be implemented by classes that are interested in knowing which {@link Dialect} is in use
 * 
 * @author Eric Dalquist
 */
public interface DialectAware extends Aware {
	/**
	 * @return true if the class cares about the specific persistence unit
	 */
	boolean supports(String persistenceUnit);
	
	/**
	 * @param persistenceUnit The JPA persistence unit the dialect is fore
	 * @param dialect The dialect
	 */
	void setDialect(String persistenceUnit, Dialect dialect);
}
