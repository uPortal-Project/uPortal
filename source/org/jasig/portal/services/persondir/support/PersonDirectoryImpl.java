/* Copyright 2004 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/

package org.jasig.portal.services.persondir.support;

import java.util.Iterator;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jasig.portal.security.IPerson;
import org.jasig.portal.security.provider.RestrictedPerson;

import org.jasig.portal.UserIdentityStoreFactory;
import org.jasig.portal.security.PersonFactory;

import org.jasig.portal.services.persondir.IPersonDirectory;

/**
 * A standard implementation of PersonDirectory which is a thin wrapper
 * around a PersonAttributeDao.
 * @author andrew.petro@yale.edu
 * @version $Revision$ $Date$
 */
public class PersonDirectoryImpl implements IPersonDirectory {

    private static final Log log = LogFactory.getLog(PersonDirectoryImpl.class);
    
    /**
     * A DAO that knows how to go from usernames to attributes.
     * This implementation is a thin wrapper around this delegate.
     */
    private PersonAttributeDao attributeSource;
    
    /* (non-Javadoc)
     * @see edu.yale.its.portal.services.persondir.PersonDirectory#getUserDirectoryInformation(java.lang.String)
     */
    public Map getUserDirectoryInformation(String username) {
        return this.attributeSource.attributesForUser(username);
    }

    /* (non-Javadoc)
     * @see edu.yale.its.portal.services.persondir.PersonDirectory#getUserDirectoryInformation(java.lang.String, org.jasig.portal.security.IPerson)
     */
    public void getUserDirectoryInformation(String uid, IPerson person) {
        Map attribs = this.attributeSource.attributesForUser(uid);
        for (Iterator iter = attribs.keySet().iterator(); iter.hasNext();) {
            String key = (String) iter.next();
            person.setAttribute(key, attribs.get(key));
        }
    }

    /* (non-Javadoc)
     * @see edu.yale.its.portal.services.persondir.PersonDirectory#getRestrictedPerson(java.lang.String)
     */
    public RestrictedPerson getRestrictedPerson(String uid) {
        /*
         * Instead of this static invocation of PersonFactory, we could instead
         * rely on someone to inject us an instance of PersonFactory.
         * One step at a time. 
         */
        IPerson person = PersonFactory.createPerson();
        person.setAttribute(IPerson.USERNAME, uid);
        try {
            /*
             * Instead of this static lookup, we could instead rely upon someone to
             * inject us with a UserIdentityStore.  One step at a time.
             */
            person.setID(UserIdentityStoreFactory.getUserIdentityStoreImpl().getPortalUID(person));
        } catch (Throwable t) {
            // Should we fail here?
            log.error("Failed to set ID of the RestrictedPerson.", t);
        }
        
        // populate our new person with attributes
        getUserDirectoryInformation(uid, person);
        
        return new RestrictedPerson(person);
    }

    /**
     * @return Returns the attributeSource.
     */
    public PersonAttributeDao getAttributeSource() {
        return this.attributeSource;
    }
    /**
     * @param attributeSource The attributeSource to set.
     */
    public void setAttributeSource(PersonAttributeDao attributeSource) {
        this.attributeSource = attributeSource;
    }

    /* (non-Javadoc)
     * @see org.jasig.portal.services.persondir.IPersonDirectory#cachePerson(java.lang.String, org.jasig.portal.security.IPerson)
     */
    public void cachePerson(String uid, IPerson person) {
        // do nothing -- this class does not cache, though it may be wrapped
        // by a caching wrapper.
    }
}