/**
 * Copyright © 2003 The JA-SIG Collaborative.  All rights reserved.
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
package org.jasig.portal.channels.wsrp;

import org.jasig.portal.wsrp.types.CacheControl;

/**
 * A wrapper for portlet markup that gets placed into a markup cache
 * and keeps track of the time the markup was cached and when the
 * markup expires.
 * @author Ken Weiner, kweiner@unicon.net
 * @version $Revision$
 */
public class MarkupWrapper {

    private String markup;
    private CacheControl cacheControl;
    private long cacheTime;
    
    /**
     * Constructs a MarkupWrapper.
     * @param markup the portlet markup to cache
     * @param cacheControl the cache control structure
     */
    public MarkupWrapper(String markup, CacheControl cacheControl) {
        this.markup = markup;
        this.cacheControl = cacheControl;
        this.cacheTime = System.currentTimeMillis();
    }
        
    // Getters
    public long getCacheTime() { return this.cacheTime; }
    public CacheControl getCacheControl() { return this.cacheControl; }
    public String getMarkup() { return this.markup; }
    
    // Setters
    public void setCacheTime(long cacheTime) { this.cacheTime = cacheTime; }
    public void setCacheControl(CacheControl cacheControl) { this.cacheControl = cacheControl; }
    public void setMarkup(String markup) { this.markup = markup; }
    
    /**
     * Reveals whether or not this markup has been cached longer
     * than its expiration time.  It is possible that the cache
     * never expires.  This is indicated in WSRP by an expires value of -1.
     * @return <code>true</code> if the markup has expired, otherwise <code>false</code>
     */
    public boolean hasExpired() {
        long currentTime = System.currentTimeMillis();
        int expires = cacheControl.getExpires();
        return expires != -1 || cacheTime + expires < currentTime;
    }
}
