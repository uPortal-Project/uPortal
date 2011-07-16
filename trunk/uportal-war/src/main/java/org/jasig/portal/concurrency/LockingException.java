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
 * A <code>LockingException</code> describes a problem that has arisen during
 * an attempt to create or alter an <code>IEntityLock</code>.  If the problem
 * occurs in the lock store, the <code>LockingException</code> should wrap an
 * <code>Exception</code> specific to the store, like a <code>java.sql.SQLException</code>.
 *
 * @author Dan Ellentuck
 * @version $Revision$
 */
public class LockingException extends org.jasig.portal.PortalException {
    
    /**
     * Instantiate a LockingException with the given cause.
     * @param cause Throwable that caused the locking problem
     */
    public LockingException(Throwable cause) {
        super(cause);
    }

    /**
     * Instantiate a LockingException with the given message.
     * @param msg message describing nature of locking problem
     */
    public LockingException(String msg) {
        super(msg);
    }

    /**
     * Instantiate a LockingException with the given message
     * and underlying cause.
     * @param msg message describing nature of locking problem
     * @param cause underlying cause
     */
    public LockingException(String msg, Throwable cause) {
        super(msg, cause);
    }
}
