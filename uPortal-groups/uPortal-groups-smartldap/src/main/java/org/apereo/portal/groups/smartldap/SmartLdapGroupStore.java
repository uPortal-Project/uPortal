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
package org.apereo.portal.groups.smartldap;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.concurrent.ConcurrentException;
import org.apache.commons.lang3.concurrent.LazyInitializer;
import org.apereo.portal.EntityIdentifier;
import org.apereo.portal.groups.ComponentGroupServiceDescriptor;
import org.apereo.portal.groups.EntityGroupImpl;
import org.apereo.portal.groups.EntityImpl;
import org.apereo.portal.groups.EntityTestingGroupImpl;
import org.apereo.portal.groups.GroupsException;
import org.apereo.portal.groups.IEntity;
import org.apereo.portal.groups.IEntityGroup;
import org.apereo.portal.groups.IEntityGroupStore;
import org.apereo.portal.groups.IEntityGroupStoreFactory;
import org.apereo.portal.groups.IGroupMember;
import org.apereo.portal.groups.ILockableEntityGroup;
import org.apereo.portal.security.IPerson;
import org.apereo.portal.security.PersonFactory;
import org.apereo.services.persondir.IPersonAttributeDao;
import org.apereo.services.persondir.IPersonAttributes;
import org.danann.cernunnos.Task;
import org.danann.cernunnos.runtime.RuntimeRequestResponse;
import org.danann.cernunnos.runtime.ScriptRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.ldap.core.AttributesMapper;
import org.springframework.ldap.core.ContextSource;

public final class SmartLdapGroupStore implements IEntityGroupStore {

    // Instance Members.
    private String memberOfAttributeName = "memberOf"; // default

    public void setMemberOfAttributeName(String memberOfAttributeName) {
        this.memberOfAttributeName = memberOfAttributeName;
    }

    private String baseGroupDn = null;

    public void setBaseGroupDn(String baseGroupDn) {
        this.baseGroupDn = baseGroupDn;
    }

    private String childGroupKeyRegex = null;

    public void setChildGroupKeyRegex(String childGroupKeyRegex) {
        this.childGroupKeyRegex = childGroupKeyRegex;
    }

    private String groupTreeSeparator = ":";

    public void setGroupTreeSeparator(String groupTreeSeparator) {
        this.groupTreeSeparator = groupTreeSeparator;
    }

    private String filter = "(objectCategory=group)"; // default

    public void setFilter(String filter) {
        this.filter = filter;
    }

    private ContextSource ldapContext =
            null; // default;  must be set if used -- validated in refreshTree()

    public void setLdapContext(ContextSource ldapContext) {
        this.ldapContext = ldapContext;
    }

    private boolean resolveMemberGroups = false; // default

    public void setResolveMemberGroups(boolean resolveMemberGroups) {
        this.resolveMemberGroups = resolveMemberGroups;
    }

    private List<String> resolveDnList =
            Collections.emptyList(); // default;  used with resolveMemberGroups

    public void setResolveDn(String resolveDn) {
        this.resolveDnList = Collections.singletonList(resolveDn);
    }

    public void setResolveDnList(List<String> resolveDnList) {
        this.resolveDnList = Collections.unmodifiableList(resolveDnList);
    }

    private AttributesMapper attributesMapper;

    @Required
    public void setAttributesMapper(AttributesMapper attributesMapper) {
        this.attributesMapper = attributesMapper;
    }

    /**
     * Period after which SmartLdap will drop and rebuild the groups tree. May be overridden in
     * SmartLdapGroupStoreConfix.xml. A value of zero or less (negative) disables this feature.
     */
    private long groupsTreeRefreshIntervalSeconds = 900; // default

    public void setGroupsTreeRefreshIntervalSeconds(long groupsTreeRefreshIntervalSeconds) {
        this.groupsTreeRefreshIntervalSeconds = groupsTreeRefreshIntervalSeconds;
    }

    /** Timestamp (milliseconds) of the last tree refresh. */
    private volatile long lastTreeRefreshTime = 0;

    // Cernunnos tech...
    private final ScriptRunner runner = new ScriptRunner();
    private final Task initTask =
            runner.compileTask(getClass().getResource("init.crn").toExternalForm());

