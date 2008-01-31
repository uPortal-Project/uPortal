/* Copyright 2008 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/

package org.jasig.portal;

public abstract class BaseChannelCacheEntry implements CacheEntry {
    
    private String channelId;
    
    public BaseChannelCacheEntry(String channelId) {
        this.channelId = channelId;
    }
    
    public String getChannelId(){
        return channelId;
    }
    
}
