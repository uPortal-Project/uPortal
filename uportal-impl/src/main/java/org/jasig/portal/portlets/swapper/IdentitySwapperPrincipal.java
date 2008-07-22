/**
 * Copyright 2007 The JA-SIG Collaborative.  All rights reserved.
 * See license distributed with this file and
 * available online at http://www.uportal.org/license.html
 */
package org.jasig.portal.portlets.swapper;

import org.jasig.portal.security.IPerson;
import org.jasig.portal.security.IPrincipal;

/**
 * Implements an immutable IPrincipal for use with the identity swapper
 * 
 * @author Eric Dalquist
 * @version $Revision$
 */
public class IdentitySwapperPrincipal implements IPrincipal {
    private static final long serialVersionUID = 1L;

    private final IPerson person;

    public IdentitySwapperPrincipal(IPerson person) {
        this.person = person;
    }

    /* (non-Javadoc)
     * @see org.jasig.portal.security.IPrincipal#getFullName()
     */
    public String getFullName() {
        return this.person.getFullName();
    }

    /* (non-Javadoc)
     * @see org.jasig.portal.security.IPrincipal#getGlobalUID()
     */
    public String getGlobalUID() {
        return this.person.getName();
    }

    /* (non-Javadoc)
     * @see org.jasig.portal.security.IPrincipal#getUID()
     */
    public String getUID() {
        return this.person.getName();
    }

    /* (non-Javadoc)
     * @see org.jasig.portal.security.IPrincipal#setUID(java.lang.String)
     */
    public void setUID(String UID) {
        throw new UnsupportedOperationException("UID is fixed for a swapped user.");
    }

}
