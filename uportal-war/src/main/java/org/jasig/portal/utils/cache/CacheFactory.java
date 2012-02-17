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

package org.jasig.portal.utils.cache;

import java.io.Serializable;
import java.util.Map;

import net.sf.ehcache.Ehcache;

/**
 * Interface for a cache factory that returns a cache that externally acts like a map.
 * Underlying caches don't have to be a map, but they must be wrapped in the map interface
 * if they do not.
 *
 * @author Scott Battaglia
 * @version $Revision$ $Date$
 * @deprecated Inject the {@link Ehcache} instances directly into spring managed beans using @Qualifier to specify the cache name
 */
public interface CacheFactory {

    /** The cache to be used to hold principals. */
    public static final String PRINCIPAL_CACHE = "org.jasig.portal.security.provider.AuthorizationImpl.AUTH_PRINCIPAL_CACHE";
    
    public static final String ENTITY_PARENTS_CACHE = "org.jasig.portal.security.provider.AuthorizationImpl.ENTITY_PARENTS_CACHE";

	/** The cache to be used to hold names. */
    public static final String NAME_CACHE = "org.jasig.portal.groups.CompositeEntityIdentifier.NAME_PARSE_CACHE";

	/** The cache to be used to hold content. */
    public static final String CONTENT_CACHE = "org.jasig.portal.channels.CONTENT_CACHE";

    /** Generic default cache that can be used for anything */
    public static final String DEFAULT = "org.jasig.portal.utils.cache.DEFAULT_CACHE";
    
    /**
     * Method to retrieve a cache by name.
     *
     * @param cacheName the name of the cache to retrieve.
     * @return the cache that is referenced by the cache name provided.
     * @throws IllegalArgumentException if a cache by that name cannot be retrieved.
     */
    public <K extends Serializable, V> Map<K, V> getCache(String cacheName) throws IllegalArgumentException;

    /**
     * Method to retrieve a default cache without a name.
     * @return the default cache for this CacheFactory
     */
    public <K extends Serializable, V> Map<K, V> getCache();
}