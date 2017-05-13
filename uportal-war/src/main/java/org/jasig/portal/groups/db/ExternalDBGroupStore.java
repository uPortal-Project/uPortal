/*
 * Copyright 2014 Jasig.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jasig.portal.groups.db;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jasig.portal.EntityIdentifier;
import org.jasig.portal.groups.EntityImpl;
import org.jasig.portal.groups.EntityTestingGroupImpl;
import org.jasig.portal.groups.GroupsException;
import org.jasig.portal.groups.IEntity;
import org.jasig.portal.groups.IEntityGroup;
import org.jasig.portal.groups.IEntityGroupStore;
import org.jasig.portal.groups.IEntitySearcher;
import org.jasig.portal.groups.IEntityStore;
import org.jasig.portal.groups.IGroupConstants;
import org.jasig.portal.groups.IGroupMember;
import org.jasig.portal.groups.ILockableEntityGroup;
import org.jasig.portal.security.IPerson;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

/**
 *
 * @author Chris White <christopher.white@manchester.ac.uk>
 */
public class ExternalDBGroupStore implements IEntityGroupStore, IEntityStore, IEntitySearcher {

    public ExternalDBGroupStore(ApplicationContext context) {
        this.spring_context = context;
        spring_context.getAutowireCapableBeanFactory().autowireBean(this);
    }

    @Autowired ThreadPoolTaskExecutor executor;
    @Autowired ThreadPoolTaskScheduler scheduler;

    @Override
    public void finalize() throws Throwable {
        log.info("Closing Scheduled tasks -- ");
        ExecutorService pool = scheduler.getScheduledExecutor();
            log.info("Disbale new tasks from being submitted");
           pool.shutdown(); // Disable new tasks from being submitted
            try {
                log.warn("waiting for existing tasks to teminate");
              // Wait a while for existing tasks to terminate
              if (!pool.awaitTermination(5, TimeUnit.SECONDS)) {
                  log.warn("Cancel currently executing tasks");
                pool.shutdownNow(); // Cancel currently executing tasks
                // Wait a while for tasks to respond to being cancelled
                log.warn("Waiting for tasks to respond to being cancelled");
                if (!pool.awaitTermination(5, TimeUnit.SECONDS))
                    log.error("Pool did not terminate");
//                    System.err.println("Pool did not terminate");
              }
            } catch (InterruptedException ie) {
              // (Re-)Cancel if current thread also interrupted
              pool.shutdownNow();
              // Preserve interrupt status
              Thread.currentThread().interrupt();
            }
            
//        scheduler.getScheduledExecutor().shutdownNow();
        scheduler.shutdown();
        executor.shutdown();
        try {
            if (spring_context instanceof AbstractApplicationContext)
                ((AbstractApplicationContext)this.spring_context).close();
        } finally {
            super.finalize();
        }
        
    }       

    
    private final Log log = LogFactory.getLog(getClass());
    
//    public static final String ROOT_KEY = "db_root";
    public static final String ROOT_KEY = "DB Root";
    public static final String ROOT_DESC = "A root group provided for the ExternalDBGroupStore.";
    public static final IEntityGroup ROOT_GROUP = createRootGroup();
    
    @Value(value = "${GroupStore.GroupQuery}")
    private String query = "";
    @Value(value = "${GroupStore.GroupMemberQuery}")
    private String groupMemberQuery = "";
    @Value(value = "${GroupStore.GroupMembershipQuery}")
    private String groupMembershipQuery = "";
    @Value(value = "${GroupStore.GroupEntityMembersQuery}")
    private String groupEntityMembersQuery = "";
    private ApplicationContext spring_context = null;
    private final Map<String,IEntityGroup> groups = new HashMap<String, IEntityGroup>();

    @Autowired
    private JdbcTemplate jdbcTemplate;
    
    
    @Scheduled(cron = "${GroupStore.update.cron}")
    private void updateSchedule() {
        this.updateGroups();
    }
    
    @Async
    protected void updateGroups() {
        List<Map<String, Object>> groupList = jdbcTemplate.queryForList(query);
            
            Map<String, IEntityGroup> newGroups  = new HashMap<String, IEntityGroup>();
            
            for (Map<String,Object> groupDef : groupList) {
                IEntityGroup group = new EntityTestingGroupImpl((String) groupDef.get("group_name"), IPerson.class);
                group.setName((String) groupDef.get("group_name"));
                group.setDescription((String) groupDef.get("group_name"));
                newGroups.put(group.getName(), group);
            }
            
            synchronized (groups) {
                groups.clear();
                groups.putAll(newGroups);
            }
            log.warn(groups);
    }
    
    @Override
    public boolean contains(IEntityGroup group, IGroupMember member) throws GroupsException {
        log.warn("Contains ?  "+group.getKey() + " : "+member.getKey());
        if (member.getEntityType() != IPerson.class) return false;

        return !jdbcTemplate.queryForList(groupMemberQuery, group.getName(), member.getKey()).isEmpty();
//        int rows = .size();
//        return rows > 0;
        
    }

