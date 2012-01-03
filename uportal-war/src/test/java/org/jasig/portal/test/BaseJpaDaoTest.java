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
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Callable;

import org.aopalliance.intercept.MethodInvocation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.orm.jpa.JpaInterceptor;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionOperations;
import org.springframework.transaction.support.TransactionTemplate;

/**
 * Base class for JPA based unit tests that want TX and entity manager support
 * 
 * @author Eric Dalquist
 * @version $Revision$
 */
public abstract class BaseJpaDaoTest {
    protected JpaInterceptor jpaInterceptor;
    protected TransactionOperations transactionOperations;
    
    @Autowired
    public final void setJpaInterceptor(JpaInterceptor jpaInterceptor) {
        this.jpaInterceptor = jpaInterceptor;
    }

    @Autowired
    public void setPlatformTransactionManager(PlatformTransactionManager platformTransactionManager) {
        final TransactionTemplate transactionTemplate = new TransactionTemplate(platformTransactionManager);
        transactionTemplate.afterPropertiesSet();
        this.transactionOperations = transactionTemplate;
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
            throw new RuntimeException(e);
        }
    }
    
    /**
     * Executes the callback inside of a {@link JpaInterceptor} inside of a {@link TransactionCallback}
     */
    public final <T> T executeInTransaction(final Callable<T> callable) {
        return this.transactionOperations.execute(new TransactionCallback<T>() {

            @Override
            public T doInTransaction(TransactionStatus status) {
                return execute(callable);
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