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

package org.jasig.portal.layout;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;

import org.jasig.portal.IUserProfile;
import org.jasig.portal.PortalException;
import org.jasig.portal.layout.immutable.ImmutableUserLayoutManagerWrapper;
import org.jasig.portal.security.IPerson;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;


/**
 * A factory class for obtaining {@link IUserLayoutManager} implementations.
 *
 * @author Peter Kharchenko  {@link <a href="mailto:pkharchenko@interactivebusiness.com"">pkharchenko@interactivebusiness.com"</a>}
 * @version 1.0
 */
@Component
public class UserLayoutManagerFactory implements BeanFactoryAware {
    public static final String USER_LAYOUT_MANAGER_PROTOTYPE_BEAN_NAME = "userLayoutManager";
    
    private BeanFactory beanFactory;
    private Collection<LayoutEventListener> layoutEventListeners = Collections.emptySet();
    
    @Override
    public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
        this.beanFactory = beanFactory;
    }

    @Autowired
    public void setLayoutEventListeners(Collection<LayoutEventListener> layoutEventListeners) {
        this.layoutEventListeners = new LinkedHashSet<LayoutEventListener>(layoutEventListeners);
    }
    
    /**
     * Obtain a regular user layout manager implementation
     *
     * @return an <code>IUserLayoutManager</code> value
     */
    public IUserLayoutManager getUserLayoutManager(IPerson person, IUserProfile profile) throws PortalException {
        final IUserLayoutManager userLayoutManager = (IUserLayoutManager)this.beanFactory.getBean(USER_LAYOUT_MANAGER_PROTOTYPE_BEAN_NAME, person, profile);
        
        userLayoutManager.addLayoutEventListeners(layoutEventListeners);
        
        return new TransientUserLayoutManagerWrapper(userLayoutManager);
    }

    /**
     * Returns an immutable version of a user layout manager.
     *
     * @param man an <code>IUserLayoutManager</code> value
     * @return an immutable <code>IUserLayoutManager</code> value
     * @exception PortalException if an error occurs
     */
    public IUserLayoutManager immutableUserLayoutManager(IUserLayoutManager man) throws PortalException {
        return new ImmutableUserLayoutManagerWrapper(man);
    }
}
