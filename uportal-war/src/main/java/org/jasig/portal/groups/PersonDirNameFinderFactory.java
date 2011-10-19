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

 package org.jasig.portal.groups;

import org.jasig.portal.spring.locator.PersonAttributeDaoLocator;
import org.jasig.services.persondir.IPersonAttributeDao;

/**
 * Factory for creating and obtaining reference to 
 * a static singleton <code>PersonDirNameFinder</code>.
 * @author Alex Vigdor
 * @version $Revision$
 */
public class PersonDirNameFinderFactory implements IEntityNameFinderFactory {

    /**
     * Lazily initialized static singleton PersonDirNameFinder backed
     * by the IPersonAttributeDao provided by the PersonDirectory static service.
     */
    private static PersonDirNameFinder PERSON_DIR_NAME_FINDER_INSTANCE;

    /**
     * Do-nothing constructor.
     */
    public PersonDirNameFinderFactory() {
        super();
    }

    /**
     * Get the static singleton PersonDirNameFinder instance backed by
     * PersonDirectory.
     * 
     * @return the static singleton PersonDirNameFinder backed by PersonDirectory
     */
    public IEntityNameFinder newFinder() {

        if (PERSON_DIR_NAME_FINDER_INSTANCE == null) {
            PersonDirNameFinderFactory.storeSingleton();
        }

        return PERSON_DIR_NAME_FINDER_INSTANCE;

    }

    /**
     * Intantiates the static singleton field PERSON_DIR_NAME_FINDER_INSTANCE.
     * Synchronized to guarantee singletonness of the field.
     */
    private synchronized static void storeSingleton() {
        // recheck that we need to run because field could have been 
        // set since we decided to invoke this method but before we acquired
        // the lock on this object
        if (PERSON_DIR_NAME_FINDER_INSTANCE == null) {
            IPersonAttributeDao personAttributeDao = PersonAttributeDaoLocator
                    .getPersonAttributeDao();
            PERSON_DIR_NAME_FINDER_INSTANCE = new PersonDirNameFinder(
                    personAttributeDao);
        }
    }

}
