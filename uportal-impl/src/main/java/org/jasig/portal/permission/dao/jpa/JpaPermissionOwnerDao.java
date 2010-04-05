package org.jasig.portal.permission.dao.jpa;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jasig.portal.channel.IChannelDefinition;
import org.jasig.portal.permission.IPermissionOwner;
import org.jasig.portal.permission.dao.IPermissionOwnerDao;
import org.springframework.dao.support.DataAccessUtils;

/**
 * 
 * @author Jen Bourey, jbourey@unicon.net
 * @version $Revision$
 */
public class JpaPermissionOwnerDao implements IPermissionOwnerDao {

    private static final String FIND_ALL_PERMISSION_OWNERS = "from PermissionOwnerImpl owner";
    private static final String FIND_PERMISSION_OWNER_BY_FNAME = 
        "from PermissionOwnerImpl owner where owner.fname = :fname";

    protected final Log log = LogFactory.getLog(getClass());

    private EntityManager entityManager;

    /**
     * @param entityManager
     *            the entityManager to set
     */
    @PersistenceContext
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

    public IPermissionOwner getOrCreatePermissionOwner(String fname) {
        IPermissionOwner owner = getPermissionOwner(fname);
        if (owner == null) {
            owner = new PermissionOwnerImpl();
            owner.setFname(fname);
        }
        return owner;
    }
    
    public IPermissionOwner getPermissionOwner(String fname){
        final Query query = this.entityManager.createQuery(FIND_PERMISSION_OWNER_BY_FNAME);
        query.setParameter("fname", fname);
        query.setMaxResults(1);
        
        @SuppressWarnings("unchecked")
        final List<IPermissionOwner> owners = query.getResultList();
        IPermissionOwner owner = (IPermissionOwner) DataAccessUtils.uniqueResult(owners);
        return owner;
        
    }

    public IPermissionOwner saveOwner(IPermissionOwner owner) {
        this.entityManager.persist(owner);
        return owner;
    }

}
