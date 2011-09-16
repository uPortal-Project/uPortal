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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jasig.portal.ChannelRenderer.IWorker;
import org.jasig.portal.ChannelRendererFactoryImpl.ChannelRenderThreadPoolExecutor;
import org.jasig.portal.layout.node.IUserLayoutChannelDescription;

/**
 * @author Eric Dalquist
 * @version $Revision$
 */
public class ThreadPoolStatistics {

    private static final int MAXIMUM_ACCEPTABLE_LONG_RUNNERS = 3;
    private static final long EXTRA_LONG_MULTIPLIER = 2L;

    private int numberPermittedErrantByFname = 0;  // Configure in renderingPipelineContext.xml
    private float errantTimeoutMultiplier = 2.0F;
    
    private final boolean errorPool;
    private final Log log = LogFactory.getLog(getClass());
    
    public ThreadPoolStatistics() {
        errorPool = false;
    }
    
    public ThreadPoolStatistics(boolean errorPool) {
        this.errorPool = errorPool;
    }
    
    /**
     * Maximum number of errant threads a portlet may occupy before the portlet 
     * itself is considered errant.  An errant portlet will not be allocated 
     * new threads (users will see the Error Channel).
     * 
     * @param numberPermittedErrantByFname The desired maximum, or 0 to turn the 
     * feature off 
     */
    public void setNumberPermittedErrantByFname(int numberPermittedErrantByFname) {
        this.numberPermittedErrantByFname = numberPermittedErrantByFname;
    }
    
    /**
     * Multiplier (of the timeout value configured in Portlet Manager) used to 
     * calculate the point at which a rendering thread is considered errant.  So 
     * if the multiplier is 2.0F, the thread is errant when it exceeds 2x the 
     * timeout. 
     * 
     * @param errantTimeoutMultiplyer
     */
    public void setErrantTimeoutMultiplier(float errantTimeoutMultiplier) {
        this.errantTimeoutMultiplier = errantTimeoutMultiplier;
    }

    private ThreadPoolExecutor getThreadPoolExecutor() {
        if (errorPool) {
            return ChannelRendererFactoryImpl.cErrorThreadPool;
        }
        
        return ChannelRendererFactoryImpl.cSharedThreadPool;
    }

    public boolean isShutdown() {
        return getThreadPoolExecutor().isShutdown();
    }

    public boolean isTerminating() {
        return getThreadPoolExecutor().isTerminating();
    }

    public boolean isTerminated() {
        return getThreadPoolExecutor().isTerminated();
    }

    public void setThreadFactory(ThreadFactory threadFactory) {
        getThreadPoolExecutor().setThreadFactory(threadFactory);
    }

    public void setRejectedExecutionHandler(RejectedExecutionHandler handler) {
        getThreadPoolExecutor().setRejectedExecutionHandler(handler);
    }

    public void setCorePoolSize(int corePoolSize) {
        getThreadPoolExecutor().setCorePoolSize(corePoolSize);
    }

    public int getCorePoolSize() {
        return getThreadPoolExecutor().getCorePoolSize();
    }

    public boolean allowsCoreThreadTimeOut() {
        return getThreadPoolExecutor().allowsCoreThreadTimeOut();
    }

    public void allowCoreThreadTimeOut(boolean value) {
        getThreadPoolExecutor().allowCoreThreadTimeOut(value);
    }

    public void setMaximumPoolSize(int maximumPoolSize) {
        getThreadPoolExecutor().setMaximumPoolSize(maximumPoolSize);
    }

    public int getMaximumPoolSize() {
        return getThreadPoolExecutor().getMaximumPoolSize();
    }

    public void setKeepAliveTime(long time, TimeUnit unit) {
        getThreadPoolExecutor().setKeepAliveTime(time, unit);
    }

    public long getKeepAliveTime(TimeUnit unit) {
        return getThreadPoolExecutor().getKeepAliveTime(unit);
    }

    public int getQueueSize() {
        return getThreadPoolExecutor().getQueue().size();
    }

    public void purge() {
        getThreadPoolExecutor().purge();
    }

    public int getPoolSize() {
        return getThreadPoolExecutor().getPoolSize();
    }

    public int getActiveCount() {
        return getThreadPoolExecutor().getActiveCount();
    }

    public int getLargestPoolSize() {
        return getThreadPoolExecutor().getLargestPoolSize();
    }

    public long getTaskCount() {
        return getThreadPoolExecutor().getTaskCount();
    }

    public long getCompletedTaskCount() {
        return getThreadPoolExecutor().getCompletedTaskCount();
    }
    
