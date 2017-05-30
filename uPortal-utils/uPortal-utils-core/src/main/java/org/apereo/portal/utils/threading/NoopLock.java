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

import java.util.Date;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;

/**
 * A Lock that doesn't actually Lock, useful if a non-locking placeholder is needed,
 *
 */
public class NoopLock implements Lock {
    public static final NoopLock INSTANCE = new NoopLock();

    /* (non-Javadoc)
     * @see java.util.concurrent.locks.Lock#lock()
     */
    @Override
    public void lock() {}

    /* (non-Javadoc)
     * @see java.util.concurrent.locks.Lock#lockInterruptibly()
     */
    @Override
    public void lockInterruptibly() throws InterruptedException {}

    /* (non-Javadoc)
     * @see java.util.concurrent.locks.Lock#tryLock()
     */
    @Override
    public boolean tryLock() {
        return true;
    }

    /* (non-Javadoc)
     * @see java.util.concurrent.locks.Lock#tryLock(long, java.util.concurrent.TimeUnit)
     */
    @Override
    public boolean tryLock(long time, TimeUnit unit) throws InterruptedException {
        return true;
    }

    /* (non-Javadoc)
     * @see java.util.concurrent.locks.Lock#unlock()
     */
    @Override
    public void unlock() {}

    /* (non-Javadoc)
     * @see java.util.concurrent.locks.Lock#newCondition()
     */
    @Override
    public Condition newCondition() {
        return new NoopCondition();
    }

    private static final class NoopCondition implements Condition {
        @Override
        public void await() throws InterruptedException {}

        @Override
        public void awaitUninterruptibly() {}

        @Override
        public long awaitNanos(long nanosTimeout) throws InterruptedException {
            return 0;
        }

        @Override
        public boolean await(long time, TimeUnit unit) throws InterruptedException {
            return true;
        }

        @Override
        public boolean awaitUntil(Date deadline) throws InterruptedException {
            return true;
        }

        @Override
        public void signal() {}

        @Override
        public void signalAll() {}
    }
}
