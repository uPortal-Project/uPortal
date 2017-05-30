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

import java.util.Comparator;

/**
 * Base comparator that is used for comparing two objects that are not directly comparable.
 *
 * <p>Implementations have a custom {@link #getComparable(Object)} method that returns an
 * appropriate {@link Comparable} for the object.
 *
 * @param <T> The root type being compared
 * @param <C> The type that extracted for comparison
 */
public abstract class ComparableExtractingComparator<T, C extends Comparable<? super C>>
        implements Comparator<T> {
    private final Comparator<C> comparator;

    public ComparableExtractingComparator() {
        this(null);
    }

    public ComparableExtractingComparator(Comparator<C> comparator) {
        this.comparator = comparator;
    }

    @Override
    public final int compare(T o1, T o2) {
        final C c1 = this.getComparable(o1);
        final C c2 = this.getComparable(o2);

        if (this.comparator != null) {
            return this.comparator.compare(c1, c2);
        }

        if (c1 == null && c1 == c2) {
            return 0;
        }

        return c1.compareTo(c2);
    }

    protected abstract C getComparable(T o);
}
