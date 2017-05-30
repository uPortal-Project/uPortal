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
package org.apereo.portal.persondir.dao.jpa;

import com.google.common.base.Function;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.persistence.EntityManager;
import javax.persistence.Parameter;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CollectionJoin;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.ListJoin;
import javax.persistence.criteria.ParameterExpression;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.Validate;
import org.apereo.portal.jpa.BasePortalJpaDao;
import org.apereo.portal.persondir.ILocalAccountDao;
import org.apereo.portal.persondir.ILocalAccountPerson;
import org.apereo.portal.persondir.LocalAccountQuery;
import org.springframework.dao.support.DataAccessUtils;
import org.springframework.stereotype.Repository;

@Repository("localAccountDao")
public class JpaLocalAccountDaoImpl extends BasePortalJpaDao implements ILocalAccountDao {
    private CriteriaQuery<LocalAccountPersonImpl> findAllAccountsQuery;
    private CriteriaQuery<LocalAccountPersonImpl> findAccountByNameQuery;
    private CriteriaQuery<String> findAvailableAttributesQuery;
    private ParameterExpression<String> nameParameter;

    @Override
    public void afterPropertiesSet() throws Exception {
        this.nameParameter = this.createParameterExpression(String.class, "name");

        this.findAllAccountsQuery =
                this.createCriteriaQuery(
                        new Function<CriteriaBuilder, CriteriaQuery<LocalAccountPersonImpl>>() {
                            @Override
                            public CriteriaQuery<LocalAccountPersonImpl> apply(CriteriaBuilder cb) {
                                final CriteriaQuery<LocalAccountPersonImpl> criteriaQuery =
                                        cb.createQuery(LocalAccountPersonImpl.class);
                                final Root<LocalAccountPersonImpl> accountRoot =
                                        criteriaQuery.from(LocalAccountPersonImpl.class);
                                accountRoot
                                        .fetch(LocalAccountPersonImpl_.attributes, JoinType.LEFT)
                                        .fetch(
                                                LocalAccountPersonAttributeImpl_.values,
                                                JoinType.LEFT);
                                criteriaQuery.select(accountRoot);

                                return criteriaQuery;
                            }
                        });

        this.findAccountByNameQuery =
                this.createCriteriaQuery(
                        new Function<CriteriaBuilder, CriteriaQuery<LocalAccountPersonImpl>>() {
                            @Override
                            public CriteriaQuery<LocalAccountPersonImpl> apply(CriteriaBuilder cb) {
                                final CriteriaQuery<LocalAccountPersonImpl> criteriaQuery =
                                        cb.createQuery(LocalAccountPersonImpl.class);
                                final Root<LocalAccountPersonImpl> accountRoot =
                                        criteriaQuery.from(LocalAccountPersonImpl.class);
                                accountRoot
                                        .fetch(LocalAccountPersonImpl_.attributes, JoinType.LEFT)
                                        .fetch(
                                                LocalAccountPersonAttributeImpl_.values,
                                                JoinType.LEFT);
                                criteriaQuery.select(accountRoot);
                                criteriaQuery.where(
                                        cb.equal(
                                                accountRoot.get(LocalAccountPersonImpl_.name),
                                                nameParameter));

                                return criteriaQuery;
                            }
                        });

        this.findAvailableAttributesQuery =
                this.createCriteriaQuery(
                        new Function<CriteriaBuilder, CriteriaQuery<String>>() {
                            @Override
                            public CriteriaQuery<String> apply(CriteriaBuilder cb) {
                                final CriteriaQuery<String> criteriaQuery =
                                        cb.createQuery(String.class);
                                final Root<LocalAccountPersonAttributeImpl> accountRoot =
                                        criteriaQuery.from(LocalAccountPersonAttributeImpl.class);
                                criteriaQuery.select(
                                        accountRoot.get(LocalAccountPersonAttributeImpl_.name));
                                criteriaQuery.distinct(true);

                                return criteriaQuery;
                            }
                        });
    }

    @Override
    public ILocalAccountPerson getPerson(long id) {
        return this.getEntityManager().find(LocalAccountPersonImpl.class, id);
    }

    @Override
    @PortalTransactional
    public ILocalAccountPerson createPerson(String username) {
        final ILocalAccountPerson person = new LocalAccountPersonImpl(username);

        this.getEntityManager().persist(person);

        return person;
    }

    @Override
    public ILocalAccountPerson getPerson(String username) {
        final TypedQuery<LocalAccountPersonImpl> query =
                this.createCachedQuery(this.findAccountByNameQuery);
        query.setParameter(this.nameParameter, username);

        final List<LocalAccountPersonImpl> accounts = query.getResultList();
        return DataAccessUtils.uniqueResult(accounts);
    }

