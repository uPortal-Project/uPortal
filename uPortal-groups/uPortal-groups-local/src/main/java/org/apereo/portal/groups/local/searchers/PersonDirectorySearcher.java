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
package org.apereo.portal.groups.local.searchers;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apereo.portal.EntityIdentifier;
import org.apereo.portal.IBasicEntity;
import org.apereo.portal.groups.GroupsException;
import org.apereo.portal.groups.local.ITypedEntitySearcher;
import org.apereo.portal.security.IPerson;
import org.apereo.services.persondir.IPersonAttributeDao;
import org.apereo.services.persondir.IPersonAttributes;
import org.apereo.services.persondir.support.IUsernameAttributeProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/** Searches the portal DB for people. Used by EntitySearcherImpl */
@Service
public class PersonDirectorySearcher implements ITypedEntitySearcher {
    private static final Log log = LogFactory.getLog(PersonDirectorySearcher.class);

    private final Class<? extends IBasicEntity> personEntityType = IPerson.class;
    private final IPersonAttributeDao personAttributeDao;
    private final IUsernameAttributeProvider usernameAttributeProvider;

    @Autowired
    public PersonDirectorySearcher(
            IPersonAttributeDao personAttributeDao,
            IUsernameAttributeProvider usernameAttributeProvider) {
        this.personAttributeDao = personAttributeDao;
        this.usernameAttributeProvider = usernameAttributeProvider;
    }

    @Override
    public EntityIdentifier[] searchForEntities(String query, SearchMethod method)
            throws GroupsException {
        query = transformQuery(query, method);
        log.debug("Searching for a person directory account matching query string " + query);

        final String usernameAttribute = this.usernameAttributeProvider.getUsernameAttribute();
        final Map<String, Object> queryMap = Collections.singletonMap(usernameAttribute, query);
        final Set<IPersonAttributes> results = this.personAttributeDao.getPeople(queryMap, null);
        // create an array of EntityIdentifiers from the search results
        final List<EntityIdentifier> entityIdentifiers = new ArrayList<>(results.size());
        for (final IPersonAttributes personAttributes : results) {
            entityIdentifiers.add(
                    new EntityIdentifier(personAttributes.getName(), this.personEntityType));
        }

        return entityIdentifiers.toArray(new EntityIdentifier[entityIdentifiers.size()]);
    }

    private String transformQuery(String query, SearchMethod method) throws GroupsException {
        // Ignores CS / CI
        switch (method) {
            case DISCRETE:
            case DISCRETE_CI:
                return query;
            case STARTS_WITH:
            case STARTS_WITH_CI:
                return query + IPersonAttributeDao.WILDCARD;
            case ENDS_WITH:
            case ENDS_WITH_CI:
                return IPersonAttributeDao.WILDCARD + query;
            case CONTAINS:
            case CONTAINS_CI:
                return IPersonAttributeDao.WILDCARD + query + IPersonAttributeDao.WILDCARD;
            default:
                throw new GroupsException("Unknown search type");
        }
    }

    @Override
    public Class<? extends IBasicEntity> getType() {
        return this.personEntityType;
    }
}
