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
package org.apereo.portal.events.aggr.dao.jpa;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import javax.persistence.MappedSuperclass;
import javax.persistence.PostPersist;
import javax.persistence.PostUpdate;
import javax.persistence.Transient;
import org.apereo.portal.events.aggr.BaseAggregatedDimensionConfig;
import org.apereo.portal.utils.IncludeExcludeUtils;

/**
 * Base impl for aggregated dimension configs, if the subclass does not override {@link
 * #isIncluded(Object)} it should call {@link #clearIncludedCache()} after the includes or excludes
 * are modified.
 *
 */
@MappedSuperclass
public abstract class BaseAggregatedDimensionConfigImpl<D>
        implements BaseAggregatedDimensionConfig<D> {
    private static final long serialVersionUID = 1L;

    @Transient private final Map<D, Boolean> includedCache = new HashMap<D, Boolean>();

    @PostUpdate
    @PostPersist
    protected void clearIncludedCache() {
        includedCache.clear();
    }

    @Override
    public final boolean isIncluded(D dimension) {
        final Boolean cachedInclude = includedCache.get(dimension);
        if (cachedInclude != null) {
            return cachedInclude;
        }

        final Set<D> included = this.getIncluded();
        final Set<D> excluded = this.getExcluded();
        final boolean include =
                (!included.isEmpty() || !excluded.isEmpty())
                        && IncludeExcludeUtils.included(dimension, included, excluded);
        includedCache.put(dimension, include);
        return include;
    }

    @Override
    public final int hashCode() {
        final int prime = 31;
        int result = 1;
        result =
                prime * result
                        + ((getAggregatorType() == null) ? 0 : getAggregatorType().hashCode());
        return result;
    }

    @Override
    public final boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null) return false;
        if (getClass() != obj.getClass()) return false;
        BaseAggregatedDimensionConfigImpl other = (BaseAggregatedDimensionConfigImpl) obj;
        if (getAggregatorType() == null) {
            if (other.getAggregatorType() != null) return false;
        } else if (!getAggregatorType().equals(other.getAggregatorType())) return false;
        return true;
    }
}
