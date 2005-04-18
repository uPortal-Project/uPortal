/* Copyright 2001 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/

package org.jasig.portal;

import org.jasig.portal.properties.PropertiesManager;
import org.jasig.portal.utils.threading.PriorityThreadFactory;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.emory.mathcs.backport.java.util.concurrent.ExecutorService;
import edu.emory.mathcs.backport.java.util.concurrent.LinkedBlockingQueue;
import edu.emory.mathcs.backport.java.util.concurrent.ThreadPoolExecutor;
import edu.emory.mathcs.backport.java.util.concurrent.TimeUnit;

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

    private static final Log log = LogFactory.getLog(ChannelRendererFactoryImpl.class);
    
    /** <p>Thread pool per factory.</p> */
    private ExecutorService mThreadPool = null;

    /** <p>Shared thread pool for all factories.</p> */
    private static ExecutorService cSharedThreadPool = null;

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
            log.error(
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
            cSharedThreadPool = new ThreadPoolExecutor(initialThreads, maxThreads, 0L, TimeUnit.MILLISECONDS, new LinkedBlockingQueue(), new PriorityThreadFactory(threadPriority));
        }
        else
        {
            this.mThreadPool = new ThreadPoolExecutor(initialThreads, maxThreads, 0L, TimeUnit.MILLISECONDS, new LinkedBlockingQueue(), new PriorityThreadFactory(threadPriority));
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
            (null == this.mThreadPool) ? cSharedThreadPool : this.mThreadPool
            );
    }
}
