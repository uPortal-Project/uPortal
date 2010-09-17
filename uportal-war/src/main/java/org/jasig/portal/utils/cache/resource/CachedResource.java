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

import java.security.MessageDigest;

import org.springframework.core.io.Resource;

/**
 * Returned by the {@link CachingResourceLoader}. Represents a IO loaded resource including
 * information about when it was last loaded.
 * 
 * @author Eric Dalquist
 * @version $Revision$
 */
public interface CachedResource<T> {
    
    /**
     * @return The Resource that was loaded
     */
    public Resource getResource();
    
    /**
     * @return The cached resource
     */
    public T getCachedResource();
    
    /**
     * @return The timestamp for when the resource was last loaded.
     */
    public long getLastLoadTime();
    
    /**
     * Must be thread safe and follow data visibility rules
     * @return The timestamp for the last time the resource was checked for modification
     */
    public long getLastCheckTime();
    
    /**
     * Sets the timestamp for the last time the resource was checked for modification
     * Must be thread safe and follow data visibility rules
     */
    public void setLastCheckTime(long lastCheckTime);
    
    /**
     * Optional, may return null if no digesting was done.
     * 
     * @return The Base64 encoded {@link MessageDigest} output from digesting the resource during loading.
     */
    public String getLastLoadDigest();
    
    /**
     * Optional, may return null if no digesting was done.
     * Warning, this method makes a COPY of the returned byte[] for every invocation.
     * 
     * @return The byte[] {@link MessageDigest} output from digesting the resource during loading.
     */
    public byte[] getLastLoadDigestBytes();
    
    /**
     * Optional, may return null if no digesting was done.
     * 
     * @return The {@link MessageDigest} algorithm used for digesting the loaded resource
     */
    public String getDigestAlgorithm();
}
