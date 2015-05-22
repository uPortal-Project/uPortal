/**
 * Licensed to Apereo under one or more contributor license
 * agreements. See the NOTICE file distributed with this work
 * for additional information regarding copyright ownership.
 * Apereo licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License.  You may obtain a
 * copy of the License at the following location:
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.jasig.portal.portlets.groupadmin;

import org.jasig.portal.EntityIdentifier;
import org.jasig.portal.groups.pags.dao.*;
import org.jasig.portal.groups.pags.testers.AdHocGroupTester;
import org.jasig.portal.layout.dlm.remoting.IGroupListHelper;
import org.jasig.portal.security.IAuthorizationPrincipal;
import org.jasig.portal.security.IPermission;
import org.jasig.portal.security.IPerson;
import org.jasig.portal.security.RuntimeAuthorizationException;
import org.jasig.portal.services.AuthorizationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Set;

/**
 * Provides helper methods for PAGS ad hoc groups administration webflows.
 * Methods check user permissions.
 * <p/>
 * This implementation was designed with limitations. Groups managed by this feature
 * will not be allowed to set members or other PAGS tests beyond the ad hoc group tester.
 *
 * @author  Benito J. Gonzalez <bgonzalez@unicon.net>
 * @see     org.jasig.portal.groups.pags.dao.IPersonAttributesGroupDefinitionDao
 * @see     org.jasig.portal.groups.pags.dao.IPersonAttributesGroupTestDefinitionDao
 * @see     org.jasig.portal.groups.pags.dao.IPersonAttributesGroupTestGroupDefinitionDao
 * @see     org.jasig.portal.groups.pags.testers.AdHocGroupTester
 * @since   4.3
 */
@Service
public final class PagsAdHocGroupAdministrationHelper {
    
    private static final String AD_HOC_GROUP_TESTER = AdHocGroupTester.class.getName();

    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Value("${ad.hoc.group.parent:Ad Hoc Groups}")
    private String adHocParentGroupName;

    @Autowired
    private IGroupListHelper groupListHelper;

    @Autowired
    private IPersonAttributesGroupDefinitionDao pagsGroupDefDao;

    @Autowired
    private IPersonAttributesGroupTestGroupDefinitionDao pagsTestGroupDefDao;

    @Autowired
    private IPersonAttributesGroupTestDefinitionDao pagsTestDefDao;

    /**
     * Verify that the group is one of the ad hoc groups that is eligible for management via this feature.
     * <ul>
     *     <li>Child of {@code ad hoc groups} group -- THIS NEEDED? -- check not implemented</li>
     *     <li>Single test group</li>
     *     <li>Single ad hoc group tester</li>
     *     <li>No group members</li>
     * </ul>
     *
     * @param group     PAGS group
     * @return          {@code true} if group meets the criteria to be managed by this UI feature; otherwise, {@code false}
     */
    public boolean isManagedAdHocGroup(IPersonAttributesGroupDefinition group) {
        if (group.getMembers().size() > 0) {
            return false;
        }
        Set<IPersonAttributesGroupTestGroupDefinition> testGroups = group.getTestGroups();
        if (testGroups.size() != 1) {
            return false;
        }
        Set<IPersonAttributesGroupTestDefinition> tests = testGroups.iterator().next().getTests();
        if (tests.size() != 1) {
            return false;
        }
        IPersonAttributesGroupTestDefinition test = tests.iterator().next();
        if (!test.getTesterClassName().equals(AD_HOC_GROUP_TESTER)) {
            return false;
        }
        return test.getIncludes().size() + test.getExcludes().size() > 0;
    }

    /**
     * Construct a group form for the group with the specified name.
     *
     * @param name      PAGS group name
     * @return          form version of specified group
     */
    public PagsAdHocGroupForm getGroupForm(String name) {
        // Should there be a view or edit check?
        logger.debug("Initializing group form for ad hoc PAGS group named {}", name);
        IPersonAttributesGroupDefinition group = getPagsGroupDefByName(name);
        assert(isManagedAdHocGroup(group));
        PagsAdHocGroupForm form = new PagsAdHocGroupForm();
        form.setName(group.getName());
        form.setDescription(group.getDescription());
        IPersonAttributesGroupTestDefinition test = getPersonAttributesGroupTestDefinition(group);
        for (String incGroup: test.getIncludes()) {
            form.addIncludes(incGroup);
        }
        for (String excGroup: test.getExcludes()) {
            form.addExcludes(excGroup);
        }
        return form;
    }