    @Resource(name = "personAttributeDao")
    private IPersonAttributeDao personAttributeDao;

    private final Logger log = LoggerFactory.getLogger(getClass());

    /**
     * Set it to true to show peoples that are members of groups. Warning must be tested before
     * production if set to true !
     */
    private boolean displayPersonMembers = false;

    public void setDisplayPersonMembers(boolean displayPersonMembers) {
        this.displayPersonMembers = displayPersonMembers;
    }

    private Map<String, IEntity> persons =
            Collections.synchronizedMap(new HashMap<String, IEntity>());

    /*
     * Indexed Collections.
     */

    /**
     * Single-object abstraction that contains all knowledge of SmartLdap groups:
     *
     * <ul>
     *   <li>Map of all groups keyed by 'key' (DN). Includes ROOT_GROUP.
     *   <li>Map of all parent relationships keyed by the 'key' (DN) of the child; the values are
     *       lists of the 'keys' (DNs) of its parents. Includes ROOT_GROUP.
     *   <li>Map of all child relationships keyed by the 'key' (DN) of the parent; the values are
     *       lists of the 'keys' (DNs) of its children. Includes ROOT_GROUP.
     *   <li>Map of all 'keys' (DNs) of SmartLdap managed groups indexed by group name in upper
     *       case. Includes ROOT_GROUP.
     * </ul>
     */
    private GroupsTree groupsTree;

    /*
     * Public API.
     */

    public static final String UNSUPPORTED_MESSAGE =
            "The SmartLdap implementation of JA-SIG Groups and Permissions (GaP) "
                    + "does not support this operation.";

    public static final String ROOT_KEY = "SmartLdap ROOT";
    public static final String ROOT_DESC = "A root group provided for the SmartLdapGroupStore.";

    private static final LazyInitializer<IEntityGroup> rootGroupInitializer =
            new LazyInitializer<IEntityGroup>() {
                @Override
                protected IEntityGroup initialize() {
                    IEntityGroup rslt = new EntityTestingGroupImpl(ROOT_KEY, IPerson.class);
                    rslt.setCreatorID("System");
                    rslt.setName(ROOT_KEY);
                    rslt.setDescription(ROOT_DESC);
                    return rslt;
                }
            };

    @Override
    public boolean contains(IEntityGroup group, IGroupMember member) throws GroupsException {
        log.warn("Unsupported method accessed:  SmartLdapGroupStore.contains");
        throw new UnsupportedOperationException(UNSUPPORTED_MESSAGE);
    }

    @Override
    public void delete(IEntityGroup group) throws GroupsException {
        log.warn("Unsupported method accessed:  SmartLdapGroupStore.delete");
        throw new UnsupportedOperationException(UNSUPPORTED_MESSAGE);
    }

    /**
     * Returns an instance of the <code>IEntityGroup</code> from the data store.
     *
     * @return org.apereo.portal.groups.IEntityGroup
     * @param key java.lang.String
     */
    @Override
    public IEntityGroup find(String key) throws GroupsException {

        if (isTreeRefreshRequired()) {
            refreshTree();
        }

        log.debug("Invoking find() for key:  {}", key);

        // All of our groups (incl. ROOT_GROUP)
        // are indexed in the 'groups' map by key...
        return groupsTree.getGroups().get(key);
    }

