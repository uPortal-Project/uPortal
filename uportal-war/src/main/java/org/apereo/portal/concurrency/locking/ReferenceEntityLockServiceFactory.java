/**
 * Licensed to Apereo under one or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information regarding copyright ownership. Apereo
 * licenses this file to you under the Apache License, Version 2.0 (the "License"); you may not use
 * this file except in compliance with the License. You may obtain a copy of the License at the
 * following location:
 *
 * <p>http://www.apache.org/licenses/LICENSE-2.0
 *
 * <p>Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apereo.portal.concurrency.locking;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apereo.portal.concurrency.IEntityLockService;
import org.apereo.portal.concurrency.IEntityLockServiceFactory;
import org.apereo.portal.concurrency.LockingException;

/**
 * Creates the reference implemetation of <code>IEntityLockService</code>.
 *
 */
public class ReferenceEntityLockServiceFactory implements IEntityLockServiceFactory {
    private static final Log log = LogFactory.getLog(ReferenceEntityLockServiceFactory.class);
    /** ReferenceEntityLockServiceFactory constructor. */
    public ReferenceEntityLockServiceFactory() {
        super();
    }
    /**
     * Return an instance of the service implementation.
     *
     * @return org.apereo.portal.concurrency.locking.IEntityLockService
     * @exception LockingException
     */
    public IEntityLockService newLockService() throws LockingException {
        try {
            return ReferenceEntityLockService.singleton();
        } catch (LockingException le) {
            log.error("ReferenceEntityLockServiceFactory.newLockService(): " + le);
            throw new LockingException(le);
        }
    }
}
