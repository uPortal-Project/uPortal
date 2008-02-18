/* Copyright 2004 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/

package org.jasig.portal.security.provider;

import java.util.Enumeration;
import java.util.List;
import java.util.Map;

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
    private static final long serialVersionUID = 1L;

    private final IPerson person;

    public RestrictedPerson(IPerson person) {
        this.person = person;
    }

    public Object getAttribute(String key) {
        return this.person.getAttribute(key);
    }

    public Object[] getAttributeValues(String key) {
        return this.person.getAttributeValues(key);
    }

    public Enumeration<String> getAttributeNames() {
        return this.person.getAttributeNames();
    }

    public Enumeration<List<Object>> getAttributes() {
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

    public void setAttribute(String key, Object value) {
        this.person.setAttribute(key, value);
    }
    
    public void setAttribute(String key, List<Object> values) {
        this.person.setAttribute(key, values);
    }

    public void setAttributes(Map<String, List<Object>> attrs) {
        this.person.setAttributes(attrs);
    }

    public void setFullName(String sFullName) {
        this.person.setFullName(sFullName);
    }

    public void setID(int sID) {
        this.person.setID(sID);
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

    public EntityIdentifier getEntityIdentifier() {
        return this.person.getEntityIdentifier();
    }

    public void setEntityIdentifier(final EntityIdentifier ei) {
    	// Nothing to do
    }

    public String getName() {
        return this.person.getName();
    }
}

