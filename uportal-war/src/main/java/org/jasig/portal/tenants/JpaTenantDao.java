/**
 * Licensed to Apereo under one or more contributor license
 * agreements. See the NOTICE file distributed with this work
 * for additional information regarding copyright ownership.
 * Apereo licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License.  You may obtain a
 * copy of the License at the following location:
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.jasig.portal.tenants;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.ParameterExpression;
import javax.persistence.criteria.Root;

import org.jasig.portal.jpa.BasePortalJpaDao;
import org.springframework.stereotype.Repository;

import com.google.common.base.Function;

@Repository
/* package-private */ class JpaTenantDao extends BasePortalJpaDao implements ITenantDao {

    private CriteriaQuery<JpaTenant> allTenantsQuery;
    private CriteriaQuery<JpaTenant> tenantByFNameQuery;
    private ParameterExpression<String> fnameParameter;

    @Override
    public void afterPropertiesSet() throws Exception {
        this.fnameParameter = this.createParameterExpression(String.class, "fname");

        this.allTenantsQuery = this.createCriteriaQuery(new Function<CriteriaBuilder, CriteriaQuery<JpaTenant>>() {
            @Override
            public CriteriaQuery<JpaTenant> apply(CriteriaBuilder cb) {
                final CriteriaQuery<JpaTenant> criteriaQuery = cb.createQuery(JpaTenant.class);
                Root<JpaTenant> root = criteriaQuery.from(JpaTenant.class);
                criteriaQuery.orderBy(cb.asc(root.get(JpaTenant_.name)));
                return criteriaQuery;
            }
        });
        this.tenantByFNameQuery = this.createCriteriaQuery(new Function<CriteriaBuilder, CriteriaQuery<JpaTenant>>() {
            @Override
            public CriteriaQuery<JpaTenant> apply(CriteriaBuilder cb) {
                final CriteriaQuery<JpaTenant> criteriaQuery = cb.createQuery(JpaTenant.class);
                Root<JpaTenant> root = criteriaQuery.from(JpaTenant.class);
                criteriaQuery.where(cb.equal(root.get(JpaTenant_.fname), fnameParameter));
                return criteriaQuery;
            }
        });
    }

    @Override
    public Set<ITenant> getAllTenants() {
        final TypedQuery<JpaTenant> query = this.createCachedQuery(this.allTenantsQuery);
        final List<JpaTenant> resultList = query.getResultList();
        return new HashSet<ITenant>(resultList);
    }

    @Override
    public ITenant getTenantByFName(String fname) {
        final TypedQuery<JpaTenant> query = createCachedQuery(this.tenantByFNameQuery);
        query.setParameter(this.fnameParameter, fname);
        final List<JpaTenant> list = query.getResultList();
        if (list.size() == 0) {
            final String msg = "Tenant not found:  " + fname;
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

        // Assertions
        if (tenant instanceof JpaTenant) {
            this.getEntityManager().persist(tenant);
        } else {
            // This object is not supported by this DAO
            final String msg = "The specified tenant is not an instanceof JpaTenant:  " + tenant.getFname();
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

        this.getEntityManager().remove(tenant);

    }

}
