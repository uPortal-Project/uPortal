/**
 * Copyright (c) 2000-2009, Jasig, Inc.
 * See license distributed with this file and available online at
 * https://www.ja-sig.org/svn/jasig-parent/tags/rel-10/license-header.txt
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
