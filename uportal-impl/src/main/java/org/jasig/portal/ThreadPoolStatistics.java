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

import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @author Eric Dalquist
 * @version $Revision$
 */
public class ThreadPoolStatistics {
    private final boolean errorPool;
    
    public ThreadPoolStatistics() {
        errorPool = false;
    }
    
    public ThreadPoolStatistics(boolean errorPool) {
        this.errorPool = errorPool;
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
}
