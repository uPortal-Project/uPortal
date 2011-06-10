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

package org.jasig.portal.utils;

import java.util.concurrent.Callable;

/**
 * @author Eric Dalquist
 * @version $Revision$
 */
public final class ContextClassloaderTemplate {
    private ContextClassloaderTemplate() {
    }
    
    public static <V> V doWithContextClassloader(ClassLoader classLoader, Callable<V> callable) throws Exception {
        //Get the current thread and current class loader
        final Thread currentThread = Thread.currentThread();
        final ClassLoader contextClassLoader = currentThread.getContextClassLoader();
        
        //Switch the thread to use the new class loader
        currentThread.setContextClassLoader(classLoader);
        
        //execute
        try {
            return callable.call();
        }
        finally {
            //Switch back to the original classloader
            currentThread.setContextClassLoader(contextClassLoader);
        }
    }
}
