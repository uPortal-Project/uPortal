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

import net.sf.ehcache.Ehcache;
import net.sf.ehcache.bootstrap.BootstrapCacheLoader;
import net.sf.ehcache.distribution.RemoteCacheException;

/**
 * Loads Elements from a random Cache Peer
 *
 * @author Greg Luck
 * @version $Id$
 */
public class JGroupsBootstrapCacheLoader implements BootstrapCacheLoader {
    /**
     * Whether to load asynchronously
     */
    protected boolean asynchronous;

    /**
     * The maximum serialized size of the elements to request from a remote cache peer during bootstrap.
     */
    protected int maximumChunkSizeBytes;

    /**
     * Creates a bootstrap cache loader that will work with RMI based distribution
     *
     * @param asynchronous Whether to load asynchronously
     */
    public JGroupsBootstrapCacheLoader(boolean asynchronous, int maximumChunkSize) {
        this.asynchronous = asynchronous;
        this.maximumChunkSizeBytes = maximumChunkSize;
    }

    /**
     * Bootstraps the cache from a random CachePeer. Requests are done in chunks estimated at 5MB Serializable size.
     * This balances memory use on each end and network performance.
     *
     * @throws RemoteCacheException if anything goes wrong with the remote call
     */
    public void load(Ehcache cache) throws RemoteCacheException {
        final JGroupsCacheManagerPeerProvider cachePeerProvider = JGroupsCacheManagerPeerProvider.getCachePeerProvider(cache);
        
        final BootstrapRequest bootstrapRequest = new BootstrapRequest(cache, this.asynchronous, this.maximumChunkSizeBytes);
        final JGroupsBootstrapManager bootstrapManager = cachePeerProvider.getBootstrapManager();
        bootstrapManager.handleBootstrapRequest(bootstrapRequest);
    }

    /**
     * @return true if this bootstrap loader is asynchronous
     */
    public boolean isAsynchronous() {
        return this.asynchronous;
    }

    /**
     * Gets the maximum chunk size
     */
    public int getMaximumChunkSizeBytes() {
        return maximumChunkSizeBytes;
    }

    /**
     * Clones this loader
     */
    @Override
    public Object clone() throws CloneNotSupportedException {
        return new JGroupsBootstrapCacheLoader(asynchronous, maximumChunkSizeBytes);
    }
}
