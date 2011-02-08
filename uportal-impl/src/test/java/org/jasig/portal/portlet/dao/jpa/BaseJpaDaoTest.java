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

package org.jasig.portal.portlet.dao.jpa;

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Method;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Callable;

import org.aopalliance.intercept.MethodInvocation;
import org.springframework.orm.jpa.JpaInterceptor;
import org.springframework.test.annotation.AbstractAnnotationAwareTransactionalTests;

/**
 * Base class for JPA based unit tests that want TX and entity manager support
 * 
 * @author Eric Dalquist
 * @version $Revision$
 */
public abstract class BaseJpaDaoTest extends AbstractAnnotationAwareTransactionalTests {
    protected JpaInterceptor jpaInterceptor;

    public BaseJpaDaoTest() {
        super();
    }

    public BaseJpaDaoTest(String name) {
        super(name);
    }
    
    public final void setJpaInterceptor(JpaInterceptor jpaInterceptor) {
        this.jpaInterceptor = jpaInterceptor;
    }

    @SuppressWarnings("unchecked")
    public <T> T execute(final Callable<T> callable) {
        try {
            return (T)this.jpaInterceptor.invoke(new MethodInvocation() {
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
            });
        }
        catch (Throwable e) {
            if (e instanceof RuntimeException) {
                throw (RuntimeException)e;
            }
            throw new RuntimeException(e);
        }
    }

    public <T> T executeInThread(String name, final Callable<T> callable) {
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

}