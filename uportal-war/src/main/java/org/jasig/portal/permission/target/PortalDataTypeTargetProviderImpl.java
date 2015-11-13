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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.jasig.portal.io.xml.IPortalDataHandlerService;
import org.jasig.portal.io.xml.IPortalDataType;
import org.springframework.beans.factory.annotation.Autowired;

public class PortalDataTypeTargetProviderImpl implements IPermissionTargetProvider {

    private Map<String, IPermissionTarget> targetMap = new HashMap<String, IPermissionTarget>();

    @Autowired
    private IPortalDataHandlerService portalDataHandlerService;

    @PostConstruct
    public void init() {
        Iterable<IPortalDataType> dataTypes = portalDataHandlerService.getExportPortalDataTypes();
        for (IPortalDataType type : dataTypes) {
            final String typeId = type.getTypeId();
            targetMap.put(typeId, new PermissionTargetImpl(typeId, typeId,
                                IPermissionTarget.TargetType.DATA_TYPE));
        }
    }

    @Override
    public IPermissionTarget getTarget(String key) {
        return targetMap.get(key);
    }

    @Override
    public Collection<IPermissionTarget> searchTargets(String term) {

        // ensure that the search term is all lowercase to aid in string comparison
        term = term.toLowerCase();

        // initialize a new collection of matching targets
        Collection<IPermissionTarget> matching = new ArrayList<IPermissionTarget>();

        // iterate through each target, comparing it to the search term
        for (IPermissionTarget target : targetMap.values()) {
            // if the target's key or display name contains the search term,
            // count it as matching
            if (target.getKey().toLowerCase().contains(term)
                    || target.getName().toLowerCase().contains(term)) {
                matching.add(target);
            }
        }

        // return the list of matching targets
        return matching;
    }

}
