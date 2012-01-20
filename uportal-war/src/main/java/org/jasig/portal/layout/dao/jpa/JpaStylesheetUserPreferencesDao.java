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

package org.jasig.portal.layout.dao.jpa;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.ParameterExpression;
import javax.persistence.criteria.Root;

import org.jasig.portal.IUserProfile;
import org.jasig.portal.jpa.BaseJpaDao;
import org.jasig.portal.layout.dao.IStylesheetUserPreferencesDao;
import org.jasig.portal.layout.om.IStylesheetDescriptor;
import org.jasig.portal.layout.om.IStylesheetUserPreferences;
import org.jasig.portal.security.IPerson;
import org.springframework.dao.support.DataAccessUtils;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

/**
 * JPA implementation of {@link IStylesheetUserPreferencesDao}
 * 
 * @author Eric Dalquist
 * @version $Revision$
 */
@Repository("stylesheetUserPreferencesDao")
public class JpaStylesheetUserPreferencesDao extends BaseJpaDao implements IStylesheetUserPreferencesDao {
    private static final String FIND_ALL_PREFERENCES_CACHE_REGION = StylesheetUserPreferencesImpl.class.getName() + ".query.FIND_ALL_PREFERENCES";
    private static final String FIND_PREFERENCES_BY_DESCRIPTOR_PERSON_PROFILE_CACHE_REGION = StylesheetUserPreferencesImpl.class.getName() + ".query.FIND_PREFERENCES_BY_DESCRIPTOR_PERSON_PROFILE_CACHE_REGION";
    
    private CriteriaQuery<StylesheetUserPreferencesImpl> findAllPreferences;
    private CriteriaQuery<StylesheetUserPreferencesImpl> findPreferencesByDescriptorUserProfileQuery;
    private ParameterExpression<StylesheetDescriptorImpl> stylesheetDescriptorParameter;
    private ParameterExpression<Integer> userIdParameter;
    private ParameterExpression<Integer> profileIdParameter;
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
    protected void buildCriteriaQueries(CriteriaBuilder cb) {
        this.stylesheetDescriptorParameter = cb.parameter(StylesheetDescriptorImpl.class, "stylesheetDescriptor");
        this.userIdParameter = cb.parameter(Integer.class, "userId");
        this.profileIdParameter = cb.parameter(Integer.class, "profileId");

        this.findAllPreferences = this.buildFindAllPreferences(cb);
        this.findPreferencesByDescriptorUserProfileQuery = this.buildFindPreferencesByDescriptorUserProfileQuery(cb);
    }
    
    protected CriteriaQuery<StylesheetUserPreferencesImpl> buildFindAllPreferences(final CriteriaBuilder cb) {
        final CriteriaQuery<StylesheetUserPreferencesImpl> criteriaQuery = cb.createQuery(StylesheetUserPreferencesImpl.class);
        final Root<StylesheetUserPreferencesImpl> descriptorRoot = criteriaQuery.from(StylesheetUserPreferencesImpl.class);
        criteriaQuery.select(descriptorRoot);
        addFetches(descriptorRoot);
        
        return criteriaQuery;
    }
    
    protected CriteriaQuery<StylesheetUserPreferencesImpl> buildFindPreferencesByDescriptorUserProfileQuery(final CriteriaBuilder cb) {
        final CriteriaQuery<StylesheetUserPreferencesImpl> criteriaQuery = cb.createQuery(StylesheetUserPreferencesImpl.class);
        final Root<StylesheetUserPreferencesImpl> descriptorRoot = criteriaQuery.from(StylesheetUserPreferencesImpl.class);
        criteriaQuery.select(descriptorRoot);
        addFetches(descriptorRoot);
        criteriaQuery.where(
            cb.and(
                cb.equal(descriptorRoot.get(StylesheetUserPreferencesImpl_.userId), this.userIdParameter),
                cb.equal(descriptorRoot.get(StylesheetUserPreferencesImpl_.stylesheetDescriptor), this.stylesheetDescriptorParameter),
                cb.equal(descriptorRoot.get(StylesheetUserPreferencesImpl_.profileId), this.profileIdParameter)
            )
        );
        
        return criteriaQuery;
    }

    /**
     * Add the needed fetches to a critera query
     */
    protected void addFetches(final Root<StylesheetUserPreferencesImpl> descriptorRoot) {
        descriptorRoot.fetch(StylesheetUserPreferencesImpl_.layoutAttributes, JoinType.LEFT);
        descriptorRoot.fetch(StylesheetUserPreferencesImpl_.outputProperties, JoinType.LEFT);
        descriptorRoot.fetch(StylesheetUserPreferencesImpl_.parameters, JoinType.LEFT);
    }
    

