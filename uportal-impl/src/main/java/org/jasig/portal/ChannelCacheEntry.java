/**
 * Copyright (c) 2000-2009, Jasig, Inc.
 * See license distributed with this file and available online at
 * https://www.ja-sig.org/svn/jasig-parent/tags/rel-10/license-header.txt
 */
package org.jasig.portal;

/**
 * This class takes care of initiating channel rendering thread, 
 * monitoring it for timeouts, retreiving cache, and returning 
 * rendering results and status.  It is used by ChannelRenderer.
 * @author <a href="mailto:pkharchenko@unicon.net">Peter Kharchenko</a>
 * @version $Revision$
 */
public class ChannelCacheEntry {
    protected Object buffer;
    protected String title;
    protected final Object validity;

    public ChannelCacheEntry(Object buffer, String title, Object validity) {
        this.buffer = buffer;
        this.title = title;
        this.validity = validity;
    }
}

