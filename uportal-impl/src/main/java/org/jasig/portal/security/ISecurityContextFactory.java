/**
 * Copyright (c) 2000-2009, Jasig, Inc.
 * See license distributed with this file and available online at
 * https://www.ja-sig.org/svn/jasig-parent/tags/rel-10/license-header.txt
 */
package org.jasig.portal.security;

/**
 * <p>A context-specific factory class interface that should be implemented
 * by factory classes defined for each context provider. The provider's
 * constructor should not be public to discourage it's instantiation through
 * means other than the corresponding factory. This formalism should be
 * followed for consistency even when the factory performs no additional
 * value-add than instantiating the appropriate context class.</p>
 *
 * @author Andrew Newman, newman@yale.edu
 * @version $Revision$
 */

public interface ISecurityContextFactory {

  public ISecurityContext getSecurityContext();

}