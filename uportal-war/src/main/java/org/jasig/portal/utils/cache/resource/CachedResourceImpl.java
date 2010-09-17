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

import java.util.Arrays;

import org.springframework.core.io.Resource;

/**
 * @author Eric Dalquist
 * @version $Revision$
 */
class CachedResourceImpl<T> implements CachedResource<T> {
    private final Resource resource;
    private final T cachedResource;
    private final long lastLoadTime;
    private volatile long lastCheckTime;
    private String lastLoadDigest;
    private byte[] lastLoadDigestBytes;
    private String digestAlgorithm;
    

    public CachedResourceImpl(Resource resource, T cachedResource, long lastLoadTime) {
        this(resource, cachedResource, lastLoadTime, null, null, null);
    }
    
    public CachedResourceImpl(Resource resource, T cachedResource, long lastLoadTime, 
            String lastLoadDigest, byte[] lastLoadDigestBytes, String digestAlgorithm) {
        this.resource = resource;
        this.cachedResource = cachedResource;
        this.lastLoadTime = lastLoadTime;
        this.lastCheckTime = lastLoadTime;
        this.lastLoadDigest = lastLoadDigest;
        this.lastLoadDigestBytes = lastLoadDigestBytes;
        this.digestAlgorithm = digestAlgorithm;
    }
    
    @Override
    public Resource getResource() {
        return this.resource;
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
    public String getLastLoadDigest() {
        return this.lastLoadDigest;
    }

    @Override
    public byte[] getLastLoadDigestBytes() {
        if (this.lastLoadDigestBytes == null) {
            return null;
        }
        
        return Arrays.copyOf(this.lastLoadDigestBytes, this.lastLoadDigestBytes.length);
    }

    @Override
    public String getDigestAlgorithm() {
        return this.digestAlgorithm;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((this.digestAlgorithm == null) ? 0 : this.digestAlgorithm.hashCode());
        result = prime * result + ((this.lastLoadDigest == null) ? 0 : this.lastLoadDigest.hashCode());
        result = prime * result + (int) (this.lastLoadTime ^ (this.lastLoadTime >>> 32));
        result = prime * result + (int) (this.lastCheckTime ^ (this.lastCheckTime >>> 32));
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
        if (this.digestAlgorithm == null) {
            if (other.digestAlgorithm != null) {
                return false;
            }
        }
        else if (!this.digestAlgorithm.equals(other.digestAlgorithm)) {
            return false;
        }
        if (this.lastLoadDigest == null) {
            if (other.lastLoadDigest != null) {
                return false;
            }
        }
        else if (!this.lastLoadDigest.equals(other.lastLoadDigest)) {
            return false;
        }
        if (this.lastLoadTime != other.lastLoadTime) {
            return false;
        }
        if (this.lastCheckTime != other.lastCheckTime) {
            return false;
        }
        if (this.resource == null) {
            if (other.resource != null) {
                return false;
            }
        }
        else if (!this.resource.equals(other.resource)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "CachedResourceImpl [resource=" + this.resource + ", lastLoadTime=" + this.lastLoadTime
                + ", lastCheckTime=" + this.lastCheckTime + ", lastLoadDigest=" + this.lastLoadDigest
                + ", digestAlgorithm=" + this.digestAlgorithm + "]";
    }
}
