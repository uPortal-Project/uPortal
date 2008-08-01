/**
 * Copyright 2007 The JA-SIG Collaborative.  All rights reserved.
 * See license distributed with this file and
 * available online at http://www.uportal.org/license.html
 */
package org.jasig.portal.tools.checks;

import java.util.Arrays;

import org.apache.commons.lang.Validate;

/**
 * Allows for checking if a method exists on a class
 * 
 * @author Eric Dalquist
 * @version $Revision$
 */
public class MethodPresenceCheck extends ClassPresenceCheck {
    private final String targetMethod;
    private final Class<?>[] arguments;

    public MethodPresenceCheck(String targetClass, String targetMethod) {
        super(targetClass);
        
        Validate.notNull(targetMethod, "targetMethod can not be null");
        
        this.targetMethod = targetMethod;
        this.arguments = new Class<?>[0];
    }
    
    public MethodPresenceCheck(String targetClass, String targetMethod, Class<?>[] arguments) {
        super(targetClass);
        
        Validate.notNull(targetMethod, "targetMethod can not be null");
        Validate.notNull(arguments, "arguments can not be null");
        Validate.noNullElements(arguments, "arguments can not contain null elements");
        
        this.targetMethod = targetMethod;
        this.arguments = arguments;
    }

    
    /* (non-Javadoc)
     * @see org.jasig.portal.tools.checks.ClassPresenceCheck#doCheckInternal()
     */
    @Override
    protected CheckResult doCheckInternal() {
        final CheckResult classCheckResult = super.doCheckInternal();
        if (!classCheckResult.isSuccess()) {
            return classCheckResult;
        }
        
        final Class<?> targetClass;
        try {
            targetClass = getClass().getClassLoader().loadClass(this.getTargetClass());
        }
        catch (ClassNotFoundException e) {
            return this.getFailureResult();
        }
        
        try {
            targetClass.getMethod(this.targetMethod, this.arguments);
        }
        catch (SecurityException e) {
            return CheckResult.createFailure(
                    "Could not access method '" + this.targetMethod + "' " +
            		"with arguments " + Arrays.asList(this.arguments) + " " +
    				"on class '" + this.getTargetClass() + "'", this.getRemediationAdvice());
        }
        catch (NoSuchMethodException e) {
            return CheckResult.createFailure(
                    "Method '" + this.targetMethod + "' " +
                    "with arguments " + Arrays.asList(this.arguments) + " " +
                    "does not exist on class '" + this.getTargetClass() + "'", this.getRemediationAdvice());
        }
        
        return CheckResult.createSuccess(
                    "Found method  '" + this.targetMethod + "' " +
                    "with arguments " + Arrays.asList(this.arguments) + " " +
                    "on class '" + this.getTargetClass() + "'");
    }
    
    
}
