/* Copyright 2008 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/

package org.jasig.portal;

import java.io.IOException;

import org.jasig.portal.serialize.CachingSerializer;

public class StringCacheEntry implements CacheEntry {
    
    private String characters;
    
    public StringCacheEntry(String characters) {
        this.characters = characters;
    }
    
    public void replayCache(CachingSerializer serializer, ChannelManager cm) throws PortalException {
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
