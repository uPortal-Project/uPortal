/**
 * Copyright 2008 The JA-SIG Collaborative.  All rights reserved.
 * See license distributed with this file and
 * available online at http://www.uportal.org/license.html
 */

package org.jasig.portal.groups.smartldap;

import java.net.URL;
import java.util.Collections;
import java.util.Iterator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.danann.cernunnos.Task;
import org.danann.cernunnos.runtime.RuntimeRequestResponse;
import org.danann.cernunnos.runtime.ScriptRunner;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.FileSystemXmlApplicationContext;

import org.jasig.portal.EntityIdentifier;
import org.jasig.portal.groups.ComponentGroupServiceDescriptor;
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
import org.jasig.portal.services.PersonDirectory;

public class SmartLdapGroupStore implements IEntityGroupStore {
		
    // Instance Members.
    private ApplicationContext spring_context = null;
    private final ScriptRunner runner;
    private final Task initTask;
    private final Log log = LogFactory.getLog(getClass());
    private boolean initialized;
    
    /*
     * Indexed Collections.
     */
    
    /**
     * Map of all groups keyed by 'key' (DN).  Includes ROOT_GROUP.
     */
    private Map<String,IEntityGroup> groups;
    
    /**
     * Map of all parent relationships keyed by the 'key' (DN) of the child;  
     * the values are lists of the 'keys' (DNs) of its parents.  
     * Includes ROOT_GROUP.
     */
    private Map<String,List<String>> parents;
    
    /**
     * Map of all child relationships keyed by the 'key' (DN) of the parent;  
     * the values are lists of the 'keys' (DNs) of its children.  
     * Includes ROOT_GROUP.
     */
    private Map<String,List<String>> children;
    
    /**
     * Map of all 'keys' (DNs) of SmartLdap managed groups indexed by group 
     * name in upper case.  Includes ROOT_GROUP.
     */
    private Map<String,List<String>> keysByUpperCaseName;
    
    /*
     * Public API.
     */

    public static final String UNSUPPORTED_MESSAGE = 
            "The SmartLdap implementation of JA-SIG Groups and Permissions (GaP) " +
            "does not support this operation.";

    public static final String ROOT_KEY = "SmartLdap ROOT";
    public static final String ROOT_DESC = "A root group provided for the SmartLdapGroupStore.";
	
    public static final EntityIdentifier ENTITY_IDENTIFIER = new EntityIdentifier(ROOT_KEY, 
											org.jasig.portal.groups.IEntityGroup.class);
	
    public static final IEntityGroup ROOT_GROUP;
    static {
	    
	    try {
	        ROOT_GROUP = new EntityTestingGroupImpl(ROOT_KEY, IPerson.class);
	        ROOT_GROUP.setCreatorID("System");
	        ROOT_GROUP.setName(ROOT_KEY);
	        ROOT_GROUP.setDescription(ROOT_DESC);
	    } catch (Throwable t) {
            throw new RuntimeException(t);
	    }

    }

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
    	
    	if (!initialized) {
    		init();
    	}

    	if (log.isDebugEnabled()) {
    		log.debug("Invoking find() for key:  " + key);
    	}
    	
