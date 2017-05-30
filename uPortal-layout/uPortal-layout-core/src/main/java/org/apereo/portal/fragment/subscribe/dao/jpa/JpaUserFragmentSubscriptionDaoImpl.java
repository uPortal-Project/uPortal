/**
 * Licensed to Apereo under one or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information regarding copyright ownership. Apereo
 * licenses this file to you under the Apache License, Version 2.0 (the "License"); you may not use
 * this file except in compliance with the License. You may obtain a copy of the License at the
 * following location:
 *
 * <p>http://www.apache.org/licenses/LICENSE-2.0
 *
 * <p>Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apereo.portal.fragment.subscribe.dao.jpa;

import com.google.common.base.Function;
import java.util.ArrayList;
import java.util.List;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.ParameterExpression;
import javax.persistence.criteria.Root;
import org.apache.commons.lang.Validate;
import org.apereo.portal.fragment.subscribe.IUserFragmentSubscription;
import org.apereo.portal.fragment.subscribe.dao.IUserFragmentSubscriptionDao;
import org.apereo.portal.jpa.BasePortalJpaDao;
import org.apereo.portal.security.IPerson;
import org.springframework.dao.support.DataAccessUtils;
import org.springframework.stereotype.Repository;

/**
 * DAO for retrieving information about fragments (pre-formatted tabs) to which a user has
 * subscribed.
 *
 */
