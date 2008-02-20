/**
 * Copyright 2007 The JA-SIG Collaborative.  All rights reserved.
 * See license distributed with this file and
 * available online at http://www.uportal.org/license.html
 */
package org.jasig.portal.utils.threading;

/**
 * Provides a DoubleCheckedCreator impl that tracks the singleton instance internally
 * 
 * @author Eric Dalquist
 * @version $Revision$
 */
public abstract class SingletonDoubleCheckedCreator<T> extends DoubleCheckedCreator<T> {
    private T instance;
    
    protected abstract T createSingleton(Object... args);

    /* (non-Javadoc)
     * @see org.jasig.portal.utils.threading.DoubleCheckedCreator#create(java.lang.Object[])
     */
    @Override
    protected final T create(Object... args) {
        final T instance = this.createSingleton(args);
        this.instance = instance;
        return instance;
    }

    /* (non-Javadoc)
     * @see org.jasig.portal.utils.threading.DoubleCheckedCreator#retrieve(java.lang.Object[])
     */
    @Override
    protected final T retrieve(Object... args) {
        return this.instance;
    }

}
