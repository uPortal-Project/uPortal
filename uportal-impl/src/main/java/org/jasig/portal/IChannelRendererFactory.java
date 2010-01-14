/**
 * Copyright (c) 2000-2009, Jasig, Inc.
 * See license distributed with this file and available online at
 * https://www.ja-sig.org/svn/jasig-parent/tags/rel-10/license-header.txt
 */
package org.jasig.portal;

/**
 * <p>The <code>IChannelRendererFactory</code> interface defines the factory
 * interface for <code>IChannelRenderer</code> objects. Provider
 * implementations must provide a single string argument constructor. The
 * string argument passed to the implementation is the key base used to
 * construct the <code>IChannelRendererFactory</code> instance in the
 * <code>ChannelRendererFactory.newInstance</code> method call. The
 * implementation may use this argument to retrieve additional configuration
 * parameters, if necessary.</p>
 *
 * @see org.jasig.portal.ChannelRendererFactory
 *
 * @author <a href="mailto:jnielsen@sct.com">Jan Nielsen</a>
 *
 * @version $Revision$
 * @deprecated IChannel rendering code will be replaced with portlet specific rendering code in a future release
 */
@Deprecated
public interface IChannelRendererFactory
{
    /** <p> Class version identifier.</p> */
    public final static String RCS_ID = "@(#) $Header$";

    /**
     * <p>Creates a new instance of a channel renderer object for the provided
     * channel and runtime data instances.</p>
     *
     * @param channel channel to render
     *
     * @param channelRuntimeData runtime data for the channel to render
     *
     * @return new instance of a channel renderer for the specified channel
     **/
    IChannelRenderer newInstance(
        IChannel channel,
        ChannelRuntimeData channelRuntimeData
        );
}
