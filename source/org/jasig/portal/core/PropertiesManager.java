/* Copyright 2004 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/

package  org.jasig.portal.core;

import java.io.IOException;
import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Provides access to properties and manages the portal base directory.
 * @author Ken Weiner, kweiner@unicon.net
 * @version $Revision$
 * @since uPortal 2.0
 */
public class PropertiesManager {
    
    protected static final Log log = LogFactory.getLog(PropertiesManager.class);
    
    public static final String PORTAL_PROPERTIES_FILE_SYSTEM_VARIABLE = "portal.properties";
    private static final String PORTAL_PROPERTIES_FILE_NAME = "/properties/portal.properties";
    private static final Properties props = new Properties();
    
    static {
        loadProps();
    }
    
    /**
     * Load up the portal properties.  Right now the portal properties is a simple
     * .properties file with name value pairs.  It may evolve to become an XML file
     * later on.
     */
    protected static void loadProps() {
        try {
            String pfile = System.getProperty(PORTAL_PROPERTIES_FILE_SYSTEM_VARIABLE);
            if (pfile == null) {
                pfile = PORTAL_PROPERTIES_FILE_NAME;
            }
            props.load(PropertiesManager.class.getResourceAsStream(pfile));
        } catch (IOException ioe) {
        	String error = "Unable to read portal.properties file.";
            log.error(error, ioe);
            throw new RuntimeException(error+ioe.toString());
        }
    }
    
    /**
     * Returns the value of a property for a given name.
     * Any whitespace is trimmed off the beginning and
     * end of the property value.    
     * @param name the name of the requested property
     * @return value the value of the property matching the requested name
     */
    public static String getProperty(String name) {
        String val = getPropertyUntrimmed(name);
        return val.trim();
    }
    
    /**
     * Returns the value of a property for a given name
     * including any whitespace that may be at the beginning
     * or end of the property value.
     * A runtime exception is thrown if the property cannot be found.
     * @param name the name of the requested property
     * @return value the value of the property matching the requested name
     */
    public static String getPropertyUntrimmed(String name) {
        String val = props.getProperty(name);
        if (val == null)
            throw new RuntimeException("Property " + name + " not found!");
        return val;
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
     */
    public static boolean getPropertyAsBoolean(String name) {
        boolean retValue = false;
        String value = getProperty(name);
        if (value != null) {
            if (value.equalsIgnoreCase("true") || value.equalsIgnoreCase("yes") || value.equalsIgnoreCase("y") || value.equalsIgnoreCase("on"))
                retValue = true;
        }
        return retValue;
    }
    
    /**
     * Returns the value of a property for a given name as a <code>byte</code>
     * @param name the name of the requested property
     * @return value the property's value as a <code>byte</code>
     */
    public static byte getPropertyAsByte(String name) {
        return Byte.parseByte(getProperty(name));
    }
    
    /**
     * Returns the value of a property for a given name as a <code>short</code>
     * @param name the name of the requested property
     * @return value the property's value as a <code>short</code>
     */
    public static short getPropertyAsShort(String name) {
        return Short.parseShort(getProperty(name));
    }
    
    /**
     * Returns the value of a property for a given name as an <code>int</code>
     * @param name the name of the requested property
     * @return value the property's value as an <code>int</code>
     */
    public static int getPropertyAsInt(String name) {
        return Integer.parseInt(getProperty(name));
    }
    
    /**
     * Returns the value of a property for a given name as a <code>long</code>
     * @param name the name of the requested property
     * @return value the property's value as a <code>long</code>
     */
    public static long getPropertyAsLong(String name) {
        return Long.parseLong(getProperty(name));
    }
    
    /**
     * Returns the value of a property for a given name as a <code>float</code>
     * @param name the name of the requested property
     * @return value the property's value as a <code>float</code>
     */
    public static float getPropertyAsFloat(String name) {
        return Float.parseFloat(getProperty(name));
    }
    
    /**
     * Returns the value of a property for a given name as a <code>long</code>
     * @param name the name of the requested property
     * @return value the property's value as a <code>long</code>
     */
    public static double getPropertyAsDouble(String name) {
        return Double.parseDouble(getProperty(name));
    }
}



