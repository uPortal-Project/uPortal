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
package org.apereo.portal.groups.pags.dao;

import static org.apereo.portal.groups.pags.dao.jpa.LocalGroupService.SERVICE_NAME_LOCAL;
import static org.apereo.portal.groups.pags.dao.jpa.PagsGroupService.SERVICE_NAME_PAGS;

import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;
import org.apache.commons.lang3.StringUtils;
import org.apereo.portal.EntityIdentifier;
import org.apereo.portal.groups.IEntityGroup;
import org.apereo.portal.groups.IGroupConstants;
import org.apereo.portal.groups.pags.dao.jpa.LocalGroupService;
import org.apereo.portal.groups.pags.dao.jpa.PagsGroupService;
import org.apereo.portal.portlet.om.IPortletDefinition;
import org.apereo.portal.security.IAuthorizationPrincipal;
import org.apereo.portal.security.IPermission;
import org.apereo.portal.security.IPerson;
import org.apereo.portal.security.RuntimeAuthorizationException;
import org.apereo.portal.services.AuthorizationServiceFacade;
import org.apereo.portal.services.GroupService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Service layer that sits atop the DAO layer and enforces permissions and/or business rules that
 * apply to CRUD operations on PAGS definitions. External clients should interact with this service
 * -- instead of the DAOs directly -- whenever the actions are undertaken on behalf of a specific
 * user.
 */
@Service
public final class PagsService {

    @Autowired private PagsGroupService pagsGroupService;
    @Autowired private LocalGroupService localGroupService;

    private static final String GROUP_NAME_VALIDATOR_REGEX = "^[\\w ]{5,500}$"; // 5-500 characters
    private static final Pattern GROUP_NAME_VALIDATOR_PATTERN =
            Pattern.compile(GROUP_NAME_VALIDATOR_REGEX);
    private static final String GROUP_DESC_VALIDATOR_REGEX =
            "^[\\w ,\\.\\(\\)]{0,500}$"; // 0-500 characters
    private static final Pattern GROUP_DESC_VALIDATOR_PATTERN =
            Pattern.compile(GROUP_DESC_VALIDATOR_REGEX);

    @Autowired private IPersonAttributesGroupDefinitionDao pagsGroupDefDao;

    private final Logger logger = LoggerFactory.getLogger(getClass());

    /**
     * All the definitions, filtered by the user's access rights.
     *
     * @param person
     * @return
     */
    public Set<IPersonAttributesGroupDefinition> getPagsDefinitions(IPerson person) {
        Set<IPersonAttributesGroupDefinition> result = new HashSet<>();
        for (IPersonAttributesGroupDefinition def :
                pagsGroupDefDao.getPersonAttributesGroupDefinitions()) {
            if (hasPermission(
                    person,
                    IPermission.VIEW_GROUP_ACTIVITY,
                    def.getCompositeEntityIdentifierForGroup().getKey())) {
                result.add(def);
            }
        }
        logger.debug("Returning PAGS definitions '{}' for user '{}'", result, person.getUserName());
        return result;
    }

    /**
     * Returns the specified definitions, provided (1) it exists and (2) the user may view it.
     *
     * @param person
     * @return
     */
    public IPersonAttributesGroupDefinition getPagsDefinitionByName(IPerson person, String name) {
        IPersonAttributesGroupDefinition result = getPagsGroupDefByName(name);
        if (result == null) {
            // Better to produce exception?  I'm thinking not, but open-minded.
            return null;
        }
        if (!hasPermission(
                person,
                IPermission.VIEW_GROUP_ACTIVITY,
                result.getCompositeEntityIdentifierForGroup().getKey())) {
            throw new RuntimeAuthorizationException(person, IPermission.VIEW_GROUP_ACTIVITY, name);
        }
        logger.debug("Returning PAGS definition '{}' for user '{}'", result, person.getUserName());
        return result;
    }

