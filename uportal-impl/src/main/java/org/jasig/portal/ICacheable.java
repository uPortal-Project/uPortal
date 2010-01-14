/**
 * Copyright (c) 2000-2009, Jasig, Inc.
 * See license distributed with this file and available online at
 * https://www.ja-sig.org/svn/jasig-parent/tags/rel-10/license-header.txt
 */
package  org.jasig.portal;

/**
 * An interface that a cacheable channel must implement.
 * @author Peter Kharchenko pkharchenko@unicon.net
 * @version $Revision$
 * @deprecated All IChannel implementations should be migrated to portlets
 */
@Deprecated
public interface ICacheable {

    /**
     * Suggested name for channel static data or runtime data prameter specifying
     * the intended scope for caching for the so parameterized instance of a channel
     * implementing this interface.  Channels implementing ICacheable are suggested to
     * read ChannelStaticData parameter 'cacheScope' and configure themselves to
     * use the indicated scope.
     */
    public static final String CHANNEL_CACHE_KEY_SCOPE_PARAM_NAME = "cacheScope";

    /**
     * Suggested value for the channel static data or runtime data parameter
     * specifying intended scope for caching, indicating that the intended
     * scope is system-wide caching.
     */
    public static final String CHANNEL_CACHE_KEY_SYSTEM_SCOPE = "system";

    /**
     * Suggested value for the channel static data or runtime data parameter
     * specifying intended scope for caching, indicating that the intended
     * scope is per-instance caching.
     */
    public static final String CHANNEL_CACHE_KEY_INSTANCE_SCOPE = "instance";

    /**
     * Requests the channel to generate a key uniquely describing its current state,
     * and a description of key usage.
     */
    public ChannelCacheKey generateKey();

    /**
     * Requests the channel to verify validity of the retrieved cache based on the validator object.
     */
    public boolean isCacheValid(Object validity);
}
