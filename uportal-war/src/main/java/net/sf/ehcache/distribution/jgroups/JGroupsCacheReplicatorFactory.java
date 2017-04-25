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

import java.util.Properties;

import net.sf.ehcache.event.CacheEventListener;
import net.sf.ehcache.event.CacheEventListenerFactory;
import net.sf.ehcache.util.PropertyUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Pierre Monestie (pmonestie__REMOVE__THIS__@gmail.com)
 * @author <a href="mailto:gluck@gregluck.com">Greg Luck</a>
 * @version $Id$
 */

public class JGroupsCacheReplicatorFactory extends CacheEventListenerFactory {
    private static final String ASYNCHRONOUS_REPLICATION_INTERVAL_MILLIS = "asynchronousReplicationIntervalMillis";

    private static final Logger LOG = LoggerFactory.getLogger(JGroupsCacheReplicatorFactory.class.getName());

    private static final String REPLICATE_PUTS = "replicatePuts";

    private static final String REPLICATE_UPDATES = "replicateUpdates";

    private static final String REPLICATE_UPDATES_VIA_COPY = "replicateUpdatesViaCopy";

    private static final String REPLICATE_REMOVALS = "replicateRemovals";

    private static final String REPLICATE_ASYNCHRONOUSLY = "replicateAsynchronously";

    /**
     * Empty arg constructor
     */
    public JGroupsCacheReplicatorFactory() {
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public CacheEventListener createCacheEventListener(Properties properties) {
        LOG.debug("Creating JGroups CacheEventListener with configuration: {}", properties);
        boolean replicatePuts = extractBooleanProperty(properties, REPLICATE_PUTS, true);
        boolean replicateUpdates = extractBooleanProperty(properties, REPLICATE_UPDATES, true);
        boolean replicateUpdatesViaCopy = extractBooleanProperty(properties, REPLICATE_UPDATES_VIA_COPY, false);
        boolean replicateRemovals = extractBooleanProperty(properties, REPLICATE_REMOVALS, true);
        boolean replicateAsync = extractBooleanProperty(properties, REPLICATE_ASYNCHRONOUSLY, true);


        if (replicateAsync) {
            long asyncTime = extractAsynchronousReplicationIntervalMillis(properties);

            return new JGroupsCacheReplicator(replicatePuts, replicateUpdates, replicateUpdatesViaCopy,
                    replicateRemovals, asyncTime);
        }

        return new JGroupsCacheReplicator(replicatePuts, replicateUpdates, replicateUpdatesViaCopy,
                replicateRemovals);
    }

    /**
     * Extract the {@link #ASYNCHRONOUS_REPLICATION_INTERVAL_MILLIS} setting from the properties
     */
    protected long extractAsynchronousReplicationIntervalMillis(Properties properties) {
        String parsedString = PropertyUtil.extractAndLogProperty(ASYNCHRONOUS_REPLICATION_INTERVAL_MILLIS, properties);
        if (parsedString == null) {
            return JGroupsCacheReplicator.DEFAULT_ASYNC_INTERVAL;
        }

        try {
            return Long.parseLong(parsedString);
        } catch (NumberFormatException e) {
            LOG.warn("Number format exception trying to set {}. " +
                    "Using the default instead. String value was: '{}'", ASYNCHRONOUS_REPLICATION_INTERVAL_MILLIS, parsedString);
            return JGroupsCacheReplicator.DEFAULT_ASYNC_INTERVAL;
        }
    }

    /**
     * Extract a Boolean out of a Property
     *
     * @param properties   the properties
     * @param propertyName the name of the property
     * @param defaultValue the deulat value id none is found
     * @return the extracted property
     */
    protected boolean extractBooleanProperty(Properties properties, String propertyName, boolean defaultValue) {
        String booleanCandidate = PropertyUtil.extractAndLogProperty(propertyName, properties);
        if (booleanCandidate != null) {
            return Boolean.parseBoolean(booleanCandidate);
        }

        return defaultValue;
    }
}
