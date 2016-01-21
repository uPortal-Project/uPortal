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
package org.jasig.portal.groups.smartldap;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.concurrent.ConcurrentException;
import org.apache.commons.lang3.concurrent.LazyInitializer;
import org.danann.cernunnos.Task;
import org.danann.cernunnos.runtime.RuntimeRequestResponse;
import org.danann.cernunnos.runtime.ScriptRunner;
import org.jasig.portal.EntityIdentifier;
import org.jasig.portal.groups.ComponentGroupServiceDescriptor;
import org.jasig.portal.groups.EntityGroupImpl;
import org.jasig.portal.groups.EntityTestingGroupImpl;
import org.jasig.portal.groups.GroupsException;
import org.jasig.portal.groups.IEntityGroup;
import org.jasig.portal.groups.IEntityGroupStore;
import org.jasig.portal.groups.IEntityGroupStoreFactory;
import org.jasig.portal.groups.IGroupConstants;
import org.jasig.portal.groups.IGroupMember;
import org.jasig.portal.groups.ILockableEntityGroup;
import org.jasig.portal.security.IPerson;
import org.jasig.portal.security.PersonFactory;
import org.jasig.services.persondir.IPersonAttributeDao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.ldap.core.AttributesMapper;
import org.springframework.ldap.core.ContextSource;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

public final class SmartLdapGroupStore implements IEntityGroupStore {

    // Instance Members.
    private String memberOfAttributeName = "memberOf";  // default
    public void setMemberOfAttributeName(String memberOfAttributeName) {
        this.memberOfAttributeName = memberOfAttributeName;
    }

    private String baseGroupDn = null;
    public void setBaseGroupDn(String baseGroupDn) {
        this.baseGroupDn = baseGroupDn;
    }

    private String filter = "(objectCategory=group)";  // default
    public void setFilter(String filter) {
        this.filter = filter;
    }

    private ContextSource ldapContext = null;  // default;  must be set if used -- validated in refreshTree()
    public void setLdapContext(ContextSource ldapContext) {
        this.ldapContext = ldapContext;
    }

    private boolean resolveMemberGroups =  false;  // default
    public void setResolveMemberGroups(boolean resolveMemberGroups) {
        this.resolveMemberGroups = resolveMemberGroups;
    }

    private List<String> resolveDnList = Collections.emptyList();  // default;  used with resolveMemberGroups
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
     * Period after which SmartLdap will drop and rebuild the groups tree.  May 
     * be overridden in SmartLdapGroupStoreConfix.xml.  A value of zero or less 
     * (negative) disables this feature.
     */
    private long groupsTreeRefreshIntervalSeconds = 900;  // default
    public void setGroupsTreeRefreshIntervalSeconds(long groupsTreeRefreshIntervalSeconds) {
        this.groupsTreeRefreshIntervalSeconds = groupsTreeRefreshIntervalSeconds;
    }

    /**
     * Timestamp (milliseconds) of the last tree refresh.
     */
    private volatile long lastTreeRefreshTime = 0;

    // Cernunnos tech...
    private final ScriptRunner runner = new ScriptRunner();
    private final Task initTask = runner.compileTask(getClass().getResource("init.crn").toExternalForm());

    @Resource(name="personAttributeDao")
    private IPersonAttributeDao personAttributeDao;

    private final Logger log = LoggerFactory.getLogger(getClass());

    /*
     * Indexed Collections.
     */

    /**
     * Single-object abstraction that contains all knowledge of SmartLdap groups:
     * <ul>
     *   <li>Map of all groups keyed by 'key' (DN).  Includes ROOT_GROUP.</li>
     *   <li>Map of all parent relationships keyed by the 'key' (DN) of the child;  
     *       the values are lists of the 'keys' (DNs) of its parents.  
     *       Includes ROOT_GROUP.</li>
     *   <li>Map of all child relationships keyed by the 'key' (DN) of the parent;  
     *       the values are lists of the 'keys' (DNs) of its children.  
     *       Includes ROOT_GROUP.</li>
     *   <li>Map of all 'keys' (DNs) of SmartLdap managed groups indexed by group 
     *       name in upper case.  Includes ROOT_GROUP.</li>
     * </ul>
     */
    private GroupsTree groupsTree;

