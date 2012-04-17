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

package org.jasig.portal.portlet.dao.jpa;

import org.jasig.portal.portlet.om.AbstractObjectId;
import org.jasig.portal.portlet.om.IPortletDefinitionId;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

/**
 * Identifies a portlet definition
 * 
 * @author Eric Dalquist
 * @version $Revision$
 */
class PortletDefinitionIdImpl extends AbstractObjectId implements IPortletDefinitionId {
    private static final long serialVersionUID = 1L;
    
    private static final LoadingCache<Long, IPortletDefinitionId> ID_CACHE = CacheBuilder.newBuilder()
            .maximumSize(1000)
            .softValues()
            .build(new CacheLoader<Long, IPortletDefinitionId>() {
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
}
