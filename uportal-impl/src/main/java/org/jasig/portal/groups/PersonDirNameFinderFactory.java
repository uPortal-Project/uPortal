/**
 * Copyright (c) 2000-2009, Jasig, Inc.
 * See license distributed with this file and available online at
 * https://www.ja-sig.org/svn/jasig-parent/tags/rel-10/license-header.txt
 */
 package org.jasig.portal.groups;

import org.jasig.portal.services.PersonDirectory;
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
            IPersonAttributeDao personAttributeDao = PersonDirectory
                    .getPersonAttributeDao();
            PERSON_DIR_NAME_FINDER_INSTANCE = new PersonDirNameFinder(
                    personAttributeDao);
        }
    }

}
