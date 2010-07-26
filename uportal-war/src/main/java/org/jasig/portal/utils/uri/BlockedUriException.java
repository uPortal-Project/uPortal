/**
 * Licensed to Jasig under one or more contributor license
 * agreements. See the NOTICE file distributed with this work
 * for additional information regarding copyright ownership.
 * Jasig licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a
 * copy of the License at:
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
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
     * Serialized format version number.  Developers must manually increment this
     * number whenever this class is changed in such a way that its serialized form
     * changes.
     */
    private static final long serialVersionUID = 1L;

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