    /** Verifies permissions and that the group doesn't already exist (case insensitive) */
    public IPersonAttributesGroupDefinition createPagsDefinition(
            IPerson person, IEntityGroup parent, String groupName, String description) {

        // What's the target of the upcoming permissions check?
        String target =
                parent != null
                        ? parent.getEntityIdentifier().getKey()
                        : IPermission
                                .ALL_GROUPS_TARGET; // Must have blanket permission to create one
        // w/o a parent

        // Verify permission
        if (!hasPermission(person, IPermission.CREATE_GROUP_ACTIVITY, target)) {
            throw new RuntimeAuthorizationException(
                    person, IPermission.CREATE_GROUP_ACTIVITY, target);
        }

        // VALIDATION STEP:  The group name & description are allowable
        if (StringUtils.isBlank(groupName)) {
            throw new IllegalArgumentException("Specified groupName is blank:  " + groupName);
        }
        if (!GROUP_NAME_VALIDATOR_PATTERN.matcher(groupName).matches()) {
            throw new IllegalArgumentException(
                    "Specified groupName is too long, too short, or contains invalid characters:  "
                            + groupName);
        }
        if (!StringUtils.isBlank(description)) { // Blank description is allowable
            if (!GROUP_DESC_VALIDATOR_PATTERN.matcher(description).matches()) {
                throw new IllegalArgumentException(
                        "Specified description is too long or contains invalid characters:  "
                                + description);
            }
        }

        // VALIDATION STEP:  We don't have a group by that name already
        EntityIdentifier[] people =
                GroupService.searchForGroups(
                        groupName, IGroupConstants.SearchMethod.DISCRETE_CI, IPerson.class);
        EntityIdentifier[] portlets =
                GroupService.searchForGroups(
                        groupName,
                        IGroupConstants.SearchMethod.DISCRETE_CI,
                        IPortletDefinition.class);
        if (people.length != 0 || portlets.length != 0) {
            throw new IllegalArgumentException("Specified groupName already in use:  " + groupName);
        }

        IPersonAttributesGroupDefinition result =
                pagsGroupDefDao.createPersonAttributesGroupDefinition(groupName, description);
        if (parent != null) {

            switch (parent.getServiceName().toString()) {
                case SERVICE_NAME_LOCAL:
                    localGroupService.addMember(parent, result);
                    break;
                case SERVICE_NAME_PAGS:
                    IPersonAttributesGroupDefinition parentDef =
                            getPagsGroupDefByName(parent.getName());
                    pagsGroupService.addMember(parentDef, result);
                    break;
                default:
                    String msg =
                            "The specified group service does not support adding members:  "
                                    + parent.getServiceName();
                    throw new UnsupportedOperationException(msg);
            }
        }

        return result;
    }

    /** NOTE -- This method assumes that pagsDef is an existing JPA-managed entity. */
    public IPersonAttributesGroupDefinition updatePagsDefinition(
            IPerson person, IPersonAttributesGroupDefinition pagsDef) {

        // Verify permission
        if (!hasPermission(
                person,
                IPermission.EDIT_GROUP_ACTIVITY,
                pagsDef.getCompositeEntityIdentifierForGroup().getKey())) {
            throw new RuntimeAuthorizationException(
                    person,
                    IPermission.EDIT_GROUP_ACTIVITY,
                    pagsDef.getCompositeEntityIdentifierForGroup().getKey());
        }

        IPersonAttributesGroupDefinition result =
                pagsGroupDefDao.updatePersonAttributesGroupDefinition(pagsDef);
        return result;
    }

    /** NOTE -- This method assumes that pagsDef is an existing JPA-managed entity. */
    public void deletePagsDefinition(IPerson person, IPersonAttributesGroupDefinition pagsDef) {

        // Verify permission
        if (!hasPermission(
                person,
                IPermission.DELETE_GROUP_ACTIVITY,
                pagsDef.getCompositeEntityIdentifierForGroup().getKey())) {
            throw new RuntimeAuthorizationException(
                    person,
                    IPermission.DELETE_GROUP_ACTIVITY,
                    pagsDef.getCompositeEntityIdentifierForGroup().getKey());
        }

        pagsGroupDefDao.deletePersonAttributesGroupDefinition(pagsDef);
    }

    /*
     * Implementation
     */

    private boolean hasPermission(IPerson person, String permission, String target) {
        EntityIdentifier ei = person.getEntityIdentifier();
        IAuthorizationPrincipal ap =
                AuthorizationServiceFacade.instance().newPrincipal(ei.getKey(), ei.getType());
        return ap.hasPermission(IPermission.PORTAL_GROUPS, permission, target);
    }

    private IPersonAttributesGroupDefinition getPagsGroupDefByName(String name) {
        Set<IPersonAttributesGroupDefinition> pagsGroups =
                pagsGroupDefDao.getPersonAttributesGroupDefinitionByName(name);
        if (pagsGroups.size() > 1) {
            logger.error("More than one PAGS group with name {} found.", name);
        }
        return pagsGroups.isEmpty() ? null : pagsGroups.iterator().next();
    }
}
