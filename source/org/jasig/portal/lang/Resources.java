/**
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
 *
 * @version "$Revision$"
 **/
public final class Resources
{
    /**
     * Returns the localized value of the specified string
     * resource. The string resource is resolved by using the client
     * parameter to lookup the associated <code>ResourceBundle</code>
     * object and returning the string value associated with the
     * specified name.
     * 
     * @param client class to use to find the resource bundle
     * 
     * @param name name of the string resource
     * 
     * @return value of the string resource
     * 
     * @throws NullPointerException if client or name is
     * <code>null</code>
     *
     * @throws java.util.MissingResourceException if the resource name
     * is not found
     **/
    public static final String getString(
        Class client,
        String name
        )
    {
        ResourceBundle bundle = ResourceBundle.getBundle(
            client.getName()
            );
        
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
     * 
     * @param name name of the string resource
     * 
     * @param objects runtime objects to be inserted into resource
     * 
     * @return value of the string resource
     * 
     * @throws NullPointerException if client or name is
     * <code>null</code>
     *
     * @throws java.util.MissingResourceException if the resource name
     * is not found
     **/
    public static final String getString(
        Class client,
        String name,
        String[] objects
        )
    {
        return MessageFormat.format(
            getString( client, name ),
            objects
            );
    }

    /**
     * Private constructor.
     **/
    private Resources(){}
}
