package org.jasig.portal.permission.dao.jpa;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jasig.portal.permission.IPermissionActivity;
import org.jasig.portal.permission.IPermissionOwner;
import org.jasig.portal.permission.dao.IPermissionOwnerDao;
import org.springframework.dao.support.DataAccessUtils;

/**
 * JpaPermissionOwnerDao provides a default JPA/Hibernate implementation of
 * the IPermissionOwnerDao interface.
 * 
 * @author Jen Bourey, jbourey@unicon.net
 * @version $Revision$
 * @since 3.3
 */
public class JpaPermissionOwnerDao implements IPermissionOwnerDao {

    private static final String FIND_ALL_PERMISSION_OWNERS = "from PermissionOwnerImpl owner";
    private static final String FIND_PERMISSION_OWNER_BY_FNAME = 
        "from PermissionOwnerImpl owner where owner.fname = :fname";
    private static final String FIND_PERMISSION_ACTIVITY_BY_OWNER_ID = 
        "from PermissionActivityImpl activity left join fetch activity.owner where activity.owner.id = :ownerId and activity.fname = :fname";
    private static final String FIND_PERMISSION_ACTIVITY_BY_OWNER_FNAME = 
        "from PermissionActivityImpl activity left join fetch activity.owner where activity.owner.fname = :ownerFname and activity.fname = :activityFname";

    protected final Log log = LogFactory.getLog(getClass());

    private EntityManager entityManager;

    /**
     * @param entityManager
     *            the entityManager to set
     */
    @PersistenceContext(unitName="uPortalPersistence")
    public void setEntityManager(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.jasig.portal.permissions.dao.IPermissionOwnerDao#getAllPermissible()
     */
    public List<IPermissionOwner> getAllPermissionOwners() {

        final Query query = this.entityManager
                .createQuery(FIND_ALL_PERMISSION_OWNERS);
        
        @SuppressWarnings("unchecked")
        final List<IPermissionOwner> owners = query.getResultList();
        
        return owners;
    }

    /*
     * (non-Javadoc)
     * @see org.jasig.portal.permission.dao.IPermissionOwnerDao#getOrCreatePermissionOwner(java.lang.String)
     */
    public IPermissionOwner getOrCreatePermissionOwner(String fname) {
        IPermissionOwner owner = getPermissionOwner(fname);
        if (owner == null) {
            owner = new PermissionOwnerImpl();
            owner.setFname(fname);
        }
        return owner;
    }
    
    /*
     * (non-Javadoc)
     * @see org.jasig.portal.permission.dao.IPermissionOwnerDao#getPermissionOwner(java.lang.Long)
     */
    public IPermissionOwner getPermissionOwner(Long id){
        return entityManager.find(PermissionOwnerImpl.class, id);
    }

    /*
     * (non-Javadoc)
     * @see org.jasig.portal.permission.dao.IPermissionOwnerDao#getPermissionOwner(java.lang.String)
     */
    public IPermissionOwner getPermissionOwner(String fname){
        final Query query = this.entityManager.createQuery(FIND_PERMISSION_OWNER_BY_FNAME);
        query.setParameter("fname", fname);
        query.setMaxResults(1);
        
        @SuppressWarnings("unchecked")
        final List<IPermissionOwner> owners = query.getResultList();
        IPermissionOwner owner = (IPermissionOwner) DataAccessUtils.uniqueResult(owners);
        return owner;
        
    }
    
    /*
     * (non-Javadoc)
     * @see org.jasig.portal.permission.dao.IPermissionOwnerDao#saveOwner(org.jasig.portal.permission.IPermissionOwner)
     */
    public IPermissionOwner saveOwner(IPermissionOwner owner) {
        this.entityManager.persist(owner);
        return owner;
    }

    /*
     * (non-Javadoc)
     * @see org.jasig.portal.permission.dao.IPermissionOwnerDao#getOrCreatePermissionActivity(org.jasig.portal.permission.IPermissionOwner, java.lang.String)
     */
    public IPermissionActivity getOrCreatePermissionActivity(
            IPermissionOwner owner, String fname) {
        IPermissionActivity activity = getPermissionActivity(owner.getId(), fname);
        if (activity == null) {
            activity = new PermissionActivityImpl(owner);
            activity.setFname(fname);
        }
        return activity;
    }

    /*
     * (non-Javadoc)
     * @see org.jasig.portal.permission.dao.IPermissionOwnerDao#getPermissionActivity(java.lang.Long)
     */
    public IPermissionActivity getPermissionActivity(Long id) {
        return entityManager.find(PermissionActivityImpl.class, id);
    }

    /*
     * (non-Javadoc)
     * @see org.jasig.portal.permission.dao.IPermissionOwnerDao#getPermissionActivity(java.lang.Long, java.lang.String)
     */
    public IPermissionActivity getPermissionActivity(Long ownerId,
            String activityFname) {
        final Query query = this.entityManager.createQuery(FIND_PERMISSION_ACTIVITY_BY_OWNER_ID);
        query.setParameter("ownerId", ownerId);
        query.setParameter("fname", activityFname);
        query.setMaxResults(1);
        
        @SuppressWarnings("unchecked")
        final List<IPermissionActivity> activities = query.getResultList();
        IPermissionActivity activity = (IPermissionActivity) DataAccessUtils.uniqueResult(activities);
        return activity;
    }

    /*
     * (non-Javadoc)
     * @see org.jasig.portal.permission.dao.IPermissionOwnerDao#getPermissionActivity(java.lang.String, java.lang.String)
     */
    public IPermissionActivity getPermissionActivity(String ownerFname,
            String activityFname) {
        final Query query = this.entityManager.createQuery(FIND_PERMISSION_ACTIVITY_BY_OWNER_FNAME);
        query.setParameter("ownerFname", ownerFname);
        query.setParameter("activityFname", activityFname);
        query.setMaxResults(1);
        
        @SuppressWarnings("unchecked")
        final List<IPermissionActivity> activities = query.getResultList();
        IPermissionActivity activity = (IPermissionActivity) DataAccessUtils.uniqueResult(activities);
        return activity;
    }

    /*
     * (non-Javadoc)
     * @see org.jasig.portal.permission.dao.IPermissionOwnerDao#savePermissionActivity(org.jasig.portal.permission.IPermissionActivity)
     */
    public IPermissionActivity savePermissionActivity(
            IPermissionActivity activity) {
        this.entityManager.persist(activity);
        return activity;
    }

}
