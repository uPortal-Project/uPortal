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

package org.jasig.portal.permission.target;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.annotation.PostConstruct;

import org.jasig.portal.portlet.om.IPortletType;
import org.jasig.portal.portletpublishing.xml.PortletPublishingDefinition;
import org.jasig.portal.portlets.portletadmin.xmlsupport.IChannelPublishingDefinitionDao;
import org.jasig.portal.security.IPermission;
import org.springframework.beans.factory.annotation.Autowired;

public class PortletTypeTargetProviderImpl implements IPermissionTargetProvider {

    public static final IPermissionTarget ALL_PORTLET_TYPES_PROVIDER = 
            new PermissionTargetImpl(IPermission.ALL_PORTLET_TYPES, IPermission.ALL_PORTLET_TYPES);

    private Map<String,IPermissionTarget> targets;

    @Autowired
    private IChannelPublishingDefinitionDao channelPublishingDefinitionDao;

    @PostConstruct
    private void loadTargets() {

        // Add the "super" target...
        final Map<String,IPermissionTarget> targets = new HashMap<String,IPermissionTarget>();
        targets.put(IPermission.ALL_PORTLET_TYPES, ALL_PORTLET_TYPES_PROVIDER);

        // Add database-defined cpds...
        final Map<IPortletType, PortletPublishingDefinition> cpds = channelPublishingDefinitionDao.getChannelPublishingDefinitions();
        for (Map.Entry<IPortletType, PortletPublishingDefinition> y : cpds.entrySet()) {
            IPermissionTarget target = new PermissionTargetImpl(y.getKey().getName(), y.getKey().getName());
            targets.put(y.getKey().getName(), target);
        }

        this.targets = Collections.unmodifiableMap(targets);

    }

    @Override
    public IPermissionTarget getTarget(String key) {
        return targets.get(key); // Null if unknown
    }

    @Override
    public Collection<IPermissionTarget> searchTargets(String term) {

        Set<IPermissionTarget> rslt = new HashSet<IPermissionTarget>();

        // Search case-insensitive
        final String lowerTerm = term.toLowerCase();
        for (Map.Entry<String,IPermissionTarget> y : targets.entrySet()) {
            if (y.getKey().toLowerCase().contains(lowerTerm)) {
                rslt.add(y.getValue());
            }
        }

        return rslt;

    }

}
