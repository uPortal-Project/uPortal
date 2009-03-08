/**
 * Copyright (c) 2000-2009, Jasig, Inc.
 * See license distributed with this file and available online at
 * https://www.ja-sig.org/svn/jasig-parent/tags/rel-10/license-header.txt
 */
package org.jasig.portal.utils.threading;

import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

/**
 * Provides a DoubleCheckedCreator impl that tracks the singleton instance internally
 * 
 * @author Eric Dalquist
 * @version $Revision$
 */
public abstract class SingletonDoubleCheckedCreator<T> extends DoubleCheckedCreator<T> {
    private final AtomicBoolean creating = new AtomicBoolean(false);
    private final AtomicBoolean created = new AtomicBoolean(false);
    private T instance;
    
    /**
     * Called only once as long as it returns successfully
     * 
     * @see DoubleCheckedCreator#create(Object...)
     */
    protected abstract T createSingleton(Object... args);

    /* (non-Javadoc)
     * @see org.jasig.portal.utils.threading.DoubleCheckedCreator#create(java.lang.Object[])
     */
    @Override
    protected final T create(Object... args) {
        if (this.creating.get()) {
            throw new IllegalStateException("Singleton creator has been called again while creation is in progress, this is indicative of a creation loop in a single thread");
        }
        
        this.creating.set(true);
        try {
            final T instance = this.createSingleton(args);
            this.instance = instance;
            this.created.set(true);
            return instance;
        }
        finally {
            this.creating.set(false);
        }
    }

    /* (non-Javadoc)
     * @see org.jasig.portal.utils.threading.DoubleCheckedCreator#retrieve(java.lang.Object[])
     */
    @Override
    protected final T retrieve(Object... args) {
        return this.instance;
    }
    
    /**
     * @return true if the singleton has been created as of this call
     */
    public final boolean isCreated() {
        return this.created.get();
    }

    /**
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE)
                .append("instance", this.instance)
                .toString();
    }
}
