/**
 * Copyright © 2001 The JA-SIG Collaborative.  All rights reserved.
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

package  org.jasig.portal;

import java.io.IOException;
import java.util.Properties;

import org.jasig.portal.services.LogService;

/**
 * Provides access to properties and manages the portal base directory.
 * @author Ken Weiner, kweiner@interactivebusiness.com
 * @version $Revision$
 * @since uPortal 2.0
 */
public class PropertiesManager {

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
  protected static void loadProps () {
    try {
      props.load(PropertiesManager.class.getResourceAsStream(PORTAL_PROPERTIES_FILE_NAME));
    } catch (IOException ioe) {
      LogService.log(LogService.ERROR, "Unable to read portal.properties file.");
      LogService.log(LogService.ERROR, ioe);
    }
  }

  /**
   * Returns the value of a property for a given name.
   * Any whitespace is trimmed off the beginning and
   * end of the property value. A runtime exception is thrown 
   * if the property cannot be found.
   * @param name the name of the requested property
   * @return value the value of the property matching the requested name
   */
  public static String getProperty(String name) {
    String val = props.getProperty(name);
    if (val == null)
      throw new RuntimeException("Property " + name + " not found!");
    else
      val = val.trim();
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
      if (value.equalsIgnoreCase("true") ||
          value.equalsIgnoreCase("yes") ||
          value.equalsIgnoreCase("y") ||
          value.equalsIgnoreCase("on"))
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



