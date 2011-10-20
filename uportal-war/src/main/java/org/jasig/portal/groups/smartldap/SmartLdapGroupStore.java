/**
 * Licensed to Jasig under one or more contributor license
 * agreements. See the NOTICE file distributed with this work
 * for additional information regarding copyright ownership.
 * Jasig licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a
 * copy of the License at:
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.jasig.portal.groups.smartldap;

import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
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
import org.jasig.portal.spring.locator.PersonAttributeDaoLocator;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.FileSystemXmlApplicationContext;

public final class SmartLdapGroupStore implements IEntityGroupStore {
		
    // Instance Members.
    private ApplicationContext spring_context = null;
    
    /**
     * Period after which SmartLdap will drop and rebuild the groups tree.  May 
     * be overridden in SmartLdapGroupStoreConfix.xml.  A value of zero or less 
     * (negative) disables this feature.
     */
    private long groupsTreeRefreshIntervalSeconds = 900;  // default

    /**
     * Timestamp (milliseconds) of the last tree refresh.
     */
    private volatile long lastTreeRefreshTime = 0;

    private final ScriptRunner runner;
    private final Task initTask;
    private final Log log = LogFactory.getLog(getClass());
    
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

    public static final IEntityGroup ROOT_GROUP = createRootGroup();

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

    	if (log.isDebugEnabled()) {
    		log.debug("Invoking find() for key:  " + key);
    	}
    	
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

    	List<IEntityGroup> rslt = new LinkedList<IEntityGroup>();
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
    	} else if (gm.isEntity() && gm.getEntityType().equals(ROOT_GROUP.getEntityType())) {	
    	    
    	    // Ask the individual...
    		EntityIdentifier ei = gm.getUnderlyingEntityIdentifier();
    		Map<String,List<Object>> seed = new HashMap<String,List<Object>>();
    		List<Object> seedValue = new LinkedList<Object>();
    		seedValue.add(ei.getKey());
    		seed.put(IPerson.USERNAME, seedValue);
    		Map<String,List<Object>> attr = PersonAttributeDaoLocator.getPersonAttributeDao().getMultivaluedUserAttributes(seed);
            // avoid NPEs and unnecessary IPerson creation
            if (attr != null && !attr.isEmpty()) {
                IPerson p = PersonFactory.createPerson();
                p.setAttributes(attr);

                // Analyze its memberships...
                String attrName = (String) spring_context.getBean("memberOfAttributeName");
                Object groupKeys = p.getAttributeValues(attrName);
                // IPerson returns null if no value is defined for this attribute...
                if (groupKeys != null) {

                    List<String> list = new LinkedList<String>();
                    if (groupKeys instanceof String) {
                        list.add((String) groupKeys);
                    } else if (groupKeys instanceof Object[]) {
                        Object[] objs = (Object[]) groupKeys;
                        for (Object o : objs) {
                            list.add((String) o);
                        }
                    } else if (groupKeys instanceof List) {
                        List<?> objs = (List<?>) groupKeys;
                        for (Object o : objs) {
                            list.add((String) o);
                        }
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

    	if (log.isDebugEnabled()) {
    		log.debug("Invoking findEntitiesForGroup() for group:  " + group.getLocalKey());
    	}
    	
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

    	if (log.isDebugEnabled()) {
    		log.debug("Invoking findMemberGroupKeys() for group:  " + group.getLocalKey());
    	}

    	List<String> rslt = new LinkedList<String>();
    	for (Iterator it=findMemberGroups(group); it.hasNext();) {
    		IEntityGroup g = (IEntityGroup) it.next();
    		// Return composite keys here...
    		rslt.add(g.getKey());
    	}
    	
    	return rslt.toArray(new String[0]);
    	
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

    	if (log.isDebugEnabled()) {
    		log.debug("Invoking findMemberGroups() for group:  " + group.getLocalKey());
    	}

    	List<IEntityGroup> rslt = new LinkedList<IEntityGroup>();
    	
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

    	if (log.isDebugEnabled()) {
    		log.debug("Invoking searchForGroups():  query=" + query + ", method=" 
    				+ method + ", leaftype=" + leaftype.getClass().getName());
    	}

    	// We only match the IPerson leaf type...
    	if (!leaftype.equals(ROOT_GROUP.getEntityType())) {
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
    	String regex = null;
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
    	
    	List<EntityIdentifier> rslt = new LinkedList<EntityIdentifier>(); 
    	for (Map.Entry<String,List<String>> y : groupsTree.getKeysByUpperCaseName().entrySet()) {
    		if (y.getKey().matches(regex)) {
    			List<String> keys = y.getValue();
    			for (String k : keys) {
    				rslt.add(new EntityIdentifier(k, IEntityGroup.class));
    			}
    		}
    	}
    	
    	return rslt.toArray(new EntityIdentifier[0]);

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
        List<String> filteredChildren = new ArrayList<String>();
        for (String key : keysOfChildren) {
            if (!groupChain.contains(key)) {
                filteredChildren.add(key);
            } else {
                // Circular reference detected!
                StringBuilder msg = new StringBuilder();
                msg.append("Circular reference detected and removed for the following groups:  '")
                                                .append(key).append("' and '")
                                                .append(record.getGroup().getLocalKey()).append("'");
                log.warn(msg.toString());
            }
        }
        if (filteredChildren.size() < keysOfChildren.size()) {
            rslt = new LdapRecord(record.getGroup(), filteredChildren);
        }
        
        return rslt;

    }
    
    public boolean hasUndiscoveredChildrenWithinDn(LdapRecord record, String baseDn, Set<LdapRecord> groupsSet) {
        
        boolean rslt = false;  // default

        for (String childKey : record.getKeysOfChildren()) {
            if (childKey.endsWith(baseDn)) {
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
                    if (log.isTraceEnabled()) {
                        log.trace("Child group is already in collection:  " + childKey);
                    }
                }
            }
        }
        
        if (log.isTraceEnabled()) {
            log.trace("Query for children of parent group '" + record.getGroup().getLocalKey() + "':  " + rslt);
        }

        return rslt;
        
    }

    /*
     * Implementation.
     */
    
    private static IEntityGroup createRootGroup() {
        
        IEntityGroup rslt = new EntityTestingGroupImpl(ROOT_KEY, IPerson.class);
        rslt.setCreatorID("System");
        rslt.setName(ROOT_KEY);
        rslt.setDescription(ROOT_DESC);
        
        return rslt;

    }

    private SmartLdapGroupStore() {
        
    	// Spring tech...
    	URL u = getClass().getResource("/properties/groups/SmartLdapGroupStoreConfig.xml");
		spring_context = new FileSystemXmlApplicationContext(u.toExternalForm());
		
		// Interval between tree rebuilds
		if (spring_context.containsBean("groupsTreeRefreshIntervalSeconds")) {
		    groupsTreeRefreshIntervalSeconds = (Long) spring_context.getBean("groupsTreeRefreshIntervalSeconds");
		}

		// Cernunnos tech...
		runner = new ScriptRunner();
        initTask = runner.compileTask(getClass().getResource("init.crn").toExternalForm());

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
        Set<LdapRecord> set = new HashSet<LdapRecord>();
        req.setAttribute("GROUPS", set);
        req.setAttribute("smartLdapGroupStore", this);
        SubQueryCounter queryCounter = new SubQueryCounter();
        req.setAttribute("queryCounter", queryCounter);
        req.setAttribute("baseFilter", spring_context.getBean("filter"));
        for (String name : spring_context.getBeanDefinitionNames()) {
            req.setAttribute(name, spring_context.getBean(name));
        }
        runner.run(initTask, req);
        
        if (log.isInfoEnabled()) {
            String msg = "init() found " + set.size() + " records.";
            log.info(msg);
        }
        
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
        new_groups.put(ROOT_GROUP.getLocalKey(), ROOT_GROUP);

        // new_parents (I am a parent for all groups that have no other parent)...
        List<String> childrenOfRoot = Collections.synchronizedList(new LinkedList<String>());   // for later...
        for (String possibleChildKey : new_groups.keySet()) {
            if (!possibleChildKey.equals(ROOT_GROUP.getLocalKey()) && !new_parents.containsKey(possibleChildKey)) {
                List<String> p = Collections.synchronizedList(new LinkedList<String>());
                p.add(ROOT_GROUP.getLocalKey());
                new_parents.put(possibleChildKey, p);
                childrenOfRoot.add(possibleChildKey);   // for later...
            }
        }
        
        // new_children...
        new_children.put(ROOT_GROUP.getLocalKey(), childrenOfRoot);
        
        // new_keysByUpperCaseName...
        List<String> groupsWithMyName = new_keysByUpperCaseName.get(ROOT_GROUP.getName().toUpperCase());
        if (groupsWithMyName == null) {
            // I am the first group with my name (pretty likely)...
            groupsWithMyName = Collections.synchronizedList(new LinkedList<String>());
            new_keysByUpperCaseName.put(ROOT_GROUP.getName().toUpperCase(), groupsWithMyName);
        }
        groupsWithMyName.add(ROOT_GROUP.getLocalKey());

        if (log.isInfoEnabled()) {
            long benchmark = System.currentTimeMillis() - timestamp;
            log.info("Refresh of groups tree completed in " + benchmark + " milliseconds");
            log.info("Total number of LDAP queries:  " + (queryCounter.getCount() + 1));
            String msg = "init() :: final size of each collection is as follows..."
                            + "\n\tgroups=" + new_groups.size()
                            + "\n\tparents=" + new_parents.size()
                            + "\n\tchildren=" + new_children.size()
                            + "\n\tkeysByUpperCaseName=" + new_keysByUpperCaseName.size();
            log.info(msg);
        }
        
        if (log.isTraceEnabled()) {
            
            StringBuilder msg = new StringBuilder();

            // new_groups...
            msg.setLength(0);
            msg.append("Here are the keys of the new_groups collection:");
            for (String s : new_groups.keySet()) {
                msg.append("\n\t").append(s);
            }
            log.trace(msg.toString());
            
            // new_parents...
            msg.setLength(0);
            msg.append("Here are the parents of each child in the new_parents collection:");
            for (Map.Entry<String,List<String>> y : new_parents.entrySet()) {
                msg.append("\n\tchild=").append(y.getKey());
                for (String s : y.getValue()) {
                    msg.append("\n\t\tparent=").append(s);
                }
            }
            log.trace(msg.toString());
            
            // new_children...
            msg.setLength(0);
            msg.append("Here are the children of each parent in the new_children collection:");
            for (Map.Entry<String,List<String>> y : new_children.entrySet()) {
                msg.append("\n\tparent=").append(y.getKey());
                for (String s : y.getValue()) {
                    msg.append("\n\t\tchild=").append(s);
                }
            }
            log.trace(msg.toString());
            
            // new_keysByUpperCaseName...
            msg.append("Here are the groups that have each name in the new_keysByUpperCaseName collection:");
            for (Map.Entry<String,List<String>> y : new_keysByUpperCaseName.entrySet()) {
                msg.append("\n\tname=").append(y.getKey());
                for (String s : y.getValue()) {
                    msg.append("\n\t\tgroup=").append(s);
                }
            }
            log.trace(msg.toString());
            
        }

        return new GroupsTree(new_groups, new_parents, new_children, new_keysByUpperCaseName);

    }

    /*
     * Nested Types.
     */

    public static final class Factory implements IEntityGroupStoreFactory {
        
        private static final IEntityGroupStore INSTANCE = new SmartLdapGroupStore();
        
        /*
         * Public API.
         */

        public IEntityGroupStore newGroupStore() throws GroupsException {
            return INSTANCE;
        }
    
        public IEntityGroupStore newGroupStore(ComponentGroupServiceDescriptor svcDescriptor) throws GroupsException {
            return INSTANCE;
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
        
        public void increment() {
            ++count;
        }
        
        public int getCount() {
            return count;
        }
        
    }

}
