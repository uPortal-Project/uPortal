package org.jasig.portal.jpa.cache;

import java.io.Serializable;
import java.util.Deque;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import javax.persistence.EntityManager;

import net.sf.ehcache.Ehcache;
import net.sf.ehcache.Element;

import org.jasig.portal.jpa.AbstractEntityManagerEvent;
import org.jasig.portal.jpa.EntityManagerClosingEvent;
import org.jasig.portal.jpa.EntityManagerCreatedEvent;
import org.jasig.portal.utils.cache.CacheKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
public class EntityManagerCache implements ApplicationListener<AbstractEntityManagerEvent> {
    //Thread local map of PU name to the EMId
    private static final ThreadLocal<Map<String, Deque<String>>> CURRENT_ENTITY_MANAGER_SESSIONS = new ThreadLocal<Map<String,Deque<String>>>();
    //Map of PU name to current open EM count
    private static final LoadingCache<String, AtomicInteger> OPEN_EM_COUNTER = CacheBuilder.newBuilder()
            .build(new CacheLoader<String, AtomicInteger>() {
                @Override
                public AtomicInteger load(String key) throws Exception {
                    return new AtomicInteger();
                }
            });
    
    protected final Logger logger = LoggerFactory.getLogger(getClass());
    
    //TODO track statistics
    private Ehcache contentCache;
    
    @Autowired
    @Qualifier("org.jasig.portal.jpa.cache.EntityManagerCache")
    public void setContentCache(Ehcache contentCache) {
        this.contentCache = contentCache;
    }

    public void put(String persistenceUnitName, Serializable key, Object value) {
        final Map<String, Deque<String>> currentEntityManagers = CURRENT_ENTITY_MANAGER_SESSIONS.get();
        final Deque<String> entityManagerIds = currentEntityManagers.get(persistenceUnitName);
        
        if (entityManagerIds == null || entityManagerIds.isEmpty()) {
            throw new IllegalStateException("Cannot access cache for persistent unit " + persistenceUnitName + ", it has no active EntityManager: key=" + key);
        }
        
        //TODO purge stuff out of the ehcache instance on EntityManagerClosingEvent
        final String entityManagerId = entityManagerIds.getFirst();
        final CacheKey cacheKey = CacheKey.build(this.getClass().getName(), entityManagerId, key);
        this.contentCache.put(new Element(cacheKey, value));
    }
    
    public <T> T get(String persistenceUnitName, Serializable key) {
        final Map<String, Deque<String>> currentEntityManagers = CURRENT_ENTITY_MANAGER_SESSIONS.get();
        final Deque<String> entityManagerIds = currentEntityManagers.get(persistenceUnitName);
        
        if (entityManagerIds == null || entityManagerIds.isEmpty()) {
            throw new IllegalStateException("Cannot access cache for persistent unit " + persistenceUnitName + ", it has no active EntityManager: key=" + key);
        }
        
        final String entityManagerId = entityManagerIds.getFirst();
        final CacheKey cacheKey = CacheKey.build(this.getClass().getName(), entityManagerId, key);
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

            //Get the currentEntityManagers Map
            final Map<String, Deque<String>> currentEntityManagers = CURRENT_ENTITY_MANAGER_SESSIONS.get();
            if (currentEntityManagers == null || currentEntityManagers.isEmpty()) {
                throw new IllegalStateException("Closing " + entityManagerId + " but there is no currentEntityManagers Map for this Thread");
            }
            
            //Get the Deque of current entityManagerIds
            final Deque<String> entityManagerIds = currentEntityManagers.get(persistenceUnitName);
            if (entityManagerIds == null || entityManagerIds.isEmpty()) {
                throw new IllegalStateException("Closing " + entityManagerId + " but there is no entityManagerIds Deque for this Thread");
            }
            
            //Get the current EMiD for this PU
            final String currentEntityManagerId = entityManagerIds.getFirst();
            if (!currentEntityManagerId.equals(entityManagerId)) {
                throw new IllegalStateException("Closing " + entityManagerId + " but the current EntityManagerId is " + currentEntityManagerId);
            }
            
            //Remove the current EMiD
            entityManagerIds.removeFirst();
            //TODO remove cached entries related to this EMiD
            
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
}
