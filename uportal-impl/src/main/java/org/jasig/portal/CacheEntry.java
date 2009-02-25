/**
 * Copyright (c) 2000-2009, Jasig, Inc.
 * See license distributed with this file and available online at
 * https://www.ja-sig.org/svn/jasig-parent/tags/rel-10/license-header.txt
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
