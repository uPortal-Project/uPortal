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

package org.jasig.portal.groups.pags.dao.jpa;

import javax.persistence.EntityManager;

import org.apache.commons.lang.Validate;
import org.jasig.portal.jpa.BasePortalJpaDao;
import org.jasig.portal.groups.pags.dao.IPersonAttributesGroupTestGroupDefinitionDao;
import org.jasig.portal.groups.pags.dao.IPersonAttributesGroupDefinition;
import org.jasig.portal.groups.pags.dao.IPersonAttributesGroupTestGroupDefinition;
import org.springframework.stereotype.Repository;

/**
 * @author Shawn Connolly, sconnolly@unicon.net
 */
@Repository("personAttributesGroupTestGroupDefinitionDao")
public class JpaPersonAttributesGroupTestGroupDefinitionDao extends BasePortalJpaDao implements IPersonAttributesGroupTestGroupDefinitionDao {

    @PortalTransactional
    @Override
    public IPersonAttributesGroupTestGroupDefinition updatePersonAttributesGroupTestGroupDefinition(IPersonAttributesGroupTestGroupDefinition personAttributesGroupTestGroupDefinition) {
        Validate.notNull(personAttributesGroupTestGroupDefinition, "personAttributesGroupTestGroupDefinition can not be null");

        final IPersonAttributesGroupTestGroupDefinition persistentDefinition;
        final EntityManager entityManager = this.getEntityManager();
        if (entityManager.contains(personAttributesGroupTestGroupDefinition)) {
            persistentDefinition = personAttributesGroupTestGroupDefinition;
        } else {
            persistentDefinition = entityManager.merge(personAttributesGroupTestGroupDefinition);
        }

        this.getEntityManager().persist(persistentDefinition);
        return persistentDefinition;
    }

    @PortalTransactional
    @Override
    public void deletePersonAttributesGroupTestGroupDefinition(IPersonAttributesGroupTestGroupDefinition definition) {
        Validate.notNull(definition, "definition can not be null");

        final IPersonAttributesGroupTestGroupDefinition persistentDefinition;
        final EntityManager entityManager = this.getEntityManager();
        if (entityManager.contains(definition)) {
            persistentDefinition = definition;
        } else {
            persistentDefinition = entityManager.merge(definition);
        }
        entityManager.remove(persistentDefinition);
    }

    @PortalTransactional
    @Override
    public IPersonAttributesGroupTestGroupDefinition createPersonAttributesGroupTestGroupDefinition(IPersonAttributesGroupDefinition group) {
        final IPersonAttributesGroupTestGroupDefinition personAttributesGroupTestGroupDefinition = new PersonAttributesGroupTestGroupDefinitionImpl((PersonAttributesGroupDefinitionImpl)group);
        this.getEntityManager().persist(personAttributesGroupTestGroupDefinition);
        return personAttributesGroupTestGroupDefinition;
    }

}
