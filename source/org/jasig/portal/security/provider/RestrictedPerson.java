/* Copyright 2004 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/

package org.jasig.portal.security.provider;

import java.util.Enumeration;

import org.jasig.portal.EntityIdentifier;
import org.jasig.portal.security.IPerson;
import org.jasig.portal.security.ISecurityContext;

/**
 * An IPerson object that wraps another IPerson
 * object and prevents access to the
 * underlying sercurity context.
 * @author Ken Weiner, kweiner@unicon.net
 * @version $Revision$
 */
public class RestrictedPerson implements IPerson {

    private IPerson person;

    public RestrictedPerson(IPerson person) {
        this.person = person;
    }

    public Object getAttribute(String key) {
        return this.person.getAttribute(key);
    }
    
    public Object[] getAttributeValues(String key) {
        return this.person.getAttributeValues(key);
    }

    public Enumeration getAttributeNames() {
        return this.person.getAttributeNames();
    }

    public Enumeration getAttributes() {
        return this.person.getAttributes();
    }

    public String getFullName() {
        return this.person.getFullName();
    }

    public int getID() {
        return this.person.getID();
    }

    public boolean isGuest() {
        return this.person.isGuest();
    }

    /**
     * RestrictedPerson's implementation of setAttribute does nothing.
     */
    public void setAttribute(String key, Object value) {
        // Part of RestrictedPerson's restrictedness is to do nothing
        // when this method is invoked.
    }

    /**
     * RestrictedPerson's implementation of setFullName does nothing.
     */
    public void setFullName(String sFullName) {
        // Part of RestrictedPerson's restrictedness is to do nothing
        // when this method is invoked.
    }

    /**
     * RestrictedPerson's implementation of setID does nothing.
     */
    public void setID(int sID) {
        // Part of RestrictedPerson's restrictedness is to do nothing
        // when this method is invoked.
    }

    /**
     * RestrictedPerson's implementation of getSecurityContext prevents
     * access to the security context by always returning null.
     * @return null
     */
    public ISecurityContext getSecurityContext() {
        return null;
    }

    /**
     * RestrictedPerson's implementation of setSecurityContext does nothing.
     */
    public void setSecurityContext(ISecurityContext securityContext) {
        // Part of RestrictedPerson's restrictedness is to do nothing
        // when this method is invoked.
    }

    /**
     * RestrictedPerson's implementation of getEntityIdentifier always returns null,
     * as part of the restrictedness of this IPerson implementation.
     * @return null
     */
    public EntityIdentifier getEntityIdentifier() {
        return this.person.getEntityIdentifier();
    }
}

