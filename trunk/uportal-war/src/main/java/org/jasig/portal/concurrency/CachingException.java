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

package org.jasig.portal.concurrency;

/**
 * A <code>CachingException</code> describes a problem that has arisen during
 * an attempt to add, update, remove or reference a cache entry.  If the problem
 * arises in the store, the <code>CachingException</code> should wrap an
 * <code>Exception</code> specific to the store, like a <code>java.sql.SQLException</code>.
 *
 * @author Dan Ellentuck
 * @version $Revision$ $Date$
 */
public class CachingException extends org.jasig.portal.PortalException {
    
    
    /**
     * Instantiate a CachingException with the given cause.
     * @param cause A throwable that caused the caching problem.
     */
    public CachingException(Throwable cause) {
        super(cause);
    }

    /**
     * Instantiate a CachingException with the given message.
     * @param msg message describing nature of caching problem.
     */
    public CachingException(String msg) {
        super(msg);
    }
    
    /**
     * Instantiate a CachingException with the given message and underlying cause.
     * @param msg message describing nature of caching problem.
     * @param cause underlying cause.
     */
    public CachingException(String msg, Throwable cause) {
        super(msg, cause);
    }
}
