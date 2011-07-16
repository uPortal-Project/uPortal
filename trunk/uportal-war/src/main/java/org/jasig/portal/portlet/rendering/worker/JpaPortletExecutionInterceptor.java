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
import javax.persistence.EntityManagerFactory;
import javax.persistence.PersistenceException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.orm.jpa.EntityManagerFactoryUtils;
import org.springframework.orm.jpa.EntityManagerHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionSynchronizationManager;

/**
 * Opens and closes an {@link EntityManager} around the execution of a portlet, participates
 * in an existing {@link EntityManager} if one already exists.
 * 
 * @author Eric Dalquist
 * @version $Revision$
 */
@Service("JpaPortletExecutionInterceptor")
public class JpaPortletExecutionInterceptor extends PortletExecutionInterceptorAdaptor {
    private static final String ENTITY_MANAGER_FACTORY = JpaPortletExecutionInterceptor.class.getName() + ".ENTITY_MANAGER_FACTORY";
    private static final String PARTICIPATE = JpaPortletExecutionInterceptor.class.getName() + ".PARTICIPATE";
    
    private EntityManagerFactory entityManagerFactory;

    @Autowired
    public void setEntityManagerFactory(EntityManagerFactory entityManagerFactory) {
        this.entityManagerFactory = entityManagerFactory;
    }
    
    @Override
    public void preExecution(HttpServletRequest request, HttpServletResponse response, IPortletExecutionContext context) {
        //Use a local reference to the EntityManagerFactory to make sure we return the entity manager to the same place we got it from
        final EntityManagerFactory emf = entityManagerFactory;
        context.setExecutionAttribute(ENTITY_MANAGER_FACTORY, emf);        
        
        final boolean participate;
        if (TransactionSynchronizationManager.hasResource(emf)) {
            // Do not modify the EntityManager: just set the participate flag.
            participate = true;
        }
        else {
            logger.debug("Opening JPA EntityManager in PortletExecutionWorker for {}", context.getPortletWindowId());
            try {
                final EntityManager em = entityManagerFactory.createEntityManager();
                TransactionSynchronizationManager.bindResource(emf, new EntityManagerHolder(em));
            }
            catch (PersistenceException ex) {
                throw new DataAccessResourceFailureException("Could not create JPA EntityManager", ex);
            }
            
            participate = false;
        }
        
        context.setExecutionAttribute(PARTICIPATE, participate);
    }

    @Override
    public void postExecution(HttpServletRequest request, HttpServletResponse response, IPortletExecutionContext context, Exception e) {
        final EntityManagerFactory emf = (EntityManagerFactory)context.getExecutionAttribute(ENTITY_MANAGER_FACTORY);
        final boolean participate = (Boolean)context.getExecutionAttribute(PARTICIPATE);
        
        if (!participate) {
            final EntityManagerHolder emHolder = (EntityManagerHolder)TransactionSynchronizationManager.unbindResource(emf);
            logger.debug("Closing JPA EntityManager in PortletExecutionWorker for {}", context.getPortletWindowId());
            EntityManagerFactoryUtils.closeEntityManager(emHolder.getEntityManager());
        }
    }
}
