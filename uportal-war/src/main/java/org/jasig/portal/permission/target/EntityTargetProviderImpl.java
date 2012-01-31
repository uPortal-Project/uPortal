package org.jasig.portal.permission.target;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jasig.portal.layout.dlm.remoting.IGroupListHelper;
import org.jasig.portal.layout.dlm.remoting.JsonEntityBean;
import org.jasig.portal.security.IPermission;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * EntityTargetProviderImpl provides uPortal entity keys as targets.  Instances
 * of this implementation may indicate which entity types may be used as targets.
 * Target keys will be the key of the underlying entity itself, while the 
 * target human-readable name will similarly be the name of the entity.
 * 
 * TODO: This implementation currently has a number of problems.  The code
 * uses the EntityEnum class and is hardcoded to only recognize four types 
 * of entities: uPortal person groups, person entities, portlet categories,
 * and portlet entities.  This code also may perform poorly for large
 * portal installations for searches that return many results.
 * 
 * @author Jen Bourey, jbourey@unicon.net
 * @version $Revision$
 * @since 3.3
 */
public class EntityTargetProviderImpl implements IPermissionTargetProvider, Serializable {
    
    private Set<String> allowedEntityTypes = new HashSet<String>();
    
    protected transient final Log log = LogFactory.getLog(getClass());
    
    private transient IGroupListHelper groupListHelper;
    
    @Autowired(required = true)
    public void setGroupListHelper(IGroupListHelper helper) {
        this.groupListHelper = helper;
    }
    
    /**
     * Construct a new instance of targets matching the set of allowed
     * target entity types.
     * 
     * @param allowedEntityTypes
     */
    public EntityTargetProviderImpl(Set<String> allowedEntityTypes) {
        this.allowedEntityTypes = allowedEntityTypes;
    }
    
    /*
     * (non-Javadoc)
     * @see org.jasig.portal.permission.target.IPermissionTargetProvider#getTarget(java.lang.String)
     */
    public IPermissionTarget getTarget(String key) {

        /*
         * If the specified key matches one of the "all entity" style targets,
         * just return the appropriate target.
         */
        
        if (IPermission.ALL_CATEGORIES_TARGET.equals(key)) {
            return getAllCategoriesTarget();
        } else if (IPermission.ALL_PORTLETS_TARGET.equals(key)) {
            return getAllPortletsTarget();
        } else if (IPermission.ALL_GROUPS_TARGET.equals(key)) {
            return getAllGroupsTarget();
        }
        
        /*
         * Attempt to find a matching entity for each allowed entity type.  This
         * implementation will return the first entity that it finds. If the 
         * portal contains duplicate entity keys across multiple types, it's
         * possible that this implementation would demonstrate inconsistent
         * behavior.
         */
        
        for (String type : allowedEntityTypes) {
            JsonEntityBean entity = groupListHelper.getEntity(type, key, false);
            if (entity != null) {
                IPermissionTarget target = new PermissionTargetImpl(entity.getId(), entity.getName());
                return target;
            }
        }
        
        return null;
    }

    /*
     * (non-Javadoc)
     * @see org.jasig.portal.permission.target.IPermissionTargetProvider#searchTargets(java.lang.String)
     */
    public Collection<IPermissionTarget> searchTargets(String term) {

        // Initialize a new collection of matching targets.  We use a HashSet
        // implementation here to prevent duplicate target entries.
        Collection<IPermissionTarget> matching = new HashSet<IPermissionTarget>();

        /*
         * Attempt to find matching entities for each allowed entity type.
         * Any matching entities will be added to our collection.
         */
        
        for (String type : allowedEntityTypes) {
            Set<JsonEntityBean> entities = groupListHelper.search(type, term);
            for (JsonEntityBean entity : entities) {
                IPermissionTarget target = new PermissionTargetImpl(entity.getId(), entity.getName());
                matching.add(target);
            }
        }

        if (IPermission.ALL_CATEGORIES_TARGET.contains(term)) {
            matching.add(getAllCategoriesTarget());
        } else if (IPermission.ALL_PORTLETS_TARGET.contains(term)) {
            matching.add(getAllPortletsTarget());
        } else if (IPermission.ALL_GROUPS_TARGET.contains(term)) {
            matching.add(getAllGroupsTarget());
        }

        // return the list of matching targets
        return matching;
    }

    /**
     * Return an IPermissionTarget matching all categories.  This target will
     * represent any generic category-type entity, allowing administrators
     * to set permissions on ungrouped entity items.
     * 
     * @return all categories target
     */
    protected IPermissionTarget getAllCategoriesTarget() {
        return new PermissionTargetImpl(IPermission.ALL_CATEGORIES_TARGET,
                IPermission.ALL_CATEGORIES_TARGET);
    }

    /**
     * Return an IPermissionTarget matching all person groups.  This target will
     * represent any generic person group-type entity, allowing administrators
     * to set permissions on ungrouped entity items.
     * 
     * @return all groups target
     */
    protected IPermissionTarget getAllGroupsTarget() {
        return new PermissionTargetImpl(IPermission.ALL_GROUPS_TARGET,
                IPermission.ALL_GROUPS_TARGET);
    }

    /**
     * Return an IPermissionTarget matching all portlets.  This target will
     * represent any generic portlet-type entity, allowing administrators
     * to set permissions on ungrouped entity items.
     * 
     * @return all portlets target
     */
    protected IPermissionTarget getAllPortletsTarget() {
        return new PermissionTargetImpl(IPermission.ALL_PORTLETS_TARGET,
                IPermission.ALL_PORTLETS_TARGET);
    }
    
}
