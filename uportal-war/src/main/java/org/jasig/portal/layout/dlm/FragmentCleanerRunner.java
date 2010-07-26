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

package org.jasig.portal.layout.dlm;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jasig.portal.layout.IUserLayoutStore;
import org.jasig.portal.layout.UserLayoutStoreFactory;

/**
 * Simple runnable that calls the fragment cleaning method on {@link RDBMDistributedLayoutStore}
 * 
 * @author Eric Dalquist
 * @version $Revision$
 */
public class FragmentCleanerRunner implements Runnable {
    protected final Log logger = LogFactory.getLog(this.getClass());
    
    /* (non-Javadoc)
     * @see java.lang.Runnable#run()
     */
    public void run() {
        final IUserLayoutStore userLayoutStore = UserLayoutStoreFactory.getUserLayoutStoreImpl();
        if (userLayoutStore instanceof RDBMDistributedLayoutStore) {
            if (this.logger.isDebugEnabled()) {
                this.logger.debug("Running cleanFragments on " + userLayoutStore.getClass());
            }
            
            final RDBMDistributedLayoutStore distributedLayoutStore = (RDBMDistributedLayoutStore)userLayoutStore;
            distributedLayoutStore.cleanFragments();
        }
        else {
            this.logger.warn("Returned user layout store " + userLayoutStore.getClass() + " is not castable to " + RDBMDistributedLayoutStore.class);
        }
    }

}
