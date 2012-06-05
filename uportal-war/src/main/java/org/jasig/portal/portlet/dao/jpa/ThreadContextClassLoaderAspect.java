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

import java.util.Deque;
import java.util.LinkedList;

import org.aspectj.lang.ProceedingJoinPoint;
import org.springframework.core.Ordered;

/**
 * Switches context class loader for {@link Thread#currentThread()} to the class loader of
 * this class and switches it back after the method completes
 * 
 * @author Eric Dalquist
 * @version $Revision$
 */
public class ThreadContextClassLoaderAspect implements Ordered {
    private static final ClassLoader PORTAL_CLASS_LOADER = ThreadContextClassLoaderAspect.class.getClassLoader();
    private static final ThreadLocal<Deque<ClassLoader>> PREVIOUS_CLASS_LOADER = new ThreadLocal<Deque<ClassLoader>>();
    
    public static ClassLoader getPreviousClassLoader() {
        final Deque<ClassLoader> deque = PREVIOUS_CLASS_LOADER.get();
        if (deque == null) {
            return null;
        }

        return deque.peekFirst();
    }
    
    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE;
    }
    
    /**
     * Wraps the targeted execution, switching the current thread's context class loader
     * to this classes class loader.
     */
    public Object doThreadContextClassLoaderUpdate(ProceedingJoinPoint pjp) throws Throwable {
        final Thread currentThread = Thread.currentThread();
        final ClassLoader previousClassLoader = currentThread.getContextClassLoader();
        Deque<ClassLoader> deque = PREVIOUS_CLASS_LOADER.get();
        if (deque == null) {
            deque = new LinkedList<ClassLoader>();
            PREVIOUS_CLASS_LOADER.set(deque);
        }
        
        deque.push(previousClassLoader);

        try {
            currentThread.setContextClassLoader(PORTAL_CLASS_LOADER);
            return pjp.proceed();
        }
        finally {
            currentThread.setContextClassLoader(previousClassLoader);
            deque.removeFirst();
            if (deque.isEmpty()) {
                //clean up the threadlocal when the deque is empty
                PREVIOUS_CLASS_LOADER.remove();
            }
        }
    }
}
