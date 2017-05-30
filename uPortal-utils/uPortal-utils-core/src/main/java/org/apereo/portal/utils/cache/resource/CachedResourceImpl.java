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

import java.io.Serializable;
import java.util.Map;
import org.springframework.core.io.Resource;

/**
 */
class CachedResourceImpl<T> implements CachedResource<T> {
    private final Resource resource;
    private final Map<Resource, Long> additionalResources;
    private final T cachedResource;
    private final long lastLoadTime;
    private final Serializable cacheKey;
    private volatile long lastCheckTime;

    public CachedResourceImpl(
            Resource resource,
            LoadedResource<T> loadedResource,
            long lastLoadTime,
            Serializable cacheKey) {
        this.resource = resource;
        this.cachedResource = loadedResource.getLoadedResource();
        this.additionalResources = loadedResource.getAdditionalResources();
        this.lastLoadTime = lastLoadTime;
        this.lastCheckTime = lastLoadTime;
        this.cacheKey = cacheKey;
    }

    @Override
    public Resource getResource() {
        return this.resource;
    }

    @Override
    public Map<Resource, Long> getAdditionalResources() {
        return this.additionalResources;
    }

    @Override
    public T getCachedResource() {
        return this.cachedResource;
    }

    @Override
    public long getLastLoadTime() {
        return this.lastLoadTime;
    }

    @Override
    public long getLastCheckTime() {
        return this.lastCheckTime;
    }

    @Override
    public void setLastCheckTime(long lastCheckTime) {
        this.lastCheckTime = lastCheckTime;
    }

    @Override
    public Serializable getCacheKey() {
        return this.cacheKey;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result =
                prime * result
                        + ((this.additionalResources == null)
                                ? 0
                                : this.additionalResources.hashCode());
        result = prime * result + ((this.cacheKey == null) ? 0 : this.cacheKey.hashCode());
        result =
                prime * result
                        + ((this.cachedResource == null) ? 0 : this.cachedResource.hashCode());
        result = prime * result + (int) (this.lastCheckTime ^ (this.lastCheckTime >>> 32));
        result = prime * result + (int) (this.lastLoadTime ^ (this.lastLoadTime >>> 32));
        result = prime * result + ((this.resource == null) ? 0 : this.resource.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        CachedResourceImpl<?> other = (CachedResourceImpl<?>) obj;
        if (this.additionalResources == null) {
            if (other.additionalResources != null) {
                return false;
            }
        } else if (!this.additionalResources.equals(other.additionalResources)) {
            return false;
        }
        if (this.cacheKey == null) {
            if (other.cacheKey != null) {
                return false;
            }
        } else if (!this.cacheKey.equals(other.cacheKey)) {
            return false;
        }
        if (this.cachedResource == null) {
            if (other.cachedResource != null) {
                return false;
            }
        } else if (!this.cachedResource.equals(other.cachedResource)) {
            return false;
        }
        if (this.lastCheckTime != other.lastCheckTime) {
            return false;
        }
        if (this.lastLoadTime != other.lastLoadTime) {
            return false;
        }
        if (this.resource == null) {
            if (other.resource != null) {
                return false;
            }
        } else if (!this.resource.equals(other.resource)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "CachedResourceImpl [resource="
                + this.resource
                + ", additionalResources="
                + this.additionalResources
                + ", cachedResource="
                + this.cachedResource
                + ", lastLoadTime="
                + this.lastLoadTime
                + ", cacheKey="
                + this.cacheKey
                + ", lastCheckTime="
                + this.lastCheckTime
                + "]";
    }
}
