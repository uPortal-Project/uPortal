/* Copyright 2006 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
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
