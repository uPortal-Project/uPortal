/**
 * Copyright (c) 2000-2009, Jasig, Inc.
 * See license distributed with this file and available online at
 * https://www.ja-sig.org/svn/jasig-parent/tags/rel-10/license-header.txt
 */
package org.jasig.portal;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jasig.portal.channels.error.CError;
import org.jasig.portal.properties.PropertiesManager;
import org.jasig.portal.utils.threading.PriorityThreadFactory;

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
    private ThreadPoolExecutor mThreadPool = null;
    
    private static ThreadPoolExecutor cErrorThreadPool = null;

    /** <p>Shared thread pool for all factories.</p> */
    private static ThreadPoolExecutor cSharedThreadPool = null;

    private class ChannelRenderThreadPoolExecutor extends ThreadPoolExecutor {
    	final AtomicLong activeThreads;
    	final AtomicLong maxActiveThreads;
		public ChannelRenderThreadPoolExecutor(final AtomicLong activeThreads, final AtomicLong maxActiveThreads,
				int corePoolSize,
				int maximumPoolSize, long keepAliveTime, TimeUnit unit,
				BlockingQueue workQueue, ThreadFactory threadFactory) {
			super(corePoolSize, maximumPoolSize, keepAliveTime, unit,
					workQueue, threadFactory);

			this.activeThreads = activeThreads;
			this.maxActiveThreads = maxActiveThreads;
		}
		protected void beforeExecute(java.lang.Thread t,
                java.lang.Runnable r) {
			super.beforeExecute(t, r);
			final long current = activeThreads.incrementAndGet();
			if (current > maxActiveThreads.get()) {
				maxActiveThreads.set(current);
			}
		}
		protected void afterExecute(java.lang.Runnable r,
                java.lang.Throwable t) {
			super.afterExecute(r, t);
			activeThreads.decrementAndGet();
		}
	}

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
        final String keyBase, final AtomicLong activeThreads, final AtomicLong maxActiveThreads
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

        cErrorThreadPool = new ThreadPoolExecutor(20, 20, 0L, TimeUnit.MILLISECONDS, new LinkedBlockingQueue(), new PriorityThreadFactory(threadPriority, "ErrorRendering", PortalSessionManager.getThreadGroup()));

        
        if( sharedPool )
        {
            cSharedThreadPool = new ChannelRenderThreadPoolExecutor(activeThreads, maxActiveThreads, initialThreads, maxThreads, 0L, TimeUnit.MILLISECONDS, new LinkedBlockingQueue(), new PriorityThreadFactory(threadPriority, keyBase, PortalSessionManager.getThreadGroup()));
        }
        else
        {
            this.mThreadPool = new ChannelRenderThreadPoolExecutor(activeThreads, maxActiveThreads, initialThreads, maxThreads, 0L, TimeUnit.MILLISECONDS, new LinkedBlockingQueue(), new PriorityThreadFactory(threadPriority, keyBase, PortalSessionManager.getThreadGroup()));
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
    	
    	ThreadPoolExecutor threadPoolExecutor = null;
    	// Use special thread pool for CError channel rendering
    	if (channel instanceof CError){
    	    		threadPoolExecutor = cErrorThreadPool;
    	    	}else if (cSharedThreadPool != null){
                    int activeCount = cSharedThreadPool.getActiveCount();
                    int queueSize = cSharedThreadPool.getQueue().size();
                    int largestPoolSize = cSharedThreadPool.getLargestPoolSize();
                    int maxPoolSize = cSharedThreadPool.getMaximumPoolSize();
        
                    //If 75% of pool used, warn
                    if (log.isWarnEnabled() && activeCount >= (int) (0.75 * maxPoolSize)) {
                        log.warn(String.format("Rendering thread pool is nearly full. activeCount: %d/%d queueSize: %d largestPoolSize: %d",
                            activeCount,
                            maxPoolSize,
                            queueSize,
                            largestPoolSize));
                    }
                    //If queue is greater than 50% of pool, warn
                    if (log.isWarnEnabled() && queueSize >= (int) (0.5 * maxPoolSize)) {
                        log.warn(String.format("Rendering thread pool queue size is high. activeCount: %d/%d queueSize: %d largestPoolSize: %d",
                            activeCount,
                            maxPoolSize,
                            queueSize,
                            largestPoolSize));
                    }
        
                    if (log.isDebugEnabled()) {
                        log.debug(
                                 "stp-activeCount: " + activeCount + 
                                " stp-completedTaskCount: " + cSharedThreadPool.getCompletedTaskCount() + 
                                " stp-corePoolSize: " + cSharedThreadPool.getCorePoolSize() + 
                                " stp-queue-size: " + queueSize);
                    }

    	    		threadPoolExecutor = cSharedThreadPool;
    	        }else{
    	        	threadPoolExecutor = this.mThreadPool;
    	        }
    	
        return new ChannelRenderer(
            channel,
            channelRuntimeData,
            threadPoolExecutor
            );
    }
}
