/**
 * Licensed to Apereo under one or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information regarding copyright ownership. Apereo
 * licenses this file to you under the Apache License, Version 2.0 (the "License"); you may not use
 * this file except in compliance with the License. You may obtain a copy of the License at the
 * following location:
 *
 * <p>http://www.apache.org/licenses/LICENSE-2.0
 *
 * <p>Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apereo.portal.services;

import org.apereo.portal.AuthorizationException;
import org.apereo.portal.groups.GroupsException;
import org.apereo.portal.groups.IGroupMember;
import org.apereo.portal.security.IAuthorizationPrincipal;
import org.apereo.portal.security.IAuthorizationService;
import org.apereo.portal.security.IPermissionManager;
import org.apereo.portal.security.IUpdatingPermissionManager;
import org.apereo.portal.spring.locator.AuthorizationServiceLocator;
import org.apereo.portal.utils.threading.SingletonDoubleCheckedCreator;

/**
 * This class is a static facade that provides access to the Spring-managed
 * <code>authorizationService</code> bean.  The things that use it should be refactored so they
 * don't, and this class should be removed.
 *
 * @deprecated Use the Spring-managed <code>authorizationService</code> bean instead
 */
@Deprecated
public class AuthorizationServiceFacade {

    private static final SingletonDoubleCheckedCreator<AuthorizationServiceFacade>
            authorizationServiceInstance =
                    new SingletonDoubleCheckedCreator<AuthorizationServiceFacade>() {
                        @Override
                        protected AuthorizationServiceFacade createSingleton(Object... args) {
                            return new AuthorizationServiceFacade();
                        }
                    };

    protected IAuthorizationService m_authorization = null;

    private AuthorizationServiceFacade() throws AuthorizationException {
        // Get an actual authorization instance from Spring
        m_authorization = AuthorizationServiceLocator.getAuthorizationService();
    }

    /**
     * @return org.apereo.portal.groups.IGroupMember
     * @param principal IAuthorizationPrincipal
     * @exception GroupsException
     */
    public IGroupMember getGroupMember(IAuthorizationPrincipal principal) throws GroupsException {
        return m_authorization.getGroupMember(principal);
    }

    /** @return Authorization */
    public static final AuthorizationServiceFacade instance() throws AuthorizationException {
        return authorizationServiceInstance.get();
    }

    /**
     * @param owner java.lang.String
     * @return org.apereo.portal.security.IPermissionManager
     * @exception AuthorizationException
     */
    public IPermissionManager newPermissionManager(String owner) throws AuthorizationException {
        return m_authorization.newPermissionManager(owner);
    }

    /**
     * @param key java.lang.String
     * @param type java.lang.Class
     * @return org.apereo.portal.security.IAuthorizationPrincipal
     * @exception AuthorizationException
     */
    public IAuthorizationPrincipal newPrincipal(String key, Class type)
            throws AuthorizationException {
        return m_authorization.newPrincipal(key, type);
    }

    /**
     * @param groupMember
     * @return org.apereo.portal.security.IAuthorizationPrincipal
     * @exception GroupsException
     */
    public IAuthorizationPrincipal newPrincipal(IGroupMember groupMember) throws GroupsException {
        return m_authorization.newPrincipal(groupMember);
    }

    /**
     * @param owner java.lang.String
     * @return org.apereo.portal.security.IUpdatingPermissionManager
     * @exception AuthorizationException
     */
    public IUpdatingPermissionManager newUpdatingPermissionManager(String owner)
            throws AuthorizationException {
        return m_authorization.newUpdatingPermissionManager(owner);
    }

}
