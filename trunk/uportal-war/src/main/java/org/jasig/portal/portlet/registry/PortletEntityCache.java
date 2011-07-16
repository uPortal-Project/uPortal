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

package org.jasig.portal.portlet.registry;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.jasig.portal.portlet.om.IPortletEntityDescriptor;
import org.jasig.portal.portlet.om.IPortletEntityId;
import org.jasig.portal.utils.threading.NoopLock;

import com.google.common.base.Function;

/**
 * Utility for caching portlet entities and entity data in memory. Ensures a consistent view for accessing the data by
 * different sets of keys
 * 
 * @author Eric Dalquist
 * @version $Revision$
 * @param <T>
 */
class PortletEntityCache<T extends IPortletEntityDescriptor> {
    private final Lock writeLock;
    private final Lock readLock;
    
    private final Map<SubscribeKey, T> entitiesBySubscribeKey = new HashMap<SubscribeKey, T>();
    private final Map<IPortletEntityId, T> entitiesById = new HashMap<IPortletEntityId, T>();
    
    public PortletEntityCache() {
        this(true);
    }
    
    /**
     * @param threadSafe If set to false no locking is done around read or write operations and this class is NOT thread safe
     */
    public PortletEntityCache(boolean threadSafe) {
        if (threadSafe) {
            final ReadWriteLock cacheLock = new ReentrantReadWriteLock(true);
            writeLock = cacheLock.writeLock();
            readLock = cacheLock.readLock();
        }
        else {
            writeLock = NoopLock.INSTANCE;
            readLock = NoopLock.INSTANCE;
        }
    }
    
    public T storeIfAbsentEntity(IPortletEntityId portletEntityId, Function<IPortletEntityId, T> entityCreator) {
        //Check if the entity already exists (uses a read lock)
        T existingEntity = this.getEntity(portletEntityId);
        if (existingEntity != null) {
            return existingEntity;
        }
        
        writeLock.lock();
        try {
            //Check again inside the write lock
            existingEntity = this.entitiesById.get(portletEntityId);
            if (existingEntity != null) {
                return existingEntity;
            }
            
            final T entity = entityCreator.apply(portletEntityId);
            
            this.storeEntity(entity);
            
            return entity;
        }
        finally {
            writeLock.unlock();
        }
    }
    
    public T storeIfAbsentEntity(T entity) {
        final IPortletEntityId portletEntityId = entity.getPortletEntityId();
        
        //Check if the entity already exists (uses a read lock)
        T existingEntity = this.getEntity(portletEntityId);
        if (existingEntity != null) {
            return existingEntity;
        }
        
        writeLock.lock();
        try {
            //Check again inside the write lock
            existingEntity = this.entitiesById.get(portletEntityId);
            if (existingEntity != null) {
                return existingEntity;
            }
            
            this.storeEntity(entity);
        }
        finally {
            writeLock.unlock();
        }
        
        return entity;
    }
    
    public void storeEntity(T entity) {
        writeLock.lock();
        try {
            final SubscribeKey subscribeKey = new SubscribeKey(entity.getUserId(), entity.getLayoutNodeId());
            this.entitiesBySubscribeKey.put(subscribeKey, entity);
            this.entitiesById.put(entity.getPortletEntityId(), entity);
        }
        finally {
            writeLock.unlock();
        }
    }
    
    public T getEntity(String layoutNodeId, int userId) {
        if (this.entitiesBySubscribeKey.isEmpty()) {
            return null;
        }
        
        readLock.lock();
        try {
            final SubscribeKey subscribeKey = new SubscribeKey(userId, layoutNodeId);
            return this.entitiesBySubscribeKey.get(subscribeKey);
        }
        finally {
            readLock.unlock();
        }
    }
    
    public T getEntity(IPortletEntityId portletEntityId) {
        if (this.entitiesById.isEmpty()) {
            return null;
        }
        
        readLock.lock();
        try {
            return this.entitiesById.get(portletEntityId);
        }
        finally {
            readLock.unlock();
        }
    }
    
    public void removeEntity(IPortletEntityId portletEntityId) {
        writeLock.lock();
        try {
            final T entity = this.entitiesById.remove(portletEntityId);
            if (entity != null) {
                final int userId = entity.getUserId();
                final String layoutNodeId = entity.getLayoutNodeId();
                final SubscribeKey subscribeKey = new SubscribeKey(userId, layoutNodeId);
                this.entitiesBySubscribeKey.remove(subscribeKey);
            }
        }
        finally {
            writeLock.unlock();
        }
    }
}