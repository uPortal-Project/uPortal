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

import java.util.Collection;
import java.util.Iterator;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.scheduling.concurrent.ExecutorConfigurationSupport;

/**
 * Creates a {@link ThreadPoolExecutor} that behaves more like expected where new threads are created BEFORE
 * tasks are queued
 * 
 * @author Eric Dalquist
 * @version $Revision$
 */
public class DynamicThreadPoolExecutorFactoryBean extends ExecutorConfigurationSupport implements
        FactoryBean<ExecutorService>, InitializingBean, DisposableBean, RejectedExecutionHandler {

    private static final long serialVersionUID = 1L;

    private RejectedExecutionHandler rejectedExecutionHandler = new ThreadPoolExecutor.AbortPolicy();
    private BlockingQueue<Runnable> blockingQueue;

    private int corePoolSize = 1;

    private int maxPoolSize = Integer.MAX_VALUE;

    private int keepAliveSeconds = 60;

    private boolean allowCoreThreadTimeOut = false;

    private int queueCapacity = Integer.MAX_VALUE;

    private boolean exposeUnconfigurableExecutor = false;
    
    private boolean purgeOnRejection = true;

    private ThreadPoolExecutor threadPoolExecutor;
    private ExecutorService exposedExecutor;

    @Override
    public void afterPropertiesSet() {
        super.setRejectedExecutionHandler(this);
        super.afterPropertiesSet();
    }

    @Override
    public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
        if (this.blockingQueue.offer(r)) {
            return;
        }
            
        //Purge canceled tasks that are still in the queue to see if that frees up any space
        if (this.purgeOnRejection) {
            this.threadPoolExecutor.purge();
            
            if (this.blockingQueue.offer(r)) {
                //space freed up and the runnable was added to the queue
                return;
            }
        }
        
        //We really are saturated (threads all busy and queue is full)
        this.rejectedExecutionHandler.rejectedExecution(r, executor);
    }
    
    /**
     * Set if {@link ThreadPoolExecutor#purge()} should be called when a submitted task is rejected. If purge is
     * called the task will be offered again and only if the queue is still full will it be passed on to the
     * {@link RejectedExecutionHandler}
     */
    public void setPurgeOnRejection(boolean purgeOnRejection) {
        this.purgeOnRejection = purgeOnRejection;
    }

    /**
     * Set the ThreadPoolExecutor's core pool size.
     * Default is 1.
     * <p><b>This setting can be modified at runtime, for example through JMX.</b>
     */
    public void setCorePoolSize(int corePoolSize) {
        this.corePoolSize = corePoolSize;
    }

    /**
     * Set the ThreadPoolExecutor's maximum pool size.
     * Default is <code>Integer.MAX_VALUE</code>.
     * <p><b>This setting can be modified at runtime, for example through JMX.</b>
     */
    public void setMaxPoolSize(int maxPoolSize) {
        this.maxPoolSize = maxPoolSize;
    }

    /**
     * Set the ThreadPoolExecutor's keep-alive seconds.
     * Default is 60.
     * <p><b>This setting can be modified at runtime, for example through JMX.</b>
     */
    public void setKeepAliveSeconds(int keepAliveSeconds) {
        this.keepAliveSeconds = keepAliveSeconds;
    }

    /**
     * Specify whether to allow core threads to time out. This enables dynamic
     * growing and shrinking even in combination with a non-zero queue (since
     * the max pool size will only grow once the queue is full).
     * <p>Default is "false". Note that this feature is only available on Java 6
     * or above. On Java 5, consider switching to the backport-concurrent
     * version of ThreadPoolTaskExecutor which also supports this feature.
     * @see java.util.concurrent.ThreadPoolExecutor#allowCoreThreadTimeOut(boolean)
     */
    public void setAllowCoreThreadTimeOut(boolean allowCoreThreadTimeOut) {
        this.allowCoreThreadTimeOut = allowCoreThreadTimeOut;
    }

    /**
     * Set the capacity for the ThreadPoolExecutor's BlockingQueue.
     * Default is <code>Integer.MAX_VALUE</code>.
     * <p>Any positive value will lead to a LinkedBlockingQueue instance;
     * any other value will lead to a SynchronousQueue instance.
     * @see java.util.concurrent.LinkedBlockingQueue
     * @see java.util.concurrent.SynchronousQueue
     */
    public void setQueueCapacity(int queueCapacity) {
        this.queueCapacity = queueCapacity;
    }

    /**
     * Specify whether this FactoryBean should expose an unconfigurable
     * decorator for the created executor.
     * <p>Default is "false", exposing the raw executor as bean reference.
     * Switch this flag to "true" to strictly prevent clients from
     * modifying the executor's configuration.
     * @see java.util.concurrent.Executors#unconfigurableScheduledExecutorService
     */
    public void setExposeUnconfigurableExecutor(boolean exposeUnconfigurableExecutor) {
        this.exposeUnconfigurableExecutor = exposeUnconfigurableExecutor;
    }

    @Override
    protected final ExecutorService initializeExecutor(ThreadFactory threadFactory,
            RejectedExecutionHandler rejectedExecutionHandler) {

        this.blockingQueue = this.createQueue(this.queueCapacity);
        
        final AlwaysFullBlockingQueue<Runnable> queue = new AlwaysFullBlockingQueue<Runnable>(this.blockingQueue);
        
        this.threadPoolExecutor = this.createThreadPoolExecutor(this.corePoolSize, this.maxPoolSize,
                this.keepAliveSeconds, threadFactory, rejectedExecutionHandler, queue);
        
        if (this.allowCoreThreadTimeOut) {
            this.threadPoolExecutor.allowCoreThreadTimeOut(true);
        }

        // Wrap executor with an unconfigurable decorator.
        this.exposedExecutor = this.exposeUnconfigurableExecutor ? Executors.unconfigurableExecutorService(this.threadPoolExecutor) : this.threadPoolExecutor;

        return this.exposedExecutor;
    }

    protected ThreadPoolExecutor createThreadPoolExecutor(int corePoolSize, int maxPoolSize, int keepAliveSeconds,
            ThreadFactory threadFactory, RejectedExecutionHandler rejectedExecutionHandler, BlockingQueue<Runnable> queue) {
        return new ThreadPoolExecutor(corePoolSize, maxPoolSize,
                keepAliveSeconds, TimeUnit.SECONDS, queue, threadFactory, rejectedExecutionHandler);
    }

    @Override
    public ExecutorService getObject() throws Exception {
        return this.exposedExecutor;
    }

    @Override
    public Class<? extends ExecutorService> getObjectType() {
        return this.exposedExecutor != null ? this.exposedExecutor.getClass() : ExecutorService.class;
    }

    @Override
    public boolean isSingleton() {
        return true;
    }

    @Override
    public void setRejectedExecutionHandler(RejectedExecutionHandler rejectedExecutionHandler) {
        this.rejectedExecutionHandler = rejectedExecutionHandler;
    }

    /**
     * Create the BlockingQueue to use for the ThreadPoolExecutor.
     * <p>A LinkedBlockingQueue instance will be created for a positive
     * capacity value; a SynchronousQueue else.
     * @param queueCapacity the specified queue capacity
     * @return the BlockingQueue instance
     * @see java.util.concurrent.LinkedBlockingQueue
     * @see java.util.concurrent.SynchronousQueue
     */
    protected BlockingQueue<Runnable> createQueue(int queueCapacity) {
        if (queueCapacity > 0) {
            return new LinkedBlockingQueue<Runnable>(queueCapacity);
        }

        return new SynchronousQueue<Runnable>();
    }

    /**
     * {@link BlockingQueue} where {@link BlockingQueue#offer(Object)} always returns false
     */
    private static final class AlwaysFullBlockingQueue<E> implements BlockingQueue<E> {
        private final BlockingQueue<E> blockingQueue;

        public AlwaysFullBlockingQueue(BlockingQueue<E> blockingQueue) {
            this.blockingQueue = blockingQueue;
        }

        @Override
        public boolean offer(E e) {
            return false;
        }

        @Override
        public int size() {
            return this.blockingQueue.size();
        }

        @Override
        public boolean isEmpty() {
            return this.blockingQueue.isEmpty();
        }

        @Override
        public boolean add(E e) {
            return this.blockingQueue.add(e);
        }

        @Override
        public Iterator<E> iterator() {
            return this.blockingQueue.iterator();
        }

        @Override
        public E remove() {
            return this.blockingQueue.remove();
        }

        @Override
        public Object[] toArray() {
            return this.blockingQueue.toArray();
        }

        @Override
        public E poll() {
            return this.blockingQueue.poll();
        }

        @Override
        public E element() {
            return this.blockingQueue.element();
        }

        @Override
        public E peek() {
            return this.blockingQueue.peek();
        }

        @Override
        public <T> T[] toArray(T[] a) {
            return this.blockingQueue.toArray(a);
        }

        @Override
        public void put(E e) throws InterruptedException {
            this.blockingQueue.put(e);
        }

        @Override
        public boolean offer(E e, long timeout, TimeUnit unit) throws InterruptedException {
            return this.blockingQueue.offer(e, timeout, unit);
        }

        @Override
        public E take() throws InterruptedException {
            return this.blockingQueue.take();
        }

        @Override
        public E poll(long timeout, TimeUnit unit) throws InterruptedException {
            return this.blockingQueue.poll(timeout, unit);
        }

        @Override
        public int remainingCapacity() {
            return this.blockingQueue.remainingCapacity();
        }

        @Override
        public boolean remove(Object o) {
            return this.blockingQueue.remove(o);
        }

        @Override
        public boolean contains(Object o) {
            return this.blockingQueue.contains(o);
        }

        @Override
        public int drainTo(Collection<? super E> c) {
            return this.blockingQueue.drainTo(c);
        }

        @Override
        public boolean containsAll(Collection<?> c) {
            return this.blockingQueue.containsAll(c);
        }

        @Override
        public int drainTo(Collection<? super E> c, int maxElements) {
            return this.blockingQueue.drainTo(c, maxElements);
        }

        @Override
        public boolean addAll(Collection<? extends E> c) {
            return this.blockingQueue.addAll(c);
        }

        @Override
        public boolean removeAll(Collection<?> c) {
            return this.blockingQueue.removeAll(c);
        }

        @Override
        public boolean retainAll(Collection<?> c) {
            return this.blockingQueue.retainAll(c);
        }

        @Override
        public void clear() {
            this.blockingQueue.clear();
        }

        @Override
        public boolean equals(Object o) {
            return this.blockingQueue.equals(o);
        }

        @Override
        public int hashCode() {
            return this.blockingQueue.hashCode();
        }

        @Override
        public String toString() {
            return this.blockingQueue.toString();
        }
    }
}
