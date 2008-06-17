/**
 * Copyright 2007 The JA-SIG Collaborative.  All rights reserved.
 * See license distributed with this file and
 * available online at http://www.uportal.org/license.html
 */
package org.jasig.portal.utils.cache;

import java.beans.PropertyEditor;
import java.io.Serializable;

import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;

import org.apache.commons.lang.Validate;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.util.ObjectUtils;
import org.springmodules.cache.CacheException;
import org.springmodules.cache.CachingModel;
import org.springmodules.cache.FatalCacheException;
import org.springmodules.cache.FlushingModel;
import org.springmodules.cache.provider.AbstractCacheProviderFacade;
import org.springmodules.cache.provider.CacheAccessException;
import org.springmodules.cache.provider.CacheModelValidator;
import org.springmodules.cache.provider.CacheNotFoundException;
import org.springmodules.cache.provider.ReflectionCacheModelEditor;
import org.springmodules.cache.provider.ehcache.EhCacheCachingModel;
import org.springmodules.cache.provider.ehcache.EhCacheFlushingModel;
import org.springmodules.cache.provider.ehcache.EhCacheModelValidator;

/**
 * Similar to the spring-modules {@link org.springmodules.cache.provider.ehcache.EhCacheFacade} except this 
 * will create a cache if it does not already exist.
 * 
 * @author Eric Dalquist
 * @version $Revision$
 */
public class EhCacheFacade extends AbstractCacheProviderFacade {
    protected final Log logger = LogFactory.getLog(this.getClass());
    
    private final CacheModelValidator cacheModelValidator;
    private CacheManager cacheManager;
    private boolean createMissingCaches = false;

    
    public EhCacheFacade() {
        this.cacheModelValidator = new EhCacheModelValidator();
    }


    /**
     * @return the createMissingCaches
     */
    public boolean isCreateMissingCaches() {
        return createMissingCaches;
    }
    /**
     * @param createMissingCaches the createMissingCaches to set
     */
    public void setCreateMissingCaches(boolean createMissingCaches) {
        this.createMissingCaches = createMissingCaches;
    }

    /**
     * @return the cacheManager
     */
    public CacheManager getCacheManager() {
        return cacheManager;
    }
    /**
     * Sets the EHCache cache manager to use.
     * 
     * @param newCacheManager
     *          the new cache manager
     */
    @Required
    public void setCacheManager(CacheManager newCacheManager) {
        Validate.notNull(newCacheManager, "cacheManager can not be null");
        this.cacheManager = newCacheManager;
    }


    /**
     * Returns the validator of cache models. It is always an instance of
     * <code>{@link EhCacheModelValidator}</code>.
     * 
     * @return the validator of cache models
     */
    public CacheModelValidator modelValidator() {
        return this.cacheModelValidator;
    }

    /**
     * @see org.springmodules.cache.provider.CacheProviderFacade#getCachingModelEditor()
     */
    public PropertyEditor getCachingModelEditor() {
        final ReflectionCacheModelEditor editor = new ReflectionCacheModelEditor();
        editor.setCacheModelClass(EhCacheCachingModel.class);
        return editor;
    }

    /**
     * @see org.springmodules.cache.provider.CacheProviderFacade#getFlushingModelEditor()
     */
    public PropertyEditor getFlushingModelEditor() {
        final ReflectionCacheModelEditor editor = new ReflectionCacheModelEditor();
        editor.setCacheModelClass(EhCacheFlushingModel.class);
        return editor;
    }

    /**
     * Returns a EHCache cache from the cache manager.
     * 
     * @param model
     *          the model containing the name of the cache to retrieve
     * @return the cache retrieved from the cache manager
     * @throws CacheNotFoundException
     *           if the cache does not exist
     * @throws CacheAccessException
     *           wrapping any unexpected exception thrown by the cache
     */
    protected Cache getCache(CachingModel model) throws CacheNotFoundException, CacheAccessException {
        final EhCacheCachingModel ehCacheCachingModel = (EhCacheCachingModel) model;
        final String cacheName = ehCacheCachingModel.getCacheName();
        return this.getCache(cacheName);
    }

    /**
     * Returns a EHCache cache from the cache manager.
     * 
     * @param name
     *          the name of the cache
     * @return the cache retrieved from the cache manager
     * @throws CacheNotFoundException
     *           if the cache does not exist
     * @throws CacheAccessException
     *           wrapping any unexpected exception thrown by the cache
     */
    protected Cache getCache(String name) throws CacheNotFoundException, CacheAccessException {
        final Cache cache;

        try {
            if (this.cacheManager.cacheExists(name)) {
                cache = this.cacheManager.getCache(name);
                
                if (this.logger.isDebugEnabled()) {
                    this.logger.debug("Using existing EhCache for '" + name + "'");
                }
            }
            else if (this.createMissingCaches) {
                this.cacheManager.addCache(name);
                cache = this.cacheManager.getCache(name);
                
                if (this.logger.isInfoEnabled()) {
                    this.logger.info("Created new EhCache for '" + name + "'");
                }
            }
            else {
                cache = null;
                
                if (this.logger.isDebugEnabled()) {
                    this.logger.debug("No EhCache exists for '" + name + "' and createMissingCaches is false");
                }
            }
        }
        catch (Exception exception) {
            throw new CacheAccessException(exception);
        }

        if (cache == null) {
            throw new CacheNotFoundException(name);
        }

        return cache;
    }

