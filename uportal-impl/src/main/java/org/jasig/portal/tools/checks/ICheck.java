/* Copyright 2005 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/
package org.jasig.portal.tools.checks;

/**
 * A single runtime check that to be performed to validate an application deployment.
 * 
 * @version $Revision$ $Date$
 * @since uPortal 2.5
 */
public interface ICheck {

    /**
     * Perform an arbitrary check.  The result of this method should be 
     * a CheckResult representing either a success or failure of the check.  
     * 
     * Implementations should catch their own exceptions and translate them into
     * CheckResults representing failures, since the intent of this API is to translate
     * arcane deployment issues into friendly results with remediation messages.
     * 
     * However, the
     * implementation of this method may throw any RuntimeException, and 
     * clients must cope with such exceptions.  Cope with probably means translate
     * it into a CheckResult representing a failure of this check.  The
     * client of a Check implementation will be less effective in translating a thrown Throwable
     * into an intelligent CheckResult representing a failure than the Check would have been
     * in doing this itself.
     * 
     * @return a CheckResult representing the result of the check
     */
    public CheckResult doCheck();
    
    /**
     * Get a description of what it is the check is intended to check.
     * Implementations of this method must always return a non-null String and
     * should not throw anything.
     * @return a description of what it is that the check checks.
     */
    public String getDescription();
    
}
