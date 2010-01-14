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

package org.jasig.portal.utils;

import java.util.Set;

/**
 * This is a weird semaphore that makes every thread wait, until
 * all of Strings from a given set have been "checked in".
 *
 * @author Peter Kharchenko  {@link <a href="mailto:pkharchenko@interactivebusiness.com"">pkharchenko@interactivebusiness.com"</a>}
 * @version $Revision$
 */
public class SetCheckInSemaphore {
    private Set registry;

    /**
     * Creates a new <code>CountDownSemaphore</code> instance.
     *
     * @param registrySet a <code>Set</code> of key objects
     * that will have to be "checked in" before any waiting threads are allowed to proceed.
     */
    public SetCheckInSemaphore(Set registrySet) {
        this.registry=registrySet;
    }

    /**
     * Checks in with a given name, and waits for others.
     *
     * @param key an <code>Object</code> value
     */
    public synchronized void checkInAndWaitOn(Object key) {
        registry.remove(key);
        while(!registry.isEmpty()) {
            try {
                this.wait();
            } catch (InterruptedException ie) {}
        }
        this.notifyAll();
    }

    /**
     * Check in a key, but do not wait on the semaphore.
     *
     * @param key an <code>Object</code> value
     */
    public synchronized void checkIn(Object key) {
        registry.remove(key);
        if(registry.isEmpty()) {
            this.notifyAll();
        }
    }

    /**
     * Wait on the semaphore, without checking in any keys.
     *
     */
    public synchronized void waitOn() {
        while(!registry.isEmpty()) {
            try {
                this.wait();
            } catch (InterruptedException ie) {}
        }
    }

    /**
     * Checks in all the remaining values, so that all
     * threads can proceed immediately.
     *
     */
    public synchronized void checkInAll() {
        registry.clear();
        this.notifyAll();
    }

}