    /* (non-Javadoc)
     * @see org.jasig.portal.layout.dao.IStylesheetUserPreferencesDao#createStylesheetUserPreferences(org.jasig.portal.layout.om.IStylesheetDescriptor, org.jasig.portal.security.IPerson, org.jasig.portal.UserProfile)
     */
    @Transactional
    @Override
    public IStylesheetUserPreferences createStylesheetUserPreferences(IStylesheetDescriptor stylesheetDescriptor, IPerson person, IUserProfile profile) {
        final int userId = person.getID();
        final int profileId = profile.getProfileId();
        final StylesheetUserPreferencesImpl stylesheetUserPreferences = new StylesheetUserPreferencesImpl(stylesheetDescriptor, userId, profileId);
        
        this.entityManager.persist(stylesheetUserPreferences);
        
        return stylesheetUserPreferences;
    }
    
    @Transactional
    @Override
    public IStylesheetUserPreferences createStylesheetUserPreferences(IStylesheetDescriptor stylesheetDescriptor, int userId, int profileId) {
        final StylesheetUserPreferencesImpl stylesheetUserPreferences = new StylesheetUserPreferencesImpl(stylesheetDescriptor, userId, profileId);
        
        this.entityManager.persist(stylesheetUserPreferences);
        
        return stylesheetUserPreferences;
    }

    /* (non-Javadoc)
     * @see org.jasig.portal.layout.dao.IStylesheetUserPreferencesDao#getStylesheetUserPreferences()
     */
    @Override
    public List<? extends IStylesheetUserPreferences> getStylesheetUserPreferences() {
        final TypedQuery<StylesheetUserPreferencesImpl> query = this.createQuery(this.findAllPreferences, FIND_ALL_PREFERENCES_CACHE_REGION);
        return query.getResultList();
    }
    
    /* (non-Javadoc)
     * @see org.jasig.portal.layout.dao.IStylesheetUserPreferencesDao#getStylesheetUserPreferences(int)
     */
    @Override
    public IStylesheetUserPreferences getStylesheetUserPreferences(long id) {
        return this.entityManager.find(StylesheetUserPreferencesImpl.class, id);
    }

    /* (non-Javadoc)
     * @see org.jasig.portal.layout.dao.IStylesheetUserPreferencesDao#getStylesheetUserPreferences(long, org.jasig.portal.security.IPerson, org.jasig.portal.UserProfile)
     */
    @Override
    public IStylesheetUserPreferences getStylesheetUserPreferences(IStylesheetDescriptor stylesheetDescriptor, IPerson person, IUserProfile profile) {
        return this.getStylesheetUserPreferences(stylesheetDescriptor, person.getID(), profile.getProfileId());
    }
    
    @SuppressWarnings("deprecation")
    @Deprecated
    @Override
    public IStylesheetUserPreferences getStylesheetUserPreferences(IStylesheetDescriptor stylesheetDescriptor, int personId, int profileId) {
        final TypedQuery<StylesheetUserPreferencesImpl> query = this.createQuery(findPreferencesByDescriptorUserProfileQuery, FIND_PREFERENCES_BY_DESCRIPTOR_PERSON_PROFILE_CACHE_REGION);
        query.setParameter(this.stylesheetDescriptorParameter, (StylesheetDescriptorImpl)stylesheetDescriptor);
        query.setParameter(this.userIdParameter, personId);
        query.setParameter(this.profileIdParameter, profileId);
        
        final List<StylesheetUserPreferencesImpl> results = query.getResultList();
        
        return DataAccessUtils.uniqueResult(results);
    }

    /* (non-Javadoc)
     * @see org.jasig.portal.layout.dao.IStylesheetUserPreferencesDao#storeStylesheetUserPreferences(org.jasig.portal.layout.om.IStylesheetUserPreferences)
     */
    @Transactional
    @Override
    public void storeStylesheetUserPreferences(IStylesheetUserPreferences stylesheetUserPreferences) {
        this.entityManager.persist(stylesheetUserPreferences);
    }
    
    /* (non-Javadoc)
     * @see org.jasig.portal.layout.dao.IStylesheetUserPreferencesDao#deleteStylesheetUserPreferences(org.jasig.portal.layout.om.IStylesheetUserPreferences)
     */
    @Transactional
    @Override
    public void deleteStylesheetUserPreferences(IStylesheetUserPreferences stylesheetUserPreferences) {
        this.entityManager.remove(stylesheetUserPreferences);
    }
}
