/**
 * Copyright (c) 2000-2009, Jasig, Inc.
 * See license distributed with this file and available online at
 * https://www.ja-sig.org/svn/jasig-parent/tags/rel-10/license-header.txt
 */
package org.jasig.portal.utils.cache;

import java.io.Serializable;
import java.util.Map;

/**
 * Interface for a cache factory that returns a cache that externally acts like a map.
 * Underlying caches don't have to be a map, but they must be wrapped in the map interface
 * if they do not.
 *
 * @author Scott Battaglia
 * @version $Revision$ $Date$
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