    public void analyze() {
        
        // We can only log if we have a ChannelRenderThreadPoolExecutor (so not for errorPool)...
        ThreadPoolExecutor exec = this.getThreadPoolExecutor();
        if (!(exec instanceof ChannelRenderThreadPoolExecutor)) {
            return;
        }
        ChannelRenderThreadPoolExecutor crtpe = (ChannelRenderThreadPoolExecutor) exec;
        
        // Gather current worker data
        final Map<String,List<StatusTuple>> statusMap = new HashMap<String,List<StatusTuple>>();
        Set<IWorker> activeWorkers = crtpe.getActiveWorkers();
        for (Runnable r : activeWorkers) {
            IWorker w = (IWorker) r;
            IUserLayoutChannelDescription channelDesc = w.getUserLayoutChannelDescription();
            String fname = channelDesc.getFunctionalName();
            List<StatusTuple> list = statusMap.get(fname);
            if (list == null) {
                // First time...
                list = new ArrayList<StatusTuple>();
                statusMap.put(fname, list);
            }
            Long threadReceivedTime = w.getThreadReceivedTime();
            Long elapsedTime = threadReceivedTime != null ? System.currentTimeMillis() - threadReceivedTime : null;
            list.add(
                new StatusTuple(channelDesc.getTimeout(), elapsedTime)
            );
        }
        
        Set<String> errantPortlets = new HashSet<String>(); 
        for (Map.Entry<String,List<StatusTuple>> y : statusMap.entrySet()) {
            String fname = y.getKey();
            if (analyzePortlet(fname, y.getValue())) {
                errantPortlets.add(fname);
            }
        }
        crtpe.setErrantPortlets(errantPortlets);
        
    }
    
    /**
     * 
     * @param fname
     * @param activeWorkers
     * @return true If the portlet itself is in an errant state, otherwise false
     */
    private boolean analyzePortlet(String fname, List<StatusTuple> activeWorkers) {

        int renderCount = 0;
        int errantCount = 0;
        int longRunningCount = 0;
        long longestRunTime = 0;
        long channelTimeout = 0;
        
        for (StatusTuple p : activeWorkers) {

            ++renderCount;
            long elapsedTime = p.getElapsedTime() != null ? p.getElapsedTime() : 0L;
            
            // A thread is 'long-running' if it has exceeded the timeout
            if (elapsedTime > p.getTimeout()) {
                ++longRunningCount;
            }
            if (elapsedTime > longestRunTime) {
                longestRunTime = elapsedTime;
            }

            // A thread is 'errant' if it meets the configured criteria;  a 
            // portlet with too many errant threads will not be allocated new 
            // ones
            long errantThreashold = Math.round(this.errantTimeoutMultiplier * p.getTimeout());
            if (elapsedTime > errantThreashold) {
                ++errantCount;
            }

            // get this once...
            if (channelTimeout == 0) {
                channelTimeout = p.getTimeout();
            }

        }
        
        // Build the report
        StringBuilder bld = new StringBuilder();
        bld.append("Active IWorker instances for channel '").append(fname)
                        .append("' [numberCurrentlyRendering=").append(renderCount)
                        .append(", numberExceedingTimeout=").append(longRunningCount)
                        .append(", numberErrant=").append(errantCount)
                        .append(", longestRunningMillis=").append(longestRunTime)
                        .append("]");
        
        // Choose which level to log it
        if (longRunningCount > MAXIMUM_ACCEPTABLE_LONG_RUNNERS && longestRunTime > EXTRA_LONG_MULTIPLIER * channelTimeout && log.isWarnEnabled()) {
            log.warn(bld.toString());
        } else if (longRunningCount > MAXIMUM_ACCEPTABLE_LONG_RUNNERS && log.isInfoEnabled()) {
            log.info(bld.toString());
        } else if (log.isTraceEnabled()) {
            log.trace(bld.toString());
        }
        
        // Single whether this portlet itself should be considered errant
        return numberPermittedErrantByFname < 1 // any value under 1 truns the feature off
                    ? false 
                    : errantCount > numberPermittedErrantByFname;
        
    }
    
    private static final class StatusTuple {

        private final long timeout;
        private final Long elapsedTime;
        
        public StatusTuple(long timeout, Long elapsedTime) {
            this.timeout = timeout;
            this.elapsedTime = elapsedTime;
        }
        
        public long getTimeout() {
            return timeout;
        }
        
        public Long getElapsedTime() {
            return elapsedTime;
        }
        
    }

}
