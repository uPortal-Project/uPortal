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

import java.io.PrintWriter;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.FutureTask;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RunnableFuture;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jasig.portal.ChannelRenderer.IWorker;
import org.jasig.portal.channels.BaseChannel;
import org.jasig.portal.channels.error.CError;
import org.jasig.portal.channels.portlet.IPortletAdaptor;
import org.jasig.portal.layout.node.IUserLayoutChannelDescription;
import org.jasig.portal.properties.PropertiesManager;
import org.jasig.portal.utils.threading.PriorityThreadFactory;
import org.xml.sax.ContentHandler;

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
    static ChannelRenderThreadPoolExecutor cSharedThreadPool = null;

    public static class ChannelRenderThreadPoolExecutor extends ThreadPoolExecutor {
        private final AtomicLong activeThreads;
        private final AtomicLong maxActiveThreads;
        private volatile Set<String> errantPortlets = Collections.emptySet();
        
        /*
         * Track activeWorkers in a Set with ConcurrentHashMap multithreaded 
         * behavior (see http://dhruba.name/2009/08/05/concurrent-set-implementations-in-java-6/)
         */
        private final Set<WorkerFutureTask<?>> activeWorkers = Collections.newSetFromMap(new ConcurrentHashMap<WorkerFutureTask<?>,Boolean>());

        public ChannelRenderThreadPoolExecutor(final AtomicLong activeThreads, final AtomicLong maxActiveThreads,
                int corePoolSize, int maximumPoolSize, long keepAliveTime, TimeUnit unit, BlockingQueue<Runnable> workQueue,
                ThreadFactory threadFactory) {
            super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue, threadFactory);

            this.activeThreads = activeThreads;
            this.maxActiveThreads = maxActiveThreads;
        }
        
        @Override
        protected <V> RunnableFuture<V> newTaskFor(Runnable r, V v) {
            if (r instanceof IWorker) {
                return new WorkerFutureTask<V>((IWorker) r);
            } else {
                return super.newTaskFor(r, v);
            }
        }

        @Override
        protected void beforeExecute(java.lang.Thread t, java.lang.Runnable r) {
            super.beforeExecute(t, r);
            final long current = activeThreads.incrementAndGet();
            if (current > maxActiveThreads.get()) {
                maxActiveThreads.set(current);
            }
            if (r instanceof WorkerFutureTask) {
                activeWorkers.add((WorkerFutureTask<?>) r);
            }
        }

        @Override
        protected void afterExecute(java.lang.Runnable r, java.lang.Throwable t) {
            super.afterExecute(r, t);
            activeThreads.decrementAndGet();
            if (r instanceof WorkerFutureTask) {
                activeWorkers.remove((WorkerFutureTask<?>) r);
            }
        }
        
        public Set<IWorker> getActiveWorkers() {
            Set<WorkerFutureTask<?>> workers = new HashSet<WorkerFutureTask<?>>(activeWorkers);  // defensive copy
            Set<IWorker> rslt = new HashSet<IWorker>();
            for (WorkerFutureTask<?> wft : workers) {
                IWorker k = wft.getWorker();
                rslt.add(k);
            }
            return rslt;
        }
        
        Set<String> getErrantPortlets() {
            return this.errantPortlets;
        }
        
        /**
         * Package-private because the ThreadPoolStatistics class is the only one that should be setting
         */
        void setErrantPortlets(Set<String> errantPortlets) {
            this.errantPortlets = Collections.unmodifiableSet(errantPortlets);
        }

    }
    
    private static final class WorkerFutureTask<V> extends FutureTask<V> {

        private final IWorker worker;
        
        public WorkerFutureTask(IWorker worker) {
            super(worker, null);
            this.worker = worker;
        }

        public IWorker getWorker() {
            return worker;
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

        cErrorThreadPool = new ThreadPoolExecutor(20, 20, 0L, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<Runnable>(),
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
    public IChannelRenderer newInstance(IUserLayoutChannelDescription channelDesc, IChannel channel, ChannelRuntimeData channelRuntimeData) {

        ThreadPoolExecutor threadPoolExecutor = null;
        Set<String> errant = Collections.emptySet();
        
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
            errant = ((ChannelRenderThreadPoolExecutor) cSharedThreadPool).getErrantPortlets();
        }
        else {
            threadPoolExecutor = this.mThreadPool;
        }
        
        // Choose whether to submit the channel itself or to replace it due to errant threads
        IChannel channelToSubmit = channel;  // default
        if (errant.contains(channelDesc.getFunctionalName())) {
            if (log.isDebugEnabled()) {
                // This log entry can be at DEBUG because it's mostly 
                // redundant with the stack trace that will be logged 
                // when SkipRenderingBecauseOfTooManyErrantThreadsChannel 
                // renders
                log.debug("Skipping render of portlet '" + channelDesc.getFunctionalName() +
                        "' because it is deemed to be errant (leaking rendering threads)");
            }
            channelToSubmit = new SkipRenderingBecauseOfTooManyErrantThreadsChannel(channelDesc.getFunctionalName());
        }

        return new ChannelRenderer(channelDesc, channelToSubmit, channelRuntimeData, threadPoolExecutor);
    }

    private static final class SkipRenderingBecauseOfTooManyErrantThreadsChannel extends BaseChannel implements IPortletAdaptor {
        
        private static final String EXCEPTION_MESSAGE = "The following portlet has been skipped because it has become unresponsive:  "; 
        private final String fname;
        
        public SkipRenderingBecauseOfTooManyErrantThreadsChannel(String fname) {
            this.fname = fname;
        }

        @Override
        public void renderXML(ContentHandler out) throws PortalException {
            throw new PortalException(EXCEPTION_MESSAGE + fname);
        }

        @Override
        public void prepareForRefresh() {
            throw new PortalException(EXCEPTION_MESSAGE + fname);
        }

        @Override
        public void prepareForReset() {
            throw new PortalException(EXCEPTION_MESSAGE + fname);
        }

        @Override
        public void renderCharacters(PrintWriter pw) throws PortalException {
            throw new PortalException(EXCEPTION_MESSAGE + fname);
        }

        @Override
        public ChannelCacheKey generateKey() {
            throw new PortalException(EXCEPTION_MESSAGE + fname);
        }

        @Override
        public boolean isCacheValid(Object validity) {
            throw new PortalException(EXCEPTION_MESSAGE + fname);
        }

        @Override
        public void setResponse(HttpServletResponse response)
                throws PortalException {
            throw new PortalException(EXCEPTION_MESSAGE + fname);
        }

        @Override
        public void processAction() throws PortalException {
            throw new PortalException(EXCEPTION_MESSAGE + fname);
        }

        @Override
        public void setPortalControlStructures(PortalControlStructures pcs)
                throws PortalException {
            throw new PortalException(EXCEPTION_MESSAGE + fname);
        }
        
    }

}
