/**
 *  Copyright 2003-2010 Terracotta, Inc.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package net.sf.ehcache.distribution.jgroups;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import net.sf.ehcache.Ehcache;

/**
 * Tracks the status of a bootstrap request
 * 
 * @author Eric Dalquist
 * @version $Revision$
 */
public class BootstrapRequest {
    /**
     * Possible states of a BootstrapRequest
     */
    public enum BootstrapStatus {
        /**
         * There is no pending request to a peer for bootstrap
         */
        UNSENT,
        /**
         * There is a pending request to a peer for bootstrap
         */
        REQUESTED,
        /**
         * The peer responded that it could not fulfill the bootstrap request
         */
        INCOMPLETE,
        /**
         * The peer responded that it fulfilled the bootstrap request
         */
        COMPLETE;
    }
    
    private volatile CountDownLatch boostrapCompleteLatch = new CountDownLatch(1);
    private final AtomicLong replicated = new AtomicLong();
    private final Ehcache cache;
    private final boolean asynchronous;
    private final int chunkSize;
    private volatile BootstrapStatus bootstrapStatus = BootstrapStatus.UNSENT;

    /**
     * Create a new bootstrap request for the specified cache
     */
    public BootstrapRequest(Ehcache cache, boolean asynchronous, int chunkSize) {
        this.cache = cache;
        this.asynchronous = asynchronous;
        this.chunkSize = chunkSize;
    }
    
    /**
     * @return The current status of the bootstrap request
     */
    public BootstrapStatus getBootstrapStatus() {
        return this.bootstrapStatus;
    }

    /**
     * @param bootstrapStatus The current status of the bootstrap request 
     */
    public void setBootstrapStatus(BootstrapStatus bootstrapStatus) {
        if (bootstrapStatus == null) {
            throw new IllegalArgumentException("BootstrapStatus cannot be null");
        }
        this.bootstrapStatus = bootstrapStatus;
    }

    /**
     * @return If the bootstrap request should be handled asynchronously
     */
    public boolean isAsynchronous() {
        return this.asynchronous;
    }

    /**
     * @return The maximum serialized size of the elements to request from a remote cache peer during bootstrap.
     */
    public int getChunkSize() {
        return this.chunkSize;
    }

    /**
     * Reset the replicationCount and waitForBootstrap latch to their initial states
     */
    public void reset() {
        this.boostrapCompleteLatch = new CountDownLatch(1);
        this.replicated.set(0);
        this.bootstrapStatus = BootstrapStatus.UNSENT;
    }
    
    /**
     * Signal that bootstrapping is complete
     */
    public void boostrapComplete(BootstrapStatus status) {
        this.bootstrapStatus = status;
        this.boostrapCompleteLatch.countDown();
    }
    
    /**
     * Waits for the receiver to signal that the current bootstrap request is complete
     */
    public boolean waitForBoostrap(long timeout, TimeUnit unit) throws InterruptedException {
        return this.boostrapCompleteLatch.await(timeout, unit);
    }
    
    /**
     * Count a received bootstrap replication 
     */
    public void countReplication() {
        this.replicated.incrementAndGet();
    }
    
    /**
     * @return The number of bootstrap replication responses received
     */
    public long getReplicationCount() {
        return this.replicated.get();
    }
    
    /**
     * @return The cache that is being bootstrapped
     */
    public Ehcache getCache() {
        return this.cache;
    }

    
    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return "BootstrapRequest [cache=" + this.cache.getName() + 
            ", bootstrapStatus=" + this.bootstrapStatus + 
            ", boostrapCompleteLatch=" + this.boostrapCompleteLatch.getCount() + 
            ", replicated=" + this.replicated + 
            ", asynchronous=" + this.asynchronous + 
            ", chunkSize=" + this.chunkSize + "]";
    }

    
}
