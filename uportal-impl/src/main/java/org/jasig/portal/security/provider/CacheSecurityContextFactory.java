/**
 * Copyright (c) 2000-2009, Jasig, Inc.
 * See license distributed with this file and available online at
 * https://www.ja-sig.org/svn/jasig-parent/tags/rel-10/license-header.txt
 */
package org.jasig.portal.security.provider;



import org.jasig.portal.security.ISecurityContext;
import org.jasig.portal.security.ISecurityContextFactory;

/**
 * <p>The factory class for the cache security context. Just returns a new
 * instance of the CacheSecurityContext. See the notes and warnings
 * associated with the CacheSecurityContext class.</p>
 *
 * @author Andrew Newman, newman@yale.edu
 * @version $Revision$
 */
public class CacheSecurityContextFactory implements ISecurityContextFactory {

  public ISecurityContext getSecurityContext() {
    return new CacheSecurityContext();
  }
}