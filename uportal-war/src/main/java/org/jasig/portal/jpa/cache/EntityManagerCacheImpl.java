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
package org.jasig.portal.jpa.cache;

import java.io.Serializable;
import java.util.Deque;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.atomic.AtomicInteger;

import javax.persistence.EntityManager;

import net.sf.ehcache.Ehcache;
import net.sf.ehcache.Element;

import org.jasig.portal.jpa.AbstractEntityManagerEvent;
import org.jasig.portal.jpa.EntityManagerClosingEvent;
import org.jasig.portal.jpa.EntityManagerCreatedEvent;
import org.jasig.portal.utils.cache.CacheKey;
import org.jasig.portal.utils.cache.SimpleCacheEntryTag;
import org.jasig.portal.utils.cache.TaggedCacheEntryPurger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Service;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

/**
 * Assumes only one {@link EntityManager} per {@link AbstractEntityManagerEvent#getPersistenceUnitName()} can be active
 * per thread.
 */
@Service
public class EntityManagerCacheImpl implements ApplicationListener<AbstractEntityManagerEvent>, DisposableBean, EntityManagerCache {
    private static final String CACHE_TAG = "entityManagerId";
    private static final String CACHE_KEY_SOURCE = EntityManagerCacheImpl.class.getName();
    
    //Thread local map of PersistenceUnit name to the Stack of EntityManagerIds
    private static final ThreadLocal<Map<String, Deque<String>>> CURRENT_ENTITY_MANAGER_SESSIONS = new ThreadLocal<Map<String,Deque<String>>>();
    
    //Map of PersistentUnit names to current open EntityManager count
    private static final LoadingCache<String, AtomicInteger> OPEN_EM_COUNTER = CacheBuilder.newBuilder()
            .build(new CacheLoader<String, AtomicInteger>() {
                @Override
                public AtomicInteger load(String key) throws Exception {
                    return new AtomicInteger();
                }
            });
    
    protected final Logger logger = LoggerFactory.getLogger(getClass());
    
    private Ehcache contentCache;
    private TaggedCacheEntryPurger taggedCacheEntryPurger;
    
    @Autowired
    @Qualifier("org.jasig.portal.jpa.cache.EntityManagerCache")
    public void setContentCache(Ehcache contentCache) {
        this.contentCache = contentCache;
    }
    
    @Autowired
    public void setTaggedCacheEntryPurger(TaggedCacheEntryPurger taggedCacheEntryPurger) {
        this.taggedCacheEntryPurger = taggedCacheEntryPurger;
    }

    @Override
    public void put(String persistenceUnitName, Serializable key, Object value) {
        final Map<String, Deque<String>> currentEntityManagers = CURRENT_ENTITY_MANAGER_SESSIONS.get();
        if (currentEntityManagers == null) {
            logger.error("There is no currentEntityManagers Map in the ThreadLocal, no EntityManager scoped caching will be done. persistenceUnitName=" + persistenceUnitName + ", key=" + key, new Throwable());
            return;
        }
        
        final Deque<String> entityManagerIds = currentEntityManagers.get(persistenceUnitName);
        if (entityManagerIds == null || entityManagerIds.isEmpty()) {
            logger.error("Cannot access cache for persistent unit " + persistenceUnitName + ", it has no active EntityManager, no EntityManager scoped caching will be done. key=" + key, new Throwable());
            return;
        }
        
        final String entityManagerId = entityManagerIds.getFirst();
        final SimpleCacheEntryTag entityManagerIdTag = new SimpleCacheEntryTag(CACHE_TAG, entityManagerId);
        final CacheKey cacheKey = CacheKey.buildTagged(CACHE_KEY_SOURCE, entityManagerIdTag, entityManagerId, key);
        this.contentCache.put(new Element(cacheKey, value));
    }
    
    @Override
    public <T> T get(String persistenceUnitName, Serializable key) {
        final Map<String, Deque<String>> currentEntityManagers = CURRENT_ENTITY_MANAGER_SESSIONS.get();
        if (currentEntityManagers == null) {
            logger.error("There is no currentEntityManagers Map in the ThreadLocal, no EntityManager scoped caching will be done. persistenceUnitName=" + persistenceUnitName + ", key=" + key, new Throwable());
            return null;
        }
        
        final Deque<String> entityManagerIds = currentEntityManagers.get(persistenceUnitName);
        if (entityManagerIds == null || entityManagerIds.isEmpty()) {
            logger.error("Cannot access cache for persistent unit " + persistenceUnitName + ", it has no active EntityManager, no EntityManager scoped caching will be done. key=" + key, new Throwable());
            return null;
        }
        
        final String entityManagerId = entityManagerIds.getFirst();
        final SimpleCacheEntryTag entityManagerIdTag = new SimpleCacheEntryTag(CACHE_TAG, entityManagerId);
        final CacheKey cacheKey = CacheKey.buildTagged(CACHE_KEY_SOURCE, entityManagerIdTag, entityManagerId, key);
        final Element element = this.contentCache.get(cacheKey);
        
        if (element == null) {
            return null;
        }
        
        return (T)element.getObjectValue();
    }
    
