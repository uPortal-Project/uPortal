/**
 * Copyright (c) 2000-2009, Jasig, Inc.
 * See license distributed with this file and available online at
 * https://www.ja-sig.org/svn/jasig-parent/tags/rel-10/license-header.txt
 */
package org.jasig.portal.layout.dlm;

import org.danann.cernunnos.Task;
import org.jasig.portal.utils.threading.SingletonDoubleCheckedCreator;
import org.springframework.beans.factory.annotation.Required;

public final class LayoutStoreProvider {

    private Task lookupNoderefTask;
    private Task lookupPathrefTask;

    private final SingletonDoubleCheckedCreator<RDBMDistributedLayoutStore> creator = new SingletonDoubleCheckedCreator<RDBMDistributedLayoutStore>() {
        protected RDBMDistributedLayoutStore createSingleton(Object... args) {
            RDBMDistributedLayoutStore rslt = null;
            try {
                rslt = new RDBMDistributedLayoutStore();
                rslt.setLookupNoderefTask(lookupNoderefTask);
                rslt.setLookupPathrefTask(lookupPathrefTask);
            } catch (Throwable t) {
                String msg = "Failed to instantiate RDBMDistributedLayoutStore.";
                throw new RuntimeException(msg, t);
            }
            return rslt;
        }
    };

    @Required
    public void setLookupNoderefTask(Task k) {
        this.lookupNoderefTask = k;
    }

    @Required
    public void setLookupPathrefTask(Task k) {
        this.lookupPathrefTask = k;
    }
    
    public RDBMDistributedLayoutStore getLayoutStore() {
        return creator.get();
    }

}
