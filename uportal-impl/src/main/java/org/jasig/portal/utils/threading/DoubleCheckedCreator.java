/**
 * Copyright 2007 The JA-SIG Collaborative.  All rights reserved.
 * See license distributed with this file and
 * available online at http://www.uportal.org/license.html
 */
package org.jasig.portal.utils.threading;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.apache.commons.lang.Validate;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Implementation of double-checked locking for object creation using a {@link ReadWriteLock}
 * 
 * @author Eric Dalquist
 * @version $Revision$
 */
public abstract class DoubleCheckedCreator<T> {
    protected final Log logger = LogFactory.getLog(this.getClass());
    
    private final ReadWriteLock readWriteLock;
    private final Lock readLock;
    private final Lock writeLock;

    public DoubleCheckedCreator() {
        this(new ReentrantReadWriteLock());
    }

    public DoubleCheckedCreator(ReadWriteLock readWriteLock) {
        Validate.notNull(readWriteLock, "readWriteLock can not be null");
        this.readWriteLock = readWriteLock;
        this.readLock = this.readWriteLock.readLock();
        this.writeLock = this.readWriteLock.writeLock();
    }

    /**
     * @param args Arguments to use when creating the object
     * @return A newly created object
     */
    protected abstract T create(Object... args);

    /**
     * @param args Arguments to use when retrieving the object
     * @return An existing object if available
     */
    protected abstract T retrieve(Object... args);

    /**
     * The default impl returns true if value is null.
     * 
     * @param value The object to validate
     * @param args Arguments to use when validating the object
     * @return true if the object is invalid and should be created, false if not.
     */
    protected boolean invalid(T value, Object... args) {
        return value == null;
    }

    /**
     * Double checking retrieval/creation of an object
     * 
     * @param args Optional arguments to pass to {@link #retrieve(Object...)}, {@link #create(Object...)}, and {@link #invalid(Object, Object...)}.
     * @return A retrieved or created object.
     */
    public final T get(Object... args) {
        //Grab a read lock to try retrieving the object
        this.readLock.lock();
        try {

            //See if the object already exists
            T value = this.retrieve(args);
            if (this.invalid(value, args)) {
                //Switch to a write lock
                this.readLock.unlock();
                this.writeLock.lock();
                
                //Check if it exists now, create it if it doesn't
                try {
                    value = this.retrieve(args);
                
                    if (this.invalid(value, args)) {
                        value = this.create(args);
                        
                        if (this.logger.isDebugEnabled()) {
                            this.logger.debug("Created new Object='" + value + "'");
                        }
                    }
                    else if (this.logger.isDebugEnabled()) {
                        this.logger.debug("Using retrieved Object='" + value + "'");
                    }
                }
                finally {
                    //switch back to the read lock
                    this.readLock.lock();
                    this.writeLock.unlock();
                }
            }
            else if (this.logger.isDebugEnabled()) {
                this.logger.debug("Using retrieved Object='" + value + "'");
            }

            return value;
        }
        finally {
            this.readLock.unlock();
        }
    }
}
