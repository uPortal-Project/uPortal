/**
 * Copyright � 2003 The JA-SIG Collaborative.  All rights reserved.
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
package org.jasig.portal.security;

import org.jasig.portal.UserIdentityStoreFactory;
import org.jasig.portal.properties.PropertiesManager;
import org.jasig.portal.security.provider.PersonImpl;

/**
 * Creates a person.
 * <p>
 * Can create representations of a <i>system</i> user and a <i>guest</i> user.
 * <p>
 * <i>system</i> users have an ID of 0
 * <p>
 * <i>guest</i> users have both of the following characteristics<br>
 * <ol>
 *   <li>User is not successfully authenticated with the portal.</li>
 *   <li>User name matches the value of the property
 *       <code>org.jasig.portal.security.PersonFactory.guest_user_name</code>
 *       in <code>portal.properties</code>.</li>
 * </ol>
 * @author Ken Weiner, kweiner@unicon.net
 * @version $Revision$
 */
public class PersonFactory {
    
    /**
     * The guest user name specified in portal.properties.
     */
    public static final String GUEST_USERNAME = PropertiesManager.getProperty("org.jasig.portal.security.PersonFactory.guest_user_name");

    /**
     * Creates an empty <code>IPerson</code> implementation. 
     * @return an empty <code>IPerson</code> implementation
     */
    public static IPerson createPerson() {
        return new PersonImpl();
    }

    /**
     * Creates a <i>system</i> user.
     * @return a <i>system</i> user
     */
    public static IPerson createSystemPerson() {
        IPerson person = createPerson();
        person.setAttribute(IPerson.USERNAME, "SYSTEM_USER");
        person.setID(0);
        return person;
    }

    /**
     * Creates a <i>guest</i> user.
     * @return <i>guest</i> user
     * @throws Exception
     */
    public static IPerson createGuestPerson() throws Exception {
        IPerson person = createPerson();
        person.setAttribute(IPerson.USERNAME, GUEST_USERNAME);
        person.setID(UserIdentityStoreFactory.getUserIdentityStoreImpl().getPortalUID(person));
        person.setSecurityContext(InitialSecurityContextFactory.getInitialContext("root"));
        return person;
    }
    
}
