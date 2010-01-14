/**
 * Licensed to Jasig under one or more contributor license
 * agreements. See the NOTICE file distributed with this work
 * for additional information regarding copyright ownership.
 * Jasig licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a
 * copy of the License at:
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.jasig.portal.properties;


/**
 * An undeclared (Runtime) exception to be thrown by the PropertiesManager
 * when a property is requested but cannot be found.
 * @author andrew.petro@yale.edu
 * @version $Revision$ $Date$
 * @since uPortal 2.4
 */
public class MissingPropertyException extends RuntimeException {

    /** The name of the missing property.*/
    private final String propertyName;
    
    /** True if a prior MissingPropertyException has been thrown for this 
     * missing property by the throwing object if it keeps track.  False otherwise.
     */
    private final boolean alreadyReported;
    
	/**
	 * Instantiate a MissingPropertyException for a particular missing property.
	 * @param propertyName - the name of the property the value of which could not be found.
	 */
	public MissingPropertyException(String propertyName){
		this.propertyName = propertyName;
        this.alreadyReported = false;
	}
    
    /**
     * Instantiate a MissingPropertyException for a particular missing property,
     * indicating whether the throwing object has already thrown a MissingPropertyException for this 
     * property.
     * @param propertyName name of missing property
     * @param alreadyReported true if already reported
     */
    public MissingPropertyException(String propertyName, 
            boolean alreadyReported){
        this.propertyName = propertyName;
        this.alreadyReported = alreadyReported;
    }
    
    /**
     * Instantiate a MissingPropertyException for a particular missing property,
     * indicating whether the throwing object has already thrown a MissingPropertyException for this 
     * property, and supplying an underlying cause.
     * @param propertyName name of missing property
     * @param alreadyReported true if already reported
     * @param cause underlying cause
     */
    public MissingPropertyException(String propertyName, 
            boolean alreadyReported,
            Throwable cause){
        super(cause);
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