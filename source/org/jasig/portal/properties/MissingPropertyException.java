/*
 * Copyright © 2004 The JA-SIG Collaborative.  All rights reserved.
 * See notice at end of file.
 */
package org.jasig.portal.properties;


/**
 * An undeclared (Runtime) exception to be thrown by the PropertiesManager
 * when a property is requested but cannot be found.
 * @author andrew.petro@yale.edu
 */
public class MissingPropertyException extends RuntimeException {

    /** The name of the missing property.*/
    private String propertyName;
    
    /** True if a prior MissingPropertyException has been thrown for this 
     * missing property by the throwing object if it keeps track.  False otherwise.
     */
    private boolean alreadyReported = false;
    
	/**
	 * Instantiate a MissingPropertyException for a particular missing property.
	 * @param propertyName - the name of the property the value of which could not be found.
	 */
	public MissingPropertyException(String propertyName){
		this.propertyName = propertyName;
	}
    
    /**
     * Instantiate a MissingPropertyException for a particular missing property,
     * indicating whether the throwing object has already thrown a MissingPropertyException for this 
     * property.
     * @param propertyName
     * @param alreadyReported
     */
    public MissingPropertyException(String propertyName, boolean alreadyReported){
        this.propertyName = propertyName;
        this.alreadyReported = alreadyReported;
    }
    
    public String getMessage(){
        return "The property [" + this.propertyName + "] could not be found.";
    }
    
    /**
     * Has the throwing object already reported (thrown a MissingPropertyException for) this
     * particular missing property.
     * Objects handling this exception might choose to predicate their logging detail on this 
     * property, for instance.
     * @return Returns true this property has already 
     * been reported as missing by the throwing object.
     */
    public boolean isAlreadyReported() {
        return this.alreadyReported;
    }
    
    
    /**
     * Get the name of the missing property.
     * @return Returns the name of the missing property.
     */
    public String getPropertyName() {
        return this.propertyName;
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
