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

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;

/**
 * Utility class for safely interacting with a read-write lock
 *
 */
public class ReadWriteLockTemplate {
    public static <T> T doWithLock(ReadWriteLock readWriteLock, ReadWriteCallback<T> c) {
        final ReadResult<T> readResult;

        final Lock readLock = readWriteLock.readLock();
        readLock.lock();
        try {
            readResult = c.doInReadLock();
        } finally {
            readLock.unlock();
        }

        if (!readResult.isDoWriteLock()) {
            return readResult.getResult();
        }

        final Lock writeLock = readWriteLock.writeLock();
        writeLock.lock();
        try {
            return c.doInWriteLock(readResult);
        } finally {
            writeLock.unlock();
        }
    }
}
