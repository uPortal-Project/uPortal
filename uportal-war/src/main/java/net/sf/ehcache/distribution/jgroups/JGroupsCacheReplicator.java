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

import java.io.Serializable;
import java.rmi.RemoteException;
import java.util.Arrays;
import java.util.List;

import net.sf.ehcache.CacheException;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Ehcache;
import net.sf.ehcache.Element;
import net.sf.ehcache.distribution.CacheManagerPeerProvider;
import net.sf.ehcache.distribution.CachePeer;
import net.sf.ehcache.distribution.CacheReplicator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Pierre Monestie (pmonestie[at]@gmail.com)
 * @author <a href="mailto:gluck@gregluck.com">Greg Luck</a>
 * @version $Id$
 *          <p/> This implements CacheReplicator using JGroups as underlying
 *          replication mechanism The peer provider should be of type
 *          JGroupsCacheManagerPeerProvider It is assumed that the cachepeer is
 *          a JGroupsCacheManagerPeerProvider
 */
public class JGroupsCacheReplicator implements CacheReplicator {
    /**
     * The default interval for async cache replication
     */
    public static final long DEFAULT_ASYNC_INTERVAL = 1000;

    private static final Logger LOG = LoggerFactory.getLogger(JGroupsCacheReplicator.class.getName());

    private final long asynchronousReplicationInterval;

    /**
     * Whether or not to replicate puts
     */
    private final boolean replicatePuts;

    /**
     * Whether or not to replicate updates
     */
    private final boolean replicateUpdates;

    /**
     * Replicate update via copying, if false via deleting
     */
    private final boolean replicateUpdatesViaCopy;

    /**
     * Whether or not to replicate remove events
     */
    private final boolean replicateRemovals;

    private boolean alive;

    /**
     * Constructor called by factory, does synchronous replication
     */
    public JGroupsCacheReplicator(boolean replicatePuts, boolean replicateUpdates, boolean replicateUpdatesViaCopy,
                                  boolean replicateRemovals) {

        this(replicatePuts, replicateUpdates, replicateUpdatesViaCopy, replicateRemovals, -1);
    }

    /**
     * Constructor called by factory, does asynchronous replication
     */
    public JGroupsCacheReplicator(boolean replicatePuts, boolean replicateUpdates, boolean replicateUpdatesViaCopy,
                                  boolean replicateRemovals, long asynchronousReplicationInterval) {

        this.replicatePuts = replicatePuts;
        this.replicateUpdates = replicateUpdates;
        this.replicateUpdatesViaCopy = replicateUpdatesViaCopy;
        this.replicateRemovals = replicateRemovals;

        this.asynchronousReplicationInterval = asynchronousReplicationInterval;
        this.alive = true;
    }


    /**
     * {@inheritDoc}
     */
    public boolean alive() {
        return this.alive;
    }

    /**
     * {@inheritDoc}
     */
    public boolean isReplicateUpdatesViaCopy() {
        return replicateUpdatesViaCopy;
    }

    /**
     * {@inheritDoc}
     */
    public boolean notAlive() {
        return !this.alive;
    }

    /**
     * {@inheritDoc}
     */
    public void dispose() {
        this.alive = false;
    }

    /**
     * {@inheritDoc}
     */
    public void notifyElementExpired(Ehcache cache, Element element) {
        //Ignored
    }

    /**
     * {@inheritDoc}
     */
    public void notifyElementPut(Ehcache cache, Element element) throws CacheException {
        if (notAlive() || !replicatePuts) {
            return;
        }

        replicatePutNotification(cache, element);
    }

    /**
     * {@inheritDoc}
     */
    public void notifyElementRemoved(Ehcache cache, Element element) throws CacheException {
        if (notAlive() || !replicateRemovals) {
            return;
        }

        replicateRemoveNotification(cache, element);
    }

    /**
     * {@inheritDoc}
     */
    public void notifyElementUpdated(Ehcache cache, Element element) throws CacheException {
        if (notAlive() || !replicateUpdates) {
            return;
        }

        if (replicateUpdatesViaCopy) {
            replicatePutNotification(cache, element);
        } else {
            replicateRemoveNotification(cache, element);
        }

    }

    /**
     * {@inheritDoc}
     */
    public void notifyElementEvicted(Ehcache cache, Element element) {
        //Ignore
    }

    /**
     * {@inheritDoc}
     */
    public void notifyRemoveAll(Ehcache cache) {
        if (replicateRemovals) {
            final String cacheName = cache.getName();
            LOG.debug("Remove all elements called on {}", cacheName);
            JGroupEventMessage e = new JGroupEventMessage(JGroupEventMessage.REMOVE_ALL, null, null, cacheName,
                    this.asynchronousReplicationInterval);
            sendNotification(cache, e);
        }
    }

    private void replicatePutNotification(Ehcache cache, Element element) {
        if (!element.isKeySerializable()) {
            LOG.warn("Key {} is not Serializable and cannot be replicated.", element.getObjectKey());
            return;
        }
        if (!element.isSerializable()) {
            LOG.warn("Object with key {} is not Serializable and cannot be updated via copy", element.getObjectKey());
            return;
        }
        JGroupEventMessage e = new JGroupEventMessage(JGroupEventMessage.PUT, (Serializable) element.getObjectKey(), element,
                cache.getName(), this.asynchronousReplicationInterval);

        sendNotification(cache, e);
    }

    private void replicateRemoveNotification(Ehcache cache, Element element) {
        if (!element.isKeySerializable()) {
            LOG.warn("Key {} is not Serializable and cannot be replicated.", element.getObjectKey());
            return;
        }
        JGroupEventMessage e = new JGroupEventMessage(JGroupEventMessage.REMOVE, (Serializable) element.getObjectKey(), null,
                cache.getName(), this.asynchronousReplicationInterval);

        sendNotification(cache, e);
    }

    /**
     * Used to send notification to the peer. If Async this method simply add
     * the element to the replication queue. If not async, searches for the
     * cachePeer and send the Message. That way the class handles both async and
     * sync replication Sending is delegated to the peer (of type JGroupsCacheManagerPeerProvider)
     *
     * @param cache
     * @param eventMessage
     */
    protected void sendNotification(Ehcache cache, JGroupEventMessage eventMessage) {
        final List<CachePeer> peers = this.listRemoteCachePeers(cache);

        for (final CachePeer peer : peers) {
            try {
                peer.send(Arrays.asList(eventMessage));
            } catch (RemoteException e) {
                LOG.warn("Failed to send message '" + eventMessage + "' to peer '" + peer + "'", e);
            }
        }

    }

    /**
     * Package protected List of cache peers
     *
     * @param cache
     * @return a list of {@link CachePeer} peers for the given cache, excluding
     *         the local peer.
     */
    @SuppressWarnings("unchecked")
    private List<CachePeer> listRemoteCachePeers(Ehcache cache) {
        final CacheManager cacheManager = cache.getCacheManager();
        final CacheManagerPeerProvider provider = cacheManager.getCacheManagerPeerProvider(JGroupsCacheManagerPeerProvider.SCHEME_NAME);
        return provider.listRemoteCachePeers(cache);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }
}
