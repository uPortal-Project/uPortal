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

package org.jasig.portal.groups.local.searchers;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jasig.portal.EntityIdentifier;
import org.jasig.portal.IBasicEntity;
import org.jasig.portal.groups.GroupsException;
import org.jasig.portal.groups.local.ITypedEntitySearcher;
import org.jasig.services.persondir.IPersonAttributeDao;
import org.jasig.services.persondir.IPersonAttributes;
import org.jasig.services.persondir.support.IUsernameAttributeProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Searches the portal DB for people.  Used by EntitySearcherImpl
 *
 * @author Alex Vigdor
 * @version $Revision$
 */
@Service
public class PersonDirectorySearcher implements ITypedEntitySearcher {
    private static final Log log = LogFactory.getLog(PersonDirectorySearcher.class);

    private final Class<? extends IBasicEntity> personEntityType = org.jasig.portal.security.IPerson.class;
    private IPersonAttributeDao personAttributeDao;
    private IUsernameAttributeProvider usernameAttributeProvider;

    @Autowired
    public void setUsernameAttributeProvider(IUsernameAttributeProvider usernameAttributeProvider) {
        this.usernameAttributeProvider = usernameAttributeProvider;
    }

    @Autowired
    public void setPersonAttributeDao(IPersonAttributeDao personAttributeDao) {
        this.personAttributeDao = personAttributeDao;
    }

    @Override
    public EntityIdentifier[] searchForEntities(String query, int method) throws GroupsException {
        
        switch (method) {
            case IS: {
                break;
            }
            case STARTS_WITH: {
                query = query + IPersonAttributeDao.WILDCARD;
                break;
            }
            case ENDS_WITH: {
                query = IPersonAttributeDao.WILDCARD + query;
                break;
            }
            case CONTAINS: {
                query = IPersonAttributeDao.WILDCARD + query + IPersonAttributeDao.WILDCARD;
                break; 
            }
            default: {
                throw new GroupsException("Unknown search type");
            }
        }
        
        log.debug("Searching for a person directory account matching query string " + query);
        
        final String usernameAttribute = this.usernameAttributeProvider.getUsernameAttribute();
        final Map<String, Object> queryMap = Collections.<String, Object>singletonMap(usernameAttribute, query);
        final Set<IPersonAttributes> results = this.personAttributeDao.getPeople(queryMap);
        
        // create an array of EntityIdentifiers from the search results
        final List<EntityIdentifier> entityIdentifiers = new ArrayList<EntityIdentifier>(results.size());
        for (final IPersonAttributes personAttributes : results) {
            entityIdentifiers.add(new EntityIdentifier(personAttributes.getName(), this.personEntityType));
        }

        return entityIdentifiers.toArray(new EntityIdentifier[entityIdentifiers.size()]);
    }

    @Override
    public Class<? extends IBasicEntity> getType() {
        return this.personEntityType;
    }
}
