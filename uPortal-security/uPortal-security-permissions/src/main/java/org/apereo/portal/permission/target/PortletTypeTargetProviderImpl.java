/**
 * Licensed to Jasig under one or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information regarding copyright ownership. Jasig
 * licenses this file to you under the Apache License, Version 2.0 (the "License"); you may not use
 * this file except in compliance with the License. You may obtain a copy of the License at:
 *
 * <p>http://www.apache.org/licenses/LICENSE-2.0
 *
 * <p>Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apereo.portal.permission.target;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.apache.commons.lang3.Validate;
import org.apereo.portal.permission.target.IPermissionTarget.TargetType;
import org.apereo.portal.portlet.om.IPortletType;
import org.apereo.portal.portletpublishing.xml.PortletPublishingDefinition;
import org.apereo.portal.portlets.portletadmin.xmlsupport.IChannelPublishingDefinitionDao;
import org.apereo.portal.security.IPermission;
import org.springframework.beans.factory.annotation.Autowired;

public class PortletTypeTargetProviderImpl implements IPermissionTargetProvider {

    /**
     * This is an example of a "collective target." It represents all instances of a certain class
     * of thing (portlet types, in this case). NOTE: There are other collective targets; e.g.
     * ALL_PORTLETS and ALL_GROUPS. Implementation of collective targets is uneven -- logic for most
     * of them is baked into AnyUnblockedGrantPermissionPolicy, whereas the logic for this one is
     * written in PortletAdministrationHelper. (Arguably neither place is appropriate; they might be
     * better in the service layer for permissions: AuthorizationImpl.)
     */
    public static final IPermissionTarget ALL_PORTLET_TYPES_TARGET =
            new PermissionTargetImpl(
                    IPermission.ALL_PORTLET_TYPES,
                    IPermission.ALL_PORTLET_TYPES,
                    TargetType.PORTLET_TYPE);

    @Autowired private IChannelPublishingDefinitionDao channelPublishingDefinitionDao;

    @Override
    public IPermissionTarget getTarget(String key) {
        Validate.notBlank(key, "Argument 'key' cannot be blank");

        IPermissionTarget result = null; // defualt
        if (key.equals(IPermission.ALL_PORTLET_TYPES)) {
            result = ALL_PORTLET_TYPES_TARGET;
        } else {
            // Search database-defined cpds...
            final Map<IPortletType, PortletPublishingDefinition> cpds =
                    channelPublishingDefinitionDao.getChannelPublishingDefinitions();
            for (Map.Entry<IPortletType, PortletPublishingDefinition> y : cpds.entrySet()) {
                if (y.getKey().getName().equals(key)) {
                    result =
                            new PermissionTargetImpl(
                                    y.getKey().getName(),
                                    y.getKey().getName(),
                                    TargetType.PORTLET_TYPE);
                }
            }
        }

        return result;
    }

    @Override
    public Collection<IPermissionTarget> searchTargets(String term) {
        Validate.notBlank(term, "Argument 'term' cannot be blank");

        Set<IPermissionTarget> result = new HashSet<IPermissionTarget>();

        // Search case-insensitive
        final String lowerTerm = term.toLowerCase();
        if (IPermission.ALL_PORTLET_TYPES.toLowerCase().contains(lowerTerm)) {
            result.add(ALL_PORTLET_TYPES_TARGET);
        }
        final Map<IPortletType, PortletPublishingDefinition> cpds =
                channelPublishingDefinitionDao.getChannelPublishingDefinitions();
        for (Map.Entry<IPortletType, PortletPublishingDefinition> y : cpds.entrySet()) {
            if (y.getKey().getName().toLowerCase().contains(lowerTerm)) {
                IPermissionTarget target =
                        new PermissionTargetImpl(
                                y.getKey().getName(),
                                y.getKey().getName(),
                                TargetType.PORTLET_TYPE);
                result.add(target);
            }
        }

        return result;
    }
}
