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

package org.jasig.portal.persondir.dao.jpa;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import javax.persistence.EntityManager;
import javax.persistence.Parameter;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CollectionJoin;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.ListJoin;
import javax.persistence.criteria.ParameterExpression;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.Validate;
import org.jasig.portal.jpa.BasePortalJpaDao;
import org.jasig.portal.persondir.ILocalAccountDao;
import org.jasig.portal.persondir.ILocalAccountPerson;
import org.jasig.portal.persondir.LocalAccountQuery;
import org.jasig.services.persondir.IPersonAttributeDao;
import org.jasig.services.persondir.util.CaseCanonicalizationMode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.support.DataAccessUtils;
import org.springframework.stereotype.Repository;

import com.google.common.base.Function;

@Repository("localAccountDao")
public class JpaLocalAccountDaoImpl extends BasePortalJpaDao implements ILocalAccountDao {
    private CriteriaQuery<LocalAccountPersonImpl> findAllAccountsQuery;
    private CriteriaQuery<LocalAccountPersonImpl> findAccountByNameQuery;
    private CriteriaQuery<String> findAvailableAttributesQuery;
    private ParameterExpression<String> nameParameter;
    @Value("${org.jasig.portal.persondir.ILocalAccountDao.usernameCaseCanonicalizationMode:NONE}")
    private CaseCanonicalizationMode usernameCaseCanonicalizationMode = CaseCanonicalizationMode.NONE;
    private Locale usernameCaseCanonicalizationLocale = Locale.getDefault();
    @Value("${org.jasig.portal.persondir.ILocalAccountDao.usernameCaseCanonicalizationMode.inconsistentlyCasedPersistentUsernames:true}")
    private boolean inconsistentlyCasedPersistentUsernames = true;

