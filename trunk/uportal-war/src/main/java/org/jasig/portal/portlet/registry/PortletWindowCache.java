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

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.jasig.portal.portlet.om.IPortletEntityId;
import org.jasig.portal.portlet.om.IPortletWindowDescriptor;
import org.jasig.portal.portlet.om.IPortletWindowId;
import org.jasig.portal.utils.threading.NoopLock;

import com.google.common.base.Function;

/**
 * Utility for caching portlet windows and window data in memory. Ensures a consistent view for accessing the data by
 * different sets of keys
 * 
 * @author Eric Dalquist
 * @version $Revision$
 * @param <T>
 */
class PortletWindowCache<T extends IPortletWindowDescriptor> {
    private final Lock writeLock;
    private final Lock readLock;
    
    private final Map<IPortletEntityId, Set<T>> windowSetByEntityId = new HashMap<IPortletEntityId, Set<T>>();
    private final Map<IPortletWindowId, T> windowsById = new HashMap<IPortletWindowId, T>();
    
    public PortletWindowCache() {
        this(true);
    }
    
    /**
     * @param threadSafe If set to false no locking is done around read or write operations and this class is NOT thread safe
     */
    public PortletWindowCache(boolean threadSafe) {
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
    
    public T storeIfAbsentWindow(IPortletWindowId portletWindowId, Function<IPortletWindowId, T> windowCreator) {
        //Check if the entity already exists (uses a read lock)
        T existingWindow = this.getWindow(portletWindowId);
        if (existingWindow != null) {
            return existingWindow;
        }
        
        writeLock.lock();
        try {
            //Check again inside the write lock
            existingWindow = this.windowsById.get(portletWindowId);
            if (existingWindow != null) {
                return existingWindow;
            }
            
            final T window = windowCreator.apply(portletWindowId);

            this.storeWindow(window);
            
            return window;
        }
        finally {
            writeLock.unlock();
        }
    }
    
    public T storeIfAbsentWindow(T window) {
        final IPortletWindowId portletWindowId = window.getPortletWindowId();
        
        //Check if the entity already exists (uses a read lock)
        T existingWindow = this.getWindow(portletWindowId);
        if (existingWindow != null) {
            return existingWindow;
        }
        
        writeLock.lock();
        try {
            //Check again inside the write lock
            existingWindow = this.windowsById.get(portletWindowId);
            if (existingWindow != null) {
                return existingWindow;
            }
            
            this.storeWindow(window);
        }
        finally {
            writeLock.unlock();
        }
        
        return window;
    }
    
    public void storeWindow(T window) {
        writeLock.lock();
        try {
            final IPortletEntityId portletEntityId = window.getPortletEntityId();
            final Set<T> windowSet = this.getWindowSet(portletEntityId, true);
            windowSet.add(window);
            
            final IPortletWindowId portletWindowId = window.getPortletWindowId();
            this.windowsById.put(portletWindowId, window);
        }
        finally {
            writeLock.unlock();
        }
    }
    
    public boolean containsWindow(IPortletWindowId portletWindowId) {
        if (this.windowSetByEntityId.isEmpty()) {
            return false;
        }
        
        readLock.lock();
        try {
            return this.windowsById.containsKey(portletWindowId);
        }
        finally {
            readLock.unlock();
        }
    }
    
    public Set<T> getWindows(IPortletEntityId portletEntityId) {
        if (this.windowSetByEntityId.isEmpty()) {
            return null;
        }
        
        readLock.lock();
        try {
            final Set<T> windowSet = this.getWindowSet(portletEntityId, false);
            if (windowSet == null) {
                return Collections.emptySet();
            }
            
            return Collections.unmodifiableSet(windowSet);
        }
        finally {
            readLock.unlock();
        }
    }
    
    public T getWindow(IPortletWindowId portletWindowId) {
        if (this.windowsById.isEmpty()) {
            return null;
        }
        
        readLock.lock();
        try {
            return this.windowsById.get(portletWindowId);
        }
        finally {
            readLock.unlock();
        }
    }
    
    public void removeWindow(IPortletWindowId portletWindowId) {
        writeLock.lock();
        try {
            final T window = this.windowsById.remove(portletWindowId);
            if (window != null) {
                final IPortletEntityId portletEntityId = window.getPortletEntityId();
                final Set<T> windowSet = this.getWindowSet(portletEntityId, false);
                if (windowSet != null) {
                    windowSet.remove(window);
                }
            }
        }
        finally {
            writeLock.unlock();
        }
    }

    protected Set<T> getWindowSet(final IPortletEntityId portletEntityId, boolean create) {
        Set<T> windowSet = this.windowSetByEntityId.get(portletEntityId);
        if (windowSet == null && create) {
            windowSet = new LinkedHashSet<T>();
            this.windowSetByEntityId.put(portletEntityId, windowSet);
        }
        return windowSet;
    }
}