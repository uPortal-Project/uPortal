/*
 * Copyright © 2004 The JA-SIG Collaborative.  All rights reserved.
 * See notice at end of file.
 */
package org.jasig.portal.properties;

/**
 * This is an undeclared (Runtime) exception to be thrown by the 
 * PropertiesManager when a property is requested but cannot be 
 * parsed and returned as the desired type.
 * @author andrew.petro@yale.edu
 */
public class BadPropertyException extends RuntimeException {
    
    /** The name of the property */
    private String propertyName;
    
    /** The value of the property */
    private String propertyValue;
    
    /** The desired type, as which the property could not be parsed. */
    private String desiredType;
    
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
    
    public String getMessage(){
        return "The property [" + this.propertyName + "] had value [" + this.propertyValue + "] which could not be parsed as type [" + this.desiredType + "].";
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

/*
 * Copyright © 2004 The JA-SIG Collaborative.  All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. Redistributions of any form whatsoever must retain the following
 *    acknowledgment:
 *    "This product includes software developed by the JA-SIG Collaborative
 *    (http://www.jasig.org/)."
 *
 * THIS SOFTWARE IS PROVIDED BY THE JA-SIG COLLABORATIVE "AS IS" AND ANY
 * EXPRESSED OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL THE JA-SIG COLLABORATIVE OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
 * NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 * OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 */