    /**
     * Returns an <code>Iterator</code> over the <code>Collection</code> of <code>IEntityGroups
     * </code> that the <code>IGroupMember</code> belongs to.
     *
     * @return java.util.Iterator
     * @param gm org.apereo.portal.groups.IEntityGroup
     */
    @Override
    public Iterator findParentGroups(IGroupMember gm) throws GroupsException {

        if (isTreeRefreshRequired()) {
            refreshTree();
        }

        List<IEntityGroup> rslt = new ArrayList<>();
        final IEntityGroup root = getRootGroup();
        if (gm.isGroup()) {
            // Check the local indeces...
            IEntityGroup group = (IEntityGroup) gm;
            List<String> list = groupsTree.getParents().get(group.getLocalKey());
            if (list != null) {
                // should only reach this code if its a SmartLdap managed group...
                for (String s : list) {
                    rslt.add(groupsTree.getGroups().get(s));
                }
            }
        } else if (!gm.isGroup() && gm.getLeafType().equals(root.getLeafType())) {

            // Ask the individual...
            EntityIdentifier ei = gm.getUnderlyingEntityIdentifier();
            IPersonAttributes attr = personAttributeDao.getPerson(ei.getKey());
            // avoid NPEs and unnecessary IPerson creation
            if (attr != null && attr.getAttributes() != null && !attr.getAttributes().isEmpty()) {
                IPerson p = PersonFactory.createPerson();
                p.setAttributes(attr.getAttributes());

                // Analyze its memberships...
                Object[] groupKeys = p.getAttributeValues(memberOfAttributeName);
                // IPerson returns null if no value is defined for this attribute...
                if (groupKeys != null) {

                    List<String> list = new ArrayList<>();
                    for (Object o : groupKeys) {
                        list.add((String) o);
                    }

                    for (String s : list) {
                        if (groupsTree.getGroups().containsKey(s)) {
                            rslt.add(groupsTree.getGroups().get(s));
                        }
                    }
                }
            }
        }

        return rslt.iterator();
    }

    /**
     * Returns an <code>Iterator</code> over the <code>Collection</code> of <code>IEntities</code>
     * that are members of this <code>IEntityGroup</code>.
     *
     * @return java.util.Iterator
     * @param group org.apereo.portal.groups.IEntityGroup
     */
    @Override
    public Iterator findEntitiesForGroup(IEntityGroup group) throws GroupsException {

        if (isTreeRefreshRequired()) {
            refreshTree();
        }

        log.debug("Invoking findEntitiesForGroup() for group:  {}", group.getLocalKey());

        // We only deal w/ group-group relationships here...
        // return findMemberGroups(group);

        List<IEntity> rslt = new ArrayList<IEntity>();
        if (displayPersonMembers) {
            List<String> list = groupsTree.getPersonChildren().get(group.getLocalKey());
            if (list != null) {
                // should only reach this code if its a SmartLdap managed group...
                for (String s : list) {
                    rslt.add(persons.get(s));
                }
            }
        }
        return rslt.iterator();
    }

    @Override
    public ILockableEntityGroup findLockable(String key) throws GroupsException {
        log.warn("Unsupported method accessed:  SmartLdapGroupStore.findLockable");
        throw new UnsupportedOperationException(UNSUPPORTED_MESSAGE);
    }

    /**
     * Returns a <code>String[]</code> containing the keys of <code>IEntityGroups</code> that are
     * members of this <code>IEntityGroup</code>. In a composite group system, a group may contain a
     * member group from a different service. This is called a foreign membership, and is only
     * possible in an internally-managed service. A group store in such a service can return the key
     * of a foreign member group, but not the group itself, which can only be returned by its local
     * store.
     *
     * @return String[]
     * @param group org.apereo.portal.groups.IEntityGroup
     */
    @Override
    public String[] findMemberGroupKeys(IEntityGroup group) throws GroupsException {

        if (isTreeRefreshRequired()) {
            refreshTree();
        }

        log.debug("Invoking findMemberGroupKeys() for group:  {}", group.getLocalKey());

        List<String> rslt = new ArrayList<>();
        for (Iterator it = findMemberGroups(group); it.hasNext(); ) {
            IEntityGroup g = (IEntityGroup) it.next();
            // Return composite keys here...
            rslt.add(g.getKey());
        }

        return rslt.toArray(new String[rslt.size()]);
    }

    /**
     * Returns an <code>Iterator</code> over the <code>Collection</code> of <code>IEntityGroups
     * </code> that are members of this <code>IEntityGroup</code>.
     *
     * @return java.util.Iterator
     * @param group org.apereo.portal.groups.IEntityGroup
     */
    @Override
    public Iterator findMemberGroups(IEntityGroup group) throws GroupsException {

        if (isTreeRefreshRequired()) {
            refreshTree();
        }

        log.debug("Invoking findMemberGroups() for group:  {}", group.getLocalKey());

        List<IEntityGroup> rslt = new ArrayList<>();

        List<String> list = groupsTree.getChildren().get(group.getLocalKey());
        if (list != null) {
            // should only reach this code if its a SmartLdap managed group...
            for (String s : list) {
                rslt.add(groupsTree.getGroups().get(s));
            }
        }

        return rslt.iterator();
    }

