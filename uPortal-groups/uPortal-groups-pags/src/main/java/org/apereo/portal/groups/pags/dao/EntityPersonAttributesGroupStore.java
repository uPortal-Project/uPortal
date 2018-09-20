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

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;
import org.apereo.portal.EntityIdentifier;
import org.apereo.portal.groups.EntityImpl;
import org.apereo.portal.groups.EntityTestingGroupImpl;
import org.apereo.portal.groups.GroupsException;
import org.apereo.portal.groups.IEntity;
import org.apereo.portal.groups.IEntityGroup;
import org.apereo.portal.groups.IEntityGroupStore;
import org.apereo.portal.groups.IEntitySearcher;
import org.apereo.portal.groups.IEntityStore;
import org.apereo.portal.groups.IGroupMember;
import org.apereo.portal.groups.ILockableEntityGroup;
import org.apereo.portal.groups.pags.IPersonTester;
import org.apereo.portal.groups.pags.PagsGroup;
import org.apereo.portal.groups.pags.TestGroup;
import org.apereo.portal.security.IPerson;
import org.apereo.portal.security.PersonFactory;
import org.apereo.portal.security.provider.RestrictedPerson;
import org.apereo.portal.spring.locator.ApplicationContextLocator;
import org.apereo.portal.spring.locator.EntityTypesLocator;
import org.apereo.portal.spring.locator.PersonAttributeDaoLocator;
import org.apereo.services.persondir.IPersonAttributeDao;
import org.apereo.services.persondir.IPersonAttributes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;

/**
 * The Person Attributes Group Store uses attributes stored in the IPerson object to determine group
 * membership. It can use attributes from any data source supported by the PersonDirectory service.
 *
 * @since 4.1
 */
