/* Copyright 2001, 2004 The JA-SIG Collaborative.  All rights reserved.
 *  See license distributed with this file and
 *  available online at http://www.uportal.org/license.html
 */

package org.jasig.portal.services;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jasig.portal.security.IPerson;
import org.jasig.portal.security.provider.RestrictedPerson;

import org.jasig.portal.services.persondir.IPersonDirectory;
import org.jasig.portal.services.persondir.support.SpringPersonDirectoryImpl;

/**
 * PersonDirectory is a source for user attributes.  It is configurable via a
 * Spring beans.dtd compliant configuration file in the properties directory
 * called personDirectory.xml. 
 * 
 * This class serves as the lookup mechanism and the interface by which client
 * code accesses user attributes.  Implementation of actually getting attributes
 * is delegated to an IPersonDirectory implementation which is declared as the
 * bean named "personDirectory" in the personDirectory.xml Spring configuration
 * file.
 * 
 * The default configuration of that file implements the legacy behavior of using
 * the PersonDirs.xml file for configuration.
 * 
 * @author Howard Gilbert
 * @author andrew.petro@yale.edu
 * @version $Revision$ $Date$
 */
public class PersonDirectory {

    private static final Log log = LogFactory.getLog(PersonDirectory.class);

    /**
     * This instance variable used to contain the set of attributes mapped in
     * PersonDir.xml.  It now is merely an empty Set.  It is no longer used by
     * PersonDirectory and should be removed in a future release.
     * @deprecated you cannot get the list of attributes in the abstract, only
     * for a particular user.
     */
    public static HashSet propertynames = new HashSet();

    private static PersonDirectory instance;

    private IPersonDirectory impl;

    private PersonDirectory(IPersonDirectory impl) {
        this.impl = impl;
    }

    /**
     * Obtain the singleton instance of PersonDirectory.
     * 
     * @return the singleton instance of PersonDirectory.
     */
    public static synchronized PersonDirectory instance() {
        /*
         * If our singleton exists already, return it. Otherwise, try to
         * instantiate it using Spring to parse an XML file
         * conforming to beans.dtd defining an IPersonDirectory named
         * "personDirectory" (see SpringPersonDirectoryImpl implementation).
         * The default configuration of SpringPersonDirectoryImpl,
         * as specified in its personDirectory.xml file, is to use the legacy
         * PersonDir.xml file to configure PersonDirectory.
         */
        if (instance == null) {
            try {
                instance = new PersonDirectory(new SpringPersonDirectoryImpl(
                        "/properties/personDirectory.xml"));
            } catch (Throwable t) {
                log.error("Error instantiating PersonDirectory", t);
            }
        }
        return instance;
    }

    /**
     * This method used to return an iterator over the names of attributes. The
     * method behavior is not well defined because attribute sources may choose
     * to return different attributes depending upon about whom they are asked.
     * Therefore, you can only know the attributes for particular users, not the
     * namespace of all possible attributes.
     * 
     * @return an iterator for an empty list.
     * @deprecated no longer supported
     */
    public static Iterator getPropertyNamesIterator() {
        return new ArrayList().iterator();
    }

    /**
     * Obtain a HashTable of attributes for the given user.
     * 
     * @param username -
     *                the name of the user
     * @return a Hashtable from user names to attributes.
     */
    public Hashtable getUserDirectoryInformation(String username) {
        /*
         * Currently we translate from the Map to a Hashtable.
         */
        Hashtable table = new Hashtable();
        table.putAll(this.impl.getUserDirectoryInformation(username));
        return table;
    }

    /**
     * Populate an IPerson with the attributes from the user directory for the
     * given uid.
     * 
     * @param uid -
     *                person for whom we are obtaining attributes
     * @param person -
     *                person object into which to store the attributes
     */
    public void getUserDirectoryInformation(String uid, IPerson person) {
        this.impl.getUserDirectoryInformation(uid, person);
    }

    /**
     * Returns a reference to a restricted IPerson represented by the supplied
     * user ID. The restricted IPerson allows access to person attributes, but
     * not the security context.
     * 
     * @param uid
     *                the user ID
     * @return the corresponding person, restricted so that its security context
     *            is inaccessible
     */
    public RestrictedPerson getRestrictedPerson(String uid) {
        return this.impl.getRestrictedPerson(uid);
    }
    
    /**
     * Supplies to PersonDirectory an updated IPerson to cache for the particular 
     * user identifier.
     * May have no effect, depending upon whether and how the underlying
     * IPerson implementation implements this method.
     * @param uid - user identifier
     * @param person - IPerson keyed by the uid
     */
    public void cachePerson(String uid, IPerson person){
        this.impl.cachePerson(uid, person);
    }

}