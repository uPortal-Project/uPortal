/* Copyright 2004 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/

package  org.jasig.portal.properties;

import java.util.Collections;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Provides access to properties.
 * <p>It is important to understand that usage of this class is different from what you might
 * be used to in java.util.Properties.  Specifically, when you get a Properties property,
 * if that property is not set, the return value is NULL.  However, when you call the basic getters here,
 * if the property is not set, a RuntimeException is thrown.  These methods will never return null (except
 * if you pass in null as the default return value for the methods that take a default).</p>
 * <p>There are methods to get properties as various primitive types, int, double, float, etc.  
 * When you invoke one of these methods on a property that is found but cannot be parsed as
 * your desired type, a RuntimeException is thrown.</p>
 * <p>There are 
 * corresponding methods which take as a second parameter a default value.  These methods, instead of
 * throwing a RuntimeException when the property cannot be found, return the default value.  You can
 * use the default value "null" to invoke getProperty() with semantics more like the java.util.Properties object.
 * These augmented accessors which take defaults will be, I hope, especially useful in static initializers.  Providing a 
 * default in your static initializer will keep your class from blowing up at initialization when your property cannot be found.
 * This seems especially advantageous when there is a plausible default value.</p>
 * <p>This class has a comprehensive JUnit testcase.  Please keep the testcase up to date with any changes you make to this class.</p>
 * @author Ken Weiner, kweiner@unicon.net
 * @author howard.gilbert@yale.edu
 * @author andrew.petro@yale.edu
 * @version $Revision$ $Date$
 * @since uPortal 2.0
 */
public class PropertiesManager {
    
    protected static final Log log = LogFactory.getLog(PropertiesManager.class);
    
    public static final String PORTAL_PROPERTIES_FILE_SYSTEM_VARIABLE = "portal.properties";
    private static final String PORTAL_PROPERTIES_FILE_NAME = "/properties/portal.properties";
    private static Properties props = null;
    
    /**
     * A set of the names of properties that clients of this class attempt to access
     * but which were not set in the properties file.
     * This Set allows this class to report about missing properties and to
     * log each missing property only the first time it is requested.
     */
    private static final Set missingProperties = Collections.synchronizedSet(new HashSet());
    
    /**
     * Setter method to set the underlying Properties.
     * This is a public method to allow poor-man's static dependency injection of the Properties from wherever you want to get them.
     * If Properties have not been injected before any accessor method is invoked, PropertiesManager will invoke loadProperties() to attempt
     * to load its own properties.  You might call this from a context listener, say.
     * If Properties have already been loaded or injected, this method will overwrite them.
     * @param props - Properties to be injected.
     */
    public static synchronized void setProperties(Properties props){
        PropertiesManager.props = props;
    }
    
    /**
     * Load up the portal properties.  Right now the portal properties is a simple
     * .properties file with name value pairs.  It may evolve to become an XML file
     * later on.
     */
    protected static void loadProps() {
        Properties properties = new Properties();
        try {
            String pfile = System.getProperty(PORTAL_PROPERTIES_FILE_SYSTEM_VARIABLE);
            if (pfile == null) {
                pfile = PORTAL_PROPERTIES_FILE_NAME;
            }
            properties.load(PropertiesManager.class.getResourceAsStream(pfile));
            PropertiesManager.props = properties;
        } catch (Throwable t) {
            log.error("Unable to read portal.properties file.", t);
        }
    }
    
    /**
     * Returns the value of a property for a given name.
     * Any whitespace is trimmed off the beginning and
     * end of the property value.
     * Note that this method will never return null.
     * If the requested property cannot be found, this method throws an UndeclaredPortalException.
     * @param name the name of the requested property
     * @return value the value of the property matching the requested name
     * @throws MissingPropertyException - if the requested property cannot be found
     */
    public static String getProperty(String name) throws MissingPropertyException{
        if (log.isTraceEnabled()){
            log.trace("entering getProperty(" + name + ")");
        }
        if (PropertiesManager.props == null)
            loadProps();
      String val = getPropertyUntrimmed(name);
      val = val.trim();
      if (log.isTraceEnabled()){
        log.trace("returning from getProperty(" + name + ") with return value [" + val + "]");
      }
      return val;
    }
    
