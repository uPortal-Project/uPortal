/* Copyright 2008 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/

package org.jasig.portal;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.jasig.portal.serialize.CachingSerializer;

/**
 * The cache entry objects of the rendering pipeline.
 *
 */
public interface CacheEntry {
    public CacheType getCacheType();
    public void replayCache(CachingSerializer serializer, ChannelManager cm,
        HttpServletRequest req, HttpServletResponse res) throws PortalException;
}