    @Override
    public void onApplicationEvent(AbstractEntityManagerEvent event) {
        final String persistenceUnitName = event.getPersistenceUnitName();
        final AtomicInteger counter = OPEN_EM_COUNTER.getUnchecked(persistenceUnitName);
        final String entityManagerId = event.getEntityManagerId();
        
        if (event instanceof EntityManagerCreatedEvent) {
            final int count = counter.incrementAndGet();
            if (logger.isTraceEnabled()) {
                logger.trace("CREATE " + count + " - " + entityManagerId + " " + Thread.currentThread().getName());
            }

            //Get/Create the currentEntityManagers Map
            Map<String, Deque<String>> currentEntityManagers = CURRENT_ENTITY_MANAGER_SESSIONS.get();
            if (currentEntityManagers == null) {
                currentEntityManagers = new HashMap<String, Deque<String>>();
                CURRENT_ENTITY_MANAGER_SESSIONS.set(currentEntityManagers);
            }
            
            //Get/Create the Deque of current entityManagerIds
            Deque<String> entityManagerIds = currentEntityManagers.get(persistenceUnitName);
            if (entityManagerIds == null) {
                entityManagerIds = new LinkedList<String>();
                currentEntityManagers.put(persistenceUnitName, entityManagerIds);
            }
            
            //Set the current EMiD for this PU
            entityManagerIds.offerFirst(entityManagerId);
        }
        else if (event instanceof EntityManagerClosingEvent) {
            final int count = counter.decrementAndGet();
            if (logger.isTraceEnabled()) {
                logger.trace("CLOSE  " + count + " - " + entityManagerId + " " + Thread.currentThread().getName());
            }

            //Purge any cached data related to this entity manager
            taggedCacheEntryPurger.purgeCacheEntries(new SimpleCacheEntryTag(CACHE_TAG, entityManagerId));

            //Get the currentEntityManagers Map
            final Map<String, Deque<String>> currentEntityManagers = CURRENT_ENTITY_MANAGER_SESSIONS.get();
            if (currentEntityManagers == null || currentEntityManagers.isEmpty()) {
                logger.error("Closing " + entityManagerId + " but there is no currentEntityManagers Map for this Thread", new Throwable());
            }
            
            //Get the Deque of current entityManagerIds
            final Deque<String> entityManagerIds = currentEntityManagers.get(persistenceUnitName);
            if (entityManagerIds == null || entityManagerIds.isEmpty()) {
                logger.error("Closing " + entityManagerId + " but there is no entityManagerIds Deque for this Thread", new Throwable());
            }
            
            //Get the current EMiD for this PU
            final String currentEntityManagerId = entityManagerIds.getFirst();
            if (!currentEntityManagerId.equals(entityManagerId)) {
                logger.error("Closing " + entityManagerId + " but the current EntityManagerId is " + currentEntityManagerId, new Throwable());
            }
            
            //Remove the current EMiD
            entityManagerIds.removeFirst();
            
            //If nothing else is tracked for this PU remove the deque
            if (entityManagerIds.isEmpty()) {
                currentEntityManagers.remove(persistenceUnitName);
            }
            
            //If nothing else is tracked for this thread remove the local
            if (currentEntityManagers.isEmpty()) {
                CURRENT_ENTITY_MANAGER_SESSIONS.remove();
            }
        }
    }

    @Override
    public void destroy() throws Exception {
        for (final Entry<String, AtomicInteger> entityManagerCountEntry : OPEN_EM_COUNTER.asMap().entrySet()) {
            final AtomicInteger counter = entityManagerCountEntry.getValue();
            final int count = counter.get();
            if (count > 0) {
                final String persistenceUnit = entityManagerCountEntry.getKey();
                logger.error("PersistenceUnit {} has {} EntityManagers that were opened but never closed", persistenceUnit, count);
            }
        }
    }
}
