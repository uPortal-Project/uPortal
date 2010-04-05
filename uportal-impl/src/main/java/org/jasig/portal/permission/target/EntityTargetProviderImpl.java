package org.jasig.portal.permission.target;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jasig.portal.groups.IEntityGroup;
import org.jasig.portal.groups.IGroupMember;
import org.jasig.portal.portlets.groupselector.EntityEnum;
import org.jasig.portal.services.EntityNameFinderService;
import org.jasig.portal.services.GroupService;

/**
 * EntityTargetProviderImpl provides uPortal entity keys as targets.  Instances
 * of this implementation may indicate which entity types may be used as targets.
 * Target keys will be the key of the underlying entity itself, while the 
 * target human-readable name will similarly be the naem of the entity.
 * 
 * TODO: This implementation currently has a number of problems.  The code
 * uses the EntityEnum class and is hardcoded to only recognize four types 
 * of entities: uPortal person groups, person entities, portlet categories,
 * and portlet entities.  This code is also likely to perform poorly for large
 * portal installations and isn't capable of updating it's target map when
 * entities and entity membership information in the portal is updated.  Much
 * of the current logic is copied from the CGroupsManager initialization method.
 * 
 * @author Jen Bourey, jbourey@unicon.net
 * @version $Revision$
 * @since 3.3
 */
public class EntityTargetProviderImpl implements IPermissionTargetProvider, Serializable {
    
    private Set<EntityEnum> allowedEntityTypes = new HashSet<EntityEnum>();
    
    private Map<String, IPermissionTarget> targets = new HashMap<String, IPermissionTarget>();
    
    protected final Log log = LogFactory.getLog(getClass());
    
    /**
     * Construct a new instance of targets matching the set of allowed
     * target entity types.
     * 
     * @param allowedEntityTypes
     */
    public EntityTargetProviderImpl(Set<String> allowedEntityTypes) {
        for (String type : allowedEntityTypes) {
            this.allowedEntityTypes.add(EntityEnum.getEntityEnum(type));
        }
        init();
    }
    
    /**
     * Initialize this entity target provider.
     */
    private void init() {
        
        boolean personGroup = allowedEntityTypes.contains(EntityEnum.GROUP);
        boolean portletGroup = allowedEntityTypes.contains(EntityEnum.CATEGORY);
        boolean person = allowedEntityTypes.contains(EntityEnum.PERSON);
        boolean portlet = allowedEntityTypes.contains(EntityEnum.CHANNEL);
        
        if (personGroup || person) {
            initType(GroupService.EVERYONE, personGroup, person);
        }
        
        if (portletGroup || portlet) {
            initType(GroupService.CHANNEL_CATEGORIES, portletGroup, portlet);
        }

    }
    
    /**
     * Add all allowed members of this root group.
     * 
     * @param root
     * @param includeGroups
     * @param includeIndividuals
     */
    private void initType(String root, boolean includeGroups, boolean includeIndividuals) {

        IEntityGroup rootGroup = GroupService.getDistinguishedGroup(root);
        
        // if we've been asked to include groups, add the group itself to the 
        // target map
        if (includeGroups) {
            addGroupMember(rootGroup);
        }

        // iterate through the group's membership list, adding all allowed
        // entities
        @SuppressWarnings("unchecked")
        Iterator allgroups = rootGroup.getAllMembers();
        while (allgroups.hasNext()) {
            IGroupMember g = (IGroupMember) allgroups.next();
            if ((g.isGroup() && includeGroups) || (g.isEntity() && includeIndividuals)) {
                addGroupMember(g);
            }
        }

    }
    
    /**
     * Construct a target object from a group member to our internal target map.
     * 
     * @param member
     */
    private void addGroupMember(IGroupMember member) {
        
        // if the target hasn't yet been added to our map, add it now
        if (targets.get(member.getKey()) == null) {

            try {
                
                // get the name for this target
                String name = null;
                if (member instanceof IEntityGroup) {
                    IEntityGroup group = (IEntityGroup) member;
                    name = group.getName();
                } else {
                    // TODO: We don't actually want to be doing this for
                    // thousands of users.  Ack!
                    name = EntityNameFinderService.instance().getNameFinder(
                            member.getClass()).getName(member.getKey());
                }
                
                // create a new target for this key and name
                IPermissionTarget target = new PermissionTargetImpl(member.getKey(), name);
                
                // add the target to our map
                targets.put(member.getKey(), target);
                
            } catch (Exception e) {
                log.error("init():: unable to add target", e);
            }
        }
    }
    

    /*
     * (non-Javadoc)
     * @see org.jasig.portal.permission.target.IPermissionTargetProvider#getTarget(java.lang.String)
     */
    public IPermissionTarget getTarget(String key) {
        return targets.get(key);
    }

    /*
     * (non-Javadoc)
     * @see org.jasig.portal.permission.target.IPermissionTargetProvider#getTargetKeys()
     */
    public Set<String> getTargetKeys() {
        return targets.keySet();
    }

    /*
     * (non-Javadoc)
     * @see org.jasig.portal.permission.target.IPermissionTargetProvider#getTargets()
     */
    public Collection<IPermissionTarget> getTargets() {
        return targets.values();
    }

}
