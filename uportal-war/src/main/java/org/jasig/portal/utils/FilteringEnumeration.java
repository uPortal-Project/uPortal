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

import java.util.Enumeration;

/**
 * {@link Enumeration} wrapper that can be used to selectively exclude elements from another
 * {@link Enumeration} without having to load the entire enum into a temporary collection
 * 
 * @author Eric Dalquist
 * @version $Revision$
 */
public abstract class FilteringEnumeration<E> implements Enumeration<E> {
    private final Enumeration<E> delegate;
    private E next;
    
    public FilteringEnumeration(Enumeration<E> delegate) {
        this.delegate = delegate;
        findNextElement();
    }

    /* (non-Javadoc)
     * @see java.util.Enumeration#hasMoreElements()
     */
    @Override
    public final boolean hasMoreElements() {
        return next != null;
    }

    /* (non-Javadoc)
     * @see java.util.Enumeration#nextElement()
     */
    @Override
    public final E nextElement() {
        final E nextElement = next;
        findNextElement();
        return nextElement;
    }

    /**
     * @return true if the element should be included, false if it should be skipped
     */
    protected abstract boolean includeElement(E element);

    private void findNextElement() {
        while (this.delegate.hasMoreElements() && next == null) {
            final E nextElement = this.delegate.nextElement();
            if (this.includeElement(nextElement)) {
                this.next = nextElement;
            }
        }
    }
}
