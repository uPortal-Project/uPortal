/* Copyright 2001, 2004 The JA-SIG Collaborative.  All rights reserved.
 *  See license distributed with this file and
 *  available online at http://www.uportal.org/license.html
 */

package org.jasig.portal.services;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jasig.portal.security.IPerson;
import org.jasig.portal.security.PersonFactory;
import org.jasig.portal.security.provider.RestrictedPerson;
import org.jasig.portal.services.persondir.support.IPersonAttributeDao;
import org.jasig.portal.spring.PortalApplicationContextFacade;
import org.springframework.beans.factory.BeanFactory;

/**
 * PersonDirectory is a source for user attributes.  It is configurable via a
 * Spring beans.dtd compliant configuration file in the properties directory
 * called personDirectory.xml. 
 * 
 * This class serves as the lookup mechanism and the interface by which client
 * code accesses user attributes.  Implementation of actually getting attributes
 * is delegated to an IPersonDirectory implementation which is declared as the
 * bean named "personAttributeDao" in the personDirectory.xml Spring configuration
 * file.
 * 
 * The default configuration of that file implements the legacy behavior of using
 * the PersonDirs.xml file for configuration.
 * 
 * @author Howard Gilbert
 * @author andrew.petro@yale.edu
 * @author Eric Dalquist <a href="mailto:edalquist@unicon.net">edalquist@unicon.net</a>
 * @version $Revision$ $Date$
 */
public class PersonDirectory {

    private static final Log log = LogFactory.getLog(PersonDirectory.class);

    /**
     * This instance variable used to contain the set of attributes mapped in
     * PersonDir.xml.  It now is merely an empty Set.  It is no longer used by
     * PersonDirectory and should be removed in a future release.
     * 
     * @deprecated you cannot get the list of attributes in the abstract, only
     * for a particular user.
     */
    public static HashSet propertynames = new HashSet();

    /** Singleton reference to PersonDirectory */
    private static PersonDirectory instance;

    /** Wrapped class which provides the functionality */
    private IPersonAttributeDao impl;

    /**
     * Private constructor to allow for singleton behavior.
     * 
     * @param impl The {@link IPersonAttributeDao} instance to wrap.
     */
    private PersonDirectory(IPersonAttributeDao impl) {
        this.impl = impl;
    }
    
    /**
     * Static lookup for a the Spring configured {@link IPersonAttributeDao}
     * implementation.
     * 
     * @return The Spring configured {@link IPersonAttributeDao} implementation.
     */
    public static IPersonAttributeDao getPersonAttributeDao() {
        final BeanFactory factory = PortalApplicationContextFacade.getPortalApplicationContext();
        
        final Object objectDelegate = factory.getBean("personAttributeDao");
        
        if (objectDelegate == null)
            throw new NullPointerException("Spring config file did not declare a bean named 'personDirectory'.");
        
        if (!(objectDelegate instanceof IPersonAttributeDao))
            throw new ClassCastException("Spring config file declared a 'personDirectory' bean which was not an instance of IPersonAttributeDao.");
        
        return (IPersonAttributeDao)objectDelegate;
    }

    /**
     * Obtain the singleton instance of PersonDirectory.
     * 
     * @return the singleton instance of PersonDirectory.
     * @deprecated Use {@link #getPersonAttributeDao()}
     */
    public static synchronized PersonDirectory instance() {
        if (instance == null) {
            try {
                instance = new PersonDirectory(getPersonAttributeDao());
            }
            catch (Throwable t) {
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
     * @deprecated Use {@link IPersonAttributeDao#getPossibleUserAttributeNames()}
     */
    public static Iterator getPropertyNamesIterator() {
        final Set attrNames = instance().impl.getPossibleUserAttributeNames();
        
        if (attrNames != null)
            return attrNames.iterator();
        else
            return (new ArrayList()).iterator();
    }

    /**
     * Returns a reference to a restricted IPerson represented by the supplied
     * user ID. The restricted IPerson allows access to person attributes, but
     * not the security context.
     * 
     * @param uid the user ID
     * @return the corresponding person, restricted so that its security context is inaccessible
     * @deprecated Use {@link PersonFactory#createRestrictedPerson()} and
     * {@link IPersonAttributeDao#getUserAttributes(String)} and
     * {@link RestrictedPerson#setAttributes(Map)}
     */
    public static RestrictedPerson getRestrictedPerson(final String uid) {
        final RestrictedPerson rp = PersonFactory.createRestrictedPerson();
        final Map attributes = instance().impl.getUserAttributes(uid);
        
        rp.setAttributes(attributes);
        
        return rp;
    }


    /**
     * Obtain a HashTable of attributes for the given user.
     * 
     * @param username the name of the user
     * @return a Hashtable from user names to attributes.
     * @deprecated Use {@link IPersonAttributeDao#getUserAttributes(String)}
     */
    public Hashtable getUserDirectoryInformation(String username) {
        final Map attrs = this.impl.getUserAttributes(username);
        return new Hashtable(attrs);
    }

    /**
     * Populate an IPerson with the attributes from the user directory for the
     * given uid.
     * 
     * @param uid person for whom we are obtaining attributes
     * @param person person object into which to store the attributes
     * @deprecated Use {@link IPersonAttributeDao#getUserAttributes(String)} and
     * {@link IPerson#setAttributes(Map)}
     */
    public void getUserDirectoryInformation(String uid, IPerson person) {
        final Map attrs = this.impl.getUserAttributes(uid);
        person.setAttributes(attrs);
    }
}