    @Override
    public void delete(IEntityGroup group) throws GroupsException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public IEntityGroup find(String key) throws GroupsException {
        if (key.equals(ROOT_GROUP.getLocalKey())) return ROOT_GROUP;
        return groups.get(key);
        
    }

    @Override
    public Iterator findContainingGroups(IGroupMember gm) throws GroupsException {
        log.warn("Finding Containing Groups of "+gm.getKey());
        List<IEntityGroup> rslt = new LinkedList<IEntityGroup>();
        if (gm.isGroup()) {
            IEntityGroup group = (IEntityGroup) gm;
//            if (groups.containsKey(group.getLocalKey())) rslt.add(ROOT_GROUP);
            if (groups.containsKey(group.getLocalKey())) rslt.add(ROOT_GROUP);
        } else if (gm.isEntity() && gm.getEntityType().equals(ROOT_GROUP.getEntityType())) {	
            
            List<Map<String, Object>> groupMemberships = jdbcTemplate.queryForList(groupMembershipQuery, gm.getKey());
            for (Map<String,Object> row : groupMemberships) {
                if (groups.containsKey(row.get("group_name")))
                    rslt.add(groups.get(row.get("group_name")));
            }
        } 
        log.warn(rslt);
        return rslt.iterator();
    }

    @Override
    public Iterator findEntitiesForGroup(IEntityGroup group) throws GroupsException {
        log.warn("entities ?  "+group.getLocalKey());
        if (groups.containsKey(group.getLocalKey())) {

            List<Map<String, Object>> rows = jdbcTemplate.queryForList(groupEntityMembersQuery, group.getName());

            List<IEntity> entities = new ArrayList<IEntity>();
            for (Map<String, Object> row : rows) {
                IEntity entity = newInstance((String) row.get("username"), IPerson.class);
                entities.add(entity);
            }
            
            return entities.iterator();
        } else {
            return findMemberGroups(group);
        }
        

    }

    @Override
    public ILockableEntityGroup findLockable(String key) throws GroupsException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public String[] findMemberGroupKeys(IEntityGroup group) throws GroupsException {
//        if (group.getLocalKey().equals(ROOT_GROUP.getKey())) 
        log.warn("Finding Members keys of "+group.getKey());
        if (group.getKey().equals(ROOT_GROUP.getKey())) 
            return groups.keySet().toArray(new String[0]);
        return new String[0];
    }

    @Override
    public Iterator findMemberGroups(IEntityGroup group) throws GroupsException {
//        if (group.getLocalKey().equals(ROOT_GROUP.getLocalKey())) return groups.values().iterator();
        log.warn("Finding Members of "+group.getKey());
        if (group.getKey().equals(ROOT_GROUP.getKey())) return groups.values().iterator();
        return Collections.emptyIterator();
    }

    @Override
    public IEntityGroup newInstance(Class entityType) throws GroupsException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public EntityIdentifier[] searchForGroups(String query, int method, Class leaftype) throws GroupsException {
        log.warn("Search :: "+query + " : "+leaftype.getCanonicalName());
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

    	log.warn("Search :REGEXP: "+regex + " =?= "+ROOT_GROUP.getLocalKey().toUpperCase() +" "+ (ROOT_GROUP.getLocalKey().toUpperCase().matches(regex)));
        List<EntityIdentifier> rslt = new LinkedList<EntityIdentifier>(); 
        if (ROOT_GROUP.getLocalKey().toUpperCase().matches(regex)) rslt.add(ROOT_GROUP.getEntityIdentifier());
    	for (Map.Entry<String, IEntityGroup> y : groups.entrySet()) {
    		if (y.getKey().toUpperCase().matches(regex)) {
    			rslt.add(y.getValue().getEntityIdentifier());
    		}
    	}
    	log.warn(rslt);
    	return  rslt.toArray(new EntityIdentifier[rslt.size()]);
        
    }

    @Override
    public void update(IEntityGroup group) throws GroupsException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void updateMembers(IEntityGroup group) throws GroupsException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    
    
    
    private static IEntityGroup createRootGroup() {
        
        IEntityGroup rslt = new EntityTestingGroupImpl(ROOT_KEY, IPerson.class);
        rslt.setCreatorID("System");
        rslt.setName(ROOT_KEY);
        rslt.setDescription(ROOT_DESC);
        
        return rslt;

    }
    
        @Override
    public IEntity newInstance(String key) throws GroupsException {
    	return newInstance(key, null);
    }

    @Override
    public IEntity newInstance(String key, Class type) throws GroupsException {
    	return new EntityImpl(key, type);
    }

    @Override
    public EntityIdentifier[] searchForEntities(String query, int method, Class type) throws GroupsException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

}
