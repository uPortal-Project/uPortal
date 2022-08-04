package org.apereo.portal.services.portletlist;

import org.apereo.portal.dao.portletlist.IPortletListDao;
import org.apereo.portal.dao.portletlist.IPortletList;
import org.apereo.portal.security.IPerson;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

import lombok.extern.slf4j.Slf4j;
/**
 * Service layer that sits atop the DAO layer and enforces permissions and/or business rules that
 * apply to CRUD operations on portlet lists. External clients should interact with this service
 * -- instead of the DAOs directly -- whenever the actions are undertaken on behalf of a specific
 * user.
 */
@Service
@Slf4j
public final class PortletListService implements IPortletListService {

    @Autowired
    private IPortletListDao portletListDao;

    /**
     * All the definitions, filtered by the user's access rights.
     *
     * @param person
     * @return
     */
    @Override
    public List<IPortletList> getPortletLists(IPerson person) {
        List<IPortletList> rslt = portletListDao.getPortletLists("" + person.getID());
//        for (IPortletList pList :
//            portletListDao.getPortletLists("" + person.getID())) {
            // TODO - getPortletLists only returns lists for that user, so no permissions needed... however - need to implement admin access
//            if (hasPermission(
//                person,
//                IPermission.VIEW_GROUP_ACTIVITY,
//                def.getCompositeEntityIdentifierForGroup().getKey())) {
//                rslt.add(def);
//            }
//            rslt.add(pList);
//        }
        log.debug("Returning portlet lists '{}' for user '{}'", rslt, person.getUserName());
        return rslt;
    }

    /**
     * All the definitions, filtered by the user's access rights.
     *
     * @param person
     * @return
     */
    @Override
    public IPortletList getPortletList(IPerson person, String portletListUuid) {
        IPortletList rslt = portletListDao.getPortletList("" + person.getID(), portletListUuid);
        log.debug("Returning portlet list '{}' for user '{}'", rslt, person.getUserName());
        return rslt;
    }

    /** TODO docs Verifies permissions and that the group doesn't already exist (case insensitive) */

