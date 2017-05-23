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
package org.apereo.portal.layout.dao.jpa;

import com.google.common.base.Function;
import java.util.List;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.ParameterExpression;
import javax.persistence.criteria.Root;
import org.apereo.portal.IUserProfile;
import org.apereo.portal.jpa.BasePortalJpaDao;
import org.apereo.portal.layout.dao.IStylesheetUserPreferencesDao;
import org.apereo.portal.layout.om.IStylesheetDescriptor;
import org.apereo.portal.layout.om.IStylesheetUserPreferences;
import org.apereo.portal.security.IPerson;
import org.springframework.dao.support.DataAccessUtils;
import org.springframework.stereotype.Repository;

/**
 * JPA implementation of {@link IStylesheetUserPreferencesDao}
 *
 */
@Repository("stylesheetUserPreferencesDao")
public class JpaStylesheetUserPreferencesDao extends BasePortalJpaDao
        implements IStylesheetUserPreferencesDao {
    private CriteriaQuery<StylesheetUserPreferencesImpl> findAllPreferences;
    private CriteriaQuery<StylesheetUserPreferencesImpl> findAllPreferencesForUser;
    private CriteriaQuery<StylesheetUserPreferencesImpl>
            findPreferencesByDescriptorUserProfileQuery;
    private ParameterExpression<StylesheetDescriptorImpl> stylesheetDescriptorParameter;
    private ParameterExpression<Integer> userIdParameter;
    private ParameterExpression<Integer> profileIdParameter;

    @Override
    public void afterPropertiesSet() throws Exception {
        this.stylesheetDescriptorParameter =
                this.createParameterExpression(
                        StylesheetDescriptorImpl.class, "stylesheetDescriptor");
        this.userIdParameter = this.createParameterExpression(Integer.class, "userId");
        this.profileIdParameter = this.createParameterExpression(Integer.class, "profileId");

        this.findAllPreferences =
                this.createCriteriaQuery(
                        new Function<
                                CriteriaBuilder, CriteriaQuery<StylesheetUserPreferencesImpl>>() {
                            @Override
                            public CriteriaQuery<StylesheetUserPreferencesImpl> apply(
                                    CriteriaBuilder cb) {
                                final CriteriaQuery<StylesheetUserPreferencesImpl> criteriaQuery =
                                        cb.createQuery(StylesheetUserPreferencesImpl.class);
                                final Root<StylesheetUserPreferencesImpl> descriptorRoot =
                                        criteriaQuery.from(StylesheetUserPreferencesImpl.class);
                                criteriaQuery.select(descriptorRoot);
                                addFetches(descriptorRoot);

                                return criteriaQuery;
                            }
                        });

        this.findPreferencesByDescriptorUserProfileQuery =
                this.createCriteriaQuery(
                        new Function<
                                CriteriaBuilder, CriteriaQuery<StylesheetUserPreferencesImpl>>() {
                            @Override
                            public CriteriaQuery<StylesheetUserPreferencesImpl> apply(
                                    CriteriaBuilder cb) {
                                final CriteriaQuery<StylesheetUserPreferencesImpl> criteriaQuery =
                                        cb.createQuery(StylesheetUserPreferencesImpl.class);
                                final Root<StylesheetUserPreferencesImpl> descriptorRoot =
                                        criteriaQuery.from(StylesheetUserPreferencesImpl.class);
                                criteriaQuery.select(descriptorRoot);
                                addFetches(descriptorRoot);
                                criteriaQuery.where(
                                        cb.and(
                                                cb.equal(
                                                        descriptorRoot.get(
                                                                StylesheetUserPreferencesImpl_
                                                                        .userId),
                                                        userIdParameter),
                                                cb.equal(
                                                        descriptorRoot.get(
                                                                StylesheetUserPreferencesImpl_
                                                                        .stylesheetDescriptor),
                                                        stylesheetDescriptorParameter),
                                                cb.equal(
                                                        descriptorRoot.get(
                                                                StylesheetUserPreferencesImpl_
                                                                        .profileId),
                                                        profileIdParameter)));

                                return criteriaQuery;
                            }
                        });

        this.findAllPreferencesForUser =
                this.createCriteriaQuery(
                        new Function<
                                CriteriaBuilder, CriteriaQuery<StylesheetUserPreferencesImpl>>() {
                            @Override
                            public CriteriaQuery<StylesheetUserPreferencesImpl> apply(
                                    CriteriaBuilder cb) {
                                final CriteriaQuery<StylesheetUserPreferencesImpl> criteriaQuery =
                                        cb.createQuery(StylesheetUserPreferencesImpl.class);
                                final Root<StylesheetUserPreferencesImpl> descriptorRoot =
                                        criteriaQuery.from(StylesheetUserPreferencesImpl.class);
                                criteriaQuery.select(descriptorRoot);
                                addFetches(descriptorRoot);
                                criteriaQuery.where(
                                        cb.equal(
                                                descriptorRoot.get(
                                                        StylesheetUserPreferencesImpl_.userId),
                                                userIdParameter));

                                return criteriaQuery;
                            }
                        });
    }

    /** Add the needed fetches to a critera query */
    protected void addFetches(final Root<StylesheetUserPreferencesImpl> descriptorRoot) {
        descriptorRoot.fetch(StylesheetUserPreferencesImpl_.layoutAttributes, JoinType.LEFT);
        descriptorRoot.fetch(StylesheetUserPreferencesImpl_.outputProperties, JoinType.LEFT);
        descriptorRoot.fetch(StylesheetUserPreferencesImpl_.parameters, JoinType.LEFT);
    }

    @Override
    public List<? extends IStylesheetUserPreferences> getStylesheetUserPreferencesForUser(
            int personId) {
        final TypedQuery<StylesheetUserPreferencesImpl> query =
                this.createCachedQuery(this.findAllPreferencesForUser);
        query.setParameter(this.userIdParameter, personId);

        return query.getResultList();
    }

    @PortalTransactional
    @Override
    public IStylesheetUserPreferences createStylesheetUserPreferences(
            IStylesheetDescriptor stylesheetDescriptor, IPerson person, IUserProfile profile) {
        final int userId = person.getID();
        final int profileId = profile.getProfileId();
        final StylesheetUserPreferencesImpl stylesheetUserPreferences =
                new StylesheetUserPreferencesImpl(stylesheetDescriptor, userId, profileId);

        this.getEntityManager().persist(stylesheetUserPreferences);

        return stylesheetUserPreferences;
    }

    @PortalTransactional
    @Override
    public IStylesheetUserPreferences createStylesheetUserPreferences(
            IStylesheetDescriptor stylesheetDescriptor, int userId, int profileId) {
        final StylesheetUserPreferencesImpl stylesheetUserPreferences =
                new StylesheetUserPreferencesImpl(stylesheetDescriptor, userId, profileId);

        this.getEntityManager().persist(stylesheetUserPreferences);

        return stylesheetUserPreferences;
    }

    @Override
    public List<? extends IStylesheetUserPreferences> getStylesheetUserPreferences() {
        final TypedQuery<StylesheetUserPreferencesImpl> query =
                this.createCachedQuery(this.findAllPreferences);
        return query.getResultList();
    }

    @Override
    public IStylesheetUserPreferences getStylesheetUserPreferences(long id) {
        return this.getEntityManager().find(StylesheetUserPreferencesImpl.class, id);
    }

    @Override
    public IStylesheetUserPreferences getStylesheetUserPreferences(
            IStylesheetDescriptor stylesheetDescriptor, IPerson person, IUserProfile profile) {
        return this.getStylesheetUserPreferences(
                stylesheetDescriptor, person.getID(), profile.getProfileId());
    }

    @Deprecated
    @Override
    public IStylesheetUserPreferences getStylesheetUserPreferences(
            IStylesheetDescriptor stylesheetDescriptor, int personId, int profileId) {
        final TypedQuery<StylesheetUserPreferencesImpl> query =
                this.createCachedQuery(findPreferencesByDescriptorUserProfileQuery);
        query.setParameter(
                this.stylesheetDescriptorParameter,
                (StylesheetDescriptorImpl) stylesheetDescriptor);
        query.setParameter(this.userIdParameter, personId);
        query.setParameter(this.profileIdParameter, profileId);

        final List<StylesheetUserPreferencesImpl> results = query.getResultList();

        return DataAccessUtils.uniqueResult(results);
    }

    @PortalTransactional
    @Override
    public void storeStylesheetUserPreferences(
            IStylesheetUserPreferences stylesheetUserPreferences) {
        this.getEntityManager().persist(stylesheetUserPreferences);
    }

    @PortalTransactional
    @Override
    public void deleteStylesheetUserPreferences(
            IStylesheetUserPreferences stylesheetUserPreferences) {
        this.getEntityManager().remove(stylesheetUserPreferences);
    }
}
