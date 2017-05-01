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

import java.io.IOException;
import org.springframework.core.io.Resource;

/**
 * A utility for loading resources from files, URLs or similar locations and building/compiling a
 * domain object from the resource. The domain object is cached and automatically re-loaded if the
 * underlying {@link Resource} changes.
 *
 * <p>Clients should call {@link #getResource(Resource, Loader)} every time they need the object and
 * rely on the {@link CachingResourceLoader} implementation to manage caching and reloading.
 *
 */
public interface CachingResourceLoader {
    /** Same as {@link #getResource(Resource, Loader, long)} with a 60 second check interval */
    public <T> CachedResource<T> getResource(Resource resource, Loader<T> builder)
            throws IOException;

    /**
     * Get the {@link Resource} using the {@link Loader} to compile it if needed using the specified
     * options.
     */
    public <T> CachedResource<T> getResource(
            Resource resource, Loader<T> builder, long checkInterval) throws IOException;
}
