/**
 * Copyright © 2001 The JA-SIG Collaborative.  All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. Redistributions of any form whatsoever must retain the following
 *    acknowledgment:
 *    "This product includes software developed by the JA-SIG Collaborative
 *    (http://www.jasig.org/)."
 *
 * THIS SOFTWARE IS PROVIDED BY THE JA-SIG COLLABORATIVE "AS IS" AND ANY
 * EXPRESSED OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL THE JA-SIG COLLABORATIVE OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
 * NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 * OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 */

package org.jasig.portal;

import org.jasig.portal.properties.PropertiesManager;
import org.jasig.portal.services.LogService;
import org.jasig.portal.utils.threading.BoundedThreadPool;

/**
 * <p>The <code>ChannelRendererFactoryImpl</code> creates
 * <code>IChannelRenderer</code> objects which use a bounded thread pool.</p>
 *
 * @author <a href="mailto:jnielsen@sct.com">Jan Nielsen</a>
 *
 * @version $Revision$
 **/
public final class ChannelRendererFactoryImpl
    implements IChannelRendererFactory
{
    /** <p> Class version identifier.</p> */
    public final static String RCS_ID = "@(#) $Header$";

    /** <p>Thread pool per factory.</p> */
    private BoundedThreadPool mThreadPool = null;

    /** <p>Shared thread pool for all factories.</p> */
    private static BoundedThreadPool cSharedThreadPool = null;

    /**
     * <p>Creates a new instance of a bounded thread pool channel
     * renderer factory object. The constructor should not be invoked
     * directly; it should only be constructed by the
     * <code>ChannelRendererFactory</code> object.</p>
     *
     * <p>This factory implooks for the properties:
     *
     * <pre><code>
     *  keyBase + ".threadPool_initialThreads"
     *  keyBase + ".threadPool_maxThreads"
     *  keyBase + ".threadPool_threadPriority"
     *  keyBase + ".threadPool_shared"
     * </code></pre>
     *
     * in the configuration system and then reflectively constructs the
     * factory class with the default (no-argument) constructor.</p>
     *
     * @param keyBase configuration base key
     *
     * or <code>null</code>
     */
    public ChannelRendererFactoryImpl(
        String keyBase
        )
    {
        int initialThreads = 1;
        int maxThreads = 20;
        int threadPriority = 5;
        boolean sharedPool = false;

        try
        {
            initialThreads = PropertiesManager.getPropertyAsInt(
                keyBase + ".threadPool_initialThreads"
                );

            maxThreads = PropertiesManager.getPropertyAsInt(
                keyBase + ".threadPool_maxThreads"
                );

            threadPriority = PropertiesManager.getPropertyAsInt(
                keyBase + ".threadPool_threadPriority"
                );

            sharedPool = PropertiesManager.getPropertyAsBoolean(
                keyBase + ".threadPool_shared"
                );
        }
        catch( Exception x )
        {
            LogService.log(
                LogService.ERROR,
                "ChannelRendererFactoryImpl(" + keyBase + ") failed to find configuration parameters. Constructing with: " +
                "threadPool_initialThreads = " + initialThreads + " " +
                "threadPool_maxThreads = " + maxThreads + " " +
                "threadPool_threadPriority = " + threadPriority + " " +
                "threadPool_shared = " + sharedPool,
                x
                );
        }

        if( sharedPool )
        {
            cSharedThreadPool = new BoundedThreadPool(
                initialThreads,
                maxThreads,
                threadPriority
                );
        }
        else
        {
            mThreadPool = new BoundedThreadPool(
                initialThreads,
                maxThreads,
                threadPriority
                );
        }
    }

    /**
     * <p>Creates a new instance of a channel renderer object.</p>
     *
     * @param channel channel to render
     *
     * @param channelRuntimeData runtime data for the channel to render
     *
     * @return new instance of a channel renderer for the specified channel
     **/
    public IChannelRenderer newInstance(
        IChannel channel,
        ChannelRuntimeData channelRuntimeData
        )
    {
        return new ChannelRenderer(
            channel,
            channelRuntimeData,
            (null == mThreadPool) ? cSharedThreadPool : mThreadPool
            );
    }
}
