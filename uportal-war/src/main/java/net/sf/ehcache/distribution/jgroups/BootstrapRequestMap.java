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

import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.Collections;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Simplified version of a Map that wraps a weak value reference concurrent Map of {@link BootstrapRequest}s.
 * It also handles allowing clients to wait for the map's size to change to a specific value.
 *
 * @author Eric Dalquist
 * @version $Revision$
 */
class BootstrapRequestMap {
    private static final Logger LOG = LoggerFactory.getLogger(BootstrapRequestMap.class.getName());

    private final ConcurrentMap<String, Reference<BootstrapRequest>> bootstrapRequests =
        new ConcurrentHashMap<String, Reference<BootstrapRequest>>();
    private final Object requestChangeNotifier = new Object();

    /**
     * Wait for the map to change to the specified size. Returns true if the map reached the size before
     * the timeout.
     */
    public boolean waitForMapSize(int size, long duration) {
        final long waitTime = Math.min(duration, 1000);

        final long start = System.currentTimeMillis();
        this.cleanBootstrapRequests();
        while (this.bootstrapRequests.size() != size && (System.currentTimeMillis() - start) < duration) {
            try {
                synchronized (this.requestChangeNotifier) {
                    this.requestChangeNotifier.wait(waitTime);
                }
            } catch (InterruptedException e) {
                LOG.warn("Interrupted while waiting for BootstrapRequestMap to empty", e);
            }

            this.cleanBootstrapRequests();
        }

        return this.bootstrapRequests.size() == size;
    }

    /**
     * @see Map#keySet()
     */
    public Set<String> keySet() {
        this.cleanBootstrapRequests();
        return Collections.unmodifiableSet(this.bootstrapRequests.keySet());
    }

    /**
     * @see Map#isEmpty()
     */
    public boolean isEmpty() {
        this.cleanBootstrapRequests();
        return this.bootstrapRequests.isEmpty();
    }

    /**
     * @see Map#size()
     */
    public int size() {
        this.cleanBootstrapRequests();
        return this.bootstrapRequests.size();
    }

    /**
     * @see Map#put(Object, Object)
     */
    public BootstrapRequest put(String cacheName, BootstrapRequest bootstrapRequest) {
        final Reference<BootstrapRequest> oldReference =
            this.bootstrapRequests.put(cacheName, new WeakReference<BootstrapRequest>(bootstrapRequest));

        synchronized (this.requestChangeNotifier) {
            this.requestChangeNotifier.notifyAll();
        }

        if (oldReference != null) {
            return oldReference.get();
        }

        return null;
    }

    /**
     * @see Map#get(Object)
     */
    public BootstrapRequest get(String cacheName) {
        final Reference<BootstrapRequest> reference = this.bootstrapRequests.get(cacheName);
        if (reference == null) {
            return null;
        }

        final BootstrapRequest bootstrapRequest = reference.get();
        if (bootstrapRequest == null) {
            LOG.info("BootstrapRequest for {} has been GCed, removing from requests map.", cacheName);

            //Remove GC'd entry
            if (this.bootstrapRequests.remove(cacheName, reference)) {
                synchronized (this.requestChangeNotifier) {
                    this.requestChangeNotifier.notifyAll();
                }
            }
            return null;
        }

        return bootstrapRequest;
    }

    /**
     * @see Map#remove(Object)
     */
    public BootstrapRequest remove(String cacheName) {
        final Reference<BootstrapRequest> reference = this.bootstrapRequests.remove(cacheName);
        if (reference == null) {
            return null;
        }

        synchronized (this.requestChangeNotifier) {
            this.requestChangeNotifier.notifyAll();
        }

        return reference.get();
    }

    /**
     * Iterates over the map cleaning up {@link WeakReference}s that have been GCd
     */
    public void cleanBootstrapRequests() {
        for (final Iterator<Entry<String, Reference<BootstrapRequest>>> bootstrapRequestItr =
             this.bootstrapRequests.entrySet().iterator(); bootstrapRequestItr.hasNext();) {

            final Entry<String, Reference<BootstrapRequest>> bootstrapRequestEntry = bootstrapRequestItr.next();

            if (bootstrapRequestEntry.getValue().get() == null) {
                LOG.info("BootstrapRequest for {} has been GCed, removing from requests map.", bootstrapRequestEntry.getKey());
                bootstrapRequestItr.remove();

                synchronized (this.requestChangeNotifier) {
                    this.requestChangeNotifier.notifyAll();
                }
            }
        }
    }
}
