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

package org.jasig.portal.utils;

import java.util.Iterator;

/**
 * @author Eric Dalquist
 * @version $Revision$
 */
public class IteratorWrapper<E> implements Iterator<E> {
    private final Iterator<E> iterator;
    
    public IteratorWrapper(Iterator<E> iterator) {
        this.iterator = iterator;
    }

    public boolean hasNext() {
        return this.iterator.hasNext();
    }

    public E next() {
        return this.iterator.next();
    }

    public void remove() {
        this.iterator.remove();
    }

    @Override
    public boolean equals(Object obj) {
        return this.iterator.equals(obj);
    }

    @Override
    public int hashCode() {
        return this.iterator.hashCode();
    }

    @Override
    public String toString() {
        return this.iterator.toString();
    }
}
