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
        return person.getAttribute(key);
    }
    
    public Object[] getAttributeValues(String key) {
        return person.getAttributeValues(key);
    }

    public Enumeration getAttributeNames() {
        return person.getAttributeNames();
    }

    public Enumeration getAttributes() {
        return person.getAttributes();
    }

    public String getFullName() {
        return person.getFullName();
    }

    public int getID() {
        return person.getID();
    }

    public boolean isGuest() {
        return person.isGuest();
    }

    public void setAttribute(String key, Object value) {
        setAttribute(key, value);
    }

    public void setFullName(String sFullName) {
        setFullName(sFullName);
    }

    public void setID(int sID) {
        setID(sID);
    }

    /**
     * Prevents access to the security context
     * @return null
     */
    public ISecurityContext getSecurityContext() {
        return null;
    }

    public void setSecurityContext(ISecurityContext securityContext) {
        setSecurityContext(securityContext);
    }

    public EntityIdentifier getEntityIdentifier() {
        return getEntityIdentifier();
    }
}

