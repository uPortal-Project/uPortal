/**
 * Copyright (c) 2000-2009, Jasig, Inc.
 * See license distributed with this file and available online at
 * https://www.ja-sig.org/svn/jasig-parent/tags/rel-10/license-header.txt
 */
package org.jasig.portal.layout.dlm;

import org.danann.cernunnos.Task;

public final class LayoutStoreProvider {

    private Task lookupNoderefTask;
    
    private RDBMDistributedLayoutStore layoutStore = null;
    
    public LayoutStoreProvider() {}
    
    public LayoutStoreProvider(RDBMDistributedLayoutStore layoutStore) {
        this.layoutStore = layoutStore;
    }

    public void setLookupNoderefTask(Task k) {
        this.lookupNoderefTask = k;
    }

    public synchronized RDBMDistributedLayoutStore getLayoutStore() {
        if (layoutStore != null) {
            return layoutStore;
        }
        
        try {
            final RDBMDistributedLayoutStore rslt = new RDBMDistributedLayoutStore();
            rslt.setLookupNoderefTask(lookupNoderefTask);
            
            layoutStore = rslt;
        } catch (Throwable t) {
            String msg = "Failed to instantiate RDBMDistributedLayoutStore.";
            throw new RuntimeException(msg, t);
        }
        
        return layoutStore;
    }

}
