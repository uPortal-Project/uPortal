/**
 * Copyright 2007 The JA-SIG Collaborative.  All rights reserved.
 * See license distributed with this file and
 * available online at http://www.uportal.org/license.html
 */
package org.jasig.portal.jndi;

import java.util.Hashtable;

import javax.naming.Context;
import javax.naming.NamingException;
import javax.naming.spi.InitialContextFactory;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.DisposableBean;

import tyrex.naming.MemoryContext;

/**
 * Initial context factory that provides access to close and de-reference the MemoryContext.
 * 
 * @author Eric Dalquist
 * @version $Revision$
 */
public class DisposableMemoryContextFactory implements InitialContextFactory, DisposableBean {
    private static final Log LOGGER = LogFactory.getLog(DisposableMemoryContextFactory.class);
    
    private static MemoryContext context;

    /* (non-Javadoc)
     * @see javax.naming.spi.InitialContextFactory#getInitialContext(java.util.Hashtable)
     */
    public Context getInitialContext(Hashtable<?, ?> environment) throws NamingException {
        synchronized (DisposableMemoryContextFactory.class) {
            if (context == null) {
                context = new MemoryContext(environment);
                LOGGER.info("Created new MemoryContext with environment '" + environment + "'");
            }
        }
        
        return context;
    }

    /* (non-Javadoc)
     * @see org.springframework.beans.factory.DisposableBean#destroy()
     */
    public void destroy() throws Exception {
        closeContext();
    }

    /**
     * Closes and de-references the {@link MemoryContext}, if already closed this is a noop.
     */
    public synchronized static void closeContext() {
        if (context != null) {
            LOGGER.info("Closing MemoryContext with environment '" + context.getEnvironment() + "'");
            context.close();
            context = null;
        }
    }
}