@Repository
public class JpaUserFragmentSubscriptionDaoImpl extends BasePortalJpaDao
        implements IUserFragmentSubscriptionDao {
    private CriteriaQuery<UserFragmentSubscriptionImpl> findUserFragmentInfoByPersonQuery;
    private CriteriaQuery<UserFragmentSubscriptionImpl> findUserFragmentInfoByPersonAndOwnerQuery;
    private CriteriaQuery<String> findUsersWithActiveSubscriptionsQuery;

    private ParameterExpression<Integer> userIdParameter;
    private ParameterExpression<String> fragmentOwnerParameter;

    @Override
    public void afterPropertiesSet() throws Exception {
        this.userIdParameter = this.createParameterExpression(Integer.TYPE, "userId");
        this.fragmentOwnerParameter = this.createParameterExpression(String.class, "fragmentOwner");

        this.findUserFragmentInfoByPersonQuery =
                this.createCriteriaQuery(
                        new Function<
                                CriteriaBuilder, CriteriaQuery<UserFragmentSubscriptionImpl>>() {
                            @Override
                            public CriteriaQuery<UserFragmentSubscriptionImpl> apply(
                                    CriteriaBuilder cb) {
                                final CriteriaQuery<UserFragmentSubscriptionImpl> criteriaQuery =
                                        cb.createQuery(UserFragmentSubscriptionImpl.class);
                                final Root<UserFragmentSubscriptionImpl> root =
                                        criteriaQuery.from(UserFragmentSubscriptionImpl.class);
                                criteriaQuery.select(root);
                                criteriaQuery.where(
                                        cb.equal(
                                                root.get(UserFragmentSubscriptionImpl_.userId),
                                                userIdParameter));

                                return criteriaQuery;
                            }
                        });

        this.findUserFragmentInfoByPersonAndOwnerQuery =
                this.createCriteriaQuery(
                        new Function<
                                CriteriaBuilder, CriteriaQuery<UserFragmentSubscriptionImpl>>() {
                            @Override
                            public CriteriaQuery<UserFragmentSubscriptionImpl> apply(
                                    CriteriaBuilder cb) {
                                final CriteriaQuery<UserFragmentSubscriptionImpl> criteriaQuery =
                                        cb.createQuery(UserFragmentSubscriptionImpl.class);
                                final Root<UserFragmentSubscriptionImpl> root =
                                        criteriaQuery.from(UserFragmentSubscriptionImpl.class);
                                criteriaQuery.select(root);
                                criteriaQuery.where(
                                        cb.and(
                                                cb.equal(
                                                        root.get(
                                                                UserFragmentSubscriptionImpl_
                                                                        .userId),
                                                        userIdParameter),
                                                cb.equal(
                                                        root.get(
                                                                UserFragmentSubscriptionImpl_
                                                                        .fragmentOwner),
                                                        fragmentOwnerParameter)));

                                return criteriaQuery;
                            }
                        });

        this.findUsersWithActiveSubscriptionsQuery =
                this.createCriteriaQuery(
                        new Function<CriteriaBuilder, CriteriaQuery<String>>() {
                            @Override
                            public CriteriaQuery<String> apply(CriteriaBuilder cb) {
                                final CriteriaQuery<String> criteriaQuery =
                                        cb.createQuery(String.class);
                                final Root<UserFragmentSubscriptionImpl> root =
                                        criteriaQuery.from(UserFragmentSubscriptionImpl.class);
                                criteriaQuery.select(
                                        root.get(UserFragmentSubscriptionImpl_.createdBy));
                                criteriaQuery.where(
                                        cb.equal(
                                                root.get(UserFragmentSubscriptionImpl_.active),
                                                true));

                                return criteriaQuery;
                            }
                        });
    }

    @Override
    @PortalTransactional
    public IUserFragmentSubscription createUserFragmentInfo(IPerson person, IPerson fragmentOwner) {
        final IUserFragmentSubscription userFragmentInfo =
                new UserFragmentSubscriptionImpl(person, fragmentOwner);
        this.getEntityManager().persist(userFragmentInfo);

        return userFragmentInfo;
    }

    @Override
    @PortalTransactional
    public void deleteUserFragmentInfo(IUserFragmentSubscription userFragmentInfo) {
        Validate.notNull(userFragmentInfo, "user fragment info can not be null");
        userFragmentInfo.setInactive();
        this.getEntityManager().persist(userFragmentInfo);
    }

    @Override
    public List<IUserFragmentSubscription> getUserFragmentInfo(IPerson person) {
        final TypedQuery<UserFragmentSubscriptionImpl> query =
                createCachedQuery(this.findUserFragmentInfoByPersonQuery);
        query.setParameter(this.userIdParameter, person.getID());

        final List<UserFragmentSubscriptionImpl> fragmentSubscriptions = query.getResultList();
        return new ArrayList<IUserFragmentSubscription>(fragmentSubscriptions);
    }

    @Override
    public IUserFragmentSubscription getUserFragmentInfo(IPerson person, IPerson fragmentOwner) {
        final TypedQuery<UserFragmentSubscriptionImpl> query =
                createCachedQuery(this.findUserFragmentInfoByPersonAndOwnerQuery);
        query.setParameter(this.userIdParameter, person.getID());
        query.setParameter(this.fragmentOwnerParameter, fragmentOwner.getUserName());

        final List<UserFragmentSubscriptionImpl> fragmentSubscriptions = query.getResultList();
        return DataAccessUtils.uniqueResult(fragmentSubscriptions);
    }

    @Override
    public IUserFragmentSubscription getUserFragmentInfo(long userFragmentInfoId) {

        final UserFragmentSubscriptionImpl userFragmentInfo =
                this.getEntityManager()
                        .find(UserFragmentSubscriptionImpl.class, userFragmentInfoId);

        return userFragmentInfo;
    }

    @Override
    @PortalTransactional
    public void updateUserFragmentInfo(IUserFragmentSubscription userFragmentInfo) {
        Validate.notNull(userFragmentInfo, "user fragment info can not be null");
        this.getEntityManager().persist(userFragmentInfo);
    }

    @Override
    public List<String> getAllUsersWithActiveSubscriptions() {
        final TypedQuery<String> query =
                createCachedQuery(this.findUsersWithActiveSubscriptionsQuery);
        return query.getResultList();
    }
}
