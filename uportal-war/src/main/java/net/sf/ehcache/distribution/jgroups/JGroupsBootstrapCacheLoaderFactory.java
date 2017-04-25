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

import net.sf.ehcache.bootstrap.BootstrapCacheLoader;
import net.sf.ehcache.bootstrap.BootstrapCacheLoaderFactory;
import net.sf.ehcache.util.PropertyUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * A factory to create a configured JGroupsBootstrapCacheLoader
 *
 * @author Greg Luck
 * @version $Id$
 */
public class JGroupsBootstrapCacheLoaderFactory extends BootstrapCacheLoaderFactory {

    /**
     * The property name expected in ehcache.xml for the bootstrap asynchronously switch.
     */
    public static final String BOOTSTRAP_ASYNCHRONOUSLY = "bootstrapAsynchronously";

    /**
     * The property name expected in ehcache.xml for the maximum chunk size in bytes
     */
    public static final String MAXIMUM_CHUNK_SIZE_BYTES = "maximumChunkSizeBytes";

    /**
     * The default maximum serialized size of the elements to request from a remote cache peer during bootstrap.
     */
    protected static final int DEFAULT_MAXIMUM_CHUNK_SIZE_BYTES = 5000000;

    /**
     * The highest reasonable chunk size in bytes
     */
    protected static final int ONE_HUNDRED_MB = 100000000;

    /**
     * The lowest reasonable chunk size in bytes
     */
    protected static final int FIVE_KB = 5000;

    private static final Logger LOG = LoggerFactory.getLogger(JGroupsBootstrapCacheLoaderFactory.class.getName());


    /**
     * Create a <code>BootstrapCacheLoader</code>
     *
     * @param properties implementation specific properties. These are configured as comma
     *                   separated name value pairs in ehcache.xml
     * @return a constructed BootstrapCacheLoader
     */
    @Override
    public BootstrapCacheLoader createBootstrapCacheLoader(Properties properties) {
        if (properties == null) {
            LOG.debug("Creating JGroups BootstrapCacheLoader with default configuration.");
        } else {
            LOG.debug("Creating JGroups BootstrapCacheLoader with configuration:\n{}", properties);
        }

        boolean bootstrapAsynchronously = extractAndValidateBootstrapAsynchronously(properties);
        int maximumChunkSizeBytes = extractMaximumChunkSizeBytes(properties);
        return new JGroupsBootstrapCacheLoader(bootstrapAsynchronously, maximumChunkSizeBytes);
    }

    /**
     * Extracts the value of maximumChunkSizeBytes from the properties
     */
    protected int extractMaximumChunkSizeBytes(Properties properties) {
        int maximumChunkSizeBytes = 0;
        String maximumChunkSizeBytesString = PropertyUtil.extractAndLogProperty(MAXIMUM_CHUNK_SIZE_BYTES, properties);
        if (maximumChunkSizeBytesString == null) {
            return DEFAULT_MAXIMUM_CHUNK_SIZE_BYTES;
        }

        int maximumChunkSizeBytesCandidate;
        try {
            maximumChunkSizeBytesCandidate = Integer.parseInt(maximumChunkSizeBytesString);
        } catch (NumberFormatException e) {
            LOG.warn("Number format exception trying to set chunk size to '{}'. Using the default instead.", maximumChunkSizeBytesString);
            return DEFAULT_MAXIMUM_CHUNK_SIZE_BYTES;
        }

        if ((maximumChunkSizeBytesCandidate < FIVE_KB) || (maximumChunkSizeBytesCandidate > ONE_HUNDRED_MB)) {
            LOG.warn("Trying to set the chunk size to an unreasonable number: {}. Using the default instead.",
                    maximumChunkSizeBytesCandidate);
            return DEFAULT_MAXIMUM_CHUNK_SIZE_BYTES;
        }

        return maximumChunkSizeBytes;
    }


    /**
     * Extracts the value of bootstrapAsynchronously from the properties
     */
    protected boolean extractAndValidateBootstrapAsynchronously(Properties properties) {
        String bootstrapAsynchronouslyString = PropertyUtil.extractAndLogProperty(BOOTSTRAP_ASYNCHRONOUSLY, properties);
        if (bootstrapAsynchronouslyString != null) {
            return Boolean.parseBoolean(bootstrapAsynchronouslyString);
        }

        return true;
    }
}
