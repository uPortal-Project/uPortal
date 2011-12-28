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


/**
 * DB based locking DAO.
 * <p/>
 * Locks are NOT reentrant. If ServerA tries to call getLock twice the 2nd call will return false.
 * 
 * @author Eric Dalquist
 * @version $Revision$
 */
interface IClusterLockDao {

    /**
     * Get a cluster mutex with the specified name.
     * 
     * @param mutexName the name of the mutex
     */
    ClusterMutex getClusterMutex(String mutexName);
    //TODO test concurrent create
    
    /**
     * Lock the specified mutex
     * 
     * @param mutexName The mutex to lock
     * @return True if the lock was successfully acquired, false if the lock is already held.
     */
    boolean getLock(String mutexName);
    //TODO test concurrent lock
    
    /**
     * Update the specified mutex, the mutex must already be locked by this server. It is expected
     * that a locked mutex is updated at a regular interval to inform other servers that the lock is
     * still live.
     * 
     * @param mutexName The mutex to update
     * @throws IllegalMonitorStateException if this server does not currently own the lock
     */
    void updateLock(String mutexName);
    
    /**
     * Release the specified mutex, the mutex must already be locked by this server.
     * 
     * @param mutexName The mutex to release
     * @throws IllegalMonitorStateException if this server does not currently own the lock
     */
    void releaseLock(String mutexName);

}