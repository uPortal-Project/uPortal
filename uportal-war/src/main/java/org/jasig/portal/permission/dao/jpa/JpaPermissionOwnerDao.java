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

package org.jasig.portal.permission.dao.jpa;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.ParameterExpression;
import javax.persistence.criteria.Root;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jasig.portal.jpa.BaseJpaDao;
import org.jasig.portal.permission.IPermissionActivity;
import org.jasig.portal.permission.IPermissionOwner;
import org.jasig.portal.permission.dao.IPermissionOwnerDao;
import org.springframework.dao.support.DataAccessUtils;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.google.common.base.Function;

/**
 * JpaPermissionOwnerDao provides a default JPA/Hibernate implementation of
 * the IPermissionOwnerDao interface.
 * 
 * @author Jen Bourey, jbourey@unicon.net
 * @version $Revision$
 * @since 3.3
 */
@Repository("permissionOwnerDao")
public class JpaPermissionOwnerDao extends BaseJpaDao implements IPermissionOwnerDao {
    protected final Log log = LogFactory.getLog(getClass());
    
    private CriteriaQuery<PermissionOwnerImpl> findAllPermissionOwners;
    private CriteriaQuery<PermissionOwnerImpl> findPermissionOwnerByFname;
    private ParameterExpression<String> fnameParameter;
    private EntityManager entityManager;

    @PersistenceContext(unitName = "uPortalPersistence")
    public final void setEntityManager(EntityManager entityManager) {
        this.entityManager = entityManager;
    }
    