    @Override
    public List<ILocalAccountPerson> getAllAccounts() {
        final TypedQuery<LocalAccountPersonImpl> query =
                this.createCachedQuery(this.findAllAccountsQuery);

        final List<LocalAccountPersonImpl> accounts = query.getResultList();
        return new ArrayList<ILocalAccountPerson>(accounts);
    }

    @Override
    @PortalTransactional
    public ILocalAccountPerson updateAccount(ILocalAccountPerson account) {
        Validate.notNull(account, "account can not be null");

        this.getEntityManager().persist(account);

        return account;
    }

    @Override
    @PortalTransactional
    public void deleteAccount(ILocalAccountPerson account) {
        Validate.notNull(account, "definition can not be null");

        final EntityManager entityManager = this.getEntityManager();

        final ILocalAccountPerson persistentAccount;
        if (entityManager.contains(account)) {
            persistentAccount = account;
        } else {
            persistentAccount = entityManager.merge(account);
        }

        entityManager.remove(persistentAccount);
    }

    @Override
    public List<ILocalAccountPerson> getPeople(LocalAccountQuery query) {
        final EntityManager entityManager = this.getEntityManager();
        final CriteriaBuilder cb = entityManager.getCriteriaBuilder();

        final CriteriaQuery<LocalAccountPersonImpl> criteriaQuery =
                cb.createQuery(LocalAccountPersonImpl.class);
        final Root<LocalAccountPersonImpl> accountRoot =
                criteriaQuery.from(LocalAccountPersonImpl.class);
        final CollectionJoin<LocalAccountPersonImpl, LocalAccountPersonAttributeImpl> attributes =
                accountRoot.join(LocalAccountPersonImpl_.attributes);
        final ListJoin<LocalAccountPersonAttributeImpl, String> attributeValues =
                attributes.join(LocalAccountPersonAttributeImpl_.values);

        //Due to the joins multiple rows are returned for each result
        criteriaQuery.distinct(true);
        criteriaQuery.select(accountRoot);

        final List<Predicate> whereParts = new LinkedList<Predicate>();
        final Map<Parameter<String>, String> params =
                new LinkedHashMap<Parameter<String>, String>();

        // if a username has been specified, append it to the query
        if (query.getName() != null) {
            whereParts.add(
                    cb.equal(accountRoot.get(LocalAccountPersonImpl_.name), this.nameParameter));
            params.put(this.nameParameter, query.getName());
        }

        //Build Predicate for each attribute being queried
        int paramCount = 0;
        for (Map.Entry<String, List<String>> entry : query.getAttributes().entrySet()) {
            final List<String> values = entry.getValue();
            if (values == null) {
                continue;
            }

            //For each value create a Predicate checking the attribute name and value together
            for (final String value : values) {
                if (StringUtils.isBlank(value)) {
                    continue;
                }

                //Create Parameter objects for the name and value, stick them in the params map for later use
                final ParameterExpression<String> nameParam =
                        this.createParameterExpression(String.class, "attrName" + paramCount);
                final ParameterExpression<String> valueParam =
                        this.createParameterExpression(String.class, "attrValue" + paramCount);

                params.put(nameParam, entry.getKey());
                params.put(valueParam, "%" + value.toLowerCase() + "%");

                //Build the and(eq, like) predicate and add it to the list of predicates for the where clause
                whereParts.add(
                        cb.and(
                                cb.equal(
                                        attributes.get(LocalAccountPersonAttributeImpl_.name),
                                        nameParam),
                                cb.like(cb.lower(attributeValues.as(String.class)), valueParam)));

                paramCount++;
            }
        }

        //Add the Predicates to the where clause
        criteriaQuery.where(cb.or(whereParts.toArray(new Predicate[whereParts.size()])));

        //Create the query
        final TypedQuery<LocalAccountPersonImpl> jpaQuery = this.createCachedQuery(criteriaQuery);

        //Add all of the stored up parameters to the query
        for (Map.Entry<Parameter<String>, String> entry : params.entrySet()) {
            final Parameter<String> parameter = entry.getKey();
            final String value = entry.getValue();
            jpaQuery.setParameter(parameter, value);
        }

        final List<LocalAccountPersonImpl> accounts = jpaQuery.getResultList();
        return new ArrayList<ILocalAccountPerson>(accounts);
    }

    @Override
    public Set<String> getCurrentAttributeNames() {
        final TypedQuery<String> query = this.createCachedQuery(this.findAvailableAttributesQuery);

        final List<String> nameList = query.getResultList();
        return new LinkedHashSet<String>(nameList);
    }
}
