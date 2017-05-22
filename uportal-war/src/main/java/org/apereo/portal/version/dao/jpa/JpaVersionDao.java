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
package org.apereo.portal.version.dao.jpa;

import com.google.common.base.Function;
import javax.persistence.PersistenceException;
import javax.persistence.Tuple;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.ParameterExpression;
import javax.persistence.criteria.Root;
import org.apereo.portal.jpa.BasePortalJpaDao;
import org.apereo.portal.jpa.OpenEntityManager;
import org.apereo.portal.version.dao.VersionDao;
import org.apereo.portal.version.om.Version;
import org.hibernate.exception.SQLGrammarException;
import org.springframework.dao.support.DataAccessUtils;
import org.springframework.stereotype.Repository;

@Repository("versionDao")
public class JpaVersionDao extends BasePortalJpaDao implements VersionDao {
    private CriteriaQuery<Tuple> findCoreVersionNumbers;
    private CriteriaQuery<Integer> findLocalVersionNumber;
    private ParameterExpression<String> productParameter;

    @Override
    public void afterPropertiesSet() throws Exception {
        this.productParameter = this.createParameterExpression(String.class, "product");

        this.findCoreVersionNumbers =
                this.createCriteriaQuery(
                        new Function<CriteriaBuilder, CriteriaQuery<Tuple>>() {
                            @Override
                            public CriteriaQuery<Tuple> apply(CriteriaBuilder cb) {
                                final CriteriaQuery<Tuple> criteriaQuery = cb.createTupleQuery();
                                final Root<VersionImpl> versionRoot =
                                        criteriaQuery.from(VersionImpl.class);
                                criteriaQuery.multiselect(
                                        versionRoot
                                                .get(VersionImpl_.major)
                                                .alias(VersionImpl_.major.getName()),
                                        versionRoot
                                                .get(VersionImpl_.minor)
                                                .alias(VersionImpl_.minor.getName()),
                                        versionRoot
                                                .get(VersionImpl_.patch)
                                                .alias(VersionImpl_.patch.getName()));
                                criteriaQuery.where(
                                        cb.equal(
                                                versionRoot.get(VersionImpl_.product),
                                                productParameter));

                                return criteriaQuery;
                            }
                        });

        this.findLocalVersionNumber =
                this.createCriteriaQuery(
                        new Function<CriteriaBuilder, CriteriaQuery<Integer>>() {
                            @Override
                            public CriteriaQuery<Integer> apply(CriteriaBuilder cb) {
                                final CriteriaQuery<Integer> criteriaQuery =
                                        cb.createQuery(VersionImpl_.local.getBindableJavaType());
                                final Root<VersionImpl> versionRoot =
                                        criteriaQuery.from(VersionImpl.class);
                                criteriaQuery.select(
                                        versionRoot
                                                .get(VersionImpl_.local)
                                                .alias(VersionImpl_.local.getName()));
                                criteriaQuery.where(
                                        cb.equal(
                                                versionRoot.get(VersionImpl_.product),
                                                productParameter));

                                return criteriaQuery;
                            }
                        });
    }

    @Override
    @OpenEntityManager(unitName = PERSISTENCE_UNIT_NAME)
    public Version getVersion(String product) {
        NaturalIdQuery<VersionImpl> query = this.createNaturalIdQuery(VersionImpl.class);
        query.using(VersionImpl_.product, product);
        try {
            return query.load();
        } catch (SQLGrammarException e) {
            return getSimpleVersion(product);
        }
    }

    /**
     * Load a Version object with direct field queries. Used to deal with DB upgrades where not all
     * of the fields have been loaded
     */
    private Version getSimpleVersion(String product) {
        final Tuple coreNumbers;
        try {
            final TypedQuery<Tuple> coreNumbersQuery =
                    this.createQuery(this.findCoreVersionNumbers);
            coreNumbersQuery.setParameter(this.productParameter, product);
            coreNumbers = DataAccessUtils.singleResult(coreNumbersQuery.getResultList());
        } catch (SQLGrammarException e) {
            logger.warn("UP_VERSION table doesn't exist, returning null for version of " + product);
            return null;
        }

        if (coreNumbers == null) {
            //Table exists but no version data for the product
            return null;
        }

        //Pull out the maj/min/pat values
        final Integer major =
                coreNumbers.get(
                        VersionImpl_.major.getName(), VersionImpl_.major.getBindableJavaType());
        final Integer minor =
                coreNumbers.get(
                        VersionImpl_.minor.getName(), VersionImpl_.minor.getBindableJavaType());
        final Integer patch =
                coreNumbers.get(
                        VersionImpl_.patch.getName(), VersionImpl_.patch.getBindableJavaType());

        //See if the optional local version value exists
        Integer local;
        try {
            final TypedQuery<Integer> localNumberQuery =
                    this.createQuery(this.findLocalVersionNumber);
            localNumberQuery.setParameter(this.productParameter, product);
            local = DataAccessUtils.singleResult(localNumberQuery.getResultList());
        } catch (PersistenceException e) {
            local = null;
        }

        return new VersionImpl(product, major, minor, patch, local);
    }

    @Override
    @PortalTransactional
    public Version setVersion(String product, int major, int minor, int patch, Integer local) {
        final NaturalIdQuery<VersionImpl> query = this.createNaturalIdQuery(VersionImpl.class);
        query.using(VersionImpl_.product, product);
        VersionImpl version = query.load();

        if (version == null) {
            version = new VersionImpl(product, major, minor, patch, local);
        } else {
            version.setMajor(major);
            version.setMinor(minor);
            version.setPatch(patch);
            version.setLocal(local);
        }

        this.getEntityManager().persist(version);

        return version;
    }

    @Override
    @PortalTransactional
    public Version setVersion(String product, Version version) {
        return this.setVersion(
                product,
                version.getMajor(),
                version.getMinor(),
                version.getPatch(),
                version.getLocal());
    }
}
