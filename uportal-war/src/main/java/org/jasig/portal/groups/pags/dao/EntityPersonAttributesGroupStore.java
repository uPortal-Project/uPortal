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
package org.jasig.portal.groups.pags.dao;

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
import org.jasig.portal.EntityIdentifier;
import org.jasig.portal.EntityTypes;
import org.jasig.portal.groups.EntityImpl;
import org.jasig.portal.groups.EntityTestingGroupImpl;
import org.jasig.portal.groups.GroupsException;
import org.jasig.portal.groups.IEntity;
import org.jasig.portal.groups.IEntityGroup;
import org.jasig.portal.groups.IEntityGroupStore;
import org.jasig.portal.groups.IEntitySearcher;
import org.jasig.portal.groups.IEntityStore;
import org.jasig.portal.groups.IGroupMember;
import org.jasig.portal.groups.ILockableEntityGroup;
import org.jasig.portal.groups.pags.PagsGroup;
import org.jasig.portal.groups.pags.IPersonTester;
import org.jasig.portal.groups.pags.TestGroup;
import org.jasig.portal.security.IPerson;
import org.jasig.portal.security.PersonFactory;
import org.jasig.portal.security.provider.RestrictedPerson;
import org.jasig.portal.spring.locator.ApplicationContextLocator;
import org.jasig.portal.spring.locator.PersonAttributeDaoLocator;
import org.jasig.services.persondir.IPersonAttributeDao;
import org.jasig.services.persondir.IPersonAttributes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;

/**
 * The Person Attributes Group Store uses attributes stored in the IPerson object to determine
 * group membership.  It can use attributes from any data source supported by the PersonDirectory
 * service.
 *
 * @author Shawn Connolly, sconnolly@unicon.net
 * @since 4.1
 */
public class EntityPersonAttributesGroupStore implements IEntityGroupStore, IEntityStore, IEntitySearcher {
    protected final Logger logger = LoggerFactory.getLogger(getClass());
    private static final Class<IPerson> IPERSON_CLASS = IPerson.class;
    private static final EntityIdentifier[] EMPTY_SEARCH_RESULTS = new EntityIdentifier[0];
    private IPersonAttributesGroupDefinitionDao personAttributesGroupDefinitionDao;
    private final Cache groupDefCache;

    public EntityPersonAttributesGroupStore() {
        super();
        ApplicationContext applicationContext = ApplicationContextLocator.getApplicationContext();
        this.personAttributesGroupDefinitionDao = applicationContext.getBean("personAttributesGroupDefinitionDao", IPersonAttributesGroupDefinitionDao.class);
        CacheManager cacheManager = applicationContext.getBean("cacheManager", CacheManager.class);
        this.groupDefCache = cacheManager.getCache("org.jasig.portal.groups.pags.dao.EntityPersonAttributesGroupStore");
    }

    public boolean contains(IEntityGroup group, IGroupMember member) {
        logger.debug("Checking if group {} contains member {}/{}", group.getName(), member.getKey(), member.getEntityType().getSimpleName());
        PagsGroup groupDef = convertEntityToGroupDef(group);
        if (member.isGroup())
        {
           String key = ((IEntityGroup)member).getLocalKey();
           return groupDef.hasMember(key);
        }
        else
        {
           if (member.getEntityType() != IPERSON_CLASS)
               { return false; }
           IPerson person = null;
           try {
               IPersonAttributeDao pa = PersonAttributeDaoLocator.getPersonAttributeDao();
               final IPersonAttributes personAttributes = pa.getPerson(member.getKey());

               RestrictedPerson rp = PersonFactory.createRestrictedPerson();
               if (personAttributes != null) {
                   rp.setAttributes(personAttributes.getAttributes());
               }

               person = rp;
           }
           catch (Exception ex) {
               logger.error("Exception acquiring attributes for member " + member + " while checking if group " + group + " contains this member.", ex);
               return false;
           }
           return testRecursively(groupDef, person, member);
        }
    }

    private PagsGroup convertEntityToGroupDef(IEntityGroup group) {
        IPersonAttributesGroupDefinition pagsGroup = getPagsGroupDefByName(group.getName());
        return initGroupDef(pagsGroup);
    }

    private IEntityGroup convertPagsGroupToEntity(IPersonAttributesGroupDefinition group) {
        IEntityGroup entityGroup = new EntityTestingGroupImpl(group.getName(), IPERSON_CLASS);
        entityGroup.setName(group.getName());
        entityGroup.setDescription(group.getDescription());
        return entityGroup;
    }

    public void delete(IEntityGroup group) throws GroupsException {
        throw new UnsupportedOperationException("EntityPersonAttributesGroupStore: Method delete() not supported.");
    }

