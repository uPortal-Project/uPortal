/*
 * Copyright © 2004 The JA-SIG Collaborative.  All rights reserved.
 * See notice at end of file.
 */

package  org.jasig.portal;

import org.jasig.portal.properties.BadPropertyException;
import org.jasig.portal.properties.MissingPropertyException;

/**
 * Deprecated PropertiesManager.
 * This class still exists here at org.jasig.portal.PropertiesManager in order to delegate all requests to the
 * real PropertiesManager at org.jasig.portal.properties.PropertiesManager so as not to break backwards compatibility.
 * Pre 2.4 code which imports this old PropertiesManager should still work great.
 * However, note that there is new functionality to be had from the new PropertiesManager in the form of reporting
 * of missing properties and methods which take default values.
 * Please change your imports to import the new PropertiesManager.  
 * Presumably this deprecated class will disappear in a future uPortal release.
 * @author andrew.petro@yale.edu - deprecated and moved.  see org.jasig.portal.properties.PropertiesManager
 * @version $Revision$ $Date$
 * @since uPortal 2.0
 * @deprecated since uPortal 2.4, this class has moved to org.jasig.portal.properties
 */
public class PropertiesManager {
    
    
    /**
     * Returns the value of a property for a given name.
     * @param name the name of the requested property
     * @return value the value of the property matching the requested name
     * @throws MissingPropertyException - if the requested property cannot be found
     */
    public static String getProperty(String name) throws MissingPropertyException{
       return org.jasig.portal.properties.PropertiesManager.getProperty(name);
    }
    
    /**
     * Returns the value of a property for a given name
     * including whitespace trailing the property value, but not including
     * whitespace leading the property value.
     * @param name the name of the requested property
     * @return value the value of the property matching the requested name
     * @throws MissingPropertyException - (undeclared) if the requested property is not found
     */
    public static String getPropertyUntrimmed(String name) throws MissingPropertyException {
      return org.jasig.portal.properties.PropertiesManager.getPropertyUntrimmed(name);
    }
    
    /**
     * Returns the value of a property for a given name.
     * This method can be used if the property is boolean in
     * nature and you want to make sure that <code>true</code> is
     * returned if the property is set to "true", "yes", "y", or "on"
     * (regardless of case),
     * and <code>false</code> is returned in all other cases.
     * @param name the name of the requested property
     * @return value <code>true</code> if property is set to "true", "yes", "y", or "on" regardless of case, otherwise <code>false</code>
     * @throws MissingPropertyException - when no property of the given name is declared.
     */
    public static boolean getPropertyAsBoolean(String name) throws MissingPropertyException {
      return org.jasig.portal.properties.PropertiesManager.getPropertyAsBoolean(name);
    }
    
    /**
     * Returns the value of a property for a given name as a <code>byte</code>
     * @param name the name of the requested property
     * @return value the property's value as a <code>byte</code>
     * @throws MissingPropertyException - if the property is not set
     * @throws BadPropertyException - if the property cannot be parsed as a byte
     */
    public static byte getPropertyAsByte(String name) throws MissingPropertyException, BadPropertyException {
        return org.jasig.portal.properties.PropertiesManager.getPropertyAsByte(name);
    }
    
    /**
     * Returns the value of a property for a given name as a <code>short</code>
     * @param name the name of the requested property
     * @return value the property's value as a <code>short</code>
     * @throws MissingPropertyException - if the property is not set
     * @throws BadPropertyException - if the property cannot be parsed as a short or is not set.
     */
    public static short getPropertyAsShort(String name) throws MissingPropertyException, BadPropertyException {
       return org.jasig.portal.properties.PropertiesManager.getPropertyAsShort(name);
    }
    
    /**
     * Returns the value of a property for a given name as an <code>int</code>
     * @param name the name of the requested property
     * @return value the property's value as an <code>int</code>
     * @throws MissingPropertyException - if the property is not set
     * @throws BadPropertyException - if the property cannot be parsed as an int
     */
    public static int getPropertyAsInt(String name) throws MissingPropertyException, BadPropertyException {
        return org.jasig.portal.properties.PropertiesManager.getPropertyAsInt(name);
    }
    
    /**
     * Returns the value of a property for a given name as a <code>long</code>
     * @param name the name of the requested property
     * @return value the property's value as a <code>long</code>
     * @throws MissingPropertyException - if the property is not set
     * @throws BadPropertyException - if the property cannot be parsed as a long
     */
    public static long getPropertyAsLong(String name) throws MissingPropertyException, BadPropertyException {
        return org.jasig.portal.properties.PropertiesManager.getPropertyAsLong(name);
    }
    
    /**
     * Returns the value of a property for a given name as a <code>float</code>
     * @param name the name of the requested property
     * @return value the property's value as a <code>float</code>
     * @throws MissingPropertyException - if the property is not set
     * @throws BadPropertyException - if the property cannot be parsed as a float
     */
    public static float getPropertyAsFloat(String name) throws MissingPropertyException, BadPropertyException {
       return org.jasig.portal.properties.PropertiesManager.getPropertyAsFloat(name);
    }
    
    /**
     * Returns the value of a property for a given name as a <code>long</code>
     * @param name the name of the requested property
     * @return value the property's value as a <code>double</code>
     * @throws MissingPropertyException - if the property has not been set
     * @throws BadPropertyException - if the property cannot be parsed as a double or is not set.
     */
    public static double getPropertyAsDouble(String name) throws MissingPropertyException, BadPropertyException {
       return org.jasig.portal.properties.PropertiesManager.getPropertyAsDouble(name);
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
