/**
 * Copyright (c) 2000-2009, Jasig, Inc.
 * See license distributed with this file and available online at
 * https://www.ja-sig.org/svn/jasig-parent/tags/rel-10/license-header.txt
 */
package org.jasig.portal.channels.support;

import org.jasig.portal.ChannelRuntimeProperties;

/**
 * {@link ChannelRuntimeProperties} subclass implementing {@link IChannelTitle}.
 * This class is provided as a convenience to channel developers.  No uPortal
 * framework code should be written to expect this concrete implementation -
 * the framework should only detect whether the ChannelRuntimeProperties it
 * encounters implements {@link IChannelTitle}.
 * @deprecated All IChannel implementations should be migrated to portlets
 */
@Deprecated
public final class TitledChannelRuntimeProperties
	extends ChannelRuntimeProperties
	implements IChannelTitle {

    /**
     * Title of the channel.
     */
    private String channelTitle;

    /**
     * TitledChannelRuntimeProperties requires that the desired channel title
     * be provided at instantiation.  A null desiredTitle will cause
     * TitledChannelRuntimeProperties to return null on getTitle(), indicating
     * that the channel doesn't have a desired title.
     * @param desiredTitle desired channel title, or null if no preference
     */
    public TitledChannelRuntimeProperties(String desiredTitle) {
        this.channelTitle = desiredTitle;
    }

    public String getChannelTitle() {
        if (log.isTraceEnabled()) {
            log.trace("TitledChannelRuntimeProperties: getting title [" + this.channelTitle + "]");
        }
        return this.channelTitle;
    }

}