    	// All of our groups (incl. ROOT_GROUP) 
    	// are indexed in the 'groups' map by key...
    	return groups.get(key);
    
    }

    /**
     * Returns an <code>Iterator</code> over the <code>Collection</code> of
     * <code>IEntityGroups</code> that the <code>IGroupMember</code> belongs to.
     * @return java.util.Iterator
     * @param gm org.jasig.portal.groups.IEntityGroup
     */
    public Iterator findContainingGroups(IGroupMember gm) throws GroupsException {
    	
    	if (!initialized) {
    		init();
    	}

    	List<IEntityGroup> rslt = new LinkedList<IEntityGroup>();
    	if (gm.isGroup()) {		// Check the local indeces...
    		IEntityGroup group = (IEntityGroup) gm;
    		List<String> list = parents.get(group.getLocalKey());
    		if (list != null) {
    			// should only reach this code if its a SmartLdap managed group...
        		for (String s : list) {
        			rslt.add(groups.get(s));
        		}
    		}
    	} else if (gm.isEntity()) {	// Ask the individual...

    		// Build an IPerson...
    		EntityIdentifier ei = gm.getUnderlyingEntityIdentifier();
    		Map<String,List<Object>> seed = new HashMap<String,List<Object>>();
    		List<Object> seedValue = new LinkedList<Object>();
    		seedValue.add(ei.getKey());
    		seed.put(IPerson.USERNAME, seedValue);
    		Map<String,List<Object>> attr = PersonDirectory.getPersonAttributeDao().getMultivaluedUserAttributes(seed);
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
    				if (groups.containsKey(s)) {
            			rslt.add(groups.get(s));
    				}
        		}
    		}
    		
    	} else {
    		// WTF...
    	    log.warn("The specified IGroupMember is neither a group nor an entity:  " + gm.getKey());
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
        

    	if (!initialized) {
    		init();
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

    	if (!initialized) {
    		init();
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

    	if (!initialized) {
    		init();
    	}

    	if (log.isDebugEnabled()) {
    		log.debug("Invoking findMemberGroups() for group:  " + group.getLocalKey());
    	}

    	List<IEntityGroup> rslt = new LinkedList<IEntityGroup>();
    	
    	List<String> list = children.get(group.getLocalKey());
    	if (list != null) {
			// should only reach this code if its a SmartLdap managed group...
    		for (String s : list) {
    			rslt.add(groups.get(s));
    		}
    	}
    	
    	return rslt.iterator();

    }

    public IEntityGroup newInstance(Class entityType) throws GroupsException {
        log.warn("Unsupported method accessed:  SmartLdapGroupStore.newInstance");
        throw new UnsupportedOperationException(UNSUPPORTED_MESSAGE);
    }

    public EntityIdentifier[] searchForGroups(String query, int method, Class leaftype) throws GroupsException {

    	if (!initialized) {
    		init();
    	}

    	if (log.isDebugEnabled()) {
    		log.debug("Invoking searchForGroups():  query=" + query + ", method=" 
    				+ method + ", leaftype=" + leaftype.getClass().getName());
    	}

    	// We only match the IPerson leaf type...
    	if (!leaftype.equals(IPerson.class)) {
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
    	for (Map.Entry<String,List<String>> y : keysByUpperCaseName.entrySet()) {
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
    
    public synchronized void init() {
    	
    	if (initialized) {
    		return;
    	}
    	
    	// Replace the old with the new...
        InitBundle bundle = prepareInitBundle();
        this.groups = bundle.getGroups();
        this.parents = bundle.getParents();
        this.children = bundle.getChildren();
        this.keysByUpperCaseName = bundle.getKeysByUpperCaseName();
        
        // Set the 'initialized' flag...
        this.initialized = true;

    }

    /*
     * Package API.
     */
    
    InitBundle prepareInitBundle() {
        
        // Prepare the new local indeces...
        Map<String,IEntityGroup> new_groups = Collections.synchronizedMap(new HashMap<String,IEntityGroup>());
        Map<String,List<String>> new_parents = Collections.synchronizedMap(new HashMap<String,List<String>>());
        Map<String,List<String>> new_children = Collections.synchronizedMap(new HashMap<String,List<String>>());
        Map<String,List<String>> new_keysByUpperCaseName = Collections.synchronizedMap(new HashMap<String,List<String>>());

        // Gather IEntityGroup objects from LDAP...
        RuntimeRequestResponse req = new RuntimeRequestResponse();
        List<LdapRecord> list = new LinkedList<LdapRecord>();
        req.setAttribute("GROUPS", list);
        for (String name : spring_context.getBeanDefinitionNames()) {
            req.setAttribute(name, spring_context.getBean(name));
        }
        runner.run(initTask, req);
        
        if (log.isInfoEnabled()) {
            String msg = "init() found " + list.size() + " records.";
            log.info(msg);
        }
        
        // Do a first loop to build the main catalog (new_groups)...
        for (LdapRecord r : list) {
            
            // new_groups (me)...
            IEntityGroup g = r.getGroup();
            new_groups.put(g.getLocalKey(), g);

        }
        
        // Do a second loop to build local indeces...
        for (LdapRecord r : list) {

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

        return new InitBundle(new_groups, new_parents, new_children, new_keysByUpperCaseName);

    }

    /*
     * Implementation.
     */

    private SmartLdapGroupStore() {
        
    	// Spring tech...
    	URL u = getClass().getResource("/properties/groups/SmartLdapGroupStoreConfig.xml");
		spring_context = new FileSystemXmlApplicationContext(u.toExternalForm());

		// Cernunnos tech...
		runner = new ScriptRunner();
        initTask = runner.compileTask(getClass().getResource("init.crn").toExternalForm());
        
        // SmartLdapGroupStore will be initialized on first use...
        initialized = false;

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
    
    private static final class InitBundle {
        
        // Instance Members.
        private final Map<String,IEntityGroup> groups;
        private final Map<String,List<String>> parents;
        private final Map<String,List<String>> children;
        private final Map<String,List<String>> keysByUpperCaseName;
        
        /*
         * Public API.
         */
        
        public InitBundle(Map<String,IEntityGroup> groups, Map<String,List<String>> parents, 
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

}
