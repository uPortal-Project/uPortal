/**
 * Copyright (c) 2000-2009, Jasig, Inc.
 * See license distributed with this file and available online at
 * https://www.ja-sig.org/svn/jasig-parent/tags/rel-10/license-header.txt
 */
package org.jasig.portal.security;

import org.jasig.portal.IUserIdentityStore;
import org.jasig.portal.UserIdentityStoreFactory;
import org.jasig.portal.properties.PropertiesManager;
import org.jasig.portal.security.provider.PersonImpl;
import org.jasig.portal.security.provider.RestrictedPerson;
import org.jasig.portal.utils.threading.SingletonDoubleCheckedCreator;

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
    public static final String GUEST_USERNAME = 
        PropertiesManager.getProperty("org.jasig.portal.security.PersonFactory.guest_user_name", "guest");
    
    private static final SingletonDoubleCheckedCreator<Integer> GUEST_USER_ID_LOADER = new SingletonDoubleCheckedCreator<Integer>() {
        /* (non-Javadoc)
         * @see org.jasig.portal.utils.threading.SingletonDoubleCheckedCreator#createSingleton(java.lang.Object[])
         */
        @Override
        protected Integer createSingleton(Object... args) {
            final IPerson person = (IPerson)args[0];
            final IUserIdentityStore userIdentityStore = UserIdentityStoreFactory.getUserIdentityStoreImpl();
            try {
                return userIdentityStore.getPortalUID(person);
            }
            catch (Exception e) {
                throw new RuntimeException("Error while finding user id for person: "  + person, e);
            }
        }
    };

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
        final int guestUserId = GUEST_USER_ID_LOADER.get(person);
        person.setID(guestUserId);
        person.setSecurityContext(InitialSecurityContextFactory.getInitialContext("root"));
        return person;
    }
    
    /**
     * Creates a <i>restricted</i> user.
     * @return <i>restricted</i> user
     */
    public static RestrictedPerson createRestrictedPerson() {
        IPerson person = createPerson();
        
        return new RestrictedPerson(person);
    }
    
}
