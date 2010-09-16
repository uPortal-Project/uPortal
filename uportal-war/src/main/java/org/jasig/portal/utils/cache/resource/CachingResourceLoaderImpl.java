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

package org.jasig.portal.utils.cache.resource;

import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;

import net.sf.ehcache.Ehcache;
import net.sf.ehcache.Element;
import net.sf.ehcache.constructs.blocking.SelfPopulatingCache;
import net.sf.ehcache.store.chm.ConcurrentHashMap;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jasig.portal.utils.cache.ThreadLocalCacheEntryFactory;
import org.jasig.portal.utils.io.MessageDigestInputStream;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

/**
 * Uses an {@link Ehcache} to handle caching of the resources.
 * 
 * @author Eric Dalquist
 * @version $Revision$
 */
@Service
public class CachingResourceLoaderImpl implements CachingResourceLoader {
    private static final ResourceLoaderOptions DEFAULT_OPTIONS = new ResourceLoaderOptionsBuilder();
    
    protected final Log logger = LogFactory.getLog(this.getClass());
    
    private final CachedResourceEntryFactory entryFactory = new CachedResourceEntryFactory();
    private final ConcurrentMap<String, MessageDigest> digestCache = new ConcurrentHashMap<String, MessageDigest>();
    private final ConcurrentMap<String, String> unclonableDigestCache = new ConcurrentHashMap<String, String>();
    
    private long checkInterval = TimeUnit.MINUTES.toMillis(1);
    private boolean digestInput = true;
    private String digestAlgorithm = "MD5";
    
    private Ehcache resourceCache;

    @Autowired
    public void setResourceCache(
            @Qualifier("org.jasig.portal.utils.cache.resource.CachingResourceLoader") Ehcache resourceCache) {
        this.resourceCache = new SelfPopulatingCache(resourceCache, this.entryFactory);
    }
    
    /**
     * How frequently the resource should be checked for updates (in ms). Defaults to 1 minute.
     */
    public void setCheckInterval(long checkInterval) {
        this.checkInterval = checkInterval;
    }
    /**
     * If the input should be run through a {@link MessageDigest} to generate a hash code. Useful for cache
     * keys based on the input. Defaults to true
     */
    public void setDigestInput(boolean digestInput) {
        this.digestInput = digestInput;
    }
    /**
     * The {@link MessageDigest} algorithm to use if {@link #setDigestInput(boolean)} is true. Defaults to MD5
     */
    public void setDigestAlgorithm(String digestAlgorithm) {
        this.digestAlgorithm = digestAlgorithm;
    }


    /* (non-Javadoc)
     * @see org.jasig.portal.utils.cache.CachingResourceLoader#getResource(org.springframework.core.io.Resource, org.jasig.portal.utils.cache.ResourceBuilder)
     */
    @Override
    public <T> CachedResource<T> getResource(Resource resource, ResourceBuilder<T> builder) throws IOException {
        return this.getResource(resource, builder, DEFAULT_OPTIONS);
    }

    /* (non-Javadoc)
     * @see org.jasig.portal.utils.cache.CachingResourceLoader#getResource(org.springframework.core.io.Resource, org.jasig.portal.utils.cache.ResourceBuilder, org.jasig.portal.utils.cache.ResourceLoaderOptions)
     */
    @Override
    public <T> CachedResource<T> getResource(Resource resource, ResourceBuilder<T> builder, ResourceLoaderOptions options) throws IOException {
        //Look for the resource in the cache, since it has been wrapped with a SelfPopulatingCache it should never return null.
        final GetResourceArguments<T> arguments = new GetResourceArguments<T>(resource, builder, options);
        final Element element = this.entryFactory.getWithData(this.resourceCache, resource, arguments);

        CachedResource<T> cachedResource = (CachedResource<T>)element.getObjectValue();
        if (this.logger.isTraceEnabled()) {
            this.logger.trace("Found CachedResource in cache for " + resource + ": " + cachedResource);
        }
        
        //Found it, now check if the last-load time is within the check interval
        final long lastLoadTime = cachedResource.getLastLoadTime();
        final long checkInterval = this.getCheckInterval(options);
        if (lastLoadTime + checkInterval > System.currentTimeMillis()) {
            if (this.logger.isTraceEnabled()) {
                this.logger.trace("CachedResource for " + resource + " is within checkInterval " + checkInterval + ", returning");
            }
            return cachedResource;
        }

        //Check if the resource has been modified since it was last loaded.
        final long lastModified = this.getLastModified(resource);
        if (lastModified < lastLoadTime) {
            if (this.logger.isTraceEnabled()) {
                this.logger.trace("CachedResource for " + resource + " has not been modified since last loaded " + lastLoadTime + ", returning");
            }
            return cachedResource;
        }
        
        //The resource has been modified, reload it.
        cachedResource = this.loadResource(resource, builder, options);
        
        //Cache the loaded resource
        this.resourceCache.put(new Element(resource, cachedResource));
        
        if (this.logger.isDebugEnabled()) {
            this.logger.debug("Loaded and cached " + cachedResource);
        }
        
        return cachedResource;
    }
    
