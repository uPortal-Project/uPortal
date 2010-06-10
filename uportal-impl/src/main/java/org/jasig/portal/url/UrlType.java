/**
 * Copyright 2007 The JA-SIG Collaborative.  All rights reserved.
 * See license distributed with this file and
 * available online at http://www.uportal.org/license.html
 */
package org.jasig.portal.url;

/**
 * Represents the request type of the url, all available request types should be enumerated here
 * 
 * @author Eric Dalquist
 * @version $Revision$
 */
public enum UrlType {
    /* IMPORTANT, all enum keys must be completely upper case for the helper methods to not cause problems */
    
    /**
     * Renders content
     */
    RENDER,
    /**
     * Performs an action, the result from this type of URL will always be a redirect
     */
    ACTION;
    
    private final String lowercase;
    
    private UrlType() {
        this.lowercase = this.toString().toLowerCase();
    }
    
    public String toLowercaseString() {
        return this.lowercase;
    }
    
    public static UrlType valueOfIngoreCase(String name) {
        return UrlType.valueOf(name.toUpperCase());
    }
    
    public static UrlType valueOfIngoreCase(String name, UrlType defaultValue) {
        if (name == null) {
            return defaultValue;
        }
        
        try {
            return valueOfIngoreCase(name);
        }
        catch (IllegalArgumentException e) {
            return defaultValue;
        }
    }
}