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

package org.jasig.portal.events.aggr.dao.jpa;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.persistence.Column;
import javax.persistence.MappedSuperclass;
import javax.persistence.PostPersist;
import javax.persistence.PostUpdate;
import javax.persistence.Transient;
import javax.persistence.Version;

import org.jasig.portal.events.aggr.BaseAggregatedDimensionConfig;
import org.jasig.portal.utils.IncludeExcludeUtils;

/**
 * Base impl for aggregated dimension configs, if the subclass does not override {@link #isIncluded(Object)}
 * it should call {@link #clearIncludedCache()} after the includes or excludes are modified.
 * 
 * @author Eric Dalquist
 * @version $Revision$
 */
@MappedSuperclass
public abstract class BaseAggregatedDimensionConfigImpl<D> implements BaseAggregatedDimensionConfig<D> {

    @Version
    @Column(name = "ENTITY_VERSION")
    private final long entityVersion = -1;
    
    @Transient
    private final Map<D, Boolean> includedCache = new ConcurrentHashMap<D, Boolean>();
    
    @PostUpdate
    @PostPersist
    protected void clearIncludedCache() {
        includedCache.clear();
    }
    
    @Override
    public long getVersion() {
        return this.entityVersion;
    }

    @Override
    public boolean isIncluded(D dimension) {
        final Boolean cachedInclude = includedCache.get(dimension);
        if (cachedInclude != null) {
            return cachedInclude;
        }
        
        final boolean included = IncludeExcludeUtils.included(dimension, this.getIncluded(), this.getExcluded());
        includedCache.put(dimension, included);
        return included;
    }

    @Override
    public final int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((getAggregatorType() == null) ? 0 : getAggregatorType().hashCode());
        return result;
    }

    @Override
    public final boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        BaseAggregatedDimensionConfigImpl other = (BaseAggregatedDimensionConfigImpl) obj;
        if (getAggregatorType() == null) {
            if (other.getAggregatorType() != null)
                return false;
        }
        else if (!getAggregatorType().equals(other.getAggregatorType()))
            return false;
        return true;
    }
}
