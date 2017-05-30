/**
 * Licensed to Apereo under one or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information regarding copyright ownership. Apereo
 * licenses this file to you under the Apache License, Version 2.0 (the "License"); you may not use
 * this file except in compliance with the License. You may obtain a copy of the License at the
 * following location:
 *
 * <p>http://www.apache.org/licenses/LICENSE-2.0
 *
 * <p>Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apereo.portal.utils.cache.resource;

import java.io.IOException;
import java.io.Serializable;
import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import net.sf.ehcache.Ehcache;
import net.sf.ehcache.Element;
import net.sf.ehcache.constructs.blocking.SelfPopulatingCache;
import org.apereo.portal.utils.cache.ThreadLocalCacheEntryFactory;
import org.jasig.resourceserver.aggr.om.Included;
import org.jasig.resourceserver.utils.aggr.ResourcesElementsProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

/**
 * Uses an {@link Ehcache} to handle caching of the resources.
 *
 */
@Service
public class CachingResourceLoaderImpl implements CachingResourceLoader {
    protected final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final CachedResourceEntryFactory entryFactory = new CachedResourceEntryFactory();

    private long checkInterval = TimeUnit.MINUTES.toMillis(1);

    private Ehcache resourceCache;
    private ResourcesElementsProvider resourcesElementsProvider;

    @Autowired
    public void setResourceCache(
            @Qualifier("org.apereo.portal.utils.cache.resource.CachingResourceLoader")
                    Ehcache resourceCache) {
        this.resourceCache = new SelfPopulatingCache(resourceCache, this.entryFactory);
    }

    @Autowired
    public void setResourcesElementsProvider(ResourcesElementsProvider resourcesElementsProvider) {
        this.resourcesElementsProvider = resourcesElementsProvider;
    }

    /** How frequently the resource should be checked for updates (in ms). Defaults to 1 minute. */
    public void setCheckInterval(long checkInterval) {
        this.checkInterval = checkInterval;
    }

    /* (non-Javadoc)
     * @see org.apereo.portal.utils.cache.CachingResourceLoader#getResource(org.springframework.core.io.Resource, org.apereo.portal.utils.cache.ResourceBuilder)
     */
    @Override
    public <T> CachedResource<T> getResource(Resource resource, Loader<T> builder)
            throws IOException {
        return this.getResource(resource, builder, this.checkInterval);
    }

    /* (non-Javadoc)
     * @see org.apereo.portal.utils.cache.CachingResourceLoader#getResource(org.springframework.core.io.Resource, org.apereo.portal.utils.cache.ResourceBuilder, org.apereo.portal.utils.cache.ResourceLoaderOptions)
     */
    @Override
    public <T> CachedResource<T> getResource(
            Resource resource, Loader<T> builder, long checkInterval) throws IOException {
        if (Included.PLAIN == this.resourcesElementsProvider.getDefaultIncludedType()) {
            this.logger.trace(
                    "Resoure Aggregation Disabled, ignoring resource cache and loading '"
                            + resource
                            + "' directly");
            return this.loadResource(resource, builder);
        }

        //Look for the resource in the cache, since it has been wrapped with a SelfPopulatingCache it should never return null.
        final GetResourceArguments<T> arguments = new GetResourceArguments<T>(resource, builder);
        final Element element =
                this.entryFactory.getWithData(this.resourceCache, resource, arguments);

        CachedResource<T> cachedResource = (CachedResource<T>) element.getObjectValue();
        if (this.logger.isTraceEnabled()) {
            this.logger.trace("Found " + cachedResource + " in cache");
        }

        //Found it, now check if the last-load time is within the check interval
        final long lastCheckTime = cachedResource.getLastCheckTime();
        if (lastCheckTime + checkInterval >= System.currentTimeMillis()) {
            if (this.logger.isTraceEnabled()) {
                this.logger.trace(
                        cachedResource
                                + " is within checkInterval "
                                + checkInterval
                                + ", returning");
            }
            return cachedResource;
        }
        if (this.logger.isTraceEnabled()) {
            this.logger.trace(
                    cachedResource
                            + " is older than checkInterval "
                            + checkInterval
                            + ", checking for modification");
        }

        //If the resource has not been modified return the cached resource.
        final boolean resourceModified = this.checkIfModified(cachedResource);
        if (!resourceModified) {
            cachedResource.setLastCheckTime(System.currentTimeMillis());
            this.resourceCache.put(
                    element); //do a cache put to notify the cache the object has been modified
            return cachedResource;
        }

        //The resource has been modified, reload it.
        cachedResource = this.loadResource(resource, builder);

        //Cache the loaded resource
        this.resourceCache.put(new Element(resource, cachedResource));

        if (this.logger.isDebugEnabled()) {
            this.logger.debug("Loaded and cached " + cachedResource);
        }

        return cachedResource;
    }

