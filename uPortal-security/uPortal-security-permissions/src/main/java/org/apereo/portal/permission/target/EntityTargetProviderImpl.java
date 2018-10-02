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

import java.io.Serializable;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apereo.portal.layout.dlm.remoting.IGroupListHelper;
import org.apereo.portal.layout.dlm.remoting.JsonEntityBean;
import org.apereo.portal.permission.target.IPermissionTarget.TargetType;
import org.apereo.portal.security.IPermission;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * EntityTargetProviderImpl provides uPortal entity keys as targets. Instances of this
 * implementation may indicate which entity types may be used as targets. Target keys will be the
 * key of the underlying entity itself, while the target human-readable name will similarly be the
 * name of the entity.
 *
 * <p>TODO: This implementation currently has a number of problems. The code uses the EntityEnum
 * class and is hardcoded to only recognize four types of entities: uPortal person groups, person
 * entities, portlet categories, and portlet entities. This code also may perform poorly for large
 * portal installations for searches that return many results.
 *
 * @since 3.3
 */
public class EntityTargetProviderImpl implements IPermissionTargetProvider, Serializable {

    private static final IPermissionTarget ALL_CATEGORIES_TARGET =
            new PermissionTargetImpl(
                    IPermission.ALL_CATEGORIES_TARGET,
                    IPermission.ALL_CATEGORIES_TARGET,
                    TargetType.CATEGORY);

    private static final IPermissionTarget ALL_GROUPS_TARGET =
            new PermissionTargetImpl(
                    IPermission.ALL_GROUPS_TARGET, IPermission.ALL_GROUPS_TARGET, TargetType.GROUP);

    private static final IPermissionTarget ALL_PORTLETS_TARGET =
            new PermissionTargetImpl(
                    IPermission.ALL_PORTLETS_TARGET,
                    IPermission.ALL_PORTLETS_TARGET,
                    TargetType.PORTLET);

    private static final long serialVersionUID = 1L;

    private Set<TargetType> allowedTargetTypes = new HashSet<>();

    protected final transient Log log = LogFactory.getLog(getClass());

    private transient IGroupListHelper groupListHelper;

    @Autowired(required = true)
    public void setGroupListHelper(IGroupListHelper helper) {
        this.groupListHelper = helper;
    }

    /**
     * Construct a new instance of targets matching the set of allowed target entity types.
     *
     * @param targetTypeNames
     */
    public EntityTargetProviderImpl(Set<String> targetTypeNames) {
        /*
         * Arguably this logic should be moved to the TargetType enum itself;
         * but this sort of mapping only occurs (afaik) for "entities."
         */
        for (String name : targetTypeNames) {
            switch (name) {
                case "person":
                    allowedTargetTypes.add(TargetType.PERSON);
                    break;
                case "group":
                    allowedTargetTypes.add(TargetType.GROUP);
                    break;
                case "portlet":
                    allowedTargetTypes.add(TargetType.PORTLET);
                    break;
                case "category":
                    allowedTargetTypes.add(TargetType.CATEGORY);
                    break;
                default:
                    String msg = "Unrecognized targetTypeName:  " + name;
                    throw new RuntimeException(msg);
            }
        }
    }

    /**
     * The <code>key</code> parameter <em>should</em> specify a unique entity across all 4 supported
     * types: people, groups, portlets, and categories.
     *
     * <p>Concrete examples of working keys:
     *
     * <ul>
     *   <li>admin (user)
     *   <li>local.0 (group)
     *   <li>PORTLET_ID.82 (portlet)
     *   <li>local.1 (category)
     * </ul>
     */
    @Override
    public IPermissionTarget getTarget(String key) {

        /*
         * If the specified key matches one of the "all entity" style targets,
         * just return the appropriate target.
         */
        switch (key) {
            case IPermission.ALL_CATEGORIES_TARGET:
                return ALL_CATEGORIES_TARGET;
            case IPermission.ALL_PORTLETS_TARGET:
                return ALL_PORTLETS_TARGET;
            case IPermission.ALL_GROUPS_TARGET:
                return ALL_GROUPS_TARGET;
                // Else just fall through...
        }

        /*
         * Attempt to find a matching entity for each allowed entity type.  This
         * implementation will return the first entity that it finds. If the
         * portal contains duplicate entity keys across multiple types, it's
         * possible that this implementation would demonstrate inconsistent
         * behavior.
         */
        for (TargetType targetType : allowedTargetTypes) {
            JsonEntityBean entity = groupListHelper.getEntity(targetType.toString(), key, false);
            if (entity != null) {
                IPermissionTarget target =
                        new PermissionTargetImpl(entity.getId(), entity.getName(), targetType);
                return target;
            }
        }

        return null;
    }

    /*
     * (non-Javadoc)
     * @see org.apereo.portal.permission.target.IPermissionTargetProvider#searchTargets(java.lang.String)
     */
    @Override
    public Collection<IPermissionTarget> searchTargets(String term) {

        // Initialize a new collection of matching targets.  We use a HashSet
        // implementation here to prevent duplicate target entries.
        Collection<IPermissionTarget> matching = new HashSet<IPermissionTarget>();

        /*
         * Attempt to find matching entities for each allowed entity type.
         * Any matching entities will be added to our collection.
         */
        for (TargetType targetType : allowedTargetTypes) {
            Set<JsonEntityBean> entities = groupListHelper.search(targetType.toString(), term);
            for (JsonEntityBean entity : entities) {
                IPermissionTarget target =
                        new PermissionTargetImpl(entity.getId(), entity.getName(), targetType);
                matching.add(target);
            }
        }

        if (IPermission.ALL_CATEGORIES_TARGET.contains(term)) {
            matching.add(ALL_CATEGORIES_TARGET);
        } else if (IPermission.ALL_PORTLETS_TARGET.contains(term)) {
            matching.add(ALL_PORTLETS_TARGET);
        } else if (IPermission.ALL_GROUPS_TARGET.contains(term)) {
            matching.add(ALL_GROUPS_TARGET);
        }

        // return the list of matching targets
        return matching;
    }
}
