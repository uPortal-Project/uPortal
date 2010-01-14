/**
 * Copyright (c) 2000-2009, Jasig, Inc.
 * See license distributed with this file and available online at
 * https://www.ja-sig.org/svn/jasig-parent/tags/rel-10/license-header.txt
 */
package org.jasig.portal;
/**
* @deprecated IChannel rendering code will be replaced with portlet specific rendering code in a future release
*/
@Deprecated
public abstract class BaseChannelCacheEntry implements CacheEntry {
    
    private String channelId;
    
    public BaseChannelCacheEntry(String channelId) {
        this.channelId = channelId;
    }
    
    public String getChannelId(){
        return channelId;
    }
    
}
