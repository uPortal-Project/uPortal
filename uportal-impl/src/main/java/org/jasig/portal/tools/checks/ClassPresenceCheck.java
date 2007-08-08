/* Copyright 2005 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/

package org.jasig.portal.tools.checks;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Checks that a class named as a constructor argument is present.
 * 
 * @version $Revision$ $Date$
 * @since uPortal 2.5
 */
public class ClassPresenceCheck 
    implements ICheck {
    
    protected final Log log = LogFactory.getLog(getClass());

    /**
     * The class we will be looking for.
     */
    private final String targetClass;
    
    /**
     * The CheckResult we will return if our target class is present.
     * We populate this field with a default at construction and allow it to be
     * overridden via a setter method.
     */
    private CheckResult successResult;
    
    /**
     * The CheckResult we will return if our target class is not present.
     * We populate this field with a default at costruction and allow it to be
     * overridden via a setter method.
     */
    private CheckResult failureResult;
    
    /**
     * A String describing this check.
     * We populate this field with a default at construction and allow it to be
     * overridden via a setter method.
     */
    private String checkDescription;
    
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
        
        this.checkDescription = "Checks for the presence of class "
            + targetClass;
        
        this.successResult = CheckResult.createSuccess("Successfully touched class "
                + targetClass);
        
        this.failureResult = CheckResult.createFailure(
                "Failed to touch class " + targetClass,
                "Is a required .jar missing from the /WEB-INF/lib directory or from "
                + "the JRE endorsed directory or from "
                + "the Tomcat endorsed directory?");
        
    }

    public CheckResult doCheck() {
        try {
            getClass().getClassLoader().loadClass(this.targetClass);
        } catch (ClassNotFoundException e) {
            return this.failureResult;
        } 
        return this.successResult;
    }

    public String getDescription() {
        return this.checkDescription;
    }
    
    /**
     * Set the check description which we will return for on calls to 
     * the getDescription() method we're implementing as part of being an 
     * {@link ICheck}.
     * @param description
     */
    public void setDescription(String description) {
        if (description == null) {
            throw new IllegalArgumentException("Cannot set description to null.");
        }
        this.checkDescription = description;
    }
    
    /**
     * Get the CheckResult we will return when we fail to touch our
     * configured targetClass.
     * @return CheckResult we will return on failure
     */
    CheckResult getFailureResult() {
        return this.failureResult;
    }
    
    /**
     * Set the CheckResult we will return when we fail to touch our
     * configured targetClass.
     * @param failureResult desired CheckResult on failure
     */
    void setFailureResult(CheckResult failureResult) {
        this.failureResult = failureResult;
    }
    
    /**
     * Get the CheckResult we will return when we succeed in touching 
     * our configured targetClass.
     * @return CheckResult we will return on success.
     */
    CheckResult getSuccessResult() {
        return this.successResult;
    }
    
    /**
     * Set the CheckResult we will return when we succeed in touching 
     * our configured targetClass.
     * @param successResult desired CheckResult on success.
     */
    void setSuccessResult(CheckResult successResult) {
        this.successResult = successResult;
    }
    
    /**
     * Get the name of the class for which we check.
     * @return the fully qualified name of the class for which we check.
     */
    String getTargetClass() {
        return this.targetClass;
    }
}

