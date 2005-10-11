/* Copyright 2005 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/

package org.jasig.portal.utils.uri;

import java.io.Serializable;
import java.net.URI;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


/**
 * UriScrutinizer implementation matching URIs against allowed prefixes
 * and disallowed prefixes to determine whether to block the URI.
 * 
 * <p>The URI will be allowed if it is prefixed by at least one of the allowed
 * prefixes and it is not prefixed by any of the blocked prefixes.</p>
 * 
 * <p>Instances of this class are immutable once constructed.</p>
 * <p>Instances of this class are threadsafe and serializable.</p>
 * @since uPortal 2.5.1
 */
public final class PrefixUriScrutinizer 
    implements IUriScrutinizer, Serializable {

    private final Log log = LogFactory.getLog(getClass());
    
    /**
     * Allowed prefixes for URIs examined by this scrutinizer instance.
     * URIs must match at least one of these prefixes.
     */
    private final String[] allowPrefixes;
    
    /**
     * Blocked prefixes for URIs examined by this scrutinizer instance.
     * URIs must not match any of these prefixes.
     */
    private final String[] denyPrefixes;
    
    /**
     * Create a new PrefixUriScrutinizer instance specifying the allowed
     * URI prefixes and the blocked URI prefixes.  Both arguments must not be
     * null or contain null references. This instance will block all URIs if 
     * allowPrefixesArg is empty.  This constructor will copy the argument
     * arrays, normalizing the prefix content to all-lowercase.
     * @param allowPrefixesArg non-null potentially empty array of Strings
     * @param denyPrefixesArg non-null potentially empty array of Strings
     */
    public PrefixUriScrutinizer(final String[] allowPrefixesArg, 
            final String[] denyPrefixesArg) {
        
        // method implementation is relatively long and complex because it
        // is doing argument checking and normalization.
        
        if (allowPrefixesArg == null) {
            throw new IllegalArgumentException("Cannot construct " +
            		"PrefixUriScrutinizer with null array of allow prefixes.");
        }
        
        // copy prefixes, normallizing case to lowercase
        
        String[] lowercaseAllowPrefixes = new String[allowPrefixesArg.length];
        for (int i = 0; i < allowPrefixesArg.length; i++) {
            String allowPrefix = allowPrefixesArg[i];
            if (allowPrefix == null) {
                throw new IllegalArgumentException("Illegal null in allowPrefixesArg: " + allowPrefixesArg);
            }
            lowercaseAllowPrefixes[i] = allowPrefix.toLowerCase();
        }
        
        this.allowPrefixes = lowercaseAllowPrefixes;
        
        
        if (denyPrefixesArg == null) {
            throw new IllegalArgumentException("Cannot construct " +
            		"PrefixUriScrutinizer with null array of deny prefixes.");
        }
        
        String[] lowercaseDenyPrefixes = new String[denyPrefixesArg.length];
        for (int i = 0; i < denyPrefixesArg.length; i++) {
            String denyPrefix = denyPrefixesArg[i];
            if (denyPrefix == null) {
                throw new IllegalArgumentException("Illegal null in denyPrefixesArg array.");
            }
            lowercaseDenyPrefixes[i] = denyPrefix.toLowerCase();
        }
        
        this.denyPrefixes = lowercaseDenyPrefixes;
    }

    public void scrutinize(final URI uriArg) throws BlockedUriException {
        
        if (log.isTraceEnabled()) {
            log.trace("Examinging [" + uriArg + "] with scrutinizer " + this);
        }
        
        if (uriArg == null) {
            throw new IllegalArgumentException("Cannot scrutinize a null URI.");
        }
        
        // normalize to block devious URIs -- see testcase
        URI normalizedUri = uriArg.normalize();
        
        String uriString = normalizedUri.toString();
        String lowercaseUriString = uriString.toLowerCase();
        
        // default to not accepting the parameter value
        boolean acceptParamValue = false;

        // for each allowable prefix, check for match.

        for (int allowablePrefixNum = 0; 
            allowablePrefixNum < this.allowPrefixes.length; allowablePrefixNum++) {
            
            String allowablePrefix = this.allowPrefixes[allowablePrefixNum];
            if (lowercaseUriString.startsWith(
                    allowablePrefix)) {
                acceptParamValue = true;
                
                // break out of the for loop.  
                // Only need one allowable prefix match.
                break;
            }
        }

        // if no match, fail
        if (!acceptParamValue) {
            throw new BlockedUriException(uriArg, 
                    "URI not prefixed by any of the allowed prefixes (" + this.allowPrefixes + ")");
        }
        
        for (int blockedPrefixNum = 0; 
              blockedPrefixNum < this.denyPrefixes.length; blockedPrefixNum++) {
            
            String blockedPrefix = this.denyPrefixes[blockedPrefixNum];
            if (lowercaseUriString.startsWith(
                    blockedPrefix)) {
                
                throw new BlockedUriException(uriArg,
                        "URI matched blocked prefix: " + blockedPrefix);
                
            }
        }
        
        
    }
    
    public String toString() {
        return "PrefixUriScrutinizer allow:" + this.allowPrefixes 
            + " deny:" + this.denyPrefixes;
    }
    
}