    @Override
    protected EntityManager getEntityManager() {
        return this.entityManager;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        this.fnameParameter = this.createParameterExpression(String.class, "fname");
        
        this.findAllPermissionOwners = this.createCriteriaQuery(new Function<CriteriaBuilder, CriteriaQuery<PermissionOwnerImpl>>() {
            @Override
            public CriteriaQuery<PermissionOwnerImpl> apply(CriteriaBuilder cb) {
                final CriteriaQuery<PermissionOwnerImpl> criteriaQuery = cb.createQuery(PermissionOwnerImpl.class);
                final Root<PermissionOwnerImpl> ownerRoot = criteriaQuery.from(PermissionOwnerImpl.class);
                criteriaQuery.select(ownerRoot);
                ownerRoot.fetch(PermissionOwnerImpl_.activities, JoinType.LEFT);
                
                return criteriaQuery;
            }
        });
        
        
        this.findPermissionOwnerByFname = this.createCriteriaQuery(new Function<CriteriaBuilder, CriteriaQuery<PermissionOwnerImpl>>() {
            @Override
            public CriteriaQuery<PermissionOwnerImpl> apply(CriteriaBuilder cb) {
                final CriteriaQuery<PermissionOwnerImpl> criteriaQuery = cb.createQuery(PermissionOwnerImpl.class);
                final Root<PermissionOwnerImpl> ownerRoot = criteriaQuery.from(PermissionOwnerImpl.class);
                criteriaQuery.select(ownerRoot);
                ownerRoot.fetch(PermissionOwnerImpl_.activities, JoinType.LEFT);
                criteriaQuery.where(
                        cb.equal(ownerRoot.get(PermissionOwnerImpl_.fname), fnameParameter)
                    );
                
                return criteriaQuery;
            }
        });
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.jasig.portal.permissions.dao.IPermissionOwnerDao#getAllPermissible()
     */
    @Override
    public List<IPermissionOwner> getAllPermissionOwners() {
        final TypedQuery<PermissionOwnerImpl> query = this.createCachedQuery(this.findAllPermissionOwners);
        
        final List<PermissionOwnerImpl> resultList = query.getResultList();
        return new ArrayList<IPermissionOwner>(new LinkedHashSet<IPermissionOwner>(resultList));
    }

    /*
     * (non-Javadoc)
     * @see org.jasig.portal.permission.dao.IPermissionOwnerDao#getOrCreatePermissionOwner(java.lang.String)
     */
    @Override
    @Transactional
    public IPermissionOwner getOrCreatePermissionOwner(String name, String fname) {
        IPermissionOwner owner = getPermissionOwner(fname);
        if (owner == null) {
            owner = new PermissionOwnerImpl(name, fname);
            this.entityManager.persist(owner);
        }
        return owner;
    }
    
    /*
     * (non-Javadoc)
     * @see org.jasig.portal.permission.dao.IPermissionOwnerDao#getPermissionOwner(java.lang.Long)
     */
    @Override
    public IPermissionOwner getPermissionOwner(long id){
        return entityManager.find(PermissionOwnerImpl.class, id);
    }

    /*
     * (non-Javadoc)
     * @see org.jasig.portal.permission.dao.IPermissionOwnerDao#getPermissionOwner(java.lang.String)
     */
    @Override
    public IPermissionOwner getPermissionOwner(String fname){
        final TypedQuery<PermissionOwnerImpl> query = this.createCachedQuery(this.findPermissionOwnerByFname);
        query.setParameter(this.fnameParameter, fname);
        
        final List<PermissionOwnerImpl> owners = query.getResultList();
        final IPermissionOwner owner = DataAccessUtils.uniqueResult(owners);
        return owner;
        
    }
    
    /*
     * (non-Javadoc)
     * @see org.jasig.portal.permission.dao.IPermissionOwnerDao#saveOwner(org.jasig.portal.permission.IPermissionOwner)
     */
    @Override
    @Transactional
    public IPermissionOwner saveOwner(IPermissionOwner owner) {
        this.entityManager.persist(owner);
        return owner;
    }

    /*
     * (non-Javadoc)
     * @see org.jasig.portal.permission.dao.IPermissionOwnerDao#getOrCreatePermissionActivity(org.jasig.portal.permission.IPermissionOwner, java.lang.String)
     */
    @Override
    @Transactional
    public IPermissionActivity getOrCreatePermissionActivity(
            IPermissionOwner owner, String name, String fname, String targetProviderKey) {
        IPermissionActivity activity = getPermissionActivity(owner.getId(), fname);
        if (activity == null) {
            activity = new PermissionActivityImpl(name, fname, targetProviderKey);
            owner.getActivities().add(activity);
        }
        return activity;
    }

    /*
     * (non-Javadoc)
     * @see org.jasig.portal.permission.dao.IPermissionOwnerDao#getPermissionActivity(java.lang.Long)
     */
    @Override
    public IPermissionActivity getPermissionActivity(long id) {
        return entityManager.find(PermissionActivityImpl.class, id);
    }

    /*
     * (non-Javadoc)
     * @see org.jasig.portal.permission.dao.IPermissionOwnerDao#getPermissionActivity(java.lang.Long, java.lang.String)
     */
    @Override
    public IPermissionActivity getPermissionActivity(long ownerId, String activityFname) {
        final IPermissionOwner permissionOwner = this.getPermissionOwner(ownerId);
        return findActivity(permissionOwner, activityFname);
    }

    /*
     * (non-Javadoc)
     * @see org.jasig.portal.permission.dao.IPermissionOwnerDao#getPermissionActivity(java.lang.String, java.lang.String)
     */
    @Override
    public IPermissionActivity getPermissionActivity(String ownerFname, String activityFname) {
        final IPermissionOwner permissionOwner = this.getPermissionOwner(ownerFname);
        return findActivity(permissionOwner, activityFname);
    }

    /*
     * (non-Javadoc)
     * @see org.jasig.portal.permission.dao.IPermissionOwnerDao#savePermissionActivity(org.jasig.portal.permission.IPermissionActivity)
     */
    @Override
    @Transactional
    public IPermissionActivity savePermissionActivity(IPermissionActivity activity) {
        this.entityManager.persist(activity);
        return activity;
    }


    protected IPermissionActivity findActivity(final IPermissionOwner permissionOwner, String activityFname) {
        if (permissionOwner == null) {
            return null;
        }
        
        final Set<IPermissionActivity> activities = permissionOwner.getActivities();
        for (final IPermissionActivity permissionActivity : activities) {
            if (activityFname.equals(permissionActivity.getFname())) {
                return permissionActivity;
            }
        }
        
        return null;
    }
}