    /*
     * Public API.
     */

    public static final String UNSUPPORTED_MESSAGE = 
            "The SmartLdap implementation of JA-SIG Groups and Permissions (GaP) " +
            "does not support this operation.";

    public static final String ROOT_KEY = "SmartLdap ROOT";
    public static final String ROOT_DESC = "A root group provided for the SmartLdapGroupStore.";

    private static final LazyInitializer<IEntityGroup> rootGroupInitializer = new LazyInitializer<IEntityGroup>() {
        @Override
        protected IEntityGroup initialize() {
            IEntityGroup rslt = new EntityTestingGroupImpl(ROOT_KEY, IPerson.class);
            rslt.setCreatorID("System");
            rslt.setName(ROOT_KEY);
            rslt.setDescription(ROOT_DESC);
            return rslt;
        }
    };

    public boolean contains(IEntityGroup group, IGroupMember member) throws GroupsException {
        log.warn("Unsupported method accessed:  SmartLdapGroupStore.contains");
        throw new UnsupportedOperationException(UNSUPPORTED_MESSAGE);
    }

    public void delete(IEntityGroup group) throws GroupsException {
        log.warn("Unsupported method accessed:  SmartLdapGroupStore.delete");
        throw new UnsupportedOperationException(UNSUPPORTED_MESSAGE);
    }

