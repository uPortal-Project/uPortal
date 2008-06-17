/* 
 * Copyright 2000 The JA-SIG Collaborative.  All rights reserved.
 *  See license distributed with this file and
 *  available online at http://www.uportal.org/license.html
 */

package org.jasig.portal;

/**
 * Exception thrown by the framework in response to an attempt to override a
 * channel static data parameter when that parameter is not configured to be
 * overridable.
 * 
 * @author andrew.petro@YALE.EDU
 * @author mboyd@sungardsct.com
 * @since 2.6.0
 */
public class IllegalChannelParameterOverrideException extends RuntimeException
{
    /**
     * The name of the channel parameter that could not be overridden.
     */
    private final String parameterName;

    /**
     * The value that the parameter could not be set to because the parameter is
     * not overridable.
     */
    private final String failedValue;

    /**
     * Instantiate this exception, specifying the name of the parameter that
     * could not be overridden and the value it could not be set to.
     */
    public IllegalChannelParameterOverrideException(String parameterNameArg,
            String failedValueArg)
    {
        super("Could not override channel parameter ["
            + parameterNameArg
            + "] with value ["
            + failedValueArg
            + "] because this  parameter is not configured to " 
            + "allow being overridden.");

        this.parameterName = parameterNameArg;
        this.failedValue = failedValueArg;
    }

    /**
     * Get the name of the parameter that could not be overridden.
     */
    public String getParameterName()
    {
        return this.parameterName;
    }

    /**
     * Get the value that the parameter could not be set to.
     */
    public String getFailedValue()
    {
        return this.failedValue;
    }
}