    /**
     * Return an UnsupportedOperationException !
     *
     * @param entityType
     * @return
     * @throws GroupsException
     */
    @Override
    public IEntityGroup newInstance(Class entityType) throws GroupsException {
        log.warn("Unsupported method accessed:  SmartLdapGroupStore.newInstance");
        throw new UnsupportedOperationException(UNSUPPORTED_MESSAGE);
    }

    // Treats case sensitive and case insensitive searching the same.
    @Override
    public EntityIdentifier[] searchForGroups(String query, SearchMethod method, Class leaftype)
            throws GroupsException {

        if (isTreeRefreshRequired()) {
            refreshTree();
        }

        log.debug(
                "Invoking searchForGroups():  query={}, method={}, leaftype=",
                query,
                method,
                leaftype.getName());

        // We only match the IPerson leaf type...
        final IEntityGroup root = getRootGroup();
        if (!leaftype.equals(root.getLeafType())) {
            return new EntityIdentifier[0];
        }

        // We need to escape regex special characters that appear in the query string...
        final String[][] specials =
                new String[][] {
                    /* backslash must come first! */
                    new String[] {"\\", "\\\\"},
                    new String[] {"[", "\\["},
                    /* closing ']' isn't needed b/c it's a normal character w/o a preceding '[' */
                    new String[] {"{", "\\{"},
                    /* closing '}' isn't needed b/c it's a normal character w/o a preceding '{' */
                    new String[] {"^", "\\^"},
                    new String[] {"$", "\\$"},
                    new String[] {".", "\\."},
                    new String[] {"|", "\\|"},
                    new String[] {"?", "\\?"},
                    new String[] {"*", "\\*"},
                    new String[] {"+", "\\+"},
                    new String[] {"(", "\\("},
                    new String[] {")", "\\)"}
                };
        for (String[] s : specials) {
            query = query.replace(s[0], s[1]);
        }

        // Establish the regex pattern to match on...
        String regex;
        switch (method) {
            case DISCRETE:
            case DISCRETE_CI:
                regex = query.toUpperCase();
                break;
            case STARTS_WITH:
            case STARTS_WITH_CI:
                regex = query.toUpperCase() + ".*";
                break;
            case ENDS_WITH:
            case ENDS_WITH_CI:
                regex = ".*" + query.toUpperCase();
                break;
            case CONTAINS:
            case CONTAINS_CI:
                regex = ".*" + query.toUpperCase() + ".*";
                break;
            default:
                String msg = "Unsupported search method:  " + method;
                throw new GroupsException(msg);
        }

        List<EntityIdentifier> rslt = new ArrayList<>();
        for (Map.Entry<String, List<String>> y : groupsTree.getKeysByUpperCaseName().entrySet()) {
            if (y.getKey().matches(regex)) {
                List<String> keys = y.getValue();
                for (String k : keys) {
                    rslt.add(new EntityIdentifier(k, IEntityGroup.class));
                }
            }
        }

        return rslt.toArray(new EntityIdentifier[rslt.size()]);
    }

    @Override
    public void update(IEntityGroup group) throws GroupsException {
        log.warn("Unsupported method accessed:  SmartLdapGroupStore.update");
        throw new UnsupportedOperationException(UNSUPPORTED_MESSAGE);
    }

    @Override
    public void updateMembers(IEntityGroup group) throws GroupsException {
        log.warn("Unsupported method accessed:  SmartLdapGroupStore.updateMembers");
        throw new UnsupportedOperationException(UNSUPPORTED_MESSAGE);
    }

    public LdapRecord detectAndEliminateGroupReferences(
            LdapRecord record, List<String> groupChain) {

        LdapRecord rslt = record; // default

        List<String> keysOfChildren = record.getKeysOfChildren();
        List<String> filteredChildren = new ArrayList<>();
        for (String key : keysOfChildren) {
            if (!groupChain.contains(key)) {
                filteredChildren.add(key);
            } else {
                // Circular reference detected!
                log.warn(
                        "Circular reference detected and removed for the following groups:  '{}' and '{}'",
                        key,
                        record.getGroup().getLocalKey());
            }
        }
        if (filteredChildren.size() < keysOfChildren.size()) {
            rslt = new LdapRecord(record.getGroup(), filteredChildren);
        }

        return rslt;
    }