    /**
     * Returns an instance of the <code>IEntityGroup</code> from the data store.
     * @return org.jasig.portal.groups.IEntityGroup
     * @param key java.lang.String
     */
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
     * Returns an <code>Iterator</code> over the <code>Collection</code> of
     * <code>IEntityGroups</code> that the <code>IGroupMember</code> belongs to.
     * @return java.util.Iterator
     * @param gm org.jasig.portal.groups.IEntityGroup
     */
    public Iterator findContainingGroups(IGroupMember gm) throws GroupsException {

        if (isTreeRefreshRequired()) {
            refreshTree();
        }

        List<IEntityGroup> rslt = new LinkedList<>();
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
        } else if (gm.isEntity() && gm.getEntityType().equals(root.getEntityType())) {

            // Ask the individual...
            EntityIdentifier ei = gm.getUnderlyingEntityIdentifier();
            Map<String,List<Object>> seed = new HashMap<>();
            List<Object> seedValue = new LinkedList<>();
            seedValue.add(ei.getKey());
            seed.put(IPerson.USERNAME, seedValue);
            Map<String,List<Object>> attr = personAttributeDao.getMultivaluedUserAttributes(seed);
            // avoid NPEs and unnecessary IPerson creation
            if (attr != null && !attr.isEmpty()) {
                IPerson p = PersonFactory.createPerson();
                p.setAttributes(attr);

                // Analyze its memberships...
                Object[] groupKeys = p.getAttributeValues(memberOfAttributeName);
                // IPerson returns null if no value is defined for this attribute...
                if (groupKeys != null) {

                    List<String> list = new LinkedList<>();
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
     * Returns an <code>Iterator</code> over the <code>Collection</code> of
     * <code>IEntities</code> that are members of this <code>IEntityGroup</code>.
     * @return java.util.Iterator
     * @param group org.jasig.portal.groups.IEntityGroup
     */
    public Iterator findEntitiesForGroup(IEntityGroup group) throws GroupsException {

        if (isTreeRefreshRequired()) {
            refreshTree();
        }

        log.debug("Invoking findEntitiesForGroup() for group:  {}", group.getLocalKey());

        // We only deal w/ group-group relationships here...
        return findMemberGroups(group);

    }

    public ILockableEntityGroup findLockable(String key) throws GroupsException {
        log.warn("Unsupported method accessed:  SmartLdapGroupStore.findLockable");
        throw new UnsupportedOperationException(UNSUPPORTED_MESSAGE);
    }

    /**
     * Returns a <code>String[]</code> containing the keys of  <code>IEntityGroups</code>
     * that are members of this <code>IEntityGroup</code>.  In a composite group
     * system, a group may contain a member group from a different service.  This is
     * called a foreign membership, and is only possible in an internally-managed
     * service.  A group store in such a service can return the key of a foreign member
     * group, but not the group itself, which can only be returned by its local store.
     *
     * @return String[]
     * @param group org.jasig.portal.groups.IEntityGroup
     */
    public String[] findMemberGroupKeys(IEntityGroup group) throws GroupsException {

        if (isTreeRefreshRequired()) {
            refreshTree();
        }

        log.debug("Invoking findMemberGroupKeys() for group:  {}", group.getLocalKey());

        List<String> rslt = new LinkedList<>();
        for (Iterator it=findMemberGroups(group); it.hasNext();) {
            IEntityGroup g = (IEntityGroup) it.next();
            // Return composite keys here...
            rslt.add(g.getKey());
        }

        return rslt.toArray(new String[rslt.size()]);

    }

    /**
     * Returns an <code>Iterator</code> over the <code>Collection</code> of
     * <code>IEntityGroups</code> that are members of this <code>IEntityGroup</code>.
     * @return java.util.Iterator
     * @param group org.jasig.portal.groups.IEntityGroup
     */
    public Iterator findMemberGroups(IEntityGroup group) throws GroupsException {

        if (isTreeRefreshRequired()) {
            refreshTree();
        }

        log.debug("Invoking findMemberGroups() for group:  {}", group.getLocalKey());

        List<IEntityGroup> rslt = new LinkedList<>();

        List<String> list = groupsTree.getChildren().get(group.getLocalKey());
        if (list != null) {
            // should only reach this code if its a SmartLdap managed group...
            for (String s : list) {
                rslt.add(groupsTree.getGroups().get(s));
            }
        }

        return rslt.iterator();

    }

    public IEntityGroup newInstance(Class entityType) throws GroupsException {
        log.warn("Unsupported method accessed:  SmartLdapGroupStore.newInstance");
        throw new UnsupportedOperationException(UNSUPPORTED_MESSAGE);
    }

    public EntityIdentifier[] searchForGroups(String query, int method, Class leaftype) throws GroupsException {

        if (isTreeRefreshRequired()) {
            refreshTree();
        }

        log.debug("Invoking searchForGroups():  query={}, method={}, leaftype=",
                            query, method, leaftype.getClass().getName());

        // We only match the IPerson leaf type...
        final IEntityGroup root = getRootGroup();
        if (!leaftype.equals(root.getEntityType())) {
            return new EntityIdentifier[0];
        }

        // We need to escape regex special characters that appear in the query string...
        final String[][] specials = new String[][] {
                            /* backslash must come first! */
                            new String[] { "\\", "\\\\"}, 
                            new String[] { "[", "\\[" }, 
                            /* closing ']' isn't needed b/c it's a normal character w/o a preceding '[' */
                            new String[] { "{", "\\{" }, 
                            /* closing '}' isn't needed b/c it's a normal character w/o a preceding '{' */
                            new String[] { "^", "\\^" },
                            new String[] { "$", "\\$" },
                            new String[] { ".", "\\." },
                            new String[] { "|", "\\|" },
                            new String[] { "?", "\\?" },
                            new String[] { "*", "\\*" },
                            new String[] { "+", "\\+" },
                            new String[] { "(", "\\(" },
                            new String[] { ")", "\\)" }
                        };
        for (String[] s : specials) {
            query = query.replace(s[0], s[1]);
        }

        // Establish the regex pattern to match on...
        String regex;
        switch (method) {
            case IGroupConstants.IS:
                regex = query.toUpperCase();
                break;
            case IGroupConstants.STARTS_WITH:
                regex = query.toUpperCase() + ".*";
                break;
            case IGroupConstants.ENDS_WITH: 
                regex = ".*" + query.toUpperCase();
                break;
            case IGroupConstants.CONTAINS: 
                regex = ".*" + query.toUpperCase() + ".*";
                break;
            default:
                String msg = "Unsupported search method:  " + method;
                throw new GroupsException(msg);
        }

        List<EntityIdentifier> rslt = new LinkedList<>();
        for (Map.Entry<String,List<String>> y : groupsTree.getKeysByUpperCaseName().entrySet()) {
            if (y.getKey().matches(regex)) {
                List<String> keys = y.getValue();
                for (String k : keys) {
                    rslt.add(new EntityIdentifier(k, IEntityGroup.class));
                }
            }
        }

        return rslt.toArray(new EntityIdentifier[rslt.size()]);

    }

    public void update(IEntityGroup group) throws GroupsException {
        log.warn("Unsupported method accessed:  SmartLdapGroupStore.update");
        throw new UnsupportedOperationException(UNSUPPORTED_MESSAGE);
    }

    public void updateMembers(IEntityGroup group) throws GroupsException {
        log.warn("Unsupported method accessed:  SmartLdapGroupStore.updateMembers");
        throw new UnsupportedOperationException(UNSUPPORTED_MESSAGE);
    }

    public LdapRecord detectAndEliminateGroupReferences(LdapRecord record, List<String> groupChain) {

        LdapRecord rslt = record;  // default

        List<String> keysOfChildren = record.getKeysOfChildren();
        List<String> filteredChildren = new ArrayList<>();
        for (String key : keysOfChildren) {
            if (!groupChain.contains(key)) {
                filteredChildren.add(key);
            } else {
                // Circular reference detected!
                log.warn("Circular reference detected and removed for the following groups:  '{}' and '{}'",
                                                                key, record.getGroup().getLocalKey());
            }
        }
        if (filteredChildren.size() < keysOfChildren.size()) {
            rslt = new LdapRecord(record.getGroup(), filteredChildren);
        }

        return rslt;

    }

    public boolean hasUndiscoveredChildrenWithinDn(LdapRecord record, String referenceDn, Set<LdapRecord> groupsSet) {

        boolean rslt = false;  // default

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

        log.trace("Query for children of parent group '{}':  {}", record.getGroup().getLocalKey(), rslt);

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
        final long treeExpiresTimestamp = lastTreeRefreshTime + (groupsTreeRefreshIntervalSeconds * 1000L);
        return System.currentTimeMillis() > treeExpiresTimestamp;

    }

    /**
     * Verifies that the collection of groups needs rebuilding and, if so, 
     * spawns a new worker <code>Thread</code> for that purpose.
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
        Thread refresh = new Thread("SmartLdap Refresh Worker") {
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

    private GroupsTree buildGroupsTree() {

        long timestamp = System.currentTimeMillis();

        // Prepare the new local indeces...
        Map<String,IEntityGroup> new_groups = Collections.synchronizedMap(new HashMap<String,IEntityGroup>());
        Map<String,List<String>> new_parents = Collections.synchronizedMap(new HashMap<String,List<String>>());
        Map<String,List<String>> new_children = Collections.synchronizedMap(new HashMap<String,List<String>>());
        Map<String,List<String>> new_keysByUpperCaseName = Collections.synchronizedMap(new HashMap<String,List<String>>());

        // Gather IEntityGroup objects from LDAP...
        RuntimeRequestResponse req = new RuntimeRequestResponse();
        Set<LdapRecord> set = new HashSet<>();
        req.setAttribute("GROUPS", set);
        req.setAttribute("smartLdapGroupStore", this);
        SubQueryCounter queryCounter = new SubQueryCounter();
        req.setAttribute("queryCounter", queryCounter);
        req.setAttribute("filter", filter);         // This one changes iteratively...
        req.setAttribute("baseFilter", filter);     // while this one stays the same.
        if (StringUtils.isBlank(baseGroupDn)) {
            throw new IllegalStateException("baseGroupDn property not set");
        }
        req.setAttribute("baseGroupDn", baseGroupDn);
        if (ldapContext == null) {
            throw new IllegalStateException("ldapContext property not set");
        }
        req.setAttribute("ldapContext", ldapContext);
        req.setAttribute("resolveMemberGroups", resolveMemberGroups);
        req.setAttribute("resolveDnList", resolveDnList);
        req.setAttribute("memberOfAttributeName", memberOfAttributeName);
        req.setAttribute("attributesMapper", attributesMapper);
        runner.run(initTask, req);

        log.info("init() found {} records", set.size());

        // Do a first loop to build the main catalog (new_groups)...
        for (LdapRecord r : set) {

            // new_groups (me)...
            IEntityGroup g = r.getGroup();
            new_groups.put(g.getLocalKey(), g);

        }

        // Do a second loop to build local indeces...
        for (LdapRecord r : set) {

            IEntityGroup g = r.getGroup();

            // new_parents (I am a parent for all my children)...
            for (String childKey : r.getKeysOfChildren()) {

                // NB:  We're only interested in relationships between 
                // objects in the main catalog (i.e. new_groups);  
                // discard everything else...
                if (!new_groups.containsKey(childKey)) {
                    break;
                }

                List<String> parentsList = new_parents.get(childKey);
                if (parentsList == null) {
                    // first parent for this child...
                    parentsList = Collections.synchronizedList(new LinkedList<String>());
                    new_parents.put(childKey, parentsList);
                }
                parentsList.add(g.getLocalKey());

            }

            // new_children...
            List<String> childrenList = Collections.synchronizedList(new LinkedList<String>());
            for (String childKey : r.getKeysOfChildren()) {
                // NB:  We're only interested in relationships between 
                // objects in the main catalog (i.e. new_groups);  
                // discard everything else...
                if (new_groups.containsKey(childKey)) {
                    childrenList.add(childKey);
                }
            }
            new_children.put(g.getLocalKey(), childrenList);

            // new_keysByUpperCaseName...
            List<String> groupsWithMyName = new_keysByUpperCaseName.get(g.getName().toUpperCase());
            if (groupsWithMyName == null) {
                // I am the first group with my name (pretty likely)...
                groupsWithMyName = Collections.synchronizedList(new LinkedList<String>());
                new_keysByUpperCaseName.put(g.getName().toUpperCase(), groupsWithMyName);
            }
            groupsWithMyName.add(g.getLocalKey());

        }

        /*
         * Now load the ROOT_GROUP into the collections...
         */

        // new_groups (me)...
        final IEntityGroup root = getRootGroup();
        new_groups.put(root.getLocalKey(), root);

        // new_parents (I am a parent for all groups that have no other parent)...
        List<String> childrenOfRoot = Collections.synchronizedList(new LinkedList<String>());   // for later...
        for (String possibleChildKey : new_groups.keySet()) {
            if (!possibleChildKey.equals(root.getLocalKey()) && !new_parents.containsKey(possibleChildKey)) {
                List<String> p = Collections.synchronizedList(new LinkedList<String>());
                p.add(root.getLocalKey());
                new_parents.put(possibleChildKey, p);
                childrenOfRoot.add(possibleChildKey);   // for later...
            }
        }

        // new_children...
        new_children.put(root.getLocalKey(), childrenOfRoot);

        // new_keysByUpperCaseName...
        List<String> groupsWithMyName = new_keysByUpperCaseName.get(root.getName().toUpperCase());
        if (groupsWithMyName == null) {
            // I am the first group with my name (pretty likely)...
            groupsWithMyName = Collections.synchronizedList(new LinkedList<String>());
            new_keysByUpperCaseName.put(root.getName().toUpperCase(), groupsWithMyName);
        }
        groupsWithMyName.add(root.getLocalKey());

        final long benchmark = System.currentTimeMillis() - timestamp;
        log.info("Refresh of groups tree completed in {} milliseconds", benchmark);
        log.info("Total number of LDAP queries:  {}", queryCounter.getCount() + 1);
        final String msg = "init() :: final size of each collection is as follows..."
                        + "\n\tgroups={}"
                        + "\n\tparents={}"
                        + "\n\tchildren={}"
                        + "\n\tkeysByUpperCaseName={}";
        log.info(msg, new_groups.size(), new_parents.size(), new_children.size(), new_keysByUpperCaseName.size());

        if (log.isTraceEnabled()) {

            StringBuilder sbuilder = new StringBuilder();

            // new_groups...
            sbuilder.setLength(0);
            sbuilder.append("Here are the keys of the new_groups collection:");
            for (String s : new_groups.keySet()) {
                sbuilder.append("\n\t").append(s);
            }
            log.trace(sbuilder.toString());

            // new_parents...
            sbuilder.setLength(0);
            sbuilder.append("Here are the parents of each child in the new_parents collection:");
            for (Map.Entry<String,List<String>> y : new_parents.entrySet()) {
                sbuilder.append("\n\tchild=").append(y.getKey());
                for (String s : y.getValue()) {
                    sbuilder.append("\n\t\tparent=").append(s);
                }
            }
            log.trace(sbuilder.toString());

            // new_children...
            sbuilder.setLength(0);
            sbuilder.append("Here are the children of each parent in the new_children collection:");
            for (Map.Entry<String,List<String>> y : new_children.entrySet()) {
                sbuilder.append("\n\tparent=").append(y.getKey());
                for (String s : y.getValue()) {
                    sbuilder.append("\n\t\tchild=").append(s);
                }
            }
            log.trace(sbuilder.toString());

            // new_keysByUpperCaseName...
            sbuilder.append("Here are the groups that have each name in the new_keysByUpperCaseName collection:");
            for (Map.Entry<String,List<String>> y : new_keysByUpperCaseName.entrySet()) {
                sbuilder.append("\n\tname=").append(y.getKey());
                for (String s : y.getValue()) {
                    sbuilder.append("\n\t\tgroup=").append(s);
                }
            }
            log.trace(sbuilder.toString());

        }

        return new GroupsTree(new_groups, new_parents, new_children, new_keysByUpperCaseName);

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

        public IEntityGroupStore newGroupStore() throws GroupsException {
            return instance;
        }

        public IEntityGroupStore newGroupStore(ComponentGroupServiceDescriptor svcDescriptor) throws GroupsException {
            return instance;
        }

    }

    private static final class GroupsTree {

        // Instance Members.
        private final Map<String,IEntityGroup> groups;
        private final Map<String,List<String>> parents;
        private final Map<String,List<String>> children;
        private final Map<String,List<String>> keysByUpperCaseName;

        /*
         * Public API.
         */

        public GroupsTree(Map<String,IEntityGroup> groups, Map<String,List<String>> parents, 
                                Map<String,List<String>> children, 
                                Map<String,List<String>> keysByUpperCaseName) {

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
            if (keysByUpperCaseName == null) {
                String msg = "Argument 'keysByUpperCaseName' cannot be null.";
                throw new IllegalArgumentException(msg);
            }

            // Instance Members.
            this.groups = groups;
            this.parents = parents;
            this.children = children;
            this.keysByUpperCaseName = keysByUpperCaseName;

        }

        public Map<String,IEntityGroup> getGroups() {
            return groups;
        }

        public Map<String,List<String>> getParents() {
            return parents;
        }

        public Map<String,List<String>> getChildren() {
            return children;
        }

        public Map<String,List<String>> getKeysByUpperCaseName() {
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
