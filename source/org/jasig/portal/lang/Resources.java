/* Copyright 2004 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/
package org.jasig.portal.lang;
import java.text.MessageFormat;
import java.util.ResourceBundle;
/**
 * The <code>Resources</code> class defines a set of utility methods
 * which translate an internationalized string to a localized
 * string. The transformation is done with the use of standard
 * <code>java.util.ResourceBundle</code> and
 * <code>java.text.MessageFormat</code> classes.
 *
 * @author <a href="mailto:jnielsen@sct.com">Jan Nielsen</a>
 * @version $Revision$ $Date$ 
 **/
public final class Resources {
    /**
     * Returns the localized value of the specified string
     * resource. The string resource is resolved by using the client
     * parameter to lookup the associated <code>ResourceBundle</code>
     * object and returning the string value associated with the
     * specified name.
     * @param client class to use to find the resource bundle
     * @param name name of the string resource
     * @return value of the string resource
     * @throws NullPointerException if client or name is
     * <code>null</code>
     * @throws java.util.MissingResourceException if the resource name
     * is not found
     **/
    public static final String getString(Class client, String name) {
        ResourceBundle bundle = ResourceBundle.getBundle( client.getName());
        return bundle.getString( name );
    }
    /**
     * Returns the localized value of the specified string
     * resource. The string resource is resolved by using the client
     * parameter to lookup the associated <code>ResourceBundle</code>
     * object and returning the string value associated with the
     * specified name. The <code>MessageFormat</code> class is used to
     * format the localized string using the specified runtime
     * parameters.
     * 
     * @param client class to use to find the resource bundle
     * @param name name of the string resource
     * @param objects runtime objects to be inserted into resource
     * @return value of the string resource
     * @throws NullPointerException if client or name is
     * <code>null</code>
     *
     * @throws java.util.MissingResourceException if the resource name
     * is not found
     **/
    public static final String getString(Class client, String name, String[] objects){
        return MessageFormat.format(getString( client, name ), objects);
    }
    /**
     * Private constructor.
     */
    private Resources(){
        // do-nothing constructor
    }
}
