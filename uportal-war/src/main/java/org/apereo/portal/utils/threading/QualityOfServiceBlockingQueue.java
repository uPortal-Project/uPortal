/**
 * Licensed to Apereo under one or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information regarding copyright ownership. Apereo
 * licenses this file to you under the Apache License, Version 2.0 (the "License"); you may not use
 * this file except in compliance with the License. You may obtain a copy of the License at the
 * following location:
 *
 * <p>http://www.apache.org/licenses/LICENSE-2.0
 *
 * <p>Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apereo.portal.utils.threading;

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.NoSuchElementException;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import org.apereo.portal.utils.ConcurrentMapUtils;

/**
 * A thread-safe blocking queue that places elements into sub-queues based on the key returned for
 * each element by {@link #getElementKey(Object)}. Implementations are responsible for providing the
 * logic to determine the key for an element and to determine the order in which queued elements are
 * returned via {@link #take()}, {@link #poll()}, {@link #poll(long, TimeUnit)}, {@link #remove()},
 * {@link #element()}, {@link #peek()}, {@link #drainTo(Collection)}, and {@link
 * #drainTo(Collection, int)}
 *
 * <p>The class appropriately handles {@link #peek()} such that the peeked element will be the
 * element operated on by {@link #take()}, {@link #poll()}, {@link #poll(long, TimeUnit)}, {@link
 * #remove()}, {@link #element()}, {@link #drainTo(Collection)}, and {@link #drainTo(Collection,
 * int)} no matter how much time has elapsed
 *
 * @param <K> The type of key used for grouping elements in the queue
 * @param <T> The type of elements in the queue
 */
public abstract class QualityOfServiceBlockingQueue<K, T> implements BlockingQueue<T> {
    private final ConcurrentMap<K, Queue<T>> keyedQueues = new ConcurrentHashMap<K, Queue<T>>();
    private final Set<K> queueKeySet = Collections.unmodifiableSet(this.keyedQueues.keySet());

    private final int capacity;
    private final ReadWriteLock lock = new ReentrantReadWriteLock();
    private final Lock readLock = lock.readLock();
    private final Lock writeLock = lock.writeLock();
    private final Condition notEmpty = writeLock.newCondition();
    private final Condition notFull = writeLock.newCondition();

    //Track the total size of the queue and peek data, these fields MUST be accessed within a read or write lock and
    //updated ONLY from within a write lock
    private int size = 0;
    private K peekedKey = null;

    public QualityOfServiceBlockingQueue() {
        this.capacity = Integer.MAX_VALUE;
    }

    public QualityOfServiceBlockingQueue(int capacity) {
        if (capacity <= 0) {
            throw new IllegalArgumentException();
        }

        this.capacity = capacity;
    }

    /** @return the key for the specified element */
    protected abstract K getElementKey(T e);

    /**
     * Get the next key to use for a call to {@link #take()}, {@link #poll()}, {@link #poll(long,
     * TimeUnit)}, {@link #remove()}, {@link #element()}, {@link #peek()}, {@link
     * #drainTo(Collection)}, or {@link #drainTo(Collection, int)}
     *
     * <p>This method will only be called if {@link #isEmpty()} is false and will never be called
     * concurrently. It must only return a key for which {@link #isKeyEmpty(Object)} returns false;
     */
    protected abstract K getNextElementKey();

    /** @return A read only Set of the keys in the queue */
    public final Set<K> getKeySet() {
        return queueKeySet;
    }

    /** @return true if there are no elements for the specified key */
    public final boolean isKeyEmpty(K key) {
        final Queue<T> queue = this.keyedQueues.get(key);
        if (queue == null) {
            return true;
        }

        return queue.isEmpty();
    }

    /** @return The number of elements in the queue for the specified key */
    public final int getKeySize(K key) {
        final Queue<T> queue = this.keyedQueues.get(key);
        if (queue == null) {
            return 0;
        }

        return queue.size();
    }

    /* (non-Javadoc)
     * @see java.util.concurrent.BlockingQueue#add(java.lang.Object)
     */
    @Override
    public final boolean add(T e) {
        return this.add(e, true);
    }

