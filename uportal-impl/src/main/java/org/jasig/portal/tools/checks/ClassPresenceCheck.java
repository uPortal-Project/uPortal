/* Copyright 2005 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/

package org.jasig.portal.tools.checks;


/**
 * Checks that a class named as a constructor argument is present.
 * 
 * @version $Revision$ $Date$
 * @since uPortal 2.5
 */
public class ClassPresenceCheck extends BaseCheck {

    /**
     * The class we will be looking for.
     */
    private final String targetClass;
    
    private String remediationAdvice;
    
    /**
     * @return the remediationAdvice
     */
    public String getRemediationAdvice() {
        return remediationAdvice;
    }
    /**
     * The advice to provide to resolve a failed check
     * 
     * @param remediationAdvice the remediationAdvice to set
     */
    public void setRemediationAdvice(String remediationAdvice) {
        this.remediationAdvice = remediationAdvice;
    }
    
    /**
     * Each instance of ClassPresenceCheck exists to test for the presence of
     * exactly one class.  That class must be specified as the single required
     * constructor argument, which must be a non-null String representing the
     * fully qualified name of the class.
     * 
     * @param targetClass fully qualified name of the target class
     * @throws IllegalArgumentException if targetClass is null
     */
    public ClassPresenceCheck(String targetClass) {
        if (targetClass == null) {
            throw new IllegalArgumentException("The constructor argument to the "
                    + "ClassPresenceCheck constructor was illegally null.");
        }
        this.targetClass = targetClass;
        
        
        // generate default description success and failure results
        
        this.setDescription("Checks for the presence of class " + targetClass);
        this.setRemediationAdvice("Is a required .jar missing from the /WEB-INF/lib directory or from "
                + "the JRE endorsed directory or from "
                + "the Tomcat endorsed directory?");
    }

    /* (non-Javadoc)
     * @see org.jasig.portal.tools.checks.BaseCheck#doCheckInternal()
     */
    @Override
    protected CheckResult doCheckInternal() {
        try {
            getClass().getClassLoader().loadClass(this.targetClass);
        } catch (ClassNotFoundException e) {
            return this.getFailureResult();
        } 
        return this.getSuccessResult();
    }

    /**
     * Get the CheckResult we will return when we fail to touch our
     * configured targetClass.
     * @return CheckResult we will return on failure
     */
    CheckResult getFailureResult() {
        return CheckResult.createFailure("Failed to touch class " + targetClass, this.remediationAdvice);
    }
    
    /**
     * Get the CheckResult we will return when we succeed in touching 
     * our configured targetClass.
     * @return CheckResult we will return on success.
     */
    CheckResult getSuccessResult() {
        return CheckResult.createSuccess("Successfully touched class " + targetClass);
    }
    
    /**
     * Get the name of the class for which we check.
     * @return the fully qualified name of the class for which we check.
     */
    String getTargetClass() {
        return this.targetClass;
    }
}

