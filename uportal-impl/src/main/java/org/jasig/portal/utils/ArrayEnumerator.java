/**
 * Copyright 2007 The JA-SIG Collaborative.  All rights reserved.
 * See license distributed with this file and
 * available online at http://www.uportal.org/license.html
 */
package org.jasig.portal.utils;

import java.util.Enumeration;
import java.util.NoSuchElementException;

/**
 * Implements the Enumeration interface over an Array
 * 
 * @author Eric Dalquist
 * @version $Revision$
 */
public class ArrayEnumerator<T> implements Enumeration<T> {
    private final T[] array;
    private int index;

    public ArrayEnumerator(T[] array) {
        if (array == null) {
            throw new IllegalArgumentException("array can not be null");
        }

        this.array = array;
        this.index = 0;
    }

    /* (non-Javadoc)
     * @see java.util.Enumeration#hasMoreElements()
     */
    public boolean hasMoreElements() {
        return this.index < this.array.length;
    }

    /* (non-Javadoc)
     * @see java.util.Enumeration#nextElement()
     */
    public T nextElement() {
        if (!this.hasMoreElements()) {
            throw new NoSuchElementException();
        }

        return this.array[this.index++];
    }
}