    /** Check if any of the Resources used to load the {@link CachedResource} have been modified. */
    protected <T> boolean checkIfModified(CachedResource<T> cachedResource) {
        final Resource resource = cachedResource.getResource();

        //Check if the resource has been modified since it was last loaded.
        final long lastLoadTime = cachedResource.getLastLoadTime();
        final long mainLastModified = this.getLastModified(resource);
        boolean resourceModified = lastLoadTime < mainLastModified;
        if (resourceModified) {
            this.logger.trace(
                    "Resource {} was modified at {}, reloading",
                    new Object[] {resource, mainLastModified});
            return true;
        }

        //If the main resource hasn't changed check additional resources for modifications
        for (final Map.Entry<Resource, Long> additionalResourceEntry :
                cachedResource.getAdditionalResources().entrySet()) {
            final Resource additionalResource = additionalResourceEntry.getKey();
            final Long resourceLastLoadTime = additionalResourceEntry.getValue();

            final long lastModified = this.getLastModified(additionalResource);
            if (resourceLastLoadTime < lastModified) {
                this.logger.trace(
                        "Additional resource {} for {} was modified at {}, reloading",
                        new Object[] {additionalResource, resource, lastModified});
                return true;
            }
        }

        this.logger.trace(
                "{} has not been modified since last loaded {}, returning",
                cachedResource,
                lastLoadTime);
        return false;
    }

    /** Determine the last modified time stamp for the resource */
    protected long getLastModified(Resource resource) {
        try {
            return resource.lastModified();
        } catch (IOException e) {
            this.logger.warn(
                    "Could not determine lastModified for "
                            + resource
                            + ". This resource will never be reloaded due to lastModified changing.",
                    e);
        }

        return 0;
    }

    private <T> CachedResource<T> loadResource(Resource resource, Loader<T> builder)
            throws IOException {
        final long lastLoadTime = System.currentTimeMillis();

        long lastModified = 0;
        try {
            lastModified = resource.lastModified();
        } catch (IOException e) {
            //Ignore, not all resources can have a valid lastModified returned
        }

        //Build the resource using the callback
        final LoadedResource<T> loadedResource = builder.loadResource(resource);

        final Serializable cacheKey =
                (Serializable) Arrays.asList(lastModified, loadedResource.getAdditionalResources());

        //Create the CachedResource based on if digesting was enabled
        return new CachedResourceImpl<T>(resource, loadedResource, lastLoadTime, cacheKey);
    }

    private static class GetResourceArguments<T> {
        public final Resource resource;
        public final Loader<T> builder;

        public GetResourceArguments(Resource resource, Loader<T> builder) {
            this.resource = resource;
            this.builder = builder;
        }
    }

    private class CachedResourceEntryFactory
            extends ThreadLocalCacheEntryFactory<GetResourceArguments<?>> {

        @Override
        protected Object createEntry(Object key, GetResourceArguments<?> threadData)
                throws Exception {
            return loadResource(threadData.resource, threadData.builder);
        }
    }
}
