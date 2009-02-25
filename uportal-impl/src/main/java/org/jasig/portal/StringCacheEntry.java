/**
 * Copyright (c) 2000-2009, Jasig, Inc.
 * See license distributed with this file and available online at
 * https://www.ja-sig.org/svn/jasig-parent/tags/rel-10/license-header.txt
 */
package org.jasig.portal;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.jasig.portal.serialize.CachingSerializer;

public class StringCacheEntry implements CacheEntry {
    
    private String characters;
    
    public StringCacheEntry(String characters) {
        this.characters = characters;
    }
    
    public void replayCache(CachingSerializer serializer, ChannelManager cm,
        HttpServletRequest req, HttpServletResponse res) throws PortalException {
        try {
            serializer.printRawCharacters(characters);
        } catch (IOException e) {
            throw new PortalException(e);
        }
    }

    public CacheType getCacheType() {
        return CacheType.CHARACTERS;
    }
}
