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
package org.apereo.portal.portlet.dao.jpa;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import org.apereo.portal.portlet.om.AbstractObjectId;
import org.apereo.portal.portlet.om.IPortletDefinitionId;

/** Identifies a portlet definition */
class PortletDefinitionIdImpl extends AbstractObjectId implements IPortletDefinitionId {
    private static final long serialVersionUID = 1L;

    private static final LoadingCache<Long, IPortletDefinitionId> ID_CACHE =
            CacheBuilder.newBuilder()
                    .maximumSize(1000)
                    .softValues()
                    .build(
                            new CacheLoader<Long, IPortletDefinitionId>() {
                                @Override
                                public IPortletDefinitionId load(Long key) throws Exception {
                                    return new PortletDefinitionIdImpl(key);
                                }
                            });

    public static IPortletDefinitionId create(long portletDefinitionId) {
        return ID_CACHE.getUnchecked(portletDefinitionId);
    }

    private final long longId;

    private PortletDefinitionIdImpl(Long portletDefinitionId) {
        super(portletDefinitionId.toString());
        this.longId = portletDefinitionId;
    }

    @Override
    public long getLongId() {
        return this.longId;
    }

    // TODO this matches PortletWindowDataDeserializer.InternalPortletDefinitionId
    // May want to make this class publicly available and delete the InternalPortletDefinitionId
    // to DRY out the code
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + (int) longId;
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null) return false;
        IPortletDefinitionId other = (IPortletDefinitionId) obj;
        return getStringId().equals(other.getStringId());
    }
}
