package org.jasig.portal.security;

/**
 * <p>A context-specific factory class interface that should be implemented
 * by factory classes defined for each context provider. The provider's
 * constructor should not be public to discourage it's instantiation through
 * means other than the corresponding factory. This formalism should be
 * followed for consistency even when the factory performs no additional
 * value-add than instantiating the appropriate context class.</p>
 *
 * <p>Copyright (c) Yale University 2000</p>
 *
 * <p>(Note that Yale University intends to relinquish copyright claims to
 * this code once an appropriate JASIG copyright arrangement can be
 * established -ADN)</p>
 *
 * @author Andrew Newman
 * @version $Revision$
 */

public interface SecurityContextFactory {

  public SecurityContext getSecurityContext();

}