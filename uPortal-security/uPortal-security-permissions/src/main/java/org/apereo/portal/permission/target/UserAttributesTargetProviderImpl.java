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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import org.apereo.portal.permission.target.IPermissionTarget.TargetType;
import org.apereo.services.persondir.IPersonAttributeDao;

public class UserAttributesTargetProviderImpl implements IPermissionTargetProvider {

    private IPersonAttributeDao personAttributeDao;

    /** The {@link IPersonAttributeDao} used to perform lookups. */
    public void setPersonAttributeDao(IPersonAttributeDao personLookupDao) {
        this.personAttributeDao = personLookupDao;
    }

    @Override
    public IPermissionTarget getTarget(String key) {
        return new PermissionTargetImpl(key, key, TargetType.USER_ATTRIBUTE);
    }

    @Override
    public Collection<IPermissionTarget> searchTargets(String term) {
        term = term.toLowerCase();
        final Set<String> attributes = personAttributeDao.getAvailableQueryAttributes(null);
        final List<IPermissionTarget> matches = new ArrayList<IPermissionTarget>();
        for (String attribute : attributes) {
            if (attribute.toLowerCase().contains(term)) {
                matches.add(
                        new PermissionTargetImpl(attribute, attribute, TargetType.USER_ATTRIBUTE));
            }
        }
        return matches;
    }
}