    /* (non-Javadoc)
     * @see java.util.concurrent.BlockingQueue#offer(java.lang.Object)
     */
    @Override
    public final boolean offer(T e) {
        return this.add(e, false);
    }

    /* (non-Javadoc)
     * @see java.util.concurrent.BlockingQueue#put(java.lang.Object)
     */
    @Override
    public final void put(T e) throws InterruptedException {
        this.offer(e, -1, TimeUnit.MILLISECONDS);
    }

    /* (non-Javadoc)
     * @see java.util.concurrent.BlockingQueue#offer(java.lang.Object, long, java.util.concurrent.TimeUnit)
     */
    @Override
    public final boolean offer(T e, long timeout, TimeUnit unit) throws InterruptedException {
        final Queue<T> queue = this.getOrCreateQueue(e);

        final long start = getWriteLockWithOptionalWait(timeout, unit);
        if (start == Long.MIN_VALUE) {
            //Min value signals a timeout while waiting
            return false;
        }
        final long maxWait = start >= 0 ? unit.toMillis(timeout) : -1;

        try {
            if (!this.waitForRemove(start, maxWait)) {
                return false;
            }

            final boolean added = queue.add(e);
            if (added) {
                this.size++;
                this.notEmpty.signal();
            }
            return added;
        } finally {
            this.writeLock.unlock();
        }
    }

    /* (non-Javadoc)
     * @see java.util.concurrent.BlockingQueue#take()
     */
    @Override
    public final T take() throws InterruptedException {
        return this.poll(-1, TimeUnit.MILLISECONDS);
    }

    /* (non-Javadoc)
     * @see java.util.concurrent.BlockingQueue#poll(long, java.util.concurrent.TimeUnit)
     */
    @Override
    public final T poll(final long timeout, final TimeUnit unit) throws InterruptedException {
        final long start = getWriteLockWithOptionalWait(timeout, unit);
        if (start == Long.MIN_VALUE) {
            //Min value signals a timeout while waiting
            return null;
        }
        final long maxWait = start >= 0 ? unit.toMillis(timeout) : -1;

        try {
            //Wait for an element to be available to return
            if (!this.waitForAdd(start, maxWait)) {
                return null;
            }

            return this.pollInternal(false);
        } finally {
            this.writeLock.unlock();
        }
    }

    /* (non-Javadoc)
     * @see java.util.concurrent.BlockingQueue#remainingCapacity()
     */
    @Override
    public final int remainingCapacity() {
        this.readLock.lock();
        try {
            return this.capacity - this.size;
        } finally {
            this.readLock.unlock();
        }
    }

    /* (non-Javadoc)
     * @see java.util.concurrent.BlockingQueue#remove(java.lang.Object)
     */
    @Override
    public final boolean remove(Object o) {
        //Short circuit using read-lock
        if (this.isEmpty()) {
            return false;
        }

        @SuppressWarnings("unchecked")
        final K key = this.getElementKey((T) o);
        final Queue<T> queue = this.keyedQueues.get(key);
        if (queue == null) {
            return false;
        }

        this.writeLock.lock();
        try {
            final boolean removed = queue.remove(o);
            if (removed) {
                this.size--;
                this.notFull.signal();
            }
            return removed;
        } finally {
            this.writeLock.unlock();
        }
    }

    /* (non-Javadoc)
     * @see java.util.concurrent.BlockingQueue#contains(java.lang.Object)
     */
    @Override
    public final boolean contains(Object o) {
        @SuppressWarnings("unchecked")
        final K key = this.getElementKey((T) o);
        final Queue<T> queue = this.keyedQueues.get(key);
        if (queue == null) {
            return false;
        }

        return queue.contains(o);
    }

    /* (non-Javadoc)
     * @see java.util.concurrent.BlockingQueue#drainTo(java.util.Collection)
     */
    @Override
    public final int drainTo(Collection<? super T> c) {
        return this.drainTo(c, Integer.MAX_VALUE);
    }

