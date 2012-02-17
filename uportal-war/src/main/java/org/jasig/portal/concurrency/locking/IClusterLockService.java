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

import com.google.common.base.Function;

/**
 * Service which allows actions to be executed within a cluster wide lock, locks are reentrant.
 * 
 * @author Eric Dalquist
 * @version $Revision$
 */
public interface IClusterLockService {
    public interface TryLockFunctionResult<T> {

        /**
         * @return True if the function was executed, false if not
         */
        boolean isExecuted();
        
        /**
         * @return The function result, if {@link #isExecuted()} returns false this method should be ignored
         */
        T getResult();
    }
    
    /**
     * Execute the specified function within the named mutex. If the mutex is currently owned by another thread or
     * server the method will return immediately 
     * 
     * @param mutexName Name of the lock (case sensitive)
     * @param lockFunction The fuction to call within the lock context, the parameter to the function is the lock name
     * @return The value returned by the lockFunction
     */
    <T> TryLockFunctionResult<T> doInTryLock(String mutexName, Function<String, T> lockFunction) throws InterruptedException;
    
    /**
     * Check if the current thread already owns the specified lock
     * 
     * @param mutexName Name of the lock (case sensitive)
     * @return true if the current thread owns the lock on the specified mutex
     */
    boolean isLockOwner(String mutexName);
    
    /**
     * Check if any thread or server owns the specified lock
     * 
     * @param mutexName Name of the lock (case sensitive)
     * @return true if the any thread or server owns the lock on the specified mutex
     */
    boolean isLocked(String mutexName);
}
