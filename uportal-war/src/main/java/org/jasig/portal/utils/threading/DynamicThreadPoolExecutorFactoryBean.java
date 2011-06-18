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
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.springframework.scheduling.concurrent.ThreadPoolExecutorFactoryBean;

/**
 * Creates a {@link ThreadPoolExecutor} that behaves more like expected where new threads are created BEFORE
 * tasks are queued
 * 
 * @author Eric Dalquist
 * @version $Revision$
 */
public class DynamicThreadPoolExecutorFactoryBean 
        extends ThreadPoolExecutorFactoryBean
        implements RejectedExecutionHandler {
        
    private static final long serialVersionUID = 1L;
    
    private RejectedExecutionHandler rejectedExecutionHandler = new ThreadPoolExecutor.AbortPolicy();
    private BlockingQueue<Runnable> blockingQueue;

    @Override
    public void afterPropertiesSet() {
        super.setRejectedExecutionHandler(this);
        super.afterPropertiesSet();
    }
    
    @Override
    public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
        if (!this.blockingQueue.offer(r)) {
            //We really are saturated (threads all busy and queue is full)
            rejectedExecutionHandler.rejectedExecution(r, executor);
        }
    }
    
    @Override
    public void setRejectedExecutionHandler(RejectedExecutionHandler rejectedExecutionHandler) {
        this.rejectedExecutionHandler = rejectedExecutionHandler;
    }

    @Override
    protected BlockingQueue<Runnable> createQueue(int queueCapacity) {
        this.blockingQueue = super.createQueue(queueCapacity);
        return new AlwaysFullBlockingQueue<Runnable>(this.blockingQueue);
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
