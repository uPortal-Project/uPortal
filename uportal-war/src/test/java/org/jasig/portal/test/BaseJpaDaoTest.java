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

package org.jasig.portal.test;

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Query;
import javax.persistence.metamodel.EntityType;
import javax.persistence.metamodel.Metamodel;

import org.aopalliance.intercept.MethodInvocation;
import org.jasig.portal.concurrency.CallableWithoutResult;
import org.jasig.portal.spring.MockitoFactoryBean;
import org.junit.After;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.orm.jpa.JpaInterceptor;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionOperations;
import org.springframework.transaction.support.TransactionTemplate;

/**
 * Base class for JPA based unit tests that want TX and entity manager support.
 * Also deletes all hibernate managed data from the database after each test execution 
 * 
 * @author Eric Dalquist
 * @version $Revision$
 */
public abstract class BaseJpaDaoTest {
    protected Logger logger = LoggerFactory.getLogger(getClass());
    
    protected JpaInterceptor jpaInterceptor;
    protected TransactionOperations transactionOperations;
    
    
    @Autowired
    public final void setJpaInterceptor(JpaInterceptor jpaInterceptor) {
        this.jpaInterceptor = jpaInterceptor;
    }

    @Autowired
    public final void setPlatformTransactionManager(PlatformTransactionManager platformTransactionManager) {
        final TransactionTemplate transactionTemplate = new TransactionTemplate(platformTransactionManager);
        transactionTemplate.afterPropertiesSet();
        this.transactionOperations = transactionTemplate;
    }
    
    protected abstract EntityManager getEntityManager();

    /**
     * Deletes ALL entities from the database
     */
    @After
    public final void deleteAllEntities() {
        final EntityManager entityManager = getEntityManager();
        final EntityManagerFactory entityManagerFactory = entityManager.getEntityManagerFactory();
        final Metamodel metamodel = entityManagerFactory.getMetamodel();
        Set<EntityType<?>> entityTypes = new LinkedHashSet<EntityType<?>>(metamodel.getEntities());

        do {
            final Set<EntityType<?>> failedEntitieTypes = new HashSet<EntityType<?>>();
            
            for (final EntityType<?> entityType : entityTypes) {
                final String entityClassName = entityType.getBindableJavaType().getName();
                
                try {
                    this.executeInTransaction(new CallableWithoutResult() {
                        @Override
                        protected void callWithoutResult() {
                            logger.info("Purging all: " + entityClassName);
                            
                            final Query query = entityManager.createQuery("SELECT e FROM " + entityClassName + " AS e");
                            final List<?> entities = query.getResultList();
                            logger.info("Found " + entities.size() + " " + entityClassName + " to delete");
                            for (final Object entity : entities) {
                                entityManager.remove(entity);
                            }              
                        }
                    });
                }
                catch (DataIntegrityViolationException e) {
                    logger.info("Failed to delete " + entityClassName + ". Must be a dependency of another entity");
                    failedEntitieTypes.add(entityType);
                }
            }
            
            entityTypes = failedEntitieTypes;
        } while (!entityTypes.isEmpty());
        
        
        //Reset all spring managed mocks after every test
        MockitoFactoryBean.resetAllMocks();
    }

    /**
     * Executes the callback inside of a {@link JpaInterceptor}.
     */
    @SuppressWarnings("unchecked")
    public final <T> T execute(final Callable<T> callable) {
        try {
            return (T)this.jpaInterceptor.invoke(new MethodInvocationCallable<T>(callable));
        }
        catch (Throwable e) {
            if (e instanceof RuntimeException) {
                throw (RuntimeException)e;
            }
            if (e instanceof Error) {
                throw (Error)e;
            }
            throw new RuntimeException(e);
        }
    }
    
    /**
     * Executes the callback inside of a {@link JpaInterceptor} inside of a {@link TransactionCallback}
     */
    public final <T> T executeInTransaction(final Callable<T> callable) {
        return execute(new Callable<T>() {
            @Override
            public T call() throws Exception {
                return transactionOperations.execute(new TransactionCallback<T>() {
                    @Override
                    public T doInTransaction(TransactionStatus status) {
                        try {
                            return callable.call();
                        }
                        catch (RuntimeException e) {
                            throw e;
                        }
                        catch (Exception e) {
                            throw new RuntimeException(e);
                        }
                    }
                });
            }
        });
    }

    /**
     * Executes the callback in a new thread inside of a {@link JpaInterceptor}. Waits for the
     * Thread to return.
     */
    public final <T> T executeInThread(String name, final Callable<T> callable) {
        final List<RuntimeException> exception = new LinkedList<RuntimeException>();
        final List<T> retVal = new LinkedList<T>();
        
        final Thread t2 = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    final T val = execute(callable);
                    retVal.add(val);
                }
                catch (Throwable e) {
                    if (e instanceof RuntimeException) {
                        exception.add((RuntimeException)e);                    
                    }
                    else {
                        exception.add(new RuntimeException(e));
                    }
                }
            }
        }, name);
        
        t2.start();
        try {
            t2.join();
        }
        catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        
        if (exception.size() == 1) {
            throw exception.get(0);
        }
        
        return retVal.get(0);
    }

    private static final class MethodInvocationCallable<T> implements MethodInvocation {
        private final Callable<T> callable;

        private MethodInvocationCallable(Callable<T> callable) {
            this.callable = callable;
        }

        @Override
        public Object proceed() throws Throwable {
            return callable.call();
        }

        @Override
        public Object getThis() {
            throw new UnsupportedOperationException();
        }

        @Override
        public AccessibleObject getStaticPart() {
            throw new UnsupportedOperationException();
        }

        @Override
        public Object[] getArguments() {
            throw new UnsupportedOperationException();
        }

        @Override
        public Method getMethod() {
            throw new UnsupportedOperationException();
        }
    }
}