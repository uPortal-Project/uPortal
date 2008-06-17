/* Copyright 2005 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/

package org.jasig.portal.tools.checks;

/**
 * Object representing the result of a check.  All CheckResults convey the result
 * of a check.
 * Additionally, CheckResults representing failures convey advice about how
 * to remediate the failed check.
 * @version $Revision$ $Date$
 * @since uPortal 2.5
 */
public class CheckResult {
    
    /**
     * True if this CheckResult represents a successful result.
     * False if it represents a failure.
     */
    private final boolean success;
    
    /**
     * A message describing the result of the check.
     */
    private final String message;
    
    /**
     * In the case where the check failed, a String describing how to resolve
     * the failure.
     */
    private final String remediationAdvice;
    
    private CheckResult(boolean success, String message, String remediationAdvice) {
        // constructor is private in order to
        // require usage of the static factory methods
        // which document the difference between a success and a failure.
        
        this.success = success;
        this.message = message;
        this.remediationAdvice = remediationAdvice;
        
    }
    
    /**
     * Obtain a CheckResult representing a check that succeeded -- dependency
     * for which you were checking was present, thing you asserted to be true
     * actually was true, etc.
     * @param message - a message describing the success of that check
     * @return a CheckResult representing the success
     */
    public static CheckResult createSuccess(String message){
        return new CheckResult(true, message, null);
    }
    
    /**
     * Obtain a CheckResult representing a check that failed -- dependency for which
     * you were checking was not present, thing you asserted to be true wasn't, etc.
     * @param message - a message describing the failure of the check
     * @param remediationAdvice - advice for the uPortal deployer about how to resolve this failure
     * @return a CheckResult representing this failure
     */
    public static CheckResult createFailure(String message, String remediationAdvice) {
        return new CheckResult(false, message, remediationAdvice);
    }

    /**
     * @return Returns the message.
     */
    public String getMessage() {
        return this.message;
    }
    /**
     * @return Returns the remediationAdvice.
     */
    public String getRemediationAdvice() {
        return this.remediationAdvice;
    }
    /**
     * @return Returns the success.
     */
    public boolean isSuccess() {
        return this.success;
    }
}
