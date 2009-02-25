/**
 * Copyright (c) 2000-2009, Jasig, Inc.
 * See license distributed with this file and available online at
 * https://www.ja-sig.org/svn/jasig-parent/tags/rel-10/license-header.txt
 */
package org.jasig.portal.channels.error.tt;

import org.jasig.portal.channels.error.error2xml.IThrowableToElement;
import org.jasig.portal.channels.error.error2xml.ThrowableToElement;

/**
 * Testcase for ThrowableToElement.
 * @author andrew.petro@yale.edu
 * @version $Revision$ $Date$
 */
public class ThrowableToElementTest extends AbstractThrowableToElementTest {

    private ThrowableToElement throwableToElement 
        = new ThrowableToElement();
    
    /* (non-Javadoc)
     * @see org.jasig.portal.channels.error.tt.AbstractThrowableToElementTest#getThrowableToElementInstance()
     */
    protected IThrowableToElement getThrowableToElementInstance() {
        return this.throwableToElement;
    }

    /* (non-Javadoc)
     * @see org.jasig.portal.channels.error.tt.AbstractThrowableToElementTest#supportedThrowable()
     */
    protected Throwable supportedThrowable() {
        return new Throwable("A test throwable.");
    }

    /* (non-Javadoc)
     * @see org.jasig.portal.channels.error.tt.AbstractThrowableToElementTest#unsupportedThrowable()
     */
    protected Throwable unsupportedThrowable() {
        // there is no unsupported Throwable for this class.
        return null;
    }

}