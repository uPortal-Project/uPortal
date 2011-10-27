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

package org.jasig.portal.concurrency.locking;

import java.util.concurrent.TimeUnit;

import org.springframework.dao.DataIntegrityViolationException;

/**
 * DB based locking DAO
 * 
 * @author Eric Dalquist
 * @version $Revision$
 */
interface IClusterLockDao {

    /**
     * Get a cluster mutex with the specified name, will return null if the specified
     * mutex doesn't exist. If null is returned and a mutex is required call {@link #createClusterMutex(String)}
     * and then {@link #getClusterMutex(String)} again. 
     */
    ClusterMutex getClusterMutex(final String mutexName);

    /**
     * Creates a mutex with the specified name, does not return the mutex as it will likely be in an
     * invalid state due to transaction scoping. To retrieve the newly created mutex call {@link #getClusterMutex(String)}.
     * 
     * @throws DataIntegrityViolationException If the lock already exists, this exception can be safely ignored
     */
    void createClusterMutex(final String mutexName) throws DataIntegrityViolationException;

    /**
     * Get a DB lock on the specified ClusterMutex object, MUST be called from within
     * and existing transaction.
     */
    void lock(ClusterMutex mutex);

    /**
     * Try to get a DB lock on the specified ClusterMutex object, MUST be called from within
     * and existing transaction.
     */
    boolean tryLock(ClusterMutex mutex, long time, TimeUnit unit);
    
    /**
     * Check if the specified mutex is locked
     */
    boolean isLocked(ClusterMutex mutex);

}