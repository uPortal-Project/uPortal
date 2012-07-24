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
package org.jasig.portal.jpa;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.PersistenceException;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.springframework.aop.framework.ProxyFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationEventPublisherAware;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;

/**
 * Wraps the created {@link EntityManagerFactory} so that create/close of {@link EntityManager} instances results
 * in spring events being dispatched 
 */
public class EventingLocalContainerEntityManagerFactoryBean extends LocalContainerEntityManagerFactoryBean implements ApplicationEventPublisherAware, ApplicationListener<ContextRefreshedEvent> {
    private static final AtomicLong EVENT_ID = new AtomicLong();
    private final AtomicBoolean contextReady = new AtomicBoolean(false);
    private ApplicationEventPublisher applicationEventPublisher;
    
    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        contextReady.set(true);
    }

    @Override
    public void setApplicationEventPublisher(ApplicationEventPublisher applicationEventPublisher) {
        this.applicationEventPublisher = applicationEventPublisher;
    }

    @Override
    protected EntityManagerFactory createNativeEntityManagerFactory() throws PersistenceException {
        final String persistenceUnitName = this.getPersistenceUnitName();

        //Create actual EMF
        final EntityManagerFactory nativeEntityManagerFactory = super.createNativeEntityManagerFactory();

        //Add a proxy to the EMF which results in events
        final ProxyFactory proxyFactory = new ProxyFactory(nativeEntityManagerFactory);
        proxyFactory.addAdvice(new EventingEntityMangerFactoryInterceptor(applicationEventPublisher, contextReady, persistenceUnitName));
        return (EntityManagerFactory)proxyFactory.getProxy();
    }
    
    /**
     * Interceptor that fires a {@link EntityManagerCreatedEvent} event when {@link EntityManagerFactory#createEntityManager()} or
     * {@link EntityManagerFactory#createEntityManager(java.util.Map)} is called
     */
    private static final class EventingEntityMangerFactoryInterceptor implements MethodInterceptor {
        private final ApplicationEventPublisher applicationEventPublisher;
        private final AtomicBoolean contextReady;
        private final String persistenceUnitName;

        public EventingEntityMangerFactoryInterceptor(ApplicationEventPublisher applicationEventPublisher,
                AtomicBoolean contextReady, String persistenceUnitName) {
            this.applicationEventPublisher = applicationEventPublisher;
            this.contextReady = contextReady;
            this.persistenceUnitName = persistenceUnitName;
        }

        @Override
        public Object invoke(MethodInvocation invocation) throws Throwable {
            if ("createEntityManager".equals(invocation.getMethod().getName()) && contextReady.get()) {
                final EntityManager entityManager = (EntityManager)invocation.proceed();
                
                final long entityManagerId = EVENT_ID.getAndIncrement();
                
                final EntityManagerCreatedEvent entityManagerCreatedEvent = new EntityManagerCreatedEvent(this, entityManagerId, persistenceUnitName, entityManager);
                applicationEventPublisher.publishEvent(entityManagerCreatedEvent);
                
                //Add a proxy to the EMF which results in events
                final ProxyFactory proxyFactory = new ProxyFactory(entityManager);
                proxyFactory.addAdvice(new EventingEntityMangerInterceptor(applicationEventPublisher, contextReady, entityManagerId, persistenceUnitName, entityManager));
                return (EntityManager)proxyFactory.getProxy();
            }
            
            return invocation.proceed();
        }
    }
    
    /**
     * Interceptor that fires a {@link EntityManagerClosingEvent} event when {@link EntityManager#close()} is called
     */
    private static final class EventingEntityMangerInterceptor implements MethodInterceptor {
        private final ApplicationEventPublisher applicationEventPublisher;
        private final AtomicBoolean contextReady;
        private final long entityManagerId;
        private final String persistenceUnitName;
        private final EntityManager entityManager;

        public EventingEntityMangerInterceptor(ApplicationEventPublisher applicationEventPublisher,
                AtomicBoolean contextReady, long entityManagerId, String persistenceUnitName,
                EntityManager entityManager) {
            this.applicationEventPublisher = applicationEventPublisher;
            this.contextReady = contextReady;
            this.entityManagerId = entityManagerId;
            this.persistenceUnitName = persistenceUnitName;
            this.entityManager = entityManager;
        }



        @Override
        public Object invoke(MethodInvocation invocation) throws Throwable {
            if ("close".equals(invocation.getMethod().getName()) && contextReady.get()) {
                final EntityManagerClosingEvent entityManagerClosingEvent = new EntityManagerClosingEvent(this, this.entityManagerId, this.persistenceUnitName, this.entityManager);
                this.applicationEventPublisher.publishEvent(entityManagerClosingEvent);
            }
            
            return invocation.proceed();
        }
    }
}
