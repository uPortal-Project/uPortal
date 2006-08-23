/* Copyright 2005 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/

package org.jasig.portal.channels.support;

/**
 * IChannelTitle is an interface for conveying the title of a channel.
 * <p>Its current use (as of uPortal 2.5.1) is for marking a {@link org.jasig.portal.ChannelRuntimeProperties}
 * as conveying a dynamic channel title.  That is, an {@link org.jasig.portal.IChannel} returning
 * a {@link org.jasig.portal.ChannelRuntimeProperties} that implements IChannelTitle communicates
 * to the framework a desired title for the channel, overriding any title
 * declared at the time of channel publication.</p>
 *
 * <p>Dynamic channel title capability is implemented in terms of this interface
 * rather than in terms of detection of some particular {@link org.jasig.portal.ChannelRuntimeProperties}
 * subclass so that any existing or new {@link org.jasig.portal.ChannelRuntimeProperties} subclassess
 * can be made dynamically titled.  Recommendation for channel implementors: use
 * {@link TitledChannelRuntimeProperties} rather than writing a new
 * {@link org.jasig.portal.ChannelRuntimeProperties} subclass
 * implementing this interface, where possible.</p>
 * </p>
 * @since uPortal 2.5.1
 * @version $Revision$ $Date$
 */
public interface IChannelTitle {

    /**
     * Get the desired channel title.
     * Returning <code>null</code> indicates that the channel is not specifying
     * a dynamic title and will leave it up to "the uPortal framework" to
     * provide a title for the channel.
     * <p>Currently, the fallback behavior is to
     * behave as if the channel hadn't provided an IDynamicChannelTitle at all
     * and use the title specified at channel publication.</p>
     * @return desired dynamic channel title, or null if no dynamic title.
     */
    public String getChannelTitle();

}