    public IEntityGroup find(String name) throws GroupsException {
        Set<IPersonAttributesGroupDefinition> groups = personAttributesGroupDefinitionDao.getPersonAttributesGroupDefinitionByName(name);
        if (groups.size() == 0) {
            logger.error("No PAGS group with name {} found. Check your PAGS group definitions for possible error"
                + " in member group name", name);
            return null;
        }
        IPersonAttributesGroupDefinition pagsGroup = groups.iterator().next();
        PagsGroup groupDef = initGroupDef(pagsGroup);
        IEntityGroup group = new EntityTestingGroupImpl(groupDef.getKey(), IPERSON_CLASS);
        group.setName(groupDef.getName());
        group.setDescription(groupDef.getDescription());
        return group;
    }

    private boolean testRecursively(PagsGroup groupDef, IPerson person, IGroupMember member)
        throws GroupsException {
            if ( ! groupDef.contains(person) )
                { return false;}
            else
            {
                IEntityGroup group = find(groupDef.getName());
                IEntityGroup parentGroup = null;
                Set<IEntityGroup> allParents = primGetAllContainingGroups(group, new HashSet<IEntityGroup>());
                boolean testPassed = true;
                for (Iterator<IEntityGroup> i=allParents.iterator(); i.hasNext() && testPassed;)
                {
                    parentGroup = i.next();
                    PagsGroup parentGroupDef = (PagsGroup) convertEntityToGroupDef(parentGroup);
                    testPassed = parentGroupDef.test(person);
                }

                if (!testPassed && logger.isWarnEnabled()) {
                    logger.warn("PAGS group {} contained person {}, but the person failed to be contained in"
                            + " ancesters of this group ({}). This may indicate a misconfigured PAGS group store."
                            +" Please check PAGS Entity Files", group.getKey(), member.getKey(),
                            parentGroup != null ? parentGroup.getKey() : "no parent");
                }
                return testPassed;
            }
        }
    private java.util.Set<IEntityGroup> primGetAllContainingGroups(IEntityGroup group, Set<IEntityGroup> s)
            throws GroupsException
            {
                Iterator<IEntityGroup> i = findContainingGroups(group);
                while ( i.hasNext() )
                {
                    IEntityGroup parentGroup = i.next();
                    s.add(parentGroup);
                    primGetAllContainingGroups(parentGroup, s);
                }
                return s;
            }

    public Iterator<IEntityGroup> findContainingGroups(IGroupMember member)
    throws GroupsException
    {
        logger.debug("finding containing groups for member key {}", member.getKey());
        return (member.isEntity())
          ? findContainingGroupsForEntity((IEntity)member)
          : findContainingGroupsForGroup((IEntityGroup)member);
    }

    private Iterator<IEntityGroup> findContainingGroupsForGroup(IEntityGroup group) {
        logger.debug("Finding containing groups for group {} (key {})", group.getName(), group.getKey());
         Set<IEntityGroup> parents = getContainingGroups(group.getName(), new HashSet<IEntityGroup>());
         return parents.iterator();
    }

    private Iterator<IEntityGroup> findContainingGroupsForEntity(IEntity member)
    throws GroupsException {
        Set<IPersonAttributesGroupDefinition> pagsGroups = personAttributesGroupDefinitionDao.getPersonAttributesGroupDefinitions();
        List<IEntityGroup> results = new ArrayList<IEntityGroup>();
        for (IPersonAttributesGroupDefinition pagsGroup : pagsGroups) {
            IEntityGroup group = convertPagsGroupToEntity(pagsGroup);
            if ( contains(group, member))
                { results.add(group); }
        }
        return results.iterator();
    }

    public Iterator<IEntityGroup> findEntitiesForGroup(IEntityGroup group) throws GroupsException {
        return Collections.EMPTY_LIST.iterator();
    }

    public ILockableEntityGroup findLockable(String key) throws GroupsException {
        throw new UnsupportedOperationException("EntityPersonAttributesGroupStore: Method findLockable() not supported");
    }

    public String[] findMemberGroupKeys(IEntityGroup group) throws GroupsException {

        List<String> keys = new ArrayList<String>();
        PagsGroup groupDef = convertEntityToGroupDef(group);
        if (groupDef != null)
        {
             for (Iterator<String> i = groupDef.getMembers().iterator(); i.hasNext(); )
                  { keys.add(i.next()); }
        }
        return keys.toArray(new String[]{});
    }

    public Iterator<IEntityGroup> findMemberGroups(IEntityGroup group) throws GroupsException {
        IPersonAttributesGroupDefinition pagsGroup = getPagsGroupDefByName(group.getName());
        List<IEntityGroup> results = new ArrayList<IEntityGroup>();
        for (IPersonAttributesGroupDefinition member : pagsGroup.getMembers()) {
            results.add(convertPagsGroupToEntity(member));
        }
        return results.iterator();
    }

    public IEntityGroup newInstance(Class entityType) throws GroupsException {
        throw new UnsupportedOperationException("EntityPersonAttributesGroupStore: Method newInstance() not supported");
    }

