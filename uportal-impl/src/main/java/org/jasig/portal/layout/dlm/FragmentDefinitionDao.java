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

package org.jasig.portal.layout.dlm;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import org.apache.commons.lang.Validate;
import org.springframework.dao.support.DataAccessUtils;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public class FragmentDefinitionDao implements IFragmentDefinitionDao {

    // Static Members
    private static final String GET_ALL_FRAGMENTS = "SELECT x FROM FragmentDefinition x ORDER BY x.precedence DESC";
    private static final String FIND_FRAGMENT_BY_NAME = "SELECT x FROM FragmentDefinition x WHERE x.name = :name";
    
    private static final String GET_ALL_FRAGMENTS_CACHE_REGION = FragmentDefinition.class.getName() + ".query.GET_ALL_FRAGMENTS";
    private static final String FIND_FRAGMENT_BY_NAME_CACHE_REGION = FragmentDefinition.class.getName() + ".query.FIND_FRAGMENT_BY_NAME";

    // Instance Members.
    private EntityManager entityManager;

    /**
     * @return the entityManager
     */
    public EntityManager getEntityManager() {
        return entityManager;
    }

    /**
     * @param entityManager the entityManager to set
     */
    @PersistenceContext
    public void setEntityManager(EntityManager entityManager) {
        this.entityManager = entityManager;
    }
    
    @SuppressWarnings("unchecked")
    public List<FragmentDefinition> getAllFragments() {

        final Query query = this.entityManager.createQuery(GET_ALL_FRAGMENTS);
        query.setHint("org.hibernate.cacheable", true);
        query.setHint("org.hibernate.cacheRegion", GET_ALL_FRAGMENTS_CACHE_REGION);
        final List<FragmentDefinition> rslt = query.getResultList();
        return rslt;
        
    }

    @SuppressWarnings("unchecked")
    public FragmentDefinition getFragmentDefinition(String name) {
        
        final Query query = this.entityManager.createQuery(FIND_FRAGMENT_BY_NAME);
        query.setParameter("name", name);
        query.setHint("org.hibernate.cacheable", true);
        query.setHint("org.hibernate.cacheRegion", FIND_FRAGMENT_BY_NAME_CACHE_REGION);
        query.setMaxResults(1);
        
        final List<FragmentDefinition> list = query.getResultList();
        final FragmentDefinition rslt = (FragmentDefinition) DataAccessUtils.uniqueResult(list);
        return rslt;
        
    }

    @Transactional
    public void updateFragmentDefinition(FragmentDefinition fd) {
        
        Validate.notNull(fd, "FragmentDefinition can not be null");
        this.entityManager.merge(fd);
        
    }

    @Transactional
    public void removeFragmentDefinition(FragmentDefinition fd) {
        
        Validate.notNull(fd, "FragmentDefinition can not be null");
        this.entityManager.remove(fd);
        
    }

}
