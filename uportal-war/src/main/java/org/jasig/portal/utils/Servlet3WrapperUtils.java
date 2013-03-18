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

/**
 * This class is only needed for uPortal 4.0 where it must run on Servlet 2.5 and Servlet 3.0
 * containers.
 */
public class Servlet3WrapperUtils {
    private static final boolean IS_SERVLET_30;
    
    static {
        boolean is30;
        try {
            Class.forName("javax.servlet.AsyncContext");
            is30 = true;
        }
        catch (ClassNotFoundException e) {
            is30 = false;
        }
        IS_SERVLET_30 = is30;
    }
    
    /**
     * Adds a {@link CompositeProxyFactory} wrapper around proxy if running
     * in a Servlet 3.0 environment. This wrapper is not needed in a servlet
     * 2.5 environment.
     */
    public static <T, P extends T> P addServlet3Wrapper(P proxy, T target) {
        if (IS_SERVLET_30) {
            return CompositeProxyFactory.createCompositeProxy(proxy, target);
        }
        
        return proxy;
    }
}