    /* (non-Javadoc)
     * @see java.util.concurrent.BlockingQueue#drainTo(java.util.Collection, int)
     */
    @Override
    public final int drainTo(Collection<? super T> c, int maxElements) {
        //Short circuit using read-lock
        if (this.isEmpty()) {
            return 0;
        }

        this.writeLock.lock();
        try {
            int count = 0;

            while (count < this.size && count < maxElements) {
                final K key = this.getNextElementKey();

                final Queue<T> queue = this.keyedQueues.get(key);
                if (queue == null || queue.isEmpty()) {
                    throw new IllegalStateException(
                            "getNextElementKey returned key='"
                                    + key
                                    + "' but there are no elements available for the key. This violates the contract specified for getNextElementKey: "
                                    + this.toString());
                }

                final T e = queue.poll();
                c.add(e);

                count++;
            }

            this.size -= count;

            if (count > 0) {
                this.notFull.signal();
            }

            return count;
        } finally {
            this.writeLock.unlock();
        }
    }

    /* (non-Javadoc)
     * @see java.util.Queue#remove()
     */
    @Override
    public final T remove() {
        //Short circuit using read-lock
        if (this.isEmpty()) {
            throw new NoSuchElementException();
        }

        final T e = this.poll();
        if (e == null) {
            throw new NoSuchElementException();
        }
        return e;
    }

    /* (non-Javadoc)
     * @see java.util.Queue#poll()
     */
    @Override
    public final T poll() {
        //Short circuit using read-lock
        if (this.isEmpty()) {
            return null;
        }

        return this.pollInternal(false);
    }

    /* (non-Javadoc)
     * @see java.util.Queue#element()
     */
    @Override
    public final T element() {
        //Short circuit using read-lock
        if (this.isEmpty()) {
            throw new NoSuchElementException();
        }

        final T e = this.peek();
        if (e == null) {
            throw new NoSuchElementException();
        }
        return e;
    }

    /* (non-Javadoc)
     * @see java.util.Queue#peek()
     */
    @Override
    public final T peek() {
        //Short circuit using read-lock
        if (this.isEmpty()) {
            return null;
        }

        //No existing peeked element, need to read it off the next queue
        return this.pollInternal(true);
    }

    /* (non-Javadoc)
     * @see java.util.Collection#size()
     */
    @Override
    public final int size() {
        this.readLock.lock();
        try {
            return this.size;
        } finally {
            this.readLock.unlock();
        }
    }

    /* (non-Javadoc)
     * @see java.util.Collection#isEmpty()
     */
    @Override
    public final boolean isEmpty() {
        return this.size() == 0;
    }

    /* (non-Javadoc)
     * @see java.util.Collection#iterator()
     */
    @Override
    public final Iterator<T> iterator() {
        return new ElementIterator();
    }

    /* (non-Javadoc)
     * @see java.util.Collection#toArray()
     */
    @Override
    public final Object[] toArray() {
        this.readLock.lock();
        try {
            return this.toArray(new Object[this.size]);
        } finally {
            this.readLock.unlock();
        }
    }

    /* (non-Javadoc)
     * @see java.util.Collection#toArray(T[])
     */
    @SuppressWarnings("unchecked")
    @Override
    public final <AT> AT[] toArray(AT[] a) {
        this.readLock.lock();
        try {
            //Verify the array size
            if (a.length < this.size) {
                a =
                        (AT[])
                                java.lang.reflect.Array.newInstance(
                                        a.getClass().getComponentType(), this.size);
            }

            //Trick to avoid generics warning
            final Object[] result = a;

            //Copy over all elements
            int index = 0;
            for (final Queue<T> queue : this.keyedQueues.values()) {
                for (final T e : queue) {
                    result[index++] = e;
                }
            }

            //If array is too big set next element null
            if (a.length > this.size) {
                a[this.size] = null;
            }

            return a;
        } finally {
            this.readLock.unlock();
        }
    }

    /* (non-Javadoc)
     * @see java.util.Collection#containsAll(java.util.Collection)
     */
    @Override
    public final boolean containsAll(Collection<?> c) {
        //Short circuit with size comparison first
        final int size = this.size();
        if (size == 0 || size < c.size()) {
            return false;
        }

        for (final Object o : c) {
            @SuppressWarnings("unchecked")
            final K key = this.getElementKey((T) o);
            final Queue<T> queue = this.keyedQueues.get(key);
            if (queue == null || !queue.contains(o)) {
                return false;
            }
        }

        return true;
    }

