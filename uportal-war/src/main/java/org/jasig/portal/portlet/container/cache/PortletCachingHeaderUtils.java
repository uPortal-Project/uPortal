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
package org.jasig.portal.portlet.container.cache;

import java.util.concurrent.TimeUnit;

import org.jasig.portal.portlet.rendering.PortletResourceOutputHandler;

/**
 * Utility for writing out portlet response caching related headers
 * 
 * @author Eric Dalquist
 * @version $Revision$
 */
public final class PortletCachingHeaderUtils {
    private static final int YEAR_OF_SECONDS = (int)TimeUnit.DAYS.toSeconds(365);
    
    private PortletCachingHeaderUtils() {
    }
    
    /**
     * @param cachedPortletData The {@link CachedPortletData} to base the headers on
     * @param portletResourceOutputHandler The handler to write the headers to
     * 
     * @see #setCachingHeaders(int, boolean, long, PortletResourceOutputHandler)
     * @see #setETag(String, PortletResourceOutputHandler)
     */
    public static void setCachingHeaders(CachedPortletData<?> cachedPortletData, PortletResourceOutputHandler portletResourceOutputHandler) {
        final long expirationTime = cachedPortletData.getExpirationTime();
        final int maxAge = (int)TimeUnit.MILLISECONDS.toSeconds(expirationTime - System.currentTimeMillis());
        final long timeStored = cachedPortletData.getTimeStored();
        final boolean publicScope = cachedPortletData.isPublicScope();
        
        setCachingHeaders(maxAge, publicScope, timeStored, portletResourceOutputHandler);
        final String etag = cachedPortletData.getEtag();
        if (etag != null) {
            setETag(etag, portletResourceOutputHandler);
        }
    }
    
    /**
     * Set the Last-Modified, CacheControl, and Expires headers based on the maxAge,
     * publicScope and lastModified.
     * 
     * @param maxAge Maximum age for the content, follows the portlet rules (-1 cache forever, 0 cache never, N cache seconds) 
     * @param publicScope If the content is public
     * @param lastModified The last modification timestamp of the content
     * @param portletResourceOutputHandler The handler to write the headers to
     */
    public static void setCachingHeaders(int maxAge, boolean publicScope, long lastModified, PortletResourceOutputHandler portletResourceOutputHandler) {
        if (maxAge != 0) {
            portletResourceOutputHandler.setDateHeader("Last-Modified", lastModified);
            
            if (publicScope) {
                portletResourceOutputHandler.setHeader("CacheControl", "public");
            }
            else {
                portletResourceOutputHandler.setHeader("CacheControl", "private");
            }
            
            if (maxAge < 0) {
                //If caching "forever" set expires and max-age to 1 year
                maxAge = YEAR_OF_SECONDS;
            }
        
            portletResourceOutputHandler.setDateHeader("Expires", System.currentTimeMillis() + TimeUnit.SECONDS.toMillis(maxAge));
            portletResourceOutputHandler.addHeader("CacheControl", "max-age=" + maxAge);
        }
    }
    
    /**
     * Set the ETag header based on the specified token
     * 
     * @param token ETag value
     * @param portletResourceOutputHandler The handler to write the header to
     */
    public static void setETag(String token, PortletResourceOutputHandler portletResourceOutputHandler) {
        portletResourceOutputHandler.setHeader("ETag", token);
    }
}
