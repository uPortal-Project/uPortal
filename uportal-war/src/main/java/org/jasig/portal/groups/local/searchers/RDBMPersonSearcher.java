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
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jasig.portal.EntityIdentifier;
import org.jasig.portal.IBasicEntity;
import org.jasig.portal.groups.GroupsException;
import org.jasig.portal.groups.local.ITypedEntitySearcher;
import org.jasig.portal.persondir.ILocalAccountDao;
import org.jasig.portal.spring.locator.LocalAccountDaoLocator;
import org.jasig.services.persondir.IPersonAttributes;

/**
 * Searches the portal DB for people.  Used by EntitySearcherImpl
 *
 * @author Alex Vigdor
 * @version $Revision$
 */


public class RDBMPersonSearcher  implements ITypedEntitySearcher{
    private static final Log log = LogFactory.getLog(RDBMPersonSearcher.class);
  
  private Class<? extends IBasicEntity> personDef;

  public RDBMPersonSearcher() {
    personDef = org.jasig.portal.security.IPerson.class;
  }
  
  public EntityIdentifier[] searchForEntities(String query, int method) throws GroupsException {
      
      log.debug("Searching for a local account matching query string " + query);
      
      Map<String, List<Object>> queryMap = new HashMap<String, List<Object>>();
      queryMap.put("username", Collections.<Object>singletonList(query));
      queryMap.put("given", Collections.<Object>singletonList(query));
      queryMap.put("sn", Collections.<Object>singletonList(query));
      
      // search the local account store for the given query
      ILocalAccountDao accountDao = LocalAccountDaoLocator.getLocalAccountDao();      
      List<IPersonAttributes> people = new ArrayList<IPersonAttributes>();
      people.addAll(accountDao.getPeopleWithMultivaluedAttributes(queryMap));

      // create an array of EntityIdentifiers from the search results
      EntityIdentifier[] results = new EntityIdentifier[people.size()];
      for (ListIterator<IPersonAttributes> i = people.listIterator(); i.hasNext();) {
          IPersonAttributes person = i.next();
          results[i.previousIndex()] = new EntityIdentifier(person.getName(), this.personDef);
      }
      
      return results;

  }
  
  public Class<? extends IBasicEntity> getType() {
    return personDef;
  }
}
