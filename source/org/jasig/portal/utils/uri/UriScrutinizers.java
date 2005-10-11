/* Copyright 2005 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/
package org.jasig.portal.utils.uri;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Static helper class containing static instances of immutable IURIScrutinizer
 * instances.
 * 
 * Currently, contains just one scrutinizer instance: a 
 * PrefixURIScrutinizer configured to allow http:// and https:// URIs .
 */
public class UriScrutinizers {

    private static final Log log = LogFactory.getLog(UriScrutinizers.class);
    
    /**
     * Array used in contructing the HTTP_HTTPS scrutinizer.
     */
    private static final String[] HTTP_HTTPS_STRING_ARRAY =
    {"http://", "https://" };
   
    /**
     * Static instance of a scrutinizer which restricts URIs to only using
     * the http:// and https:// methods.
     */
    public static final IUriScrutinizer HTTP_HTTPS =
        new PrefixUriScrutinizer(HTTP_HTTPS_STRING_ARRAY, new String[0]);
    

    private UriScrutinizers() {
        // private constructor prevents this class from being instantiated
        // or subclassed.  This class is designed to be used only as a static
        // helper.
    }
}
