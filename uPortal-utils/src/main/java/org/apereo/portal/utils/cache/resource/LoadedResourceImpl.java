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

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.core.io.Resource;

/**
 * Basic implementation of {@link LoadedResource}
 *
 */
public class LoadedResourceImpl<T> implements LoadedResource<T> {
    private final T resource;
    private final Map<Resource, Long> additionalSources;

    public LoadedResourceImpl(T resource) {
        this(resource, null);
    }

    public LoadedResourceImpl(T resource, Map<Resource, Long> additionalSources) {
        this.resource = resource;

        if (additionalSources == null) {
            this.additionalSources = Collections.emptyMap();
        } else {
            this.additionalSources =
                    Collections.unmodifiableMap(
                            new LinkedHashMap<Resource, Long>(additionalSources));
        }
    }

    /* (non-Javadoc)
     * @see org.apereo.portal.utils.cache.resource.LoadedResource#getResource()
     */
    @Override
    public T getLoadedResource() {
        return this.resource;
    }

    /* (non-Javadoc)
     * @see org.apereo.portal.utils.cache.resource.LoadedResource#getAdditionalSources()
     */
    @Override
    public Map<Resource, Long> getAdditionalResources() {
        return this.additionalSources;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result =
                prime * result
                        + ((this.additionalSources == null)
                                ? 0
                                : this.additionalSources.hashCode());
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
        LoadedResourceImpl<?> other = (LoadedResourceImpl<?>) obj;
        if (this.additionalSources == null) {
            if (other.additionalSources != null) {
                return false;
            }
        } else if (!this.additionalSources.equals(other.additionalSources)) {
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
        return "LoadedResourceImpl [resource="
                + this.resource
                + ", additionalSources="
                + this.additionalSources
                + "]";
    }
}