    public EntityIdentifier[] searchForGroups(String query, int method, Class leaftype) throws GroupsException {
        if ( leaftype != IPERSON_CLASS )
             { return EMPTY_SEARCH_RESULTS; }
        Set<IPersonAttributesGroupDefinition> pagsGroups = personAttributesGroupDefinitionDao.getPersonAttributesGroupDefinitions();
        List<EntityIdentifier> results = new ArrayList<EntityIdentifier>();
        switch (method) {
            case IS:
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
                    if (g.getName().toUpperCase().startsWith(query.toUpperCase())) {
                        results.add(g.getEntityIdentifier());
                    }
                }
                break;
            case ENDS_WITH:
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
                    if (g.getName().toUpperCase().indexOf(query.toUpperCase()) != -1) {
                        results.add(g.getEntityIdentifier());
                  }
                }
                break;
        }
        return results.toArray(new EntityIdentifier[]{});
    }

    public void update(IEntityGroup group) throws GroupsException {
        throw new UnsupportedOperationException("EntityPersonAttributesGroupStore: Method update() not supported.");
    }

    public void updateMembers(IEntityGroup group) throws GroupsException {
        throw new UnsupportedOperationException("EntityPersonAttributesGroupStore: Method updateMembers() not supported.");
    }



    public IEntity newInstance(String key, Class type) throws GroupsException {
        if (EntityTypes.getEntityTypeID(type) == null) {
            throw new GroupsException("Invalid entity type: "+type.getName());
        }
        return new EntityImpl(key, type);
    }

    public IEntity newInstance(String key) throws GroupsException {
        return new EntityImpl(key, null);
    }

    public EntityIdentifier[] searchForEntities(String query, int method, Class type) throws GroupsException {
        return EMPTY_SEARCH_RESULTS;
    }

    private PagsGroup initGroupDef(IPersonAttributesGroupDefinition group) {
        Element element = this.groupDefCache.get(group.getName());
        if (element != null) {
            return (PagsGroup) element.getObjectValue();
        }
        PagsGroup groupDef = new PagsGroup();
        groupDef.setKey(group.getName());
        groupDef.setName(group.getName());
        groupDef.setDescription(group.getDescription());
        addMemberKeys(groupDef, group.getMembers());
        Set<IPersonAttributesGroupTestGroupDefinition> testGroups = group.getTestGroups();
        for(IPersonAttributesGroupTestGroupDefinition testGroup : testGroups) {
            TestGroup tg = new TestGroup();
            Set<IPersonAttributesGroupTestDefinition> tests = testGroup.getTests();
            for(IPersonAttributesGroupTestDefinition test : tests) {
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
        this.groupDefCache.put(element);
        return groupDef;
    }

    private void addMemberKeys(PagsGroup groupDef, Set<IPersonAttributesGroupDefinition> members) {
        for(IPersonAttributesGroupDefinition member: members) {
            groupDef.addMember(member.getName());
        }
    }

    private IPersonTester initializeTester(IPersonAttributesGroupTestDefinition test) {
        try {
            Class<?> testerClass = Class.forName(test.getTesterClassName());
            Constructor<?> c = testerClass.getConstructor(IPersonAttributesGroupTestDefinition.class);
            Object o = c.newInstance(test);
            return (IPersonTester) o;
        } catch (Exception e) {
            logger.error("Error in initializing tester class: {}", test.getTesterClassName(), e);
            return null;
        }
   }

    private Set<IEntityGroup> getContainingGroups(String name, Set<IEntityGroup> groups) throws GroupsException {
        logger.debug("Looking up containing groups for {}", name);
        IPersonAttributesGroupDefinition pagsGroup = getPagsGroupDefByName(name);
        Set<IPersonAttributesGroupDefinition> pagsParentGroups = personAttributesGroupDefinitionDao.getParentPersonAttributesGroupDefinitions(pagsGroup);
        for (IPersonAttributesGroupDefinition pagsParent : pagsParentGroups) {
            IEntityGroup parent = convertPagsGroupToEntity(pagsParent);
            if (!groups.contains(parent)) {
                groups.add(parent);
                getContainingGroups(pagsParent.getName(), groups);
            } else {
                throw new RuntimeException("Recursive grouping detected! for " + name + " and parent " + pagsParent.getName());
            }
        }
        return groups;
    }

    /**
     * Retrieve an implementation of {@code IPersonAttributesGroupDefinition} with the given {@code name} from
     * the JPA DAO. There are two assumptions. First, that the DAO handles caching, so caching is not implemented here.
     * Second, that group names are unique. A warning will be logged if more than one group is found with the same name.
     *
     * @param name      group name used to search for group definition
     * @return          {@code IPersonAttributesGroupDefinition} of named group or null
     * @see             IPersonAttributesGroupDefinitionDao#getPersonAttributesGroupDefinitionByName(String)
     * @see             IPersonAttributesGroupDefinition
     */
    private IPersonAttributesGroupDefinition getPagsGroupDefByName(String name) {
        Set<IPersonAttributesGroupDefinition> pagsGroups = personAttributesGroupDefinitionDao.getPersonAttributesGroupDefinitionByName(name);
        if (pagsGroups.size() > 1) {
            logger.error("More than one PAGS group with name {} found.", name);
        }
        return pagsGroups.isEmpty() ? null : pagsGroups.iterator().next();
    }
}
