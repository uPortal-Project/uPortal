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

import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.util.List;

import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Ehcache;
import net.sf.ehcache.util.CacheTransactionHelper;
import org.jgroups.Address;
import org.jgroups.Message;
import org.jgroups.Receiver;
import org.jgroups.View;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handles {@link Receiver} functions around for a {@link CacheManager}
 *
 * @author Eric Dalquist
 * @version $Revision$
 */
public class JGroupsCacheReceiver implements Receiver {
    private static final Logger LOG = LoggerFactory.getLogger(JGroupsCacheReceiver.class.getName());

    private final CacheManager cacheManager;
    private final JGroupsBootstrapManager bootstrapManager;

    /**
     * Create a new {@link Receiver}
     */
    public JGroupsCacheReceiver(CacheManager cacheManager, JGroupsBootstrapManager bootstrapManager) {
        this.cacheManager = cacheManager;
        this.bootstrapManager = bootstrapManager;
    }

    /**
     * {@inheritDoc}
     */
    public void receive(Message msg) {
        if (msg == null || msg.getLength() == 0) {
            LOG.warn("Recieved an empty or null Message: {}", msg);
            return;
        }

        final Object object = msg.getObject();
        if (object == null) {
            LOG.warn("Recieved a Message with a null object: {}", msg);
            return;
        }

        if (object instanceof JGroupEventMessage) {
            this.safeHandleJGroupNotification((JGroupEventMessage)object);
        } else if (object instanceof List<?>) {
            final List<?> messages = (List<?>)object;
            LOG.trace("Recieved List of {} JGroupEventMessages", messages.size());

            for (final Object message : messages) {
                if (message == null) {
                    continue;
                }

                if (message instanceof JGroupEventMessage) {
                    this.safeHandleJGroupNotification((JGroupEventMessage) message);
                } else {
                    LOG.warn("Recieved message of type " + List.class + " but member was of type '" + message.getClass() +
                            "' and not " + JGroupEventMessage.class + ". Member ignored: " + message);
                }
            }
        } else {
            LOG.warn("Recieved message with payload of type " + object.getClass() +
                    " and not " + JGroupEventMessage.class +
                    " or List<" + JGroupEventMessage.class.getSimpleName() + ">. Message: " + msg + " payload " + object);
        }
    }

    /* ********** Local Methods ********** */

    /**
     * Have to do a little helper method like this to get around the checkstyle cyclomatic check
     */
    private void safeHandleJGroupNotification(final JGroupEventMessage message) {
        final String cacheName = message.getCacheName();
        Ehcache cache = cacheManager.getEhcache(cacheName);
        boolean started = cache != null && CacheTransactionHelper.isTransactionStarted(cache);
        if (cache != null && !started) {
            CacheTransactionHelper.beginTransactionIfNeeded(cache);
        }

        try {
            this.handleJGroupNotification(message);
        } catch (Exception e) {
            LOG.error("Failed to handle message " + message, e);
        } finally {
            if (cache != null && !started) {
                CacheTransactionHelper.commitTransactionIfNeeded(cache);
            }
        }
    }

    private void handleJGroupNotification(final JGroupEventMessage message) {
        final String cacheName = message.getCacheName();

        switch (message.getEvent()) {
            case JGroupEventMessage.BOOTSTRAP_REQUEST: {
                LOG.debug("received bootstrap request:    from {} for cache={}", message.getSerializableKey(), cacheName);
                this.bootstrapManager.sendBootstrapResponse(message);
                break;
            }
            case JGroupEventMessage.BOOTSTRAP_COMPLETE: {
                LOG.debug("received bootstrap complete:   cache={}", cacheName);
                this.bootstrapManager.handleBootstrapComplete(message);
                break;
            }
            case JGroupEventMessage.BOOTSTRAP_INCOMPLETE: {
                LOG.debug("received bootstrap incomplete: cache={}", cacheName);
                this.bootstrapManager.handleBootstrapIncomplete(message);
                break;
            }
            case JGroupEventMessage.BOOTSTRAP_RESPONSE: {
                final Serializable serializableKey = message.getSerializableKey();
                LOG.debug("received bootstrap reply:      cache={}, key={}", cacheName, serializableKey);
                this.bootstrapManager.handleBootstrapResponse(message);
                break;
            }
            default: {
                this.handleEhcacheNotification(message, cacheName);
                break;
            }
        }
    }

    private void handleEhcacheNotification(final JGroupEventMessage message, final String cacheName) {
        final Ehcache cache = this.cacheManager.getEhcache(cacheName);
        if (cache == null) {
            LOG.warn("Received message {} for cache that does not exist: {}", message, cacheName);
            return;
        }

        switch (message.getEvent()) {
            case JGroupEventMessage.REMOVE_ALL: {
                LOG.debug("received remove all:      cache={}", cacheName);
                cache.removeAll(true);
                break;
            }
            case JGroupEventMessage.REMOVE: {
                final Serializable serializableKey = message.getSerializableKey();
                if (cache.getQuiet(serializableKey) != null) {
                    LOG.debug("received remove:          cache={}, key={}", cacheName, serializableKey);
                    cache.remove(serializableKey, true);
                } else if (LOG.isTraceEnabled()) {
                    LOG.trace("received remove:          cache={}, key={} - Ignoring, key is not in the local cache.",
                            cacheName, serializableKey);
                }
                break;
            }
            case JGroupEventMessage.PUT: {
                final Serializable serializableKey = message.getSerializableKey();
                LOG.debug("received put:             cache={}, key={}", cacheName, serializableKey);
                cache.put(message.getElement(), true);
                break;
            }
            default: {
                LOG.warn("Unknown JGroupsEventMessage type recieved, ignoring message: " + message);
                break;
            }
        }
    }

    /* ********** Unused ********** */

    /**
     * {@inheritDoc}
     */
    public void getState(OutputStream output) {
        //Not Implemented
    }

    /**
     * {@inheritDoc}
     */
    public void setState(InputStream input) {
        //Not Implemented
    }

    /**
     * {@inheritDoc}
     */
    public void block() {
        //Not Implemented
    }

    /**
     * {@inheritDoc}
     */
    public void unblock() {
        //Not Implemented
    }

    /**
     * {@inheritDoc}
     */
    public void suspect(Address suspectedMbr) {
        //Not Implemented
    }

    /**
     * {@inheritDoc}
     */
    public void viewAccepted(View newView) {
        //Not Implemented
    }
}
