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

package  org.jasig.portal.services.entityproperties;

import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.collections.map.ReferenceMap;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jasig.portal.EntityIdentifier;
import org.jasig.portal.security.IPerson;
import org.jasig.portal.spring.locator.PersonAttributeDaoLocator;
import org.jasig.services.persondir.IPersonAttributeDao;
import org.jasig.services.persondir.IPersonAttributes;

/**
 * A finder implementation to provide IPerson properties derived from the
 * PersonDirectory
 *
 * @author Alex Vigdor av317@columbia.edu
 * @version $Revision$
 */
public class PersonDirPropertyFinder
        implements IEntityPropertyFinder {
    
    private static final Log log = LogFactory.getLog(PersonDirPropertyFinder.class);
    
    private Class<IPerson> person = org.jasig.portal.security.IPerson.class;
    private IPersonAttributeDao pa;
    private Map<String, Map<String, List<Object>>> cache;

    public PersonDirPropertyFinder() {
        pa = PersonAttributeDaoLocator.getPersonAttributeDao();
        cache = new ReferenceMap(ReferenceMap.HARD, ReferenceMap.SOFT, 120, .75f, true);
    }

    public String[] getPropertyNames(EntityIdentifier entityID) {
        String[] r = new String[0];
        if (entityID.getType().equals(person)) {
            final Map<String, List<Object>> properties = getPropertiesHash(entityID);
            final Set<String> propertyNames = properties.keySet();
            r = propertyNames.toArray(r);
        }
        return  r;
    }

    public String getProperty(EntityIdentifier entityID, String name) {
        String r = null;
        if (entityID.getType().equals(person)) {
            final Map<String, List<Object>> properties = getPropertiesHash(entityID);
            final List<Object> values = properties.get(name);
            
            final StringBuilder sb = new StringBuilder();
            for (Iterator<Object> iter = values.iterator(); iter.hasNext();) {
                Object value = iter.next();
                sb.append(value);
                if (iter.hasNext()) {
                    sb.append(", ");
                }
            }
            r = sb.toString();
        }
        return  r;
    }
    protected Map<String, List<Object>> getPropertiesHash(EntityIdentifier entityID) {
        final String entityIdKey = entityID.getKey();
        Map<String, List<Object>> ht = cache.get(entityIdKey);
        if (ht == null) {
            try {
                final IPersonAttributes personAttributes = pa.getPerson(entityIdKey);
                ht = personAttributes.getAttributes();
            } catch (Exception e) {
                log.error("Error getting properties hash for entityID [" + entityID + "]", e);
                ht = Collections.emptyMap();
            }
            cache.put(entityIdKey, ht);
        }
        return  new HashMap<String, List<Object>>(ht);
    }

}