    /* (non-Javadoc)
     * @see java.util.Collection#addAll(java.util.Collection)
     */
    @Override
    public final boolean addAll(Collection<? extends T> c) {
        boolean changed = false;

        for (final T o : c) {
            changed |= this.add(o);
        }

        return changed;
    }

    /* (non-Javadoc)
     * @see java.util.Collection#removeAll(java.util.Collection)
     */
    @Override
    public final boolean removeAll(Collection<?> c) {
        boolean changed = false;

        for (final Object o : c) {
            changed |= this.remove(o);
        }

        return changed;
    }

    /* (non-Javadoc)
     * @see java.util.Collection#retainAll(java.util.Collection)
     */
    @Override
    public final boolean retainAll(Collection<?> c) {
        this.writeLock.lock();
        try {
            int newSize = 0;
            for (final Queue<T> queue : this.keyedQueues.values()) {
                queue.retainAll(c);
                newSize += queue.size();
            }

            //Update the queue size
            final int oldSize = this.size;
            this.size = newSize;

            //If the updated size and old size differ things changed
            return this.size != oldSize;
        } finally {
            this.writeLock.unlock();
        }
    }

    /* (non-Javadoc)
     * @see java.util.Collection#clear()
     */
    @Override
    public final void clear() {
        this.writeLock.lock();
        try {
            this.size = 0;
            this.keyedQueues.clear();
        } finally {
            this.writeLock.unlock();
        }
    }

    /**
     * Adds the element to the queue
     *
     * @param failWhenFull If true and the queue is at capacity then this method throws a
     *     IllegalStateException, if false then false is returned
     * @return true if the element was added, false if not
     */
    private boolean add(T e, boolean failWhenFull) {
        final Queue<T> queue = this.getOrCreateQueue(e);

        this.writeLock.lock();
        try {
            if (this.size == this.capacity) {
                if (failWhenFull) {
                    throw new IllegalStateException("Queue is at capacity: " + this.capacity);
                }

                return false;
            }

            final boolean added = queue.add(e);
            if (added) {
                this.size++;
                this.notEmpty.signal();
            }
            return added;
        } finally {
            this.writeLock.unlock();
        }
    }

    /**
     * @return The Queue to use for the specified element
     * @param create If true a Queue will be created for the element if one does not already exist
     */
    private Queue<T> getOrCreateQueue(T e) {
        final K key = this.getElementKey(e);
        Queue<T> queue = this.keyedQueues.get(key);
        if (queue == null) {
            queue = new ConcurrentLinkedQueue<T>();
            queue = ConcurrentMapUtils.putIfAbsent(this.keyedQueues, key, queue);
        }
        return queue;
    }

    /**
     * Combination peek/poll method that uses a boolean parameter to switch between the two
     * behaviors
     *
     * @param peek If true this method returns the peeked element, if false it returns the polled
     *     element
     */
    private T pollInternal(boolean peek) {
        this.writeLock.lock();
        try {
            //Re-check size within the write lock
            if (this.size == 0) {
                return null;
            }

            final K key;
            if (this.peekedKey != null) {
                //If there is a peeked key use it
                key = this.peekedKey;
                if (!peek) {
                    //If not a peek consume the peekedKey
                    this.peekedKey = null;
                }
            } else {
                //Get the next element key
                key = this.getNextElementKey();
                if (peek) {
                    //If a peek store the key
                    this.peekedKey = key;
                }
            }

            //Get the associated Queue and sanitity check the value from getNextElementKey()
            final Queue<T> queue = this.keyedQueues.get(key);
            if (queue == null || queue.isEmpty()) {
                throw new IllegalStateException(
                        "getNextElementKey returned key='"
                                + key
                                + "' but there are no elements available for the key. This violates the contract specified for getNextElementKey");
            }

            if (peek) {
                //If a peek just return a peek from the queue
                return queue.peek();
            }

            //Not a peek, decrement the size and poll the queue
            this.size--;
            this.notFull.signal();
            return queue.poll();
        } finally {
            this.writeLock.unlock();
        }
    }

    /** This MUST be called while {@link #writeLock} is locked by the current thread */
    private boolean waitForRemove(long waitStart, long maxWait) throws InterruptedException {
        if (this.size == this.capacity) {
            if (!waitOnCondition(this.notFull, maxWait, waitStart)) {
                return false;
            }
        }

        return true;
    }

