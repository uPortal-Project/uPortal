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
package org.apereo.portal.tenants;

import com.google.common.base.Function;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.ParameterExpression;
import javax.persistence.criteria.Root;
import org.apereo.portal.jpa.BasePortalJpaDao;
import org.springframework.stereotype.Repository;

@Repository
/* package-private */ class JpaTenantDao extends BasePortalJpaDao implements ITenantDao {

    private CriteriaQuery<JpaTenant> allTenantsQuery;
    private CriteriaQuery<JpaTenant> tenantByNameQuery;
    private CriteriaQuery<JpaTenant> tenantByFNameQuery;
    private ParameterExpression<String> nameParameter;
    private ParameterExpression<String> fnameParameter;

    @Override
    public void afterPropertiesSet() throws Exception {
        nameParameter = createParameterExpression(String.class, "name");
        fnameParameter = createParameterExpression(String.class, "fname");

        allTenantsQuery =
                createCriteriaQuery(
                        new Function<CriteriaBuilder, CriteriaQuery<JpaTenant>>() {
                            @Override
                            public CriteriaQuery<JpaTenant> apply(CriteriaBuilder cb) {
                                final CriteriaQuery<JpaTenant> result =
                                        cb.createQuery(JpaTenant.class);
                                Root<JpaTenant> root = result.from(JpaTenant.class);
                                result.orderBy(cb.asc(root.get(JpaTenant_.name)));
                                return result;
                            }
                        });
        tenantByNameQuery =
                createCriteriaQuery(
                        new Function<CriteriaBuilder, CriteriaQuery<JpaTenant>>() {
                            @Override
                            public CriteriaQuery<JpaTenant> apply(CriteriaBuilder cb) {
                                final CriteriaQuery<JpaTenant> result =
                                        cb.createQuery(JpaTenant.class);
                                Root<JpaTenant> root = result.from(JpaTenant.class);
                                result.where(cb.equal(root.get(JpaTenant_.name), nameParameter));
                                return result;
                            }
                        });
        tenantByFNameQuery =
                createCriteriaQuery(
                        new Function<CriteriaBuilder, CriteriaQuery<JpaTenant>>() {
                            @Override
                            public CriteriaQuery<JpaTenant> apply(CriteriaBuilder cb) {
                                final CriteriaQuery<JpaTenant> result =
                                        cb.createQuery(JpaTenant.class);
                                Root<JpaTenant> root = result.from(JpaTenant.class);
                                result.where(cb.equal(root.get(JpaTenant_.fname), fnameParameter));
                                return result;
                            }
                        });
    }

    @Override
    public Set<ITenant> getAllTenants() {
        final TypedQuery<JpaTenant> query = createCachedQuery(allTenantsQuery);
        final List<JpaTenant> resultList = query.getResultList();
        return new HashSet<ITenant>(resultList);
    }

    @Override
    public ITenant getTenantByName(String name) {
        final TypedQuery<JpaTenant> query = createCachedQuery(tenantByNameQuery);
        query.setParameter(nameParameter, name);
        final List<JpaTenant> list = query.getResultList();
        if (list.size() == 0) {
            final String msg = "Tenant not found with name:  " + name;
            throw new IllegalArgumentException(msg);
        }
        return list.get(0);
    }

    @Override
    public ITenant getTenantByFName(String fname) {
        final TypedQuery<JpaTenant> query = createCachedQuery(tenantByFNameQuery);
        query.setParameter(fnameParameter, fname);
        final List<JpaTenant> list = query.getResultList();
        if (list.size() == 0) {
            final String msg = "Tenant not found with fname:  " + fname;
            throw new IllegalArgumentException(msg);
        }
        return list.get(0);
    }

    @Override
    public ITenant instantiate() {
        return new JpaTenant();
    }

    @Override
    @PortalTransactional
    public void createOrUpdateTenant(ITenant tenant) {

        EntityManager entityManager = getEntityManager();

        // Assertions
        if (tenant instanceof JpaTenant) {
            final ITenant persistentTenant;
            if (entityManager.contains(tenant)) {
                persistentTenant = tenant;
            } else {
                persistentTenant = entityManager.merge(tenant);
            }
            entityManager.persist(persistentTenant);
        } else {
            // This object is not supported by this DAO
            final String msg =
                    "The specified tenant is not an instanceof JpaTenant:  " + tenant.getFname();
            throw new IllegalArgumentException(msg);
        }
    }

    @Override
    @PortalTransactional
    public void removeTenant(ITenant tenant) {

        // Assertions
        if (tenant.getId() == -1) {
            final String msg = "The specified tenant does not exist:  " + tenant.getFname();
            throw new IllegalArgumentException(msg);
        }

        getEntityManager().remove(tenant);
    }
}
