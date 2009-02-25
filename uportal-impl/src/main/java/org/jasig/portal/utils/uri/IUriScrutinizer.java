/**
 * Copyright (c) 2000-2009, Jasig, Inc.
 * See license distributed with this file and available online at
 * https://www.ja-sig.org/svn/jasig-parent/tags/rel-10/license-header.txt
 */
package org.jasig.portal.utils.uri;

import java.net.URI;

/**
 * UriScrutinizers examine URIs to see if they should be blocked for reasons
 * of policy.
 * @since uPortal 2.5.1
 */
public interface IUriScrutinizer {

    /**
     * Scrutinize a URI to determine if access to it should be blocked for
     * reasons of policy. Throws BlockedUriException if access to the URI
     * should be blocked, conveying the reason for blockage.  
     * 
     * Blocking a URI is an exceptional and ideally rare circumstance
     * which will usually abort whatever operation was being undertaken, and so
     * this method throws on that exceptional circumstance.
     * @param uri non-null URI for examination
     * @throws BlockedUriException if access should be blocked
     */
    public void scrutinize(URI uri) throws BlockedUriException;
    
}
