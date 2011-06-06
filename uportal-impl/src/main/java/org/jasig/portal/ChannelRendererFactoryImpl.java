/**
 * Licensed to Jasig under one or more contributor license
 * agreements. See the NOTICE file distributed with this work
 * for additional information regarding copyright ownership.
 * Jasig licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a
 * copy of the License at:
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
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
 * @deprecated IChannel rendering code will be replaced with portlet specific rendering code in a future release
 */
@Deprecated
public final class ChannelRendererFactoryImpl
    implements IChannelRendererFactory
{
    /** <p> Class version identifier.</p> */
    public final static String RCS_ID = "@(#) $Header$";

    private static final Log log = LogFactory.getLog(ChannelRendererFactoryImpl.class);

    /** <p>Thread pool per factory.</p> */
    private ThreadPoolExecutor mThreadPool = null;
    
    static ThreadPoolExecutor cErrorThreadPool = null;

    /** <p>Shared thread pool for all factories.</p> */
    static ThreadPoolExecutor cSharedThreadPool = null;
    
    private class ChannelRenderThreadPoolExecutor extends ThreadPoolExecutor {
        final AtomicLong activeThreads;
        final AtomicLong maxActiveThreads;

        public ChannelRenderThreadPoolExecutor(final AtomicLong activeThreads, final AtomicLong maxActiveThreads,
                int corePoolSize, int maximumPoolSize, long keepAliveTime, TimeUnit unit, BlockingQueue<Runnable> workQueue,
                ThreadFactory threadFactory) {
            super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue, threadFactory);

            this.activeThreads = activeThreads;
            this.maxActiveThreads = maxActiveThreads;
        }

        @Override
        protected void beforeExecute(java.lang.Thread t, java.lang.Runnable r) {
            super.beforeExecute(t, r);
            final long current = activeThreads.incrementAndGet();
            if (current > maxActiveThreads.get()) {
                maxActiveThreads.set(current);
            }
        }

        @Override
        protected void afterExecute(java.lang.Runnable r, java.lang.Throwable t) {
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
    public ChannelRendererFactoryImpl(final String keyBase, final AtomicLong activeThreads,
            final AtomicLong maxActiveThreads) {
        int initialThreads = 20;
        int maxThreads = 150;
        int threadPriority = 5;
        boolean sharedPool = false;

        try {
            initialThreads = PropertiesManager.getPropertyAsInt(keyBase + ".threadPool_initialThreads");
            
            maxThreads = PropertiesManager.getPropertyAsInt(keyBase + ".threadPool_maxThreads");

            threadPriority = PropertiesManager.getPropertyAsInt(keyBase + ".threadPool_threadPriority");

            sharedPool = PropertiesManager.getPropertyAsBoolean(keyBase + ".threadPool_shared");
        }
        catch (Exception x) {
            log.error("ChannelRendererFactoryImpl(" + keyBase + ") failed to find configuration parameters. Constructing with: " + 
                    "threadPool_initialThreads = " + initialThreads + " " + 
                    "threadPool_threadPriority = " + threadPriority + " " + 
                    "threadPool_shared = " + sharedPool, x);
        }

        cErrorThreadPool = new ThreadPoolExecutor(20, 20, 0L, TimeUnit.MILLISECONDS, new LinkedBlockingQueue(),
                new PriorityThreadFactory(threadPriority, "ErrorRendering", PortalSessionManager.getThreadGroup()));

        final PriorityThreadFactory threadFactory = new PriorityThreadFactory(threadPriority, keyBase, PortalSessionManager.getThreadGroup());
        if (sharedPool) {
            cSharedThreadPool = new ChannelRenderThreadPoolExecutor(activeThreads, maxActiveThreads, initialThreads,
                    maxThreads, 1000L, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<Runnable>(), threadFactory);
        }
        else {
            this.mThreadPool = new ChannelRenderThreadPoolExecutor(activeThreads, maxActiveThreads, initialThreads,
                    maxThreads, 1000L, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<Runnable>(), threadFactory);
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
    public IChannelRenderer newInstance(IChannel channel, ChannelRuntimeData channelRuntimeData) {

        ThreadPoolExecutor threadPoolExecutor = null;
        // Use special thread pool for CError channel rendering
        if (channel instanceof CError) {
            threadPoolExecutor = cErrorThreadPool;
        }
        else if (cSharedThreadPool != null) {
            final int activeCount = cSharedThreadPool.getActiveCount();
            final int queueSize = cSharedThreadPool.getQueue().size();
            final int corePoolSize = cSharedThreadPool.getCorePoolSize();

            if (queueSize > (corePoolSize / 2)) {
                log.error(
                        "stp-queue-size: " + queueSize + " " + 
                        "stp-activeCount: " + activeCount + " " + 
                        "stp-completedTaskCount: " + cSharedThreadPool.getCompletedTaskCount() + " " + 
                        "stp-corePoolSize: " + corePoolSize + " " + 
                        "stp-poolSize: " + cSharedThreadPool.getPoolSize() + " " + 
                        "stp-maxPoolSize: " + cSharedThreadPool.getMaximumPoolSize());
            }
            else if (queueSize > (corePoolSize / 4)) {
                log.warn(
                        "stp-queue-size: " + queueSize + " " + 
                        "stp-activeCount: " + activeCount + " " + 
                        "stp-completedTaskCount: " + cSharedThreadPool.getCompletedTaskCount() + " " + 
                        "stp-corePoolSize: " + corePoolSize + " " + 
                        "stp-poolSize: " + cSharedThreadPool.getPoolSize() + " " + 
                        "stp-maxPoolSize: " + cSharedThreadPool.getMaximumPoolSize());
            }
            else {
                log.debug(
                        "stp-queue-size: " + queueSize + " " + 
                        "stp-activeCount: " + activeCount + " " + 
                        "stp-completedTaskCount: " + cSharedThreadPool.getCompletedTaskCount() + " " + 
                        "stp-corePoolSize: " + corePoolSize + " " + 
                        "stp-poolSize: " + cSharedThreadPool.getPoolSize() + " " + 
                        "stp-maxPoolSize: " + cSharedThreadPool.getMaximumPoolSize());
            }

            threadPoolExecutor = cSharedThreadPool;
        }
        else {
            threadPoolExecutor = this.mThreadPool;
        }

        return new ChannelRenderer(channel, channelRuntimeData, threadPoolExecutor);
    }
}
