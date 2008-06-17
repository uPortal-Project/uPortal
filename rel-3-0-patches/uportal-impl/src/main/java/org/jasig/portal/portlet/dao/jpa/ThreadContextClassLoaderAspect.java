/**
 * Copyright 2007 The JA-SIG Collaborative.  All rights reserved.
 * See license distributed with this file and
 * available online at http://www.uportal.org/license.html
 */
package org.jasig.portal.portlet.dao.jpa;

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
    
    /* (non-Javadoc)
     * @see org.springframework.core.Ordered#getOrder()
     */
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
        
        try {
            currentThread.setContextClassLoader(this.getClass().getClassLoader());
            return pjp.proceed();
        }
        finally {
            currentThread.setContextClassLoader(previousClassLoader);
        }
    }
}
