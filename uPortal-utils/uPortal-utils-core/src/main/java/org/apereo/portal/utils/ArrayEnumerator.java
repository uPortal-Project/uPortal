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
package org.apereo.portal.utils;

import java.util.Enumeration;
import java.util.NoSuchElementException;

/**
 * Implements the Enumeration interface over an Array
 *
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
    @Override
    public boolean hasMoreElements() {
        return this.index < this.array.length;
    }

    /* (non-Javadoc)
     * @see java.util.Enumeration#nextElement()
     */
    @Override
    public T nextElement() {
        if (!this.hasMoreElements()) {
            throw new NoSuchElementException();
        }

        return this.array[this.index++];
    }
}