    /**
     * Create a new group under the specified parent.  The new group will
     * automatically be added to the parent group.
     *
     * @param form      form representing the new group configuration
     * @param user      user performing the update
     */
    public void createGroup(PagsAdHocGroupForm form, IPerson user) {
        logger.debug("Creating group for group form [{}]", form.toString());
        if (!hasPermission(user, IPermission.CREATE_GROUP_ACTIVITY, adHocParentGroupName)) {
            throw new RuntimeAuthorizationException(user, IPermission.CREATE_GROUP_ACTIVITY, form.getName());
        }
        IPersonAttributesGroupDefinition group = this.pagsGroupDefDao.createPersonAttributesGroupDefinition(
                form.getName(), form.getDescription());
        IPersonAttributesGroupTestGroupDefinition testGroup = this.pagsTestGroupDefDao.createPersonAttributesGroupTestGroupDefinition(group);
        IPersonAttributesGroupTestDefinition test = this.pagsTestDefDao.createPersonAttributesGroupTestDefinition(
                testGroup, null, AD_HOC_GROUP_TESTER, null, form.getIncludes(), form.getExcludes());

        // add this group to the membership list for the specified parent
        IPersonAttributesGroupDefinition parentGroup = getPagsGroupDefByName(adHocParentGroupName);
        Set<IPersonAttributesGroupDefinition> parents = new HashSet<>(1);
        parents.add(parentGroup);
        group.setParents(parents);

        assert(isManagedAdHocGroup(group));
        this.pagsGroupDefDao.updatePersonAttributesGroupDefinition(group);
    }

    /**
     * Update name, description, and includes/excludes lists for a group.
     *
     * @param form      form representing the new group configuration
     * @param user      user performing the update
     */
    public void updateGroup(PagsAdHocGroupForm form, IPerson user) {
        logger.debug("Updating group for group form [{}]", form.toString());
        if (!hasPermission(user, IPermission.EDIT_GROUP_ACTIVITY, form.getName())) {
            throw new RuntimeAuthorizationException(user, IPermission.EDIT_GROUP_ACTIVITY, form.getName());
        }
        IPersonAttributesGroupDefinition group = getPagsGroupDefByName(form.getName());
        assert(isManagedAdHocGroup(group));
        group.setName(form.getName());
        group.setDescription(form.getDescription());
        IPersonAttributesGroupTestDefinition test = getPersonAttributesGroupTestDefinition(group);
        test.setIncludes(form.getIncludes());
        test.setExcludes(form.getExcludes());
        assert(isManagedAdHocGroup(group));
        this.pagsGroupDefDao.updatePersonAttributesGroupDefinition(group);
    }

    /**
     * Delete a named group from the group store, checking if the user has permission.
     * 
     * @param groupName     name of the group to be deleted
     * @param user          performing the delete operation
     */
    public void deleteGroup(String groupName, IPerson user) {
        logger.info("Deleting ad hoc PAGS group named {}", groupName);
        if (!hasPermission(user, IPermission.DELETE_GROUP_ACTIVITY, groupName)) {
            throw new RuntimeAuthorizationException(user, IPermission.DELETE_GROUP_ACTIVITY, groupName);
        }
        IPersonAttributesGroupDefinition group = getPagsGroupDefByName(groupName);
        assert(isManagedAdHocGroup(group));
        this.pagsGroupDefDao.deletePersonAttributesGroupDefinition(group);
    }

    /**
     * Check the authorization principal matching the supplied IPerson, permission and target.
     * 
     * @param person        current user to check permission against
     * @param permission    permission name to check
     * @param target        the key of the target
     * @return              {@true} if the person has permission for the checked principal; otherwise, {@code false}
     */
    private boolean hasPermission(IPerson person, String permission, String target) {
        EntityIdentifier ei = person.getEntityIdentifier();
        IAuthorizationPrincipal ap = AuthorizationService.instance().newPrincipal(ei.getKey(), ei.getType());
        return ap.hasPermission(IPermission.PORTAL_GROUPS, permission, target);
    }

    /**
     * Retrieve an implementation of {@code IPersonAttributesGroupDefinition} with the given {@code name} from
     * the DAO bean. There are two assumptions. First, that the DAO handles caching, so caching is not implemented here.
     * Second, that group names are unique. A warning will be logged if more than one group is found with the same name.
     *
     * @param name      group name used to search for group definition
     * @return          {@code IPersonAttributesGroupDefinition} of named group or null
     * @see             IPersonAttributesGroupDefinitionDao#getPersonAttributesGroupDefinitionByName(String)
     * @see             IPersonAttributesGroupDefinition
     */
    private IPersonAttributesGroupDefinition getPagsGroupDefByName(String name) {
       Set<IPersonAttributesGroupDefinition> pagsGroups = pagsGroupDefDao.getPersonAttributesGroupDefinitionByName(name);
       if (pagsGroups.size() > 1) {
           logger.error("More than one PAGS group with name {} found.", name);
       }
       return pagsGroups.isEmpty() ? null : pagsGroups.iterator().next();
    }

    /**
     * Get the embedded test object from the group. Implementation assumes that the group has already been checked for
     * a single ad hoc group test.
     *
     * @param group     group with the test
     * @return          ad hoc group tester
     */
    private IPersonAttributesGroupTestDefinition getPersonAttributesGroupTestDefinition(IPersonAttributesGroupDefinition group) {
        IPersonAttributesGroupTestGroupDefinition testGroup = group.getTestGroups().iterator().next();
        return testGroup.getTests().iterator().next();
    }
}