public class EntityPersonAttributesGroupStore
        implements IEntityGroupStore, IEntityStore, IEntitySearcher {
    private final Logger logger = LoggerFactory.getLogger(getClass());
    private static final Class<IPerson> IPERSON_CLASS = IPerson.class;
    private static final EntityIdentifier[] EMPTY_SEARCH_RESULTS = new EntityIdentifier[0];
    private IPersonAttributesGroupDefinitionDao personAttributesGroupDefinitionDao;

    /** Caches IEntityGroup (EntityGroupImpl) instances */
    private final Cache entityGroupCache;

    /** Caches PagsGroup instances */
    private final Cache pagsGroupCache;

    /**
     * Caches the result of evaluating a single IGroupMember's (direct) membership in a PAGS group
     */
    private final Cache membershipCache;

    public EntityPersonAttributesGroupStore() {
        super();
        ApplicationContext applicationContext = ApplicationContextLocator.getApplicationContext();
        this.personAttributesGroupDefinitionDao =
                applicationContext.getBean(
                        "personAttributesGroupDefinitionDao",
                        IPersonAttributesGroupDefinitionDao.class);
        CacheManager cacheManager = applicationContext.getBean("cacheManager", CacheManager.class);
        this.entityGroupCache =
                cacheManager.getCache(
                        "org.apereo.portal.groups.pags.dao.EntityPersonAttributesGroupStore.entityGroup");
        this.pagsGroupCache =
                cacheManager.getCache(
                        "org.apereo.portal.groups.pags.dao.EntityPersonAttributesGroupStore.pagsGroup");
        this.membershipCache =
                cacheManager.getCache(
                        "org.apereo.portal.groups.pags.dao.EntityPersonAttributesGroupStore.membership");
    }

    @Override
    public boolean contains(IEntityGroup group, IGroupMember member) {

        /*
         * This method has the potential to be called A LOT, especially if
         * there's a lot of portal data (portlets & groups).  It's important
         * not to waste time on nonsensical checks.
         */

        if (!IPERSON_CLASS.equals(member.getLeafType())) {
            // Maybe this call to contains() shouldn't even happen, since
            // group.getLeafType() is (presumably) IPerson.class.
            return false;
        }

        if (member.isGroup()) {
            // PAGS groups may only contain other PAGS groups (and people, of course)
            final IEntityGroup ieg = (IEntityGroup) member;
            if (!PagsService.SERVICE_NAME_PAGS.equals(ieg.getServiceName().toString())) {
                return false;
            }
        }

        final MembershipCacheKey cacheKey =
                new MembershipCacheKey(
                        group.getEntityIdentifier(), member.getUnderlyingEntityIdentifier());
        Element element = membershipCache.get(cacheKey);
        if (element == null) {

            logger.debug(
                    "Checking if group {} contains member {}/{}",
                    group.getName(),
                    member.getKey(),
                    member.getLeafType().getSimpleName());

            boolean answer = false; // default
            final PagsGroup groupDef = convertEntityToGroupDef(group);
            if (member.isGroup()) {
                final String key = ((IEntityGroup) member).getLocalKey();
                answer = groupDef.hasMember(key);
            } else {
                try {
                    final IPersonAttributeDao pa =
                            PersonAttributeDaoLocator.getPersonAttributeDao();
                    final IPersonAttributes personAttributes = pa.getPerson(member.getKey());

                    if (personAttributes != null) {
                        final RestrictedPerson rp = PersonFactory.createRestrictedPerson();
                        rp.setAttributes(personAttributes.getAttributes());
                        answer = groupDef.contains(rp);
                    }
                } catch (Exception ex) {
                    logger.error(
                            "Exception acquiring attributes for member "
                                    + member
                                    + " while checking if group "
                                    + group
                                    + " contains this member.",
                            ex);
                    return false;
                }
            }
            element = new Element(cacheKey, answer);
            membershipCache.put(element);
        }
        logger.debug(
                "Answering if group {} contains member {}/{} : {}",
                group.getName(),
                member.getKey(),
                member.getLeafType().getSimpleName(),
                (Boolean) element.getObjectValue());
        return (Boolean) element.getObjectValue();
    }

    private PagsGroup convertEntityToGroupDef(IEntityGroup group) {
        IPersonAttributesGroupDefinition pagsGroup = getPagsGroupDefByName(group.getName());
        return initGroupDef(pagsGroup);
    }

    private IEntityGroup convertPagsGroupToEntity(IPersonAttributesGroupDefinition group) {

        final String cacheKey = group.getName();
        Element element = entityGroupCache.get(cacheKey);

        if (element == null) {
            final IEntityGroup entityGroup =
                    new EntityTestingGroupImpl(group.getName(), IPERSON_CLASS);
            entityGroup.setName(group.getName());
            entityGroup.setDescription(group.getDescription());
            element = new Element(cacheKey, entityGroup);
            entityGroupCache.put(element);
        }

        return (IEntityGroup) element.getObjectValue();
    }

    @Override
    public void delete(IEntityGroup group) throws GroupsException {
        throw new UnsupportedOperationException(
                "EntityPersonAttributesGroupStore: Method delete() not supported.");
    }

    @Override
    public IEntityGroup find(String name) throws GroupsException {
        Set<IPersonAttributesGroupDefinition> groups =
                personAttributesGroupDefinitionDao.getPersonAttributesGroupDefinitionByName(name);
        if (groups.size() == 0) {
            logger.error(
                    "No PAGS group with name {} found. Check your PAGS group definitions for possible error"
                            + " in member group name",
                    name);
            return null;
        }
        IPersonAttributesGroupDefinition pagsGroup = groups.iterator().next();
        return convertPagsGroupToEntity(pagsGroup);
    }

    @Override
    public Iterator<IEntityGroup> findParentGroups(IGroupMember member) throws GroupsException {

        /*
         * This method has the potential to be called A LOT, especially if
         * there's a lot of portal data (portlets & groups).  It's important
         * not to waste time on nonsensical checks.
         */

        if (!IPERSON_CLASS.equals(member.getLeafType())) {
            // This is going to happen;  GaP code is not responsible for
            // knowing that PAGS only supports groups of IPerson (we are).
            return Collections.emptyIterator();
        }

        logger.debug("finding containing groups for member key {}", member.getKey());

        final Set<IEntityGroup> set = Collections.emptySet();
        Iterator<IEntityGroup> rslt = set.iterator(); // default

        if (member.isGroup()) {
            // PAGS groups may only contain other PAGS groups (and people, of course)
            final IEntityGroup ieg = (IEntityGroup) member;
            if (PagsService.SERVICE_NAME_PAGS.equals(ieg.getServiceName().toString())) {
                rslt = findParentGroupsForGroup((IEntityGroup) member);
            }
        } else {
            rslt = findParentGroupsForEntity((IEntity) member);
        }

        return rslt;
    }

    private Iterator<IEntityGroup> findParentGroupsForGroup(IEntityGroup group) {
        logger.debug(
                "Finding containing groups for group {} (key {})", group.getName(), group.getKey());
        Set<IEntityGroup> parents = getParentGroups(group.getName(), new HashSet<IEntityGroup>());
        return parents.iterator();
    }

    private Iterator<IEntityGroup> findParentGroupsForEntity(IEntity member)
            throws GroupsException {

        Set<IPersonAttributesGroupDefinition> pagsGroups =
                personAttributesGroupDefinitionDao.getPersonAttributesGroupDefinitions();
        List<IEntityGroup> results = new ArrayList<IEntityGroup>();
        for (IPersonAttributesGroupDefinition pagsGroup : pagsGroups) {
            IEntityGroup group = convertPagsGroupToEntity(pagsGroup);
            if (contains(group, member)) {
                results.add(group);
            }
        }
        return results.iterator();
    }

    @Override
    public Iterator<IEntityGroup> findEntitiesForGroup(IEntityGroup group) throws GroupsException {
        // PAGS groups are synthetic;  we don't support this behavior.
        return Collections.emptyIterator();
    }

    @Override
    public ILockableEntityGroup findLockable(String key) throws GroupsException {
        throw new UnsupportedOperationException(
                "EntityPersonAttributesGroupStore: Method findLockable() not supported");
    }

    @Override
    public String[] findMemberGroupKeys(IEntityGroup group) throws GroupsException {

        List<String> keys = new ArrayList<String>();
        PagsGroup groupDef =
                convertEntityToGroupDef(
                        group); // Will prevent wasting time on non-PAGS groups, if those calls even
        // happen
        if (groupDef != null) {
            for (Iterator<String> i = groupDef.getMembers().iterator(); i.hasNext(); ) {
                keys.add(i.next());
            }
        }
        return keys.toArray(new String[] {});
    }

    @Override
    public Iterator<IEntityGroup> findMemberGroups(IEntityGroup group) throws GroupsException {

        /*
         * The GaP system prevents this method from being called with a nn-PAGS group.
         */

        IPersonAttributesGroupDefinition pagsGroup = getPagsGroupDefByName(group.getName());
        List<IEntityGroup> results = new ArrayList<IEntityGroup>();
        for (IPersonAttributesGroupDefinition member : pagsGroup.getMembers()) {
            results.add(convertPagsGroupToEntity(member));
        }
        return results.iterator();
    }

    @Override
    public IEntityGroup newInstance(Class entityType) throws GroupsException {
        throw new UnsupportedOperationException(
                "EntityPersonAttributesGroupStore: Method newInstance() not supported");
    }

    @Override
    public EntityIdentifier[] searchForGroups(String query, SearchMethod method, Class leaftype)
            throws GroupsException {
        if (leaftype != IPERSON_CLASS) {
            return EMPTY_SEARCH_RESULTS;
        }
        Set<IPersonAttributesGroupDefinition> pagsGroups =
                personAttributesGroupDefinitionDao.getPersonAttributesGroupDefinitions();
        List<EntityIdentifier> results = new ArrayList<EntityIdentifier>();
        switch (method) {
            case DISCRETE:
                for (IPersonAttributesGroupDefinition pagsGroup : pagsGroups) {
                    IEntityGroup g = convertPagsGroupToEntity(pagsGroup);
                    if (g.getName().equals(query)) {
                        results.add(g.getEntityIdentifier());
                    }
                }
                break;
            case DISCRETE_CI:
                for (IPersonAttributesGroupDefinition pagsGroup : pagsGroups) {
                    IEntityGroup g = convertPagsGroupToEntity(pagsGroup);
                    if (g.getName().equalsIgnoreCase(query)) {
                        results.add(g.getEntityIdentifier());
                    }
                }
                break;
            case STARTS_WITH:
                for (IPersonAttributesGroupDefinition pagsGroup : pagsGroups) {
                    IEntityGroup g = convertPagsGroupToEntity(pagsGroup);
                    if (g.getName().startsWith(query)) {
                        results.add(g.getEntityIdentifier());
                    }
                }
                break;
            case STARTS_WITH_CI:
                for (IPersonAttributesGroupDefinition pagsGroup : pagsGroups) {
                    IEntityGroup g = convertPagsGroupToEntity(pagsGroup);
                    if (g.getName().toUpperCase().startsWith(query.toUpperCase())) {
                        results.add(g.getEntityIdentifier());
                    }
                }
                break;
            case ENDS_WITH:
                for (IPersonAttributesGroupDefinition pagsGroup : pagsGroups) {
                    IEntityGroup g = convertPagsGroupToEntity(pagsGroup);
                    if (g.getName().endsWith(query)) {
                        results.add(g.getEntityIdentifier());
                    }
                }
                break;
            case ENDS_WITH_CI:
                for (IPersonAttributesGroupDefinition pagsGroup : pagsGroups) {
                    IEntityGroup g = convertPagsGroupToEntity(pagsGroup);
                    if (g.getName().toUpperCase().endsWith(query.toUpperCase())) {
                        results.add(g.getEntityIdentifier());
                    }
                }
                break;
            case CONTAINS:
                for (IPersonAttributesGroupDefinition pagsGroup : pagsGroups) {
                    IEntityGroup g = convertPagsGroupToEntity(pagsGroup);
                    if (g.getName().indexOf(query) != -1) {
                        results.add(g.getEntityIdentifier());
                    }
                }
                break;
            case CONTAINS_CI:
                for (IPersonAttributesGroupDefinition pagsGroup : pagsGroups) {
                    IEntityGroup g = convertPagsGroupToEntity(pagsGroup);
                    if (g.getName().toUpperCase().indexOf(query.toUpperCase()) != -1) {
                        results.add(g.getEntityIdentifier());
                    }
                }
                break;
        }
        return results.toArray(new EntityIdentifier[] {});
    }

    @Override
    public void update(IEntityGroup group) throws GroupsException {
        throw new UnsupportedOperationException(
                "EntityPersonAttributesGroupStore: Method update() not supported.");
    }

    @Override
    public void updateMembers(IEntityGroup group) throws GroupsException {
        throw new UnsupportedOperationException(
                "EntityPersonAttributesGroupStore: Method updateMembers() not supported.");
    }

    @Override
    public IEntity newInstance(String key, Class type) throws GroupsException {
        /*
         * NOTE:  It seems like something should be done to prevent emitting
         * nonsense entities;  it's not clear what that would be.
         */
        if (EntityTypesLocator.getEntityTypes().getEntityIDFromType(type) == null) {
            throw new GroupsException("Invalid entity type: " + type.getName());
        }
        return new EntityImpl(key, type);
    }

    @Override
    public EntityIdentifier[] searchForEntities(String query, SearchMethod method, Class type)
            throws GroupsException {
        return EMPTY_SEARCH_RESULTS;
    }

    private PagsGroup initGroupDef(IPersonAttributesGroupDefinition group) {
        Element element = this.pagsGroupCache.get(group.getName());
        if (element != null) {
            return (PagsGroup) element.getObjectValue();
        }
        PagsGroup groupDef = new PagsGroup();
        groupDef.setKey(group.getName());
        groupDef.setName(group.getName());
        groupDef.setDescription(group.getDescription());
        addMemberKeys(groupDef, group.getMembers());
        Set<IPersonAttributesGroupTestGroupDefinition> testGroups = group.getTestGroups();
        for (IPersonAttributesGroupTestGroupDefinition testGroup : testGroups) {
            TestGroup tg = new TestGroup();
            Set<IPersonAttributesGroupTestDefinition> tests = testGroup.getTests();
            for (IPersonAttributesGroupTestDefinition test : tests) {
                IPersonTester testerInst = initializeTester(test);
                if (testerInst == null) {
                    /*
                     * A tester was intended that we cannot now recreate.  This
                     * is a potentially dangerous situation, since tests in PAGS
                     * are "or-ed" together;  a functioning group with a missing
                     * test would have a wider membership, not narrower.  (And
                     * remember -- permissions are tied to groups.)  We need to
                     * play it safe and keep this group out of the mix.
                     */
                    return null;
                }
                tg.addTest(testerInst);
            }
            groupDef.addTestGroup(tg);
        }
        element = new Element(group.getName(), groupDef);
        this.pagsGroupCache.put(element);
        return groupDef;
    }

    private void addMemberKeys(PagsGroup groupDef, Set<IPersonAttributesGroupDefinition> members) {
        for (IPersonAttributesGroupDefinition member : members) {
            groupDef.addMember(member.getName());
        }
    }

    private IPersonTester initializeTester(IPersonAttributesGroupTestDefinition test) {
        try {
            Class<?> testerClass = Class.forName(test.getTesterClassName());
            Constructor<?> c =
                    testerClass.getConstructor(IPersonAttributesGroupTestDefinition.class);
            Object o = c.newInstance(test);
            return (IPersonTester) o;
        } catch (Exception e) {
            logger.error("Error in initializing tester class: {}", test.getTesterClassName(), e);
            return null;
        }
    }

    private Set<IEntityGroup> getParentGroups(String name, Set<IEntityGroup> groups)
            throws GroupsException {
        logger.debug("Looking up containing groups for {}", name);
        IPersonAttributesGroupDefinition pagsGroup = getPagsGroupDefByName(name);
        Set<IPersonAttributesGroupDefinition> pagsParentGroups =
                personAttributesGroupDefinitionDao.getParentPersonAttributesGroupDefinitions(
                        pagsGroup);
        for (IPersonAttributesGroupDefinition pagsParent : pagsParentGroups) {
            IEntityGroup parent = convertPagsGroupToEntity(pagsParent);
            if (!groups.contains(parent)) {
                groups.add(parent);
                getParentGroups(pagsParent.getName(), groups);
            } else {
                throw new RuntimeException(
                        "Recursive grouping detected! for "
                                + name
                                + " and parent "
                                + pagsParent.getName());
            }
        }
        return groups;
    }

    /**
     * Retrieve an implementation of {@code IPersonAttributesGroupDefinition} with the given {@code
     * name} from the JPA DAO. There are two assumptions. First, that the DAO handles caching, so
     * caching is not implemented here. Second, that group names are unique. A warning will be
     * logged if more than one group is found with the same name.
     *
     * @param name group name used to search for group definition
     * @return {@code IPersonAttributesGroupDefinition} of named group or null
     * @see IPersonAttributesGroupDefinitionDao#getPersonAttributesGroupDefinitionByName(String)
     * @see IPersonAttributesGroupDefinition
     */
    private IPersonAttributesGroupDefinition getPagsGroupDefByName(String name) {
        Set<IPersonAttributesGroupDefinition> pagsGroups =
                personAttributesGroupDefinitionDao.getPersonAttributesGroupDefinitionByName(name);
        if (pagsGroups.size() > 1) {
            logger.error("More than one PAGS group with name {} found.", name);
        }
        final IPersonAttributesGroupDefinition rslt =
                pagsGroups.isEmpty() ? null : pagsGroups.iterator().next();
        return rslt;
    }
}
