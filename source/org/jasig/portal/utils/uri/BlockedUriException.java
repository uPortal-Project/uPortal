/* Copyright 2005 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/

package org.jasig.portal.utils.uri;

import java.net.URI;

/**
 * Exceptional circumstance of a requested URI being blocked by local policy.
 * Conveys the URI that was blocked and the reason it was blocked.
 * @since uPortal 2.5.1
 */
public class BlockedUriException 
    extends RuntimeException {

    /**
     * The URI that was blocked.
     */
    private final URI uri;
    
    /**
     * The reason the URI was blocked.
     */
    private final String reasonBlocked;
    
    /**
     * Create unchained exception instance.
     * @param uriArg URI being blocked
     * @param reasonBlockedArg reason for blocking the URI
     */
    public BlockedUriException(URI uriArg, String reasonBlockedArg) {
        super("Blocked URI [" + uriArg + "] because: " + reasonBlockedArg);
        this.uri = uriArg;
        this.reasonBlocked  = reasonBlockedArg;
    }
    
    /**
     * Create chained exception instance.
     * @param uriArg URI being blocked
     * @param reasonBlockedArg reason the URI was blocked.
     * @param cause underlying cause for block.
     */
    public BlockedUriException(URI uriArg, String reasonBlockedArg, Throwable cause) {
        super("Blocked URI: " + uriArg + " because: " + reasonBlockedArg, cause);
        this.uri = uriArg;
        this.reasonBlocked = reasonBlockedArg;
    }
    
    public String getReasonBlocked() {
        return this.reasonBlocked;
    }
    
    public URI getBlockedUri() {
        return this.uri;
    }
    
}
