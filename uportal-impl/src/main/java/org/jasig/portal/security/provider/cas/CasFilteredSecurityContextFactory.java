/**
 * Copyright (c) 2000-2009, Jasig, Inc.
 * See license distributed with this file and available online at
 * https://www.ja-sig.org/svn/jasig-parent/tags/rel-10/license-header.txt
 */
package org.jasig.portal.security.provider.cas;

import org.jasig.portal.security.ISecurityContext;
import org.jasig.portal.security.ISecurityContextFactory;

/**
 * Factory for CasFilteredSecurityContexts, implementing the 
 * CasValidateFilter-fronted CAS authentication approach.
 */
public class CasFilteredSecurityContextFactory 
    implements ISecurityContextFactory {

    public ISecurityContext getSecurityContext() {
        return new CasFilteredSecurityContext();
    }

}
