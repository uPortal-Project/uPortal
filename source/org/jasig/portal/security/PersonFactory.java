/* Copyright 2003 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
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
