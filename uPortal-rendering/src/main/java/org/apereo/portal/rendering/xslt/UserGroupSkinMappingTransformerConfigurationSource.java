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
package org.apereo.portal.rendering.xslt;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import javax.servlet.http.HttpServletRequest;
import org.apereo.portal.EntityIdentifier;
import org.apereo.portal.groups.IGroupConstants;
import org.apereo.portal.groups.IGroupMember;
import org.apereo.portal.security.IPerson;
import org.apereo.portal.services.GroupService;
import org.apereo.portal.user.IUserInstance;
import org.apereo.portal.user.IUserInstanceManager;
import org.apereo.portal.utils.threading.SingletonDoubleCheckedCreator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;

/**
 * Maps a user attribute to a skin. The user's attribute named by {@link
 * #setSkinAttributeName(String)} is used to look up a skin name via the {@link
 * #setAttributeToSkinMap(Map)} map and the skin name is set to in the transformer using the {@link
 * #setSkinParameterName(String)} parameter.
 */
public class UserGroupSkinMappingTransformerConfigurationSource
        extends SkinMappingTransformerConfigurationSource {
    private final SingletonDoubleCheckedCreator<Map<IGroupMember, String>>
            groupMemberToSkinMappingCreator = new GroupMemberMappingCreator();

    private IUserInstanceManager userInstanceManager;
    private Map<String, String> groupToSkinMap = Collections.emptyMap();

    private static final String LOGGER_NAME =
            UserGroupSkinMappingTransformerConfigurationSource.class.getName();

    private Logger logger;

    /**
     * Inits and/or returns already initialized logger. <br>
     * You have to use this method in order to use the logger,<br>
     * you should not call the private variable directly.<br>
     * This was done because Tomcat may instantiate all listeners before calling contextInitialized
     * on any listener.<br>
     * Note that there is no synchronization here on purpose. The object returned by getLog for a
     * logger name is<br>
     * idempotent and getLog itself is thread safe. Eventually all <br>
     * threads will see an instance level logger variable and calls to getLog will stop.
     *
     * @return the log for this class
     */
    protected Logger getLogger() {
        Logger l = this.logger;
        if (l == null) {
            l = LoggerFactory.getLogger(LOGGER_NAME);
            this.logger = l;
        }
        return l;
    }

    @Autowired
    @Lazy
    public void setUserInstanceManager(IUserInstanceManager userInstanceManager) {
        this.userInstanceManager = userInstanceManager;
    }

    /** Map of group keys to skin names. Defaults to an empty map. */
    public void setGroupToSkinMap(Map<String, String> groupToSkinMap) {
        this.groupToSkinMap = groupToSkinMap;
    }

    @Override
    protected String getSkinName(HttpServletRequest request) {

        final IUserInstance userInstance = this.userInstanceManager.getUserInstance(request);
        final IPerson person = userInstance.getPerson();

        final EntityIdentifier personIdentifier = person.getEntityIdentifier();
        final IGroupMember groupMember = GroupService.getGroupMember(personIdentifier);

        final Map<IGroupMember, String> groupMemberToSkinMapping =
                groupMemberToSkinMappingCreator.get();
        for (final Entry<IGroupMember, String> groupToSkinEntry :
                groupMemberToSkinMapping.entrySet()) {
            final IGroupMember group = groupToSkinEntry.getKey();
            if (group.isGroup() && groupMember.isDeepMemberOf(group.asGroup())) {
                final String skin = groupToSkinEntry.getValue();
                getLogger()
                        .debug(
                                "Setting skin override {} for {} because they are a member of {}",
                                new Object[] {skin, person.getUserName(), group});
                // Cache the resolution
                return skin;
            }
        }

        getLogger()
                .debug(
                        "No user {} is not a member of any configured groups, no skin override will be done",
                        person.getUserName());
        return null;
    }

    private final class GroupMemberMappingCreator
            extends SingletonDoubleCheckedCreator<Map<IGroupMember, String>> {
        @Override
        protected Map<IGroupMember, String> createSingleton(Object... args) {
            final Map<IGroupMember, String> groupMemberToSkinMapping =
                    new LinkedHashMap<IGroupMember, String>(groupToSkinMap.size());
            for (final Entry<String, String> groupToSkinEntry : groupToSkinMap.entrySet()) {
                final String group = groupToSkinEntry.getKey();
                final IGroupMember groupMember = this.findGroup(group);

                if (groupMember != null) {
                    final String skin = groupToSkinEntry.getValue();
                    groupMemberToSkinMapping.put(groupMember, skin);
                }
            }

            return groupMemberToSkinMapping;
        }

        /**
         * @param group - case sensitive
         * @return
         */
        protected IGroupMember findGroup(String group) {
            // Find group by ID
            final IGroupMember groupMember = GroupService.findGroup(group);
            if (groupMember != null) {
                return groupMember;
            }

            // No matching ID, search by name
            final EntityIdentifier[] results =
                    GroupService.searchForGroups(
                            group, IGroupConstants.SearchMethod.DISCRETE, IPerson.class);
            if (results == null || results.length == 0) {
                getLogger()
                        .warn(
                                "Configured group '"
                                        + group
                                        + "' cannot be found for skin mapping. This mapping will be ignored");
                return null;
            }

            // Warn if multiple results are found
            if (results.length > 1) {
                getLogger()
                        .warn(
                                results.length
                                        + " groups were found for skin mapping group '"
                                        + group
                                        + "'. The first result will be used. "
                                        + Arrays.toString(results));
            }

            return GroupService.getGroupMember(results[0]);
        }
    }
}