    /**
     * @return <code>true</code>. EHCache can only store Serializable objects
     * @see AbstractCacheProviderFacade#isSerializableCacheElementRequired()
     */
    @Override
    protected boolean isSerializableCacheElementRequired() {
        return false;
    }

    /**
     * Removes all the entries in the caches specified in the given flushing
     * model. The flushing model should be an instance of
     * <code>{@link EhCacheFlushingModel}</code>.
     * 
     * @param model
     *          the flushing model.
     * 
     * @throws CacheNotFoundException
     *           if the cache specified in the given model cannot be found.
     * @throws CacheAccessException
     *           wrapping any unexpected exception thrown by the cache.
     * @see AbstractCacheProviderFacade#onFlushCache(FlushingModel)
     */
    @Override
    protected void onFlushCache(FlushingModel model) throws CacheException {
        final EhCacheFlushingModel flushingModel = (EhCacheFlushingModel) model;
        final String[] cacheNames = flushingModel.getCacheNames();

        if (!ObjectUtils.isEmpty(cacheNames)) {
            CacheException cacheException = null;

            try {
                for (final String cacheName : cacheNames) {
                    final Cache cache = getCache(cacheName);
                    cache.removeAll();
                }
            }
            catch (CacheException exception) {
                cacheException = exception;
            }
            catch (Exception exception) {
                cacheException = new CacheAccessException(exception);
            }

            if (cacheException != null) {
                throw cacheException;
            }
        }
    }

    /**
     * Retrieves an object stored under the given key from the cache specified in
     * the given caching model. The caching model should be an instance of
     * <code>{@link EhCacheCachingModel}</code>.
     * 
     * @param key
     *          the key of the cache entry
     * @param model
     *          the caching model
     * @return the object retrieved from the cache. Can be <code>null</code>.
     * 
     * @throws CacheNotFoundException
     *           if the cache specified in the given model cannot be found.
     * @throws CacheAccessException
     *           wrapping any unexpected exception thrown by the cache.
     * @see AbstractCacheProviderFacade#onGetFromCache(Serializable, CachingModel)
     */
    @Override
    protected Object onGetFromCache(Serializable key, CachingModel model) throws CacheException {
        final Cache cache = this.getCache(model);
        Object cachedObject = null;

        try {
            final Element cacheElement = cache.get(key);
            if (cacheElement != null) {
                cachedObject = cacheElement.getObjectValue();
            }
        }
        catch (Exception exception) {
            throw new CacheAccessException(exception);
        }

        return cachedObject;
    }

    /**
     * Stores the given object under the given key in the cache specified in the
     * given caching model. The caching model should be an instance of
     * <code>{@link EhCacheCachingModel}</code>.
     * 
     * @param key
     *          the key of the cache entry
     * @param model
     *          the caching model
     * @param obj
     *          the object to store in the cache
     * 
     * @throws ObjectCannotBeCachedException
     *           if the object to store is not an implementation of
     *           <code>java.io.Serializable</code>.
     * @throws CacheNotFoundException
     *           if the cache specified in the given model cannot be found.
     * @throws CacheAccessException
     *           wrapping any unexpected exception thrown by the cache.
     * 
     * @see AbstractCacheProviderFacade#onPutInCache(Serializable, CachingModel,
     *      Object)
     */
    @Override
    protected void onPutInCache(Serializable key, CachingModel model, Object obj) throws CacheException {
        final Cache cache = this.getCache(model);
        final Element newCacheElement = new Element(key, obj);

        try {
            cache.put(newCacheElement);
        }
        catch (Exception exception) {
            throw new CacheAccessException(exception);
        }
    }

    /**
     * Removes the object stored under the given key from the cache specified in
     * the given caching model. The caching model should be an instance of
     * <code>{@link EhCacheCachingModel}</code>.
     * 
     * @param key
     *          the key of the cache entry
     * @param model
     *          the caching model
     * 
     * @throws CacheNotFoundException
     *           if the cache specified in the given model cannot be found.
     * @throws CacheAccessException
     *           wrapping any unexpected exception thrown by the cache.
     * @see AbstractCacheProviderFacade#onRemoveFromCache(Serializable,
     *      CachingModel)
     */
    @Override
    protected void onRemoveFromCache(Serializable key, CachingModel model) throws CacheException {
        final Cache cache = this.getCache(model);

        try {
            cache.remove(key);
        }
        catch (Exception exception) {
            throw new CacheAccessException(exception);
        }
    }

    /**
     * @see AbstractCacheProviderFacade#validateCacheManager()
     * 
     * @throws FatalCacheException
     *           if the cache manager is <code>null</code>.
     */
    @Override
    protected void validateCacheManager() throws FatalCacheException {
        this.assertCacheManagerIsNotNull(this.cacheManager);
    }

}