    /**
     * Acquires a write lock either using {@link Lock#tryLock(long, TimeUnit)} if timeout >= 0 or
     * using {@link Lock#lock()} if timeout < 0.
     *
     * @param timeout Duration to wait for lock, if less than 0 will wait forever via {@link
     *     Lock#lock()}
     * @param unit Time units for timeout
     * @return If waiting with timeout the {@link System#currentTimeMillis()} that the waiting
     *     started, if waiting with timeout timed out will return {@value Long#MIN_VALUE}
     * @throws InterruptedException
     */
    private long getWriteLockWithOptionalWait(final long timeout, final TimeUnit unit)
            throws InterruptedException {
        //Get the write lock and capture start time and max wait time if waiting a specified timeout
        final long start;
        if (timeout >= 0) {
            start = System.currentTimeMillis();

            final boolean locked = this.writeLock.tryLock(timeout, unit);
            if (!locked) {
                //Hit timeout waiting for lock
                return Long.MIN_VALUE;
            }
        } else {
            start = -1;

            this.writeLock.lock();
        }
        return start;
    }

    /** This MUST be called while {@link #writeLock} is locked by the current thread */
    private boolean waitForAdd(long waitStart, long maxWait) throws InterruptedException {
        while (this.size == 0) {
            if (!waitOnCondition(this.notEmpty, maxWait, waitStart)) {
                return false;
            }
        }

        return true;
    }

    /**
     * @param condition The condition to wait on
     * @param maxWait The maximum time in milliseconds to wait, waits forever if less than 0
     * @param waitStart The original start time of the waiting, only used if maxWait is >= 0
     */
    private boolean waitOnCondition(final Condition condition, long maxWait, long waitStart)
            throws InterruptedException {
        if (maxWait >= 0) {
            final long waited = System.currentTimeMillis() - waitStart;
            final long waitTime = maxWait - waited;
            if (waitTime <= 0) {
                //Hit timeout waiting for new element
                return false;
            }

            final boolean notified = condition.await(waitTime, TimeUnit.MILLISECONDS);
            if (!notified) {
                //Hit timeout waiting for new element
                return false;
            }
        } else {
            condition.await();
        }

        return true;
    }

    /** Iterates over the Queue's in the keyedQueues Map */
    private final class ElementIterator implements Iterator<T> {
        private final Iterator<Queue<T>> queueIterator;
        private Iterator<T> elementIterator = null;

        public ElementIterator() {
            this.queueIterator = keyedQueues.values().iterator();
        }

        /* (non-Javadoc)
         * @see java.util.Iterator#hasNext()
         */
        @Override
        public boolean hasNext() {
            return (this.elementIterator != null && this.elementIterator.hasNext())
                    || this.queueIterator.hasNext();
        }

        /* (non-Javadoc)
         * @see java.util.Iterator#next()
         */
        @Override
        public T next() {
            if (this.elementIterator == null || !this.elementIterator.hasNext()) {
                final Queue<T> queue = this.queueIterator.next();
                this.elementIterator = queue.iterator();
            }

            return this.elementIterator.next();
        }

        /* (non-Javadoc)
         * @see java.util.Iterator#remove()
         */
        @Override
        public void remove() {
            writeLock.lock();
            try {
                this.elementIterator.remove();
                size--;
                notFull.signal();
            } finally {
                writeLock.unlock();
            }
        }
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        this.readLock.lock();
        try {
            final StringBuilder str = new StringBuilder((this.size * 50) + 2);

            str.append("{");

            for (final Iterator<Entry<K, Queue<T>>> entryItr =
                            this.keyedQueues.entrySet().iterator();
                    entryItr.hasNext();
                    ) {
                final Entry<K, Queue<T>> entry = entryItr.next();
                final K key = entry.getKey();
                final Queue<T> queue = entry.getValue();
                str.append(key).append("=").append(queue.size());

                if (entryItr.hasNext()) {
                    str.append(", ");
                }
            }

            str.append("}");
            return str.toString();
        } finally {
            this.readLock.unlock();
        }
    }
}
