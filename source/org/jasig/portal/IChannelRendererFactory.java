/* Copyright 2001 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
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
 **/
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
        ChannelRuntimeData channelRuntimeData,
        PortalControlStructures pcs
        );
}