    /**
     * Determine the last modified timestamp for the resource
     */
    protected long getLastModified(Resource resource) {
        //TODO lastModified may not work for URL resources :(
        try {
            return resource.lastModified();
        }
        catch (IOException e) {
            this.logger.warn("Could not determine lastModified for " + resource + ". This resource will never be reloaded due to lastModified changing.", e);
        }
        
        return 0;
    }

    private <T> CachedResource<T> loadResource(Resource resource, ResourceBuilder<T> builder, ResourceLoaderOptions options) throws IOException {
        final long lastModified = this.getLastModified(resource);
        InputStream stream = resource.getInputStream();
        final T builtResource;
        final boolean digestInput = this.isDigestInput(options);
        MessageDigest messageDigest = null;
        try {
            //Setup the MessageDigest if it is being used 
            if (digestInput) {
                final String digestAlgorithm = this.getDigestAlgorithm(options);
                messageDigest = this.getMessageDigest(digestAlgorithm);
                stream = new MessageDigestInputStream(messageDigest, stream);
            }
            
            //Build the resource using the callback
            builtResource = builder.buildResource(resource, stream);
        }
        finally {
            IOUtils.closeQuietly(stream);
        }
        
        //Create the CachedResource based on if digesting was enabled
        final CachedResource<T> cachedResource;
        if (digestInput) {
            final String algorithm = messageDigest.getAlgorithm();
            final byte[] digestBytes = messageDigest.digest();
            final String digest = Base64.encodeBase64URLSafeString(digestBytes);
            
            cachedResource = new CachedResourceImpl<T>(resource, builtResource, lastModified, digest, digestBytes, algorithm);
        }
        else {
            cachedResource = new CachedResourceImpl<T>(resource, builtResource, lastModified);
        }
        
        return cachedResource;
    }

    private long getCheckInterval(ResourceLoaderOptions options) {
        final Long optionsCheckInterval = options.getCheckInterval();
        if (optionsCheckInterval != null) {
            return optionsCheckInterval;
        }
        
        return this.checkInterval;
    }
    
    private boolean isDigestInput(ResourceLoaderOptions options) {
        final Boolean optionsDigestInput = options.isDigestInput();
        if (optionsDigestInput != null) {
            return optionsDigestInput;
        }
        
        return this.digestInput;
    }
    
    private String getDigestAlgorithm(ResourceLoaderOptions options) {
        final String optionsDigestAlgorithm = options.getDigestAlgorithm();
        if (optionsDigestAlgorithm != null) {
            return optionsDigestAlgorithm;
        }
        
        return this.digestAlgorithm;
    }
    
    private MessageDigest getMessageDigest(String algorithm) {
        MessageDigest messageDigest = this.digestCache.get(algorithm);
        
        //If there is a cached instance return a clone of it
        if (messageDigest != null) {
            try {
                return (MessageDigest)messageDigest.clone();
            }
            catch (CloneNotSupportedException e) {
                throw new IllegalStateException("MessageDigest for '" + algorithm + "' that was previously clonable is no longer. This is a programming error.", e);
            }
        }
        
        //Create a new instance for the algorithm
        try {
            messageDigest = MessageDigest.getInstance(algorithm);
        }
        catch (NoSuchAlgorithmException e) {
            throw new IllegalArgumentException("Requested digest algorithm '" + algorithm + "' could not be found", e);
        }
        
        //Check if this algorithm can be cloned, if not return the new instance immediately
        if (this.unclonableDigestCache.containsKey(algorithm)) {
            return messageDigest;
        }
        
        //Try cloning the digest and caching it or if the clone fails registering that and returning the new instance
        try {
            final MessageDigest digestToReturn = (MessageDigest)messageDigest.clone();
            this.digestCache.put(algorithm, digestToReturn);
            return digestToReturn;
        }
        catch (CloneNotSupportedException e) {
            this.unclonableDigestCache.put(algorithm, algorithm);
            return messageDigest;
        }
    }
    
    private static class GetResourceArguments<T> {
        public final Resource resource;
        public final ResourceBuilder<T> builder;
        public final ResourceLoaderOptions options;

        public GetResourceArguments(Resource resource, ResourceBuilder<T> builder, ResourceLoaderOptions options) {
            this.resource = resource;
            this.builder = builder;
            this.options = options;
        }
    }
    
    private class CachedResourceEntryFactory extends ThreadLocalCacheEntryFactory<GetResourceArguments<?>> {

        @Override
        protected Object createEntry(Object key, GetResourceArguments<?> threadData) throws Exception {
            return loadResource(threadData.resource, threadData.builder, threadData.options);
        }
    }
}
