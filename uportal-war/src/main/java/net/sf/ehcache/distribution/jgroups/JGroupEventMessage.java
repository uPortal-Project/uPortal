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

import net.sf.ehcache.Element;
import net.sf.ehcache.distribution.LegacyEventMessage;

/**
 * An EventMessage used for JGroups
 *
 * @author Pierre Monestie (pmonestie[at]@gmail.com)
 * @author <a href="mailto:gluck@gregluck.com">Greg Luck</a>
 * @version $Id$
 *          EventMessage class for the JGroupsCacheReplicator.
 */
public class JGroupEventMessage extends LegacyEventMessage {

    /**
     * Request for bootstrap
     */
    public static final int BOOTSTRAP_REQUEST = 10;

    /**
     * Reply to bootstrap
     */
    public static final int BOOTSTRAP_RESPONSE = 11;

    /**
     * Bootstrap complete
     */
    public static final int BOOTSTRAP_COMPLETE = 12;

    /**
     * Bootstrap could not be completed for some reason
     */
    public static final int BOOTSTRAP_INCOMPLETE = 13;

    /**
     * Serialization ID
     */
    private static final long serialVersionUID = 1L;

    private final String cacheName;
    private final long asyncTime;

    /**
     * @see #JGroupEventMessage(int, Serializable, Element, String, long)
     */
    public JGroupEventMessage(int event, Serializable key, Element element, String cacheName) {
        super(event, key, element);
        this.cacheName = cacheName;
        this.asyncTime = -1;
    }


    /**
     * An event message for the JGroupsCacheReplicator. We keep as transient the
     * origin cache and we serialize the cacheName. That way the JgroupManager
     * will know from which cache the message came from
     *
     * @param event     (PUT,REMOVE,REMOVE_ALL)
     * @param key       the serializable key of the cache element
     * @param element   The element itself. In case of a put.
     * @param cacheName the name of the cache
     * @param asyncTime the asynchronous replication period for this message, -1 results in synchronous replication
     */
    public JGroupEventMessage(int event, Serializable key, Element element, String cacheName,
                              long asyncTime) {
        super(event, key, element);
        this.cacheName = cacheName;
        this.asyncTime = asyncTime;
    }

    /**
     * @return If asynchronous
     */
    public boolean isAsync() {
        return this.asyncTime >= 0;
    }

    /**
     * @return The asynchronous replication delay, if less than 0 no synchronous replication is used.
     */
    public long getAsyncTime() {
        return this.asyncTime;
    }

    /**
     * Returns the cache name
     *
     * @return the cache name
     */
    public String getCacheName() {
        return cacheName;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return "JGroupEventMessage [event=" + getEventName(this.getEvent()) +
            ", cacheName=" + this.cacheName +
            ", serializableKey=" + this.getSerializableKey() +
            ", element=" + this.getElement() + "]";
    }

    /**
     * Convert a numeric event ID to a String name.
     */
    public static String getEventName(int event) {
        final String eventName;
        switch (event) {
            case LegacyEventMessage.PUT: {
                eventName = "PUT";
                break;
            }
            case LegacyEventMessage.REMOVE: {
                eventName = "REMOVE";
                break;
            }
            case LegacyEventMessage.REMOVE_ALL: {
                eventName = "REMOVE_ALL";
                break;
            }
            case JGroupEventMessage.BOOTSTRAP_REQUEST: {
                eventName = "BOOTSTRAP_REQUEST";
                break;
            }
            case JGroupEventMessage.BOOTSTRAP_RESPONSE: {
                eventName = "BOOTSTRAP_RESPONSE";
                break;
            }
            case JGroupEventMessage.BOOTSTRAP_COMPLETE: {
                eventName = "BOOTSTRAP_COMPLETE";
                break;
            }
            case JGroupEventMessage.BOOTSTRAP_INCOMPLETE: {
                eventName = "BOOTSTRAP_INCOMPLETE";
                break;
            }
            default: {
                eventName = Integer.toString(event);
                break;
            }
        }
        return eventName;
    }
}
