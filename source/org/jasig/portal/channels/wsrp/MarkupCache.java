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

import java.util.HashMap;
import java.util.Map;

import org.jasig.portal.wsrp.Constants;
import org.jasig.portal.wsrp.types.CacheControl;

/**
 * A cache for portlet markup.
 * @author Ken Weiner, kweiner@unicon.net
 * @version $Revision$
 */
public class MarkupCache {

    private static Map cacheForAll;
    private Map cachePerUser;
    
    public MarkupCache() {
        cachePerUser = new HashMap();
    }
    
    /**
     * Add a MarkupWrapper into the cache.
     * The MarkupWrapper contains the CacheControl which contains
     * the user scope which controls the scoping of the cached markup.
     * @param key the cache key
     * @param markupWrapper the wrapped markup
     */
    public void put(String key, MarkupWrapper markupWrapper) {
        CacheControl cacheControl = markupWrapper.getCacheControl();
        if (cacheControl != null) {
            int expires = cacheControl.getExpires();
            String userScope = cacheControl.getUserScope();
            if (userScope.equals(Constants.WSRP_FOR_ALL)) {
                cacheForAll.put(key, markupWrapper);
            } else if (userScope.equals(Constants.WSRP_PER_USER)) {
                cachePerUser.put(key, markupWrapper);
            }
        }
    }
    
    /**
     * Retrieve a MarkupWrapper from the cache
     * @param key the cache key
     * @param userScope the user scope
     * @return the wrapped markup
     */
    public MarkupWrapper get(String key, String userScope) {
        MarkupWrapper markupWrapper = null;
        if (userScope.equals(Constants.WSRP_FOR_ALL)) {
            markupWrapper = (MarkupWrapper)cacheForAll.get(key);
        } else if (userScope.equals(Constants.WSRP_PER_USER)) {
            markupWrapper = (MarkupWrapper)cachePerUser.get(key);
        }
        return markupWrapper;
    }
    
    /**
     * Removes a MarkupWrapper entry from the cache.
     * @param key the cache key
     * @param userScope the user scope
     */
    public void remove(String key, String userScope) {
        if (userScope.equals(Constants.WSRP_FOR_ALL)) {
            cacheForAll.remove(key);
        } else if (userScope.equals(Constants.WSRP_PER_USER)) {
            cachePerUser.remove(key);
        }
    }

}
