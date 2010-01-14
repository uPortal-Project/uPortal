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
