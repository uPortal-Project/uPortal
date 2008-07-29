/* Copyright 2005 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/

package org.jasig.portal.tools.checks;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

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
     * True if this CheckResult represents a failed result that should stop the portal from starting
     * False if the failure should just be logged. 
     */
    private final boolean fatal;
    
    /**
     * A message describing the result of the check.
     */
    private final String message;
    
    /**
     * In the case where the check failed, a String describing how to resolve
     * the failure.
     */
    private final String remediationAdvice;
    
    private CheckResult(boolean success, boolean fatal, String message, String remediationAdvice) {
        // constructor is private in order to
        // require usage of the static factory methods
        // which document the difference between a success and a failure.
        
        this.success = success;
        this.fatal = fatal;
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
        return new CheckResult(true, false, message, null);
    }
    
    /**
     * Obtain a CheckResult representing a check that failed -- dependency for which
     * you were checking was not present, thing you asserted to be true wasn't, etc.
     * @param message - a message describing the failure of the check
     * @param remediationAdvice - advice for the uPortal deployer about how to resolve this failure
     * @return a CheckResult representing this failure
     */
    public static CheckResult createFailure(String message, String remediationAdvice) {
        return new CheckResult(false, false, message, remediationAdvice);
    }
    
    /**
     * Obtain a CheckResult representing a check that failed and should stop the portal
     * from initializaing -- dependency for which you were checking was not present,
     * thing you asserted to be true wasn't, etc.
     * 
     * @param message - a message describing the failure of the check
     * @param remediationAdvice - advice for the uPortal deployer about how to resolve this failure
     * @return a CheckResult representing this failure
     */
    public static CheckResult createFatalFailure(String message, String remediationAdvice) {
        return new CheckResult(false, true, message, remediationAdvice);
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
    
    /**
     * @return If the failure was fatal, can only be true if {@link #isSuccess()} is false
     */
    public boolean isFatal() {
        return fatal;
    }

    /**
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE)
            .append("success", this.success)
            .append("message", this.message)
            .append("remediationAdvice", this.remediationAdvice)
            .toString();
    }
}
