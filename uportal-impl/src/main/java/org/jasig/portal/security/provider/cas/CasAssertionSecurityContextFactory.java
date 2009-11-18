/**
 * Copyright (c) 2000-2009, Jasig, Inc.
 * See license distributed with this file and available online at
 * https://www.ja-sig.org/svn/jasig-parent/tags/rel-10/license-header.txt
 */
package org.jasig.portal.security.provider.cas;

import org.jasig.portal.security.ISecurityContextFactory;
import org.jasig.portal.security.ISecurityContext;

/**
 * Factory to construct new instances of {@link org.jasig.portal.security.provider.cas.CasAssertionSecurityContext}.
 *
 * @author Scott Battaglia
 * @version $Revision$ $Date$
 * @since 3.2
 */
public class CasAssertionSecurityContextFactory implements ISecurityContextFactory {

    public ISecurityContext getSecurityContext() {
        return new CasAssertionSecurityContext();
    }
}
