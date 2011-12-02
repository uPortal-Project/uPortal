/**
 * Licensed to Jasig under one or more contributor license
 * agreements. See the NOTICE file distributed with this work
 * for additional information regarding copyright ownership.
 * Jasig licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a
 * copy of the License at:
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.jasig.portal.spring.security.preauth;

import java.util.Collection;
import java.util.Collections;

import org.jasig.portal.security.IPerson;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;

/**
 * PortalAuthentication provides a uPortal-specific implementation of Spring
 * Security's Authentication interface, enabling pre-authentication scenarios
 * in uPortal.  This implementation wraps the IPerson object, delegating to it
 * for determining authentication state and user details.  The user's
 * current SecurityContext is used as the credentials object.
 * 
 * @author Jen Bourey, jennifer.bourey@gmail.com
 * @version $Revision$
 */
public class PortalAuthentication implements Authentication {
    
    private final IPerson person;
    
    public PortalAuthentication(IPerson person) {
        this.person = person;
    }

    @Override
    public String getName() {
        return person.getName();
    }

    @Override
    public Collection<GrantedAuthority> getAuthorities() {
        return Collections.<GrantedAuthority>emptyList();
    }

    @Override
    public Object getCredentials() {
        return person.getSecurityContext();
    }

    @Override
    public Object getDetails() {
        // we don't really have any other information to provide
        return null;
    }

    @Override
    public IPerson getPrincipal() {
        // use the IPerson object as the principal
        return this.person;
    }

    @Override
    public boolean isAuthenticated() {
        // delegate to the portal to determine authentication state
        return !person.isGuest();
    }

    @Override
    public void setAuthenticated(boolean authenticated) throws IllegalArgumentException {
        throw new IllegalArgumentException("PortalAuthentication implementation does not allow manually setting authenticated flag");
    }

}
