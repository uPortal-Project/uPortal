/**
 * Copyright © 2004 The JA-SIG Collaborative.  All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. Redistributions of any form whatsoever must retain the following
 *    acknowledgment:
 *    "This product includes software developed by the JA-SIG Collaborative
 *    (http://www.jasig.org/)."
 *
 * THIS SOFTWARE IS PROVIDED BY THE JA-SIG COLLABORATIVE "AS IS" AND ANY
 * EXPRESSED OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL THE JA-SIG COLLABORATIVE OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
 * NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 * OF THE POSSIBILITY OF SUCH DAMAGE.
 *
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

    public void setAttribute(String key, Object value) {
        this.person.setAttribute(key, value);
    }

    /**
     * RestrictedPerson's implementation of setFullName does nothing.
     */
    public void setFullName(String sFullName) {
        this.person.setFullName(sFullName);
    }

    /**
     * RestrictedPerson's implementation of setID does nothing.
     */
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

    /**
     * RestrictedPerson's implementation of getEntityIdentifier always returns null,
     * as part of the restrictedness of this IPerson implementation.
     * @return null
     */
    public EntityIdentifier getEntityIdentifier() {
        return this.person.getEntityIdentifier();
    }
}

