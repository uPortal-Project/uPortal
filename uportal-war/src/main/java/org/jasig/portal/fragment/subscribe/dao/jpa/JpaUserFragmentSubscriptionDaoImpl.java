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

package org.jasig.portal.fragment.subscribe.dao.jpa;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.ParameterExpression;
import javax.persistence.criteria.Root;

import org.apache.commons.lang.Validate;
import org.jasig.portal.fragment.subscribe.IUserFragmentSubscription;
import org.jasig.portal.fragment.subscribe.dao.IUserFragmentSubscriptionDao;
import org.jasig.portal.jpa.BaseJpaDao;
import org.jasig.portal.security.IPerson;
import org.springframework.dao.support.DataAccessUtils;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.google.common.base.Function;

/**
 * DAO for retrieving information about fragments (pre-formatted tabs) to 
 * which a user has subscribed.
 * 
 * @author Mary Hunt
 * @version $Revision$ $Date$
 */
@Repository
public class JpaUserFragmentSubscriptionDaoImpl extends BaseJpaDao implements IUserFragmentSubscriptionDao {
    private CriteriaQuery<UserFragmentSubscriptionImpl> findUserFragmentInfoByPersonQuery;
    private CriteriaQuery<UserFragmentSubscriptionImpl> findUserFragmentInfoByPersonAndOwnerQuery;
    private CriteriaQuery<String> findUsersWithActiveSubscriptionsQuery;

    private ParameterExpression<Integer> userIdParameter;
    private ParameterExpression<String> fragmentOwnerParameter;
    
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
        this.userIdParameter = this.createParameterExpression(Integer.TYPE, "userId");
        this.fragmentOwnerParameter = this.createParameterExpression(String.class, "fragmentOwner");
        
        this.findUserFragmentInfoByPersonQuery = this.createCriteriaQuery(new Function<CriteriaBuilder, CriteriaQuery<UserFragmentSubscriptionImpl>>() {
            @Override
            public CriteriaQuery<UserFragmentSubscriptionImpl> apply(CriteriaBuilder cb) {
                final CriteriaQuery<UserFragmentSubscriptionImpl> criteriaQuery = cb.createQuery(UserFragmentSubscriptionImpl.class);
                final Root<UserFragmentSubscriptionImpl> root = criteriaQuery.from(UserFragmentSubscriptionImpl.class);
                criteriaQuery.select(root);
                criteriaQuery.where(cb.equal(root.get(UserFragmentSubscriptionImpl_.userId), userIdParameter));
                
                return criteriaQuery;
            }
        });

        
        this.findUserFragmentInfoByPersonAndOwnerQuery = this.createCriteriaQuery(new Function<CriteriaBuilder, CriteriaQuery<UserFragmentSubscriptionImpl>>() {
            @Override
            public CriteriaQuery<UserFragmentSubscriptionImpl> apply(CriteriaBuilder cb) {
                final CriteriaQuery<UserFragmentSubscriptionImpl> criteriaQuery = cb.createQuery(UserFragmentSubscriptionImpl.class);
                final Root<UserFragmentSubscriptionImpl> root = criteriaQuery.from(UserFragmentSubscriptionImpl.class);
                criteriaQuery.select(root);
                criteriaQuery.where(cb.and(cb.equal(root.get(UserFragmentSubscriptionImpl_.userId), userIdParameter),
                        cb.equal(root.get(UserFragmentSubscriptionImpl_.fragmentOwner), fragmentOwnerParameter)));
                
                return criteriaQuery;
            }
        });
        
        this.findUsersWithActiveSubscriptionsQuery = this.createCriteriaQuery(new Function<CriteriaBuilder, CriteriaQuery<String>>() {
            @Override
            public CriteriaQuery<String> apply(CriteriaBuilder cb) {
                final CriteriaQuery<String> criteriaQuery = cb.createQuery(String.class);
                final Root<UserFragmentSubscriptionImpl> root = criteriaQuery.from(UserFragmentSubscriptionImpl.class);
                criteriaQuery.select(root.get(UserFragmentSubscriptionImpl_.createdBy));
                criteriaQuery.where(cb.equal(root.get(UserFragmentSubscriptionImpl_.active), true));
                
                return criteriaQuery;
            }
        });
    }
    
    @Override
    @Transactional
    public IUserFragmentSubscription createUserFragmentInfo(IPerson person, IPerson fragmentOwner) {
        final IUserFragmentSubscription userFragmentInfo = new UserFragmentSubscriptionImpl(person, fragmentOwner);
        this.entityManager.persist(userFragmentInfo);

        return userFragmentInfo;
    }

    @Override
    @Transactional
    public void deleteUserFragmentInfo(IUserFragmentSubscription userFragmentInfo) {
        Validate.notNull(userFragmentInfo, "user fragment info can not be null");
        userFragmentInfo.setInactive();
        this.entityManager.persist(userFragmentInfo);

    }

    @Override
    public List<IUserFragmentSubscription> getUserFragmentInfo(IPerson person) {
        final TypedQuery<UserFragmentSubscriptionImpl> query = createCachedQuery(this.findUserFragmentInfoByPersonQuery);
        query.setParameter(this.userIdParameter, person.getID());

        final List<UserFragmentSubscriptionImpl> fragmentSubscriptions = query.getResultList();
        return new ArrayList<IUserFragmentSubscription>(fragmentSubscriptions);
    }

    @Override
    public IUserFragmentSubscription getUserFragmentInfo(IPerson person, IPerson fragmentOwner) {
        final TypedQuery<UserFragmentSubscriptionImpl> query = createCachedQuery(this.findUserFragmentInfoByPersonAndOwnerQuery);
        query.setParameter(this.userIdParameter, person.getID());
        query.setParameter(this.fragmentOwnerParameter, fragmentOwner.getUserName());

        final List<UserFragmentSubscriptionImpl> fragmentSubscriptions = query.getResultList();
        return DataAccessUtils.uniqueResult(fragmentSubscriptions);

    }

    @Override
    public IUserFragmentSubscription getUserFragmentInfo(long userFragmentInfoId) {

        final UserFragmentSubscriptionImpl userFragmentInfo = this.entityManager
                .find(UserFragmentSubscriptionImpl.class, userFragmentInfoId);

        return userFragmentInfo;
    }

    @Override
    @Transactional
    public void updateUserFragmentInfo(IUserFragmentSubscription userFragmentInfo) {
        Validate.notNull(userFragmentInfo, "user fragment info can not be null");
        this.entityManager.persist(userFragmentInfo);

    }

    @Override
    public List<String> getAllUsersWithActiveSubscriptions() {
        final TypedQuery<String> query = createCachedQuery(this.findUsersWithActiveSubscriptionsQuery);
        return query.getResultList();
    }
}
