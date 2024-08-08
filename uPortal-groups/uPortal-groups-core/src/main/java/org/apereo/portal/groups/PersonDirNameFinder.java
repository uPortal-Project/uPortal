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
package org.apereo.portal.groups;

import java.util.Map;
import org.apache.commons.collections.map.ReferenceMap;
import org.apache.commons.lang.StringUtils;
import org.apereo.portal.security.IPerson;
import org.apereo.services.persondir.IPersonAttributeDao;
import org.apereo.services.persondir.IPersonAttributes;

/**
 * Implementation of <code>IEntityNameFinder</code> for <code>IPersons</code> by looking up
 * displayName from an <code>IPersonAttributeDao</code>.
 */
public class PersonDirNameFinder implements IEntityNameFinder {

    /** Data Access Object backing this name finder. */
    private IPersonAttributeDao paDao;

    /** Our cache of entity names: */
    private Map<String, String> names =
            new ReferenceMap(ReferenceMap.HARD, ReferenceMap.SOFT, true);

    /**
     * Instantiate a PersonDirNameFinder backed by the given IPersonAttributeDao.
     *
     * @param pa DAO to back this PersonDirNameFinder
     */
    PersonDirNameFinder(IPersonAttributeDao pa) {
        this.paDao = pa;
    }

    @Override
    public String getName(String key) {
        String name = this.names.get(key);

        if (name == null && key != null) {
            // cached name not found, get name from underlying DAO.
            name = primGetName(key);
            // cache the name
            this.names.put(key, name);
        }
        return name;
    }

    @Override
    public Class<IPerson> getType() {
        return IPerson.class;
    }

    /**
     * Actually lookup a user name using the underlying IPersonAttributeDao.
     *
     * @param key - entity key which in this case is a unique identifier for a user
     * @return the display name for the identified user
     */
    private String primGetName(String key) {
        String name = key;
        final IPersonAttributes personAttributes = this.paDao.getPerson(name, null);
        if (personAttributes != null) {
            Object displayName = personAttributes.getAttributeValue("displayName");
            String displayNameStr = "";
            if (displayName != null) {
                displayNameStr = String.valueOf(displayName);
                if (StringUtils.isNotEmpty(displayNameStr)) {
                    name = displayNameStr;
                }
            }
        }
        return name;
    }

    /**
     * Returns a String that represents the value of this object.
     *
     * @return a string representation of the receiver
     */
    @Override
    public String toString() {
        return "PersonDirNameFinder backed by " + this.paDao;
    }
}