    @Override
    public void afterPropertiesSet() throws Exception {
        this.nameParameter = this.createParameterExpression(String.class, "name");
        
        this.findAllAccountsQuery = this.createCriteriaQuery(new Function<CriteriaBuilder, CriteriaQuery<LocalAccountPersonImpl>>() {
            @Override
            public CriteriaQuery<LocalAccountPersonImpl> apply(CriteriaBuilder cb) {
                final CriteriaQuery<LocalAccountPersonImpl> criteriaQuery = cb.createQuery(LocalAccountPersonImpl.class);
                final Root<LocalAccountPersonImpl> accountRoot = criteriaQuery.from(LocalAccountPersonImpl.class);
                accountRoot.fetch(LocalAccountPersonImpl_.attributes, JoinType.LEFT).fetch(LocalAccountPersonAttributeImpl_.values, JoinType.LEFT);
                criteriaQuery.select(accountRoot);
                
                return criteriaQuery;
            }
        });
        
        
        this.findAccountByNameQuery = this.createCriteriaQuery(new Function<CriteriaBuilder, CriteriaQuery<LocalAccountPersonImpl>>() {
            @Override
            public CriteriaQuery<LocalAccountPersonImpl> apply(CriteriaBuilder cb) {
                final CriteriaQuery<LocalAccountPersonImpl> criteriaQuery = cb.createQuery(LocalAccountPersonImpl.class);
                final Root<LocalAccountPersonImpl> accountRoot = criteriaQuery.from(LocalAccountPersonImpl.class);
                accountRoot.fetch(LocalAccountPersonImpl_.attributes, JoinType.LEFT).fetch(LocalAccountPersonAttributeImpl_.values, JoinType.LEFT);
                criteriaQuery.select(accountRoot);
                criteriaQuery.where(
                    cb.equal(getCanonicalizedNameColumn(cb, accountRoot), nameParameter)
                );
                
                return criteriaQuery;
            }
        });
        
        
        this.findAvailableAttributesQuery = this.createCriteriaQuery(new Function<CriteriaBuilder, CriteriaQuery<String>>() {
            @Override
            public CriteriaQuery<String> apply(CriteriaBuilder cb) {
                final CriteriaQuery<String> criteriaQuery = cb.createQuery(String.class);
                final Root<LocalAccountPersonAttributeImpl> accountRoot = criteriaQuery.from(LocalAccountPersonAttributeImpl.class);
                criteriaQuery.select(accountRoot.get(LocalAccountPersonAttributeImpl_.name));
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
        username = usernameCaseCanonicalizationMode.canonicalize(username, usernameCaseCanonicalizationLocale);
        final ILocalAccountPerson person = new LocalAccountPersonImpl(username);
        
        this.getEntityManager().persist(person);
        
        return person;
    }
    
    @Override
    public ILocalAccountPerson getPerson(String username) {
        username = usernameCaseCanonicalizationMode.canonicalize(username, usernameCaseCanonicalizationLocale);
        final TypedQuery<LocalAccountPersonImpl> query = this.createCachedQuery(this.findAccountByNameQuery);
        query.setParameter(this.nameParameter, username);
        
        final List<LocalAccountPersonImpl> accounts = query.getResultList();
        return DataAccessUtils.uniqueResult(accounts);
    }

    @Override
    public List<ILocalAccountPerson> getAllAccounts() {
        final TypedQuery<LocalAccountPersonImpl> query = this.createCachedQuery(this.findAllAccountsQuery);
        
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
        }
        else {
            persistentAccount = entityManager.merge(account);
        }
        
        entityManager.remove(persistentAccount);
    }

    @Override
    public List<ILocalAccountPerson> getPeople(LocalAccountQuery query) {
        final EntityManager entityManager = this.getEntityManager();
        final CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        
        final CriteriaQuery<LocalAccountPersonImpl> criteriaQuery = cb.createQuery(LocalAccountPersonImpl.class);
        final Root<LocalAccountPersonImpl> accountRoot = criteriaQuery.from(LocalAccountPersonImpl.class);
        final CollectionJoin<LocalAccountPersonImpl, LocalAccountPersonAttributeImpl> attributes = accountRoot.join(LocalAccountPersonImpl_.attributes);
        final ListJoin<LocalAccountPersonAttributeImpl, String> attributeValues = attributes.join(LocalAccountPersonAttributeImpl_.values);
        
        //Due to the joins multiple rows are returned for each result
        criteriaQuery.distinct(true);
        criteriaQuery.select(accountRoot);
        
        final List<Predicate> whereParts = new LinkedList<Predicate>();
        final Map<Parameter<String>, String> params = new LinkedHashMap<Parameter<String>, String>();

        // if a username has been specified, append it to the query
        if (query.getName() != null) {

            final Expression canonicalizedNameColumn = getCanonicalizedNameColumn(cb, accountRoot);
            whereParts.add(cb.equal(canonicalizedNameColumn, this.nameParameter));
            final String canonicalizedNameQueryValue = usernameCaseCanonicalizationMode.canonicalize(query.getName(), usernameCaseCanonicalizationLocale);
            params.put(this.nameParameter, canonicalizedNameQueryValue);
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
                final ParameterExpression<String> nameParam = this.createParameterExpression(String.class, "attrName" + paramCount);
                final ParameterExpression<String> valueParam = this.createParameterExpression(String.class, "attrValue" + paramCount);
                
                params.put(nameParam, entry.getKey());
                if ( value.contains(IPersonAttributeDao.WILDCARD) ) {
                    params.put(valueParam, value.replaceAll(Pattern.quote(IPersonAttributeDao.WILDCARD), "%").toLowerCase());
                } else {
                    params.put(valueParam, "%" + value.toLowerCase() + "%");
                }
                
                //Build the and(eq, like) predicate and add it to the list of predicates for the where clause
                whereParts.add(
                    cb.and(
                        cb.equal(attributes.get(LocalAccountPersonAttributeImpl_.name), nameParam),
                        cb.like(cb.lower(attributeValues.as(String.class)), valueParam)
                    )
                );
                
                paramCount++;
            }
        }
        
        //Add the Predicates to the where clause
        criteriaQuery.where(
            cb.or(whereParts.toArray(new Predicate[whereParts.size()]))
        );
        
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

    private Expression getCanonicalizedNameColumn(CriteriaBuilder cb, Root<LocalAccountPersonImpl> accountRoot) {
        if ( inconsistentlyCasedPersistentUsernames ) {
            switch ( usernameCaseCanonicalizationMode ) {
                case LOWER:
                    return cb.lower(accountRoot.get(LocalAccountPersonImpl_.name));
                case UPPER:
                    return cb.upper(accountRoot.get(LocalAccountPersonImpl_.name));
                default:
                    return accountRoot.get(LocalAccountPersonImpl_.name);
            }
        } else {
            return accountRoot.get(LocalAccountPersonImpl_.name);
        }

    }

    @Override
    public Set<String> getCurrentAttributeNames() {
        final TypedQuery<String> query = this.createCachedQuery(this.findAvailableAttributesQuery);

        final List<String> nameList = query.getResultList();
        return new LinkedHashSet<String>(nameList);
    }

    public CaseCanonicalizationMode getUsernameCaseCanonicalizationMode() {
        return usernameCaseCanonicalizationMode;
    }

    public void setUsernameCaseCanonicalizationMode(CaseCanonicalizationMode usernameCaseCanonicalizationMode) {
        this.usernameCaseCanonicalizationMode = usernameCaseCanonicalizationMode;
    }

    public Locale getUsernameCaseCanonicalizationLocale() {
        return usernameCaseCanonicalizationLocale;
    }

    public void setUsernameCaseCanonicalizationLocale(Locale usernameCaseCanonicalizationLocale) {
        this.usernameCaseCanonicalizationLocale = usernameCaseCanonicalizationLocale;
    }

    public boolean isInconsistentlyCasedPersistentUsernames() {
        return inconsistentlyCasedPersistentUsernames;
    }

    /**
     * Set to {@link code} if you have a case-sensitive underlying data store
     * with username values stored in a non-canonical casing and you need
     * case-insensitive searching. Will then have the effect of applying
     * a function to the stored value prior to comparisons. Beware of
     * possibly very poor performance that can result if your storage layer
     * does not support function-based indices.
     *
     * @param inconsistentlyCasedPersistentUsernames
     */
    public void setInconsistentlyCasedPersistentUsernames(boolean inconsistentlyCasedPersistentUsernames) {
        this.inconsistentlyCasedPersistentUsernames = inconsistentlyCasedPersistentUsernames;
    }

}
