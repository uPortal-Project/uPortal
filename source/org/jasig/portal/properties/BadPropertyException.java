/* Copyright 2004 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/

package org.jasig.portal.properties;

/**
 * This is an undeclared (Runtime) exception to be thrown by the 
 * PropertiesManager when a property is requested but cannot be 
 * parsed and returned as the desired type.
 * @author andrew.petro@yale.edu
 * @version $Revision$ $Date$
 */
public class BadPropertyException extends RuntimeException {
    
    /** The name of the property */
    private final String propertyName;
    
    /** The value of the property */
    private final String propertyValue;
    
    /** The desired type, as which the property could not be parsed. */
    private final String desiredType;
    
    /**
     * Instantiate a new BadPropertyException.
     * @param propertyName - the name of the property
     * @param propertyValue - the bad value of the property.
     * @param desiredType - the name of the desired type which the value wasn't, making it bad.
     */
    public BadPropertyException(
        String propertyName,
        String propertyValue,
        String desiredType) {
        this.propertyName = propertyName;
        this.propertyValue = propertyValue;
        this.desiredType = desiredType;
    }
    
    /**
     * Instantiate a new BadPropertyException with the given underlying cause.
     * @param propertyName - the name of the property
     * @param propertyValue - the bad value of the property.
     * @param desiredType - the name of the desired type which the value wasn't, making it bad.
     * @param cause - underlying cause
     */
    public BadPropertyException(
        String propertyName,
        String propertyValue,
        String desiredType, 
        Throwable cause) {
        super(cause);
        this.propertyName = propertyName;
        this.propertyValue = propertyValue;
        this.desiredType = desiredType;
    }
    
    public String getMessage(){
        return "The property [" + this.propertyName + "] had value [" + 
        this.propertyValue + "] which could not be parsed as type [" + 
        this.desiredType + "].";
    }
    
    public String getLocalizedMessage(){
        return getMessage();
    }
    
    /**
     * Get the desired type as which the property could not be parsed.
     * @return Returns the desiredType.
     */
    public String getDesiredType() {
        return this.desiredType;
    }
    /**
     * Get the name of the bad property.
     * @return Returns the propertyName.
     */
    public String getPropertyName() {
        return this.propertyName;
    }
    /**
     * Get the actual value of the property.
     * @return Returns the propertyValue.
     */
    public String getPropertyValue() {
        return this.propertyValue;
    }
}