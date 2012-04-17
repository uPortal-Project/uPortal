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

package org.jasig.portal.utils.threading;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.RunnableFuture;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.springframework.scheduling.concurrent.ThreadPoolExecutorFactoryBean;

/**
 * NOT CURRENTLY FUNCTIONAL
 * 
 * Creates a {@link ThreadPoolExecutor} that uses a {@link QualityOfServiceBlockingQueue} as its queue.
 * 
 * @author Eric Dalquist
 * @version $Revision$
 */
public class QoSThreadPoolExecutorFactoryBean extends ThreadPoolExecutorFactoryBean {
    private static final long serialVersionUID = 1L;
    
    private Class<? extends QualityOfServiceBlockingQueue<?, Runnable>> qualityOfServiceQueueType;
    private Constructor<? extends QualityOfServiceBlockingQueue<?, Runnable>> qualityOfServiceQueueConstructor;
    private QualityOfServiceBlockingQueue<?, Runnable> qualityOfServiceBlockingQueue;
    
    /**
     * The concrete implementation of {@link QualityOfServiceBlockingQueue} to provide to the {@link ThreadPoolExecutor}
     */
    public void setQualityOfServiceQueueType(Class<? extends QualityOfServiceBlockingQueue<?, Runnable>> qualityOfServiceQueueType) {
        this.qualityOfServiceQueueType = qualityOfServiceQueueType;
    }

    /* (non-Javadoc)
     * @see org.jasig.portal.utils.threading.DynamicThreadPoolExecutorFactoryBean#afterPropertiesSet()
     */
    @Override
    public void afterPropertiesSet() {
        try {
            this.qualityOfServiceQueueConstructor = this.qualityOfServiceQueueType.getConstructor(Integer.TYPE);
        }
        catch (SecurityException e) {
            throw new RuntimeException(e);
        }
        catch (NoSuchMethodException e) {
            throw new RuntimeException("The QualityOfServiceBlockingQueue implementation MUST have a constructor that takes a single int argument. " + qualityOfServiceQueueType + " does not", e);
        }
        
        super.afterPropertiesSet();
    }

//    /* (non-Javadoc)
//     * @see org.jasig.portal.utils.threading.DynamicThreadPoolExecutorFactoryBean#createThreadPoolExecutor(int, int, int, java.util.concurrent.ThreadFactory, java.util.concurrent.RejectedExecutionHandler, java.util.concurrent.BlockingQueue)
//     */
//    @Override
//    protected ThreadPoolExecutor createThreadPoolExecutor(int corePoolSize, int maxPoolSize, int keepAliveSeconds,
//            ThreadFactory threadFactory, RejectedExecutionHandler rejectedExecutionHandler, BlockingQueue<Runnable> queue) {
//        return new QualityOfServiceThreadPoolExecutor(corePoolSize, maxPoolSize, keepAliveSeconds, TimeUnit.SECONDS, queue, threadFactory, rejectedExecutionHandler);
//    }

    /* (non-Javadoc)
     * @see org.jasig.portal.utils.threading.DynamicThreadPoolExecutorFactoryBean#createQueue(int)
     */
    @Override
    protected BlockingQueue<Runnable> createQueue(int queueCapacity) {
        try {
            this.qualityOfServiceBlockingQueue = this.qualityOfServiceQueueConstructor.newInstance(queueCapacity);
        }
        catch (InstantiationException e) {
            throw new RuntimeException(e);
        }
        catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
        catch (InvocationTargetException e) {
            throw new RuntimeException(e);
        }
        
        return this.qualityOfServiceBlockingQueue;
    }
    
    private final class QualityOfServiceThreadPoolExecutor extends ThreadPoolExecutor {
        private QualityOfServiceThreadPoolExecutor(int corePoolSize, int maximumPoolSize, long keepAliveTime,
                TimeUnit unit, BlockingQueue<Runnable> workQueue, ThreadFactory threadFactory,
                RejectedExecutionHandler handler) {
            
            super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue, threadFactory, handler);
        }

        /* (non-Javadoc)
         * @see java.util.concurrent.AbstractExecutorService#newTaskFor(java.lang.Runnable, java.lang.Object)
         */
        @Override
        protected <T> RunnableFuture<T> newTaskFor(Runnable runnable, T value) {
            throw new UnsupportedOperationException();
        }

        /* (non-Javadoc)
         * @see java.util.concurrent.AbstractExecutorService#newTaskFor(java.util.concurrent.Callable)
         */
        @SuppressWarnings("unchecked")
        @Override
        protected <T> RunnableFuture<T> newTaskFor(Callable<T> callable) {
            if (callable instanceof RunnableFuture) {
                return (RunnableFuture<T>)callable;
            }
            
            throw new UnsupportedOperationException();
        }
    }
}
