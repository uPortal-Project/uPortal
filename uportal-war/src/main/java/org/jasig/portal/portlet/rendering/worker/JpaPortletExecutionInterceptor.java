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

package org.jasig.portal.portlet.rendering.worker;

import javax.persistence.EntityManager;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.orm.jpa.EntityManagerFactoryUtils;
import org.springframework.orm.jpa.EntityManagerHolder;
import org.springframework.orm.jpa.JpaAccessor;
import org.springframework.orm.jpa.JpaInterceptor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionSynchronizationManager;

/**
 * Opens and closes an {@link EntityManager} around the execution of a portlet, participates
 * in an existing {@link EntityManager} if one already exists.
 * 
 * @author Eric Dalquist
 * @version $Revision$
 * @see JpaInterceptor
 */
public class JpaPortletExecutionInterceptor extends JpaAccessor implements IPortletExecutionInterceptor {
    private static final String IS_NEW = JpaPortletExecutionInterceptor.class.getName() + ".IS_NEW";
    private static final String ENTITY_MANAGER_FACTORY = JpaPortletExecutionInterceptor.class.getName() + ".ENTITY_MANAGER_FACTORY";
    
    @Override
    public void preExecution(HttpServletRequest request, HttpServletResponse response, IPortletExecutionContext context) {
        // Determine current EntityManager: either the transactional one
        // managed by the factory or a temporary one for the given invocation.
        EntityManager em = getTransactionalEntityManager();
        boolean isNewEm = false;
        if (em == null) {
            logger.debug("Creating new EntityManager for JpaInterceptor invocation");
            em = createEntityManager();
            isNewEm = true;
            TransactionSynchronizationManager.bindResource(getEntityManagerFactory(), new EntityManagerHolder(em));
            
            //For new EM store as attribute so it can be closed
            context.setExecutionAttribute(ENTITY_MANAGER_FACTORY, em);
        }
        
        context.setExecutionAttribute(IS_NEW, isNewEm);
    }

    @Override
    public void postExecution(HttpServletRequest request, HttpServletResponse response, IPortletExecutionContext context, Exception e) {
        boolean isNewEm = (Boolean)context.getExecutionAttribute(IS_NEW);
        if (isNewEm) {
            TransactionSynchronizationManager.unbindResource(getEntityManagerFactory());
            EntityManager em = (EntityManager)context.getExecutionAttribute(ENTITY_MANAGER_FACTORY);
            EntityManagerFactoryUtils.closeEntityManager(em);
        }
    }

    /* (non-Javadoc)
     * @see org.jasig.portal.portlet.rendering.worker.IPortletExecutionInterceptor#preSubmit(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse, org.jasig.portal.portlet.rendering.worker.IPortletExecutionContext)
     */
    @Override
    public void preSubmit(HttpServletRequest request, HttpServletResponse response, IPortletExecutionContext context) {
        //noop
    }
}
