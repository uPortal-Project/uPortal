/* Copyright 2004 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/

package org.jasig.portal.channels.error.tt;

import org.jasig.portal.AuthorizationException;

/**
 * Testcase for the AuthorizationExceptionToElement class.
 * @author andrew.petro@yale.edu
 * @version $Revision$ $Date$
 */
public final class AuthorizationExceptionToElementTest extends
        AbstractThrowableToElementTest {

    /**
     * Since the class is stateless, we only need one.
     */
    private AuthorizationExceptionToElement aeToElement 
        = new AuthorizationExceptionToElement();

    /* (non-Javadoc)
     * @see org.jasig.portal.channels.error.tt.AbstractThrowableToElementTest#getThrowableToElementInstance()
     */
    protected IThrowableToElement getThrowableToElementInstance() {
        return this.aeToElement;
    }

    /* (non-Javadoc)
     * @see org.jasig.portal.channels.error.tt.AbstractThrowableToElementTest#supportedThrowable()
     */
    protected Throwable supportedThrowable() {
        return new AuthorizationException("A message");
    }

    /* (non-Javadoc)
     * @see org.jasig.portal.channels.error.tt.AbstractThrowableToElementTest#unsupportedThrowable()
     */
    protected Throwable unsupportedThrowable() {
        return new Throwable("An unsupported throwable.");
    }

}