    public boolean hasUndiscoveredChildrenWithinDn(
            LdapRecord record, String referenceDn, Set<LdapRecord> groupsSet) {

        boolean rslt = false; // default

        for (String childKey : record.getKeysOfChildren()) {
            if (childKey.endsWith(referenceDn)) {
                // Make sure the one we found isn't already in the groupsSet;
                // NOTE!... this test takes advantage of the implementation of
                // equals() on LdapRecord, which states that 2 records with the
                // same group key are equal.
                IEntityGroup group = new EntityGroupImpl(childKey, IPerson.class);
                List<String> list = Collections.emptyList();
                LdapRecord proxy = new LdapRecord(group, list);
                if (!groupsSet.contains(proxy)) {
                    rslt = true;
                    break;
                } else {
                    log.trace("Child group is already in collection:  {}", childKey);
                }
            }
        }

        log.trace(
                "Query for children of parent group '{}':  {}",
                record.getGroup().getLocalKey(),
                rslt);

        return rslt;
    }

    /*
     * Implementation.
     */

    @PostConstruct
    private void postConstruct() {
        Factory.setInstance(this);
    }

    private IEntityGroup getRootGroup() {
        try {
            return rootGroupInitializer.get();
        } catch (ConcurrentException ce) {
            throw new RuntimeException("Failed to obtain the SmartLdap root group", ce);
        }
    }

    private boolean isTreeRefreshRequired() {

        if (groupsTree == null) {
            // Of course we need it
            return true;
        }

        if (groupsTreeRefreshIntervalSeconds <= 0) {
            // SmartLdap refresh feature may be disabled by setting
            // groupsTreeRefreshIntervalSeconds to zero or negative.
            return false;
        }

        // The 'lastTreeRefreshTime' member variable is volatile.  As of JDK 5,
        // this fact should make reads of this variable dependable in a multi-
        // threaded environment.
        final long treeExpiresTimestamp =
                lastTreeRefreshTime + (groupsTreeRefreshIntervalSeconds * 1000L);
        return System.currentTimeMillis() > treeExpiresTimestamp;
    }

    /**
     * Verifies that the collection of groups needs rebuilding and, if so, spawns a new worker
     * <code>Thread</code> for that purpose.
     */
    private synchronized void refreshTree() {

        if (!isTreeRefreshRequired()) {
            // The groupsTree was already re-built while
            // we were waiting to enter this method.
            return;
        }

        log.info("Refreshing groups tree for SmartLdap");

        // We must join the builder thread if
        // we don't have an existing groupsTree.
        final boolean doJoin = groupsTree == null;

        // In most cases, re-build the tree in a separate thread;  the current
        // request can proceed with the newly-expired groupsTree.
        Thread refresh =
                new Thread("SmartLdap Refresh Worker") {
                    @Override
                    public void run() {
                        // Replace the old with the new...
                        try {
                            groupsTree = buildGroupsTree();
                        } catch (Throwable t) {
                            log.error("SmartLdapGroupStore failed to build the groups tree", t);
                        }
                    }
                };
        refresh.setDaemon(true);
        refresh.start();
        if (doJoin) {
            try {
                log.info("Joining the SmartLdap Refresh Worker Thread");
                refresh.join();
            } catch (InterruptedException ie) {
                throw new RuntimeException(ie);
            }
        }

        // Even if the refresh thread failed, don't try
        // again for another groupsTreeRefreshIntervalSeconds.
        lastTreeRefreshTime = System.currentTimeMillis();
    }

    private String getContainingFolder(final String groupName) {
        final int index = groupName.lastIndexOf(this.groupTreeSeparator);
        return (index > 0) ? groupName.substring(0, index) : "";
    }