    /**
     * Returns the value of a property for a given name
     * including whitespace trailing the property value, but not including
     * whitespace leading the property value.
     * An UndeclaredPortalException is thrown if the property cannot be found.
     * This method will never return null.
     * @param name the name of the requested property
     * @return value the value of the property matching the requested name
     * @throws MissingPropertyException - (undeclared) if the requested property is not found
     */
    public static String getPropertyUntrimmed(String name) throws MissingPropertyException {
        if (PropertiesManager.props == null)
            loadProps();
      String val = props.getProperty(name);
      if (val == null) {
        boolean alreadyReported = registerMissingProperty(name);
            throw new MissingPropertyException(name, alreadyReported);
      }
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
     * @throws MissingPropertyException - when no property of the given name is declared.
     */
    public static boolean getPropertyAsBoolean(String name) throws MissingPropertyException {
        if (PropertiesManager.props == null)
            loadProps();
      boolean retValue = false;
      String value = getProperty(name);
      if (value != null) {
        if (value.equalsIgnoreCase("true") ||
            value.equalsIgnoreCase("yes") ||
            value.equalsIgnoreCase("y") ||
            value.equalsIgnoreCase("on")){
                        retValue = true;
        } else if (value.equalsIgnoreCase("false")
        || value.equalsIgnoreCase("no")
        || value.equalsIgnoreCase("n")
        || value.equalsIgnoreCase("off")){
            retValue = false; 
        } else {
            // this method's historical behavior, maintained here, is to return false for all values that did not match on of the true values above.
            log.error("property [" + name + "] is being accessed as a boolean but had non-canonical value [" + value + "].  Returning it as false, but this may be a property misconfiguration.");
        }
      } else {
        log.fatal("property [" + name + "] is being accessed as a boolean but was null.  Returning false.  However, it should not have been possible to get here because getProperty() throws a runtime exception or returns a non-null value.");
      }
      return retValue;
    }
    
    /**
     * Returns the value of a property for a given name as a <code>byte</code>
     * @param name the name of the requested property
     * @return value the property's value as a <code>byte</code>
     * @throws MissingPropertyException - if the property is not set
     * @throws BadPropertyException - if the property cannot be parsed as a byte
     */
    public static byte getPropertyAsByte(String name) throws MissingPropertyException, BadPropertyException {
        if (PropertiesManager.props == null)
            loadProps();
        try {
            return Byte.parseByte(getProperty(name));
        } catch (NumberFormatException nfe) {
            throw new BadPropertyException(name, getProperty(name), "byte");
        }
      
    }
    
    /**
     * Returns the value of a property for a given name as a <code>short</code>
     * @param name the name of the requested property
     * @return value the property's value as a <code>short</code>
     * @throws MissingPropertyException - if the property is not set
     * @throws BadPropertyException - if the property cannot be parsed as a short or is not set.
     */
    public static short getPropertyAsShort(String name) throws MissingPropertyException, BadPropertyException {
        if (PropertiesManager.props == null)
            loadProps();
        try {
            return Short.parseShort(getProperty(name));
        } catch (NumberFormatException nfe){
            throw new BadPropertyException(name, getProperty(name), "short");
        }
    }
    
    /**
     * Returns the value of a property for a given name as an <code>int</code>
     * @param name the name of the requested property
     * @return value the property's value as an <code>int</code>
     * @throws MissingPropertyException - if the property is not set
     * @throws BadPropertyException - if the property cannot be parsed as an int
     */
    public static int getPropertyAsInt(String name) throws MissingPropertyException, BadPropertyException {
        if (PropertiesManager.props == null)
            loadProps();
        try {
            return Integer.parseInt(getProperty(name));
        } catch (NumberFormatException nfe){
            throw new BadPropertyException(name, getProperty(name), "int");
        }
    }
    
    /**
     * Returns the value of a property for a given name as a <code>long</code>
     * @param name the name of the requested property
     * @return value the property's value as a <code>long</code>
     * @throws MissingPropertyException - if the property is not set
     * @throws BadPropertyException - if the property cannot be parsed as a long
     */
    public static long getPropertyAsLong(String name) throws MissingPropertyException, BadPropertyException {
        if (PropertiesManager.props == null)
            loadProps();
        try {
            return Long.parseLong(getProperty(name));
        } catch (NumberFormatException nfe) {
            throw new BadPropertyException(name, getProperty(name), "long");
        }
    }
    
    /**
     * Returns the value of a property for a given name as a <code>float</code>
     * @param name the name of the requested property
     * @return value the property's value as a <code>float</code>
     * @throws MissingPropertyException - if the property is not set
     * @throws BadPropertyException - if the property cannot be parsed as a float
     */
    public static float getPropertyAsFloat(String name) throws MissingPropertyException, BadPropertyException {
        if (PropertiesManager.props == null)
            loadProps();
        try {
            return Float.parseFloat(getProperty(name));
        } catch (NumberFormatException nfe) {
            throw new BadPropertyException(name, getProperty(name), "float");
        }

    }
    
    /**
     * Returns the value of a property for a given name as a <code>long</code>
     * @param name the name of the requested property
     * @return value the property's value as a <code>double</code>
     * @throws MissingPropertyException - if the property has not been set
     * @throws BadPropertyException - if the property cannot be parsed as a double or is not set.
     */
    public static double getPropertyAsDouble(String name) throws MissingPropertyException, BadPropertyException {
        if (PropertiesManager.props == null)
            loadProps();
        try {
            return Double.parseDouble(getProperty(name));
        } catch (NumberFormatException nfe) {
            throw new BadPropertyException(name, getProperty(name), "double");
        }
    }

    /**
     * Registers that a given property was sought but not found.
     * Currently adds the property to the set of missing properties and 
     * logs if this is the first time the property has been requested.
     * @param name - the name of the missing property
     * @return true if the property was previously registered, false otherwise
    * 
    */
   private static boolean registerMissingProperty(String name) {
       final boolean previouslyReported = !PropertiesManager.missingProperties.add(name);
       
       if (!previouslyReported){
         log.info("Property [" + name + "] was requested but not found.");
       }
       return previouslyReported;
   }
    
    /**
     * Get the value of the property with the given name.
     * If the named property is not found, returns the supplied default value.
     * This error handling behavior makes this method attractive for use in static initializers.
     * @param name - the name of the property to be retrieved.
     * @param defaultValue - a fallback default value which will be returned if the property cannot be found.
     * @return the value of the requested property, or the supplied default value if the named property cannot be found.
     * @since uPortal 2.4
     */
    public static String getProperty(String name, String defaultValue) {
        if (PropertiesManager.props == null)
            loadProps();
        String returnValue = defaultValue;
        try {
            returnValue = getProperty(name);
        } catch (MissingPropertyException mpe){
            // Do nothing, since we have already recorded and logged the missing property.
        }
        return returnValue;
    }


    
    /**
     * Get the value of a property for the given name 
     * including any whitespace that may be at the beginning or end of the property value.
     * This method returns the supplied default value if the requested property cannot be found.
     * This error handling behavior makes this method attractive for use in static initializers.
     * @param name - the name of the requested property
     * @param defaultValue - a default value to fall back on if the property cannot be found
     * @return the value of the property with the given name, or the supplied default value if the property could not be found.
     * @since uPortal 2.4
     */
    public static String getPropertyUntrimmed(String name, String defaultValue) {
        if (PropertiesManager.props == null)
            loadProps();
        String returnValue = defaultValue;
        try {
            returnValue = getPropertyUntrimmed(name);
        } catch (MissingPropertyException mpe) {
            // do nothing, since we have already logged the missing property
        }
        return returnValue;

    }





    /**
     * Get a property as a boolean, specifying a default value.
     * If for any reason we are unable to lookup the desired property, 
     * this method returns the supplied default value.
     * This error handling behavior makes this method suitable for calling from static initializers.
     * @param name - the name of the property to be accessed
     * @param defaultValue - default value that will be returned in the event of any error
     * @return the looked up property value, or the defaultValue if any problem.
     * @since uPortal 2.4
     */
    public static boolean getPropertyAsBoolean(final String name, final boolean defaultValue){
        if (PropertiesManager.props == null)
            loadProps();
        boolean returnValue = defaultValue;
        try {
            returnValue = getPropertyAsBoolean(name);
        } catch (MissingPropertyException mpe) {
            // do nothing, since we already logged the missing property
        }
        return returnValue;
    }


    
    /**
     * Get the value of the given property as a byte, specifying a fallback default value.
     * If for any reason we are unable to lookup the desired property,
     * this method returns the supplied default value.
     * This error handling behavior makes this method suitable for calling from static initializers.
     * @param name - the name of the property to be accessed
     * @param defaultValue - the default value that will be returned in the event of any error
     * @return the looked up property value, or the defaultValue if any problem.
     * @since uPortal 2.4
     */
    public static byte getPropertyAsByte(final String name, final byte defaultValue) {
        if (PropertiesManager.props == null)
            loadProps();
        byte returnValue = defaultValue;
        try {
            returnValue = getPropertyAsByte(name);
        } catch (Throwable t){
            log.error("Could not retrieve or parse as byte property [" + name + "], defaulting to [" + defaultValue + "]", t);
        }
        return returnValue;
    }
    
    /**
     * Returns the value of a property for a given name as a short.
     * If for any reason the property cannot be looked up as a short, returns the supplied default value.
     * This error handling makes this method a good choice for static initializer calls.
     * @param name - the name of the requested property
     * @param defaultValue - a default value that will be returned in the event of any error
     * @return the property value as a short or the default value in the event of any error
     * @since uPortal 2.4
     */
    public static short getPropertyAsShort(String name, short defaultValue){
        if (PropertiesManager.props == null)
            loadProps();
        short returnValue = defaultValue;
        try {
            returnValue = getPropertyAsShort(name);
        } catch (Throwable t) {
            log.error("Could not retrieve or parse as short property [" + name + "], defaulting to given value [" + defaultValue + "]", t);
        }
        return returnValue;
    }
    
    /**
     * Get the value of a given property as an int.
     * If for any reason the property cannot be looked up as an int, returns the supplied default value.
     * This error handling makes this method a good choice for static initializer calls.
     * @param name - the name of the requested property
     * @param defaultValue - a fallback default value for the property
     * @return the value of the property as an int, or the supplied default value in the event of any problem.
     * @since uPortal 2.4
     */
    public static int getPropertyAsInt(String name, int defaultValue){
        if (PropertiesManager.props == null)
            loadProps();
        int returnValue = defaultValue;
        try {
            returnValue = getPropertyAsInt(name);
        } catch (Throwable t) {
            log.error("Could not retrieve or parse as int the property [" + name + "], defaulting to " + defaultValue, t);
        }
        return returnValue;
    }
    
    /**
     * Get the value of the given property as a long.
     * If for any reason the property cannot be looked up as a long, returns the supplied default value.
     * This error handling makes this method a good choice for static initializer calls.
     * @param name - the name of the requested property
     * @param defaultValue - a fallback default value that will be returned if there is any problem
     * @return the value of the property as a long, or the supplied default value if there is any problem.
     * @since uPortal 2.4
     */
    public static long getPropertyAsLong(String name, long defaultValue) {
        if (PropertiesManager.props == null)
            loadProps();
        long returnValue = defaultValue;
        try {
            returnValue = getPropertyAsLong(name);
        } catch (Throwable t) {
            log.error("Could not retrieve or parse as long property [" + name + "], defaulting to " + defaultValue, t);
        }
        return returnValue;
    }

    /**
     * Get the value of the given property as a float.
     * If for any reason the property cannot be looked up as a float, returns the supplied default value.
     * This error handling makes this method a good choice for static initializer calls.
     * @param name - the name of the requested property
     * @param defaultValue - a fallback default value that will be returned if there is any problem
     * @return the value of the property as a float, or the supplied default value if there is any problem.
     * @since uPortal 2.4
     */
    public static float getPropertyAsFloat(String name, float defaultValue) {
        if (PropertiesManager.props == null)
            loadProps();
        float returnValue = defaultValue;
        try {
            returnValue = getPropertyAsFloat(name);
        } catch (Throwable t) {
            log.error("Could not retrieve or parse as float property [" + name + "], defaulting to " + defaultValue, t);
        }
        return returnValue;
    }
    
    /**
     * Get the value of the given property as a double.
     * If for any reason the property cannot be looked up as a double, returns the specified default value.
     * This error handling makes this method a good choice for static initializer calls.
     * @param name - the name of the requested property
     * @param defaultValue - a fallback default value that will be returned if there is any problem
     * @return the value of the property as a double, or the supplied default value if there is any problem.
     * @since uPortal 2.4
     */
    public static double getPropertyAsDouble(String name, double defaultValue){
        if (PropertiesManager.props == null)
            loadProps();
        double returnValue = defaultValue;
        try {
            returnValue = getPropertyAsDouble(name);
        } catch (Throwable t) {
            log.error("Could not retrieve or parse as double property [" + name + "], defaulting to " + defaultValue, t);
        }
        return returnValue;
    }
    
    /**
     * Get a Set of the names of properties that have been requested but were not set.
     * @return a Set of the String names of missing properties.
     * @since uPortal 2.4
     */
    public static Set getMissingProperties(){
        return PropertiesManager.missingProperties;
    }
}