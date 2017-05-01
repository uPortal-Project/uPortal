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
 * Returned by the {@link CachingResourceLoader}. Represents a IO loaded resource including
 * information about when it was last loaded.
 *
 */
public interface CachedResource<T> {

    /** @return The Resource that was loaded */
    public Resource getResource();

    /** @return Additional resource files involved with loading */
    public Map<Resource, Long> getAdditionalResources();

    /** @return The cached resource */
    public T getCachedResource();

    /** @return The timestamp for when the resource was last loaded. */
    public long getLastLoadTime();

    /**
     * Must be thread safe and follow data visibility rules
     *
     * @return The timestamp for the last time the resource was checked for modification
     */
    public long getLastCheckTime();

    /**
     * Sets the timestamp for the last time the resource was checked for modification Must be thread
     * safe and follow data visibility rules
     */
    public void setLastCheckTime(long lastCheckTime);

    /** A serializable key that represents the state of the cached resource */
    public Serializable getCacheKey();
}