    private GroupsTree buildGroupsTree() {

        long timestamp = System.currentTimeMillis();

        // Prepare the new local indeces...
        Map<String, IEntityGroup> newGroups =
                Collections.synchronizedMap(new HashMap<String, IEntityGroup>());
        Map<String, List<String>> newParents =
                Collections.synchronizedMap(new HashMap<String, List<String>>());
        Map<String, List<String>> newChildren =
                Collections.synchronizedMap(new HashMap<String, List<String>>());
        Map<String, List<String>> newKeysByUpperCaseName =
                Collections.synchronizedMap(new HashMap<String, List<String>>());
        Map<String, List<String>> newChildrenPersons =
                Collections.synchronizedMap(new HashMap<String, List<String>>());
        Map<String, IEntityGroup> newFolders =
                Collections.synchronizedMap(new HashMap<String, IEntityGroup>());

        // Gather IEntityGroup objects from LDAP...
        RuntimeRequestResponse req = new RuntimeRequestResponse();
        Set<LdapRecord> set = new HashSet<>();
        req.setAttribute("GROUPS", set);
        req.setAttribute("smartLdapGroupStore", this);
        SubQueryCounter queryCounter = new SubQueryCounter();
        req.setAttribute("queryCounter", queryCounter);
        req.setAttribute("filter", filter); // This one changes iteratively...
        req.setAttribute("baseFilter", filter); // while this one stays the same.
        if (StringUtils.isBlank(baseGroupDn)) {
            throw new IllegalStateException("baseGroupDn property not set");
        }
        req.setAttribute("baseGroupDn", baseGroupDn);
        if (ldapContext == null) {
            throw new IllegalStateException("ldapContext property not set");
        }
        req.setAttribute("childGroupKeyRegex", childGroupKeyRegex);
        req.setAttribute("groupTreeSeparator", groupTreeSeparator);
        if (groupTreeSeparator == null) {
            throw new IllegalStateException("groupTreeSeparator property not set");
        }
        req.setAttribute("ldapContext", ldapContext);
        req.setAttribute("resolveMemberGroups", resolveMemberGroups);
        req.setAttribute("resolveDnList", resolveDnList);
        req.setAttribute("memberOfAttributeName", memberOfAttributeName);
        req.setAttribute("attributesMapper", attributesMapper);
        runner.run(initTask, req);

        log.info("init() found {} records", set.size());

        // Do a first loop to build the main catalog (newGroups)...
        for (LdapRecord r : set) {

            // newGroups (me)...
            IEntityGroup g = r.getGroup();
            newGroups.put(g.getLocalKey(), g);
        }

        // Process each group in order to create the groups that will represent the Grouper's
        // folders
        // and maintain the consistency regarding the membership.
        for (String groupKey : newGroups.keySet()) {
            String currentPath = groupKey;

            // The group that represents the grouper folder is created.
            final IEntityGroup folderGroup = new EntityTestingGroupImpl(currentPath, IPerson.class);
            folderGroup.setCreatorID("System");
            folderGroup.setName(currentPath);
            folderGroup.setDescription("Grouper Folder");
            newFolders.put(currentPath, folderGroup);

            String containingFolder = this.getContainingFolder(currentPath);
            while (!containingFolder.isEmpty()) {
                // Adds the containing folder as a parent of this current path
                List<String> parentsList = newParents.get(currentPath);
                if (parentsList == null) {
                    parentsList = Collections.synchronizedList(new ArrayList<String>());
                    newParents.put(currentPath, parentsList);
                }
                parentsList.add(containingFolder);

                // Adds the current path as a child of the containing folder.
                List<String> childrenList = newChildren.get(containingFolder);
                if (childrenList == null) {
                    childrenList = Collections.synchronizedList(new ArrayList<String>());
                    newChildren.put(containingFolder, childrenList);
                }
                childrenList.add(currentPath);

                // The remaining prefix of the group has already been processed.
                if (newFolders.containsKey(containingFolder)) {
                    break;
                }
                currentPath = containingFolder;

                containingFolder = this.getContainingFolder(currentPath);
            }
        }

        // The new groups are added to the original ones.
        newGroups.putAll(newFolders);

        // Do a second loop to build local indeces...
        for (LdapRecord r : set) {

            IEntityGroup g = r.getGroup();

            // newParents (I am a parent for all my children)...
            for (String childKey : r.getKeysOfChildren()) {

                // NB:  We're only interested in relationships between
                // objects in the main catalog (i.e. newGroups);
                // discard everything else...
                // childkey case of DN
                final int keyStart = childKey.indexOf('=');
                final int keyEnd = childKey.indexOf(',');
                if (keyStart >= 0 && keyEnd >= 0 && keyStart < keyEnd) {
                    childKey = childKey.substring(keyStart + 1, keyEnd);
                }
                if (newGroups.containsKey(childKey)) {

                    List<String> parentsList = newParents.get(childKey);
                    if (parentsList == null) {
                        // first parent for this child...
                        parentsList = Collections.synchronizedList(new ArrayList<String>());
                        newParents.put(childKey, parentsList);
                    }
                    parentsList.add(g.getLocalKey());
                }
            }

            // new_children...
            List<String> childrenList = Collections.synchronizedList(new ArrayList<String>());
            List<String> childrenPersonList = Collections.synchronizedList(new ArrayList<String>());
            for (String childKey : r.getKeysOfChildren()) {
                // NB:  We're only interested in relationships between
                // objects in the main catalog (i.e. newGroups);
                // discard everything else...
                final int keyStart = childKey.indexOf('=');
                final int keyEnd = childKey.indexOf(',');
                if (keyStart >= 0 && keyEnd >= 0 && keyStart < keyEnd) {
                    childKey = childKey.substring(keyStart + 1, keyEnd);
                }
                if (newGroups.containsKey(childKey)) {
                    childrenList.add(childKey);
                } else if (displayPersonMembers) {
                    childrenPersonList.add(childKey);
                    if (!persons.containsKey(childKey)) {
                        persons.put(childKey, new EntityImpl(childKey, IPerson.class));
                    }
                }
            }
            newChildren.put(g.getLocalKey(), childrenList);
            newChildrenPersons.put(g.getLocalKey(), childrenPersonList);

            // newKeysByUpperCaseName...
            List<String> groupsWithMyName = newKeysByUpperCaseName.get(g.getName().toUpperCase());
            if (groupsWithMyName == null) {
                // I am the first group with my name (pretty likely)...
                groupsWithMyName = Collections.synchronizedList(new ArrayList<String>());
                newKeysByUpperCaseName.put(g.getName().toUpperCase(), groupsWithMyName);
            }
            groupsWithMyName.add(g.getLocalKey());
        }

        /*
         * Now load the ROOT_GROUP into the collections...
         */

        // newGroups (me)...
        final IEntityGroup root = getRootGroup();
        newGroups.put(root.getLocalKey(), root);

        // newParents (I am a parent for all groups that have no other parent)...
        List<String> childrenOfRoot =
                Collections.synchronizedList(new ArrayList<String>()); // for later...
        for (String possibleChildKey : newGroups.keySet()) {
            if (!possibleChildKey.equals(root.getLocalKey())
                    && !newParents.containsKey(possibleChildKey)) {
                List<String> p = Collections.synchronizedList(new ArrayList<String>());
                p.add(root.getLocalKey());
                newParents.put(possibleChildKey, p);
                childrenOfRoot.add(possibleChildKey); // for later...
            }
        }

        // newChildren...
        newChildren.put(root.getLocalKey(), childrenOfRoot);

        // newKeysByUpperCaseName...
        List<String> groupsWithMyName = newKeysByUpperCaseName.get(root.getName().toUpperCase());
        if (groupsWithMyName == null) {
            // I am the first group with my name (pretty likely)...
            groupsWithMyName = Collections.synchronizedList(new ArrayList<String>());
            newKeysByUpperCaseName.put(root.getName().toUpperCase(), groupsWithMyName);
        }
        groupsWithMyName.add(root.getLocalKey());

        final long benchmark = System.currentTimeMillis() - timestamp;
        log.info("Refresh of groups tree completed in {} milliseconds", benchmark);
        log.info("Total number of LDAP queries:  {}", queryCounter.getCount() + 1);
        final String msg =
                "init() :: final size of each collection is as follows..."
                        + "\n\tgroups={}"
                        + "\n\tparents={}"
                        + "\n\tchildren={}"
                        + "\n\tkeysByUpperCaseName={}";
        log.info(
                msg,
                newGroups.size(),
                newParents.size(),
                newChildren.size(),
                newKeysByUpperCaseName.size());

        if (log.isTraceEnabled()) {

            StringBuilder sbuilder = new StringBuilder();

            // newGroups...
            sbuilder.setLength(0);
            sbuilder.append("Here are the keys of the newGroups collection:");
            for (String s : newGroups.keySet()) {
                sbuilder.append("\n\t").append(s);
            }
            log.trace(sbuilder.toString());

            // newParents...
            sbuilder.setLength(0);
            sbuilder.append("Here are the parents of each child in the newParents collection:");
            for (Map.Entry<String, List<String>> y : newParents.entrySet()) {
                sbuilder.append("\n\tchild=").append(y.getKey());
                for (String s : y.getValue()) {
                    sbuilder.append("\n\t\tparent=").append(s);
                }
            }
            log.trace(sbuilder.toString());

            // newChildren...
            sbuilder.setLength(0);
            sbuilder.append("Here are the children of each parent in the newChildren collection:");
            for (Map.Entry<String, List<String>> y : newChildren.entrySet()) {
                sbuilder.append("\n\tparent=").append(y.getKey());
                for (String s : y.getValue()) {
                    sbuilder.append("\n\t\tchild=").append(s);
                }
            }
            log.trace(sbuilder.toString());

            // newKeysByUpperCaseName...
            sbuilder.append(
                    "Here are the groups that have each name in the newKeysByUpperCaseName collection:");
            for (Map.Entry<String, List<String>> y : newKeysByUpperCaseName.entrySet()) {
                sbuilder.append("\n\tname=").append(y.getKey());
                for (String s : y.getValue()) {
                    sbuilder.append("\n\t\tgroup=").append(s);
                }
            }
            log.trace(sbuilder.toString());
        }

        return new GroupsTree(
                newGroups, newParents, newChildren, newChildrenPersons, newKeysByUpperCaseName);
    }