    @Override
    public IPortletList createPortletList(IPerson owner, IPortletList toCreate) {
//        // What's the target of the upcoming permissions check?
//        String target =
//            parent != null
//                ? parent.getEntityIdentifier().getKey()
//                : IPermission
//                .ALL_GROUPS_TARGET; // Must have blanket permission to create one
//        // w/o a parent
//
//        // Verify permission
//        if (!hasPermission(person, IPermission.CREATE_GROUP_ACTIVITY, target)) {
//            throw new RuntimeAuthorizationException(
//                person, IPermission.CREATE_GROUP_ACTIVITY, target);
//        }
//
//        // VALIDATION STEP:  The group name & description are allowable
//        if (StringUtils.isBlank(groupName)) {
//            throw new IllegalArgumentException("Specified groupName is blank:  " + groupName);
//        }
//        if (!GROUP_NAME_VALIDATOR_PATTERN.matcher(groupName).matches()) {
//            throw new IllegalArgumentException(
//                "Specified groupName is too long, too short, or contains invalid characters:  "
//                    + groupName);
//        }
//        if (!StringUtils.isBlank(description)) { // Blank description is allowable
//            if (!GROUP_DESC_VALIDATOR_PATTERN.matcher(description).matches()) {
//                throw new IllegalArgumentException(
//                    "Specified description is too long or contains invalid characters:  "
//                        + description);
//            }
//        }
//
//        // VALIDATION STEP:  We don't have a group by that name already
//        EntityIdentifier[] people =
//            GroupService.searchForGroups(
//                groupName, IGroupConstants.SearchMethod.DISCRETE_CI, IPerson.class);
//        EntityIdentifier[] portlets =
//            GroupService.searchForGroups(
//                groupName,
//                IGroupConstants.SearchMethod.DISCRETE_CI,
//                IPortletDefinition.class);
//        if (people.length != 0 || portlets.length != 0) {
//            throw new IllegalArgumentException("Specified groupName already in use:  " + groupName);
//        }
        log.debug("Using DAO to create portlet list [{}] for user [{}]", toCreate.getName(), toCreate.getUserId());
        return portletListDao.createPortletList(toCreate);
    }
//
//    /**
//     * Returns the specified definitions, provided (1) it exists and (2) the user may view it.
//     *
//     * @param person
//     * @return
//     */
//    public IPersonAttributesGroupDefinition getPagsDefinitionByName(IPerson person, String name) {
//        IPersonAttributesGroupDefinition rslt = getPagsGroupDefByName(name);
//        if (rslt == null) {
//            // Better to produce exception?  I'm thinking not, but open-minded.
//            return null;
//        }
//        if (!hasPermission(
//            person,
//            IPermission.VIEW_GROUP_ACTIVITY,
//            rslt.getCompositeEntityIdentifierForGroup().getKey())) {
//            throw new RuntimeAuthorizationException(person, IPermission.VIEW_GROUP_ACTIVITY, name);
//        }
//        logger.debug("Returning PAGS definition '{}' for user '{}'", rslt, person.getUserName());
//        return rslt;
//    }
//
//    /** Verifies permissions and that the group doesn't already exist (case insensitive) */
//    public IPersonAttributesGroupDefinition createPagsDefinition(
//        IPerson person, IEntityGroup parent, String groupName, String description) {
//
//        // What's the target of the upcoming permissions check?
//        String target =
//            parent != null
//                ? parent.getEntityIdentifier().getKey()
//                : IPermission
//                .ALL_GROUPS_TARGET; // Must have blanket permission to create one
//        // w/o a parent
//
//        // Verify permission
//        if (!hasPermission(person, IPermission.CREATE_GROUP_ACTIVITY, target)) {
//            throw new RuntimeAuthorizationException(
//                person, IPermission.CREATE_GROUP_ACTIVITY, target);
//        }
//
//        // VALIDATION STEP:  The group name & description are allowable
//        if (StringUtils.isBlank(groupName)) {
//            throw new IllegalArgumentException("Specified groupName is blank:  " + groupName);
//        }
//        if (!GROUP_NAME_VALIDATOR_PATTERN.matcher(groupName).matches()) {
//            throw new IllegalArgumentException(
//                "Specified groupName is too long, too short, or contains invalid characters:  "
//                    + groupName);
//        }
//        if (!StringUtils.isBlank(description)) { // Blank description is allowable
//            if (!GROUP_DESC_VALIDATOR_PATTERN.matcher(description).matches()) {
//                throw new IllegalArgumentException(
//                    "Specified description is too long or contains invalid characters:  "
//                        + description);
//            }
//        }
//
//        // VALIDATION STEP:  We don't have a group by that name already
//        EntityIdentifier[] people =
//            GroupService.searchForGroups(
//                groupName, IGroupConstants.SearchMethod.DISCRETE_CI, IPerson.class);
//        EntityIdentifier[] portlets =
//            GroupService.searchForGroups(
//                groupName,
//                IGroupConstants.SearchMethod.DISCRETE_CI,
//                IPortletDefinition.class);
//        if (people.length != 0 || portlets.length != 0) {
//            throw new IllegalArgumentException("Specified groupName already in use:  " + groupName);
//        }
//
//        IPersonAttributesGroupDefinition rslt =
//            pagsGroupDefDao.createPersonAttributesGroupDefinition(groupName, description);
//        if (parent != null) {
//            // Should refactor this switch to instead choose a service and invoke a method on it
//            switch (parent.getServiceName().toString()) {
//                case SERVICE_NAME_LOCAL:
//                    IEntityGroup member =
//                        GroupService.findGroup(
//                            rslt.getCompositeEntityIdentifierForGroup().getKey());
//                    if (member == null) {
//                        String msg =
//                            "The specified group was created, but is not present in the store:  "
//                                + rslt.getName();
//                        throw new RuntimeException(msg);
//                    }
//                    parent.addChild(member);
//                    parent.updateMembers();
//                    break;
//                case SERVICE_NAME_PAGS:
//                    IPersonAttributesGroupDefinition parentDef =
//                        getPagsGroupDefByName(parent.getName());
//                    Set<IPersonAttributesGroupDefinition> members =
//                        new HashSet<>(parentDef.getMembers());
//                    members.add(rslt);
//                    parentDef.setMembers(members);
//                    pagsGroupDefDao.updatePersonAttributesGroupDefinition(parentDef);
//                    break;
//                default:
//                    String msg =
//                        "The specified group service does not support adding members:  "
//                            + parent.getServiceName();
//                    throw new UnsupportedOperationException(msg);
//            }
//        }
//
//        return rslt;
//    }
//
//    /** NOTE -- This method assumes that pagsDef is an existing JPA-managed entity. */
//    public IPersonAttributesGroupDefinition updatePagsDefinition(
//        IPerson person, IPersonAttributesGroupDefinition pagsDef) {
//
//        // Verify permission
//        if (!hasPermission(
//            person,
//            IPermission.EDIT_GROUP_ACTIVITY,
//            pagsDef.getCompositeEntityIdentifierForGroup().getKey())) {
//            throw new RuntimeAuthorizationException(
//                person,
//                IPermission.EDIT_GROUP_ACTIVITY,
//                pagsDef.getCompositeEntityIdentifierForGroup().getKey());
//        }
//
//        IPersonAttributesGroupDefinition rslt =
//            pagsGroupDefDao.updatePersonAttributesGroupDefinition(pagsDef);
//        return rslt;
//    }
//
//    /** NOTE -- This method assumes that pagsDef is an existing JPA-managed entity. */
//    public void deletePagsDefinition(IPerson person, IPersonAttributesGroupDefinition pagsDef) {
//
//        // Verify permission
//        if (!hasPermission(
//            person,
//            IPermission.DELETE_GROUP_ACTIVITY,
//            pagsDef.getCompositeEntityIdentifierForGroup().getKey())) {
//            throw new RuntimeAuthorizationException(
//                person,
//                IPermission.DELETE_GROUP_ACTIVITY,
//                pagsDef.getCompositeEntityIdentifierForGroup().getKey());
//        }
//
//        pagsGroupDefDao.deletePersonAttributesGroupDefinition(pagsDef);
//    }
//
//    /*
//     * Implementation
//     */
//
//    private boolean hasPermission(IPerson person, String permission, String target) {
//        EntityIdentifier ei = person.getEntityIdentifier();
//        IAuthorizationPrincipal ap =
//            AuthorizationServiceFacade.instance().newPrincipal(ei.getKey(), ei.getType());
//        return ap.hasPermission(IPermission.PORTAL_GROUPS, permission, target);
//    }
//
//    private IPersonAttributesGroupDefinition getPagsGroupDefByName(String name) {
//        Set<IPersonAttributesGroupDefinition> pagsGroups =
//            pagsGroupDefDao.getPersonAttributesGroupDefinitionByName(name);
//        if (pagsGroups.size() > 1) {
//            logger.error("More than one PAGS group with name {} found.", name);
//        }
//        return pagsGroups.isEmpty() ? null : pagsGroups.iterator().next();
//    }
}
