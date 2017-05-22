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
package org.apereo.portal.layout;

import org.apereo.portal.IUserProfile;
import org.apereo.portal.PortalException;
import org.apereo.portal.layout.immutable.ImmutableTransientUserLayoutManagerWrapper;
import org.apereo.portal.security.IPerson;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.stereotype.Component;

/**
 * A factory class for obtaining {@link IUserLayoutManager} implementations.
 */
@Component
public class UserLayoutManagerFactory implements BeanFactoryAware {
    public static final String USER_LAYOUT_MANAGER_PROTOTYPE_BEAN_NAME = "userLayoutManager";

    private BeanFactory beanFactory;

    @Override
    public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
        this.beanFactory = beanFactory;
    }

    /**
     * Obtain a regular user layout manager implementation (which allows transient layout
     * alterations). The specific layout type depends on whether the user is a guest user.
     *
     * @return an <code>IUserLayoutManager</code> value
     */
    public IUserLayoutManager getUserLayoutManager(IPerson person, IUserProfile profile)
            throws PortalException {
        final IUserLayoutManager userLayoutManager =
                (IUserLayoutManager)
                        this.beanFactory.getBean(
                                USER_LAYOUT_MANAGER_PROTOTYPE_BEAN_NAME, person, profile);

        if (person.isGuest()) {
            return new ImmutableTransientUserLayoutManagerWrapper(userLayoutManager);
        }
        return new TransientUserLayoutManagerWrapper(userLayoutManager);
    }
}
