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

package org.jasig.portal.portlet.dao.jpa;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;

import org.apache.commons.lang.Validate;
import org.jasig.portal.jpa.BasePortalJpaDao;
import org.jasig.portal.jpa.OpenEntityManager;
import org.jasig.portal.portlet.dao.IPortletTypeDao;
import org.jasig.portal.portlet.om.IPortletType;
import org.springframework.stereotype.Repository;

import com.google.common.base.Function;

/**
 * JPA/Hibernate implementation of IChannelTypeDao.  This DAO handles 
 * channel types and is not yet integrated with the channel definition persistence
 * code.
 * 
 * @author Jen Bourey, jbourey@unicon.net
 * @revision $Revision$
 */
@Repository
public class JpaPortletTypeDao extends BasePortalJpaDao implements IPortletTypeDao {
    private CriteriaQuery<PortletTypeImpl> findAllTypesQuery;

    @Override
    public void afterPropertiesSet() throws Exception {
        this.findAllTypesQuery = this.createCriteriaQuery(new Function<CriteriaBuilder, CriteriaQuery<PortletTypeImpl>>() {
            @Override
            public CriteriaQuery<PortletTypeImpl> apply(CriteriaBuilder cb) {
                final CriteriaQuery<PortletTypeImpl> criteriaQuery = cb.createQuery(PortletTypeImpl.class);
                criteriaQuery.from(PortletTypeImpl.class);
                return criteriaQuery;
            }
        });
    }
    
    
    @Override
    @PortalTransactional
	public void deletePortletType(IPortletType type) {
        Validate.notNull(type, "definition can not be null");
        
        final IPortletType persistentChanneltype;
        final EntityManager entityManager = this.getEntityManager();
        if (entityManager.contains(type)) {
            persistentChanneltype = type;
        }
        else {
            persistentChanneltype = entityManager.merge(type);
        }
    	entityManager.remove(persistentChanneltype);
	}

    @Override
    @PortalTransactional
    public IPortletType createPortletType(String name, String cpdUri) {
        Validate.notEmpty(name, "name can not be null");
        Validate.notEmpty(cpdUri, "cpdUri can not be null");
        
        final PortletTypeImpl channelType = new PortletTypeImpl(name, cpdUri);
        
        this.getEntityManager().persist(channelType);
        
        return channelType;
    }

	@Override
    public IPortletType getPortletType(int id) {
		return this.getEntityManager().find(PortletTypeImpl.class, id);
	}

	@OpenEntityManager(unitName = PERSISTENCE_UNIT_NAME)
	@Override
	public IPortletType getPortletType(String name) {
        final NaturalIdQuery<PortletTypeImpl> query = this.createNaturalIdQuery(PortletTypeImpl.class);
        query.using(PortletTypeImpl_.name, name);
        return query.load();
	}

    @Override
	public List<IPortletType> getPortletTypes() {
        final TypedQuery<PortletTypeImpl> query = this.createCachedQuery(this.findAllTypesQuery);
        final List<PortletTypeImpl> portletTypes = query.getResultList();
		return new ArrayList<IPortletType>(portletTypes);
	}

	@Override
    @PortalTransactional
	public IPortletType updatePortletType(IPortletType type) {
        Validate.notNull(type, "type can not be null");
        
        this.getEntityManager().persist(type);
        
        return type;
	}

}