    /*
     * Nested Types.
     */

    public static final class Factory implements IEntityGroupStoreFactory {

        private static IEntityGroupStore instance;

        private static void setInstance(IEntityGroupStore smartLdapGroupStore) {
            instance = smartLdapGroupStore;
        }

        /*
         * Public API.
         */
        @Override
        public IEntityGroupStore newGroupStore() throws GroupsException {
            return instance;
        }

        @Override
        public IEntityGroupStore newGroupStore(ComponentGroupServiceDescriptor svcDescriptor)
                throws GroupsException {
            return instance;
        }
    }

    private static final class GroupsTree {

        // Instance Members.
        private final Map<String, IEntityGroup> groups;
        private final Map<String, List<String>> parents;
        private final Map<String, List<String>> children;
        private final Map<String, List<String>> personChildren;
        private final Map<String, List<String>> keysByUpperCaseName;

        /*
         * Public API.
         */

        public GroupsTree(
                Map<String, IEntityGroup> groups,
                Map<String, List<String>> parents,
                Map<String, List<String>> children,
                Map<String, List<String>> personChildren,
                Map<String, List<String>> keysByUpperCaseName) {

            // Assertions.
            if (groups == null) {
                String msg = "Argument 'groups' cannot be null.";
                throw new IllegalArgumentException(msg);
            }
            if (parents == null) {
                String msg = "Argument 'parents' cannot be null.";
                throw new IllegalArgumentException(msg);
            }
            if (children == null) {
                String msg = "Argument 'children' cannot be null.";
                throw new IllegalArgumentException(msg);
            }
            if (personChildren == null) {
                String msg = "Argument 'personChildren' cannot be null.";
                throw new IllegalArgumentException(msg);
            }
            if (keysByUpperCaseName == null) {
                String msg = "Argument 'keysByUpperCaseName' cannot be null.";
                throw new IllegalArgumentException(msg);
            }

            // Instance Members.
            this.groups = groups;
            this.parents = parents;
            this.children = children;
            this.personChildren = personChildren;
            this.keysByUpperCaseName = keysByUpperCaseName;
        }

        public Map<String, IEntityGroup> getGroups() {
            return groups;
        }

        public Map<String, List<String>> getParents() {
            return parents;
        }

        public Map<String, List<String>> getChildren() {
            return children;
        }

        public Map<String, List<String>> getPersonChildren() {
            return personChildren;
        }

        public Map<String, List<String>> getKeysByUpperCaseName() {
            return keysByUpperCaseName;
        }
    }

    private static final class SubQueryCounter {

        private int count = 0;

        @SuppressWarnings("unused")
        public void increment() {
            ++count;
        }

        public int getCount() {
            return count;
        }
    }
}
