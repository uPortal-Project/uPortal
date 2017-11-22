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

import com.google.common.base.Function;
import com.google.common.collect.ForwardingList;
import java.util.Collection;

/**
 * List that runs each element being added through a fitler before adding it
 *
 * @param <E>
 */
public abstract class FilteringOnAddList<E> extends ForwardingList<E> {
    private final Function<E, E> filterAdd;
    private final boolean ignoreNull;

    public FilteringOnAddList(Function<E, E> filterAdd, boolean ignoreNull) {
        this.filterAdd = filterAdd;
        this.ignoreNull = ignoreNull;
    }

    @Override
    public boolean add(E element) {
        element = this.filterAdd.apply(element);
        if (element == null && ignoreNull) {
            return false;
        }
        return super.add(element);
    }

    @Override
    public boolean addAll(Collection<? extends E> collection) {
        boolean modified = false;
        for (final E element : collection) {
            modified = this.add(element) || modified;
        }
        return modified;
    }

    @Override
    public void add(int index, E element) {
        element = this.filterAdd.apply(element);
        if (element == null && ignoreNull) {
            return;
        }
        super.add(index, element);
    }

    @Override
    public boolean addAll(int index, Collection<? extends E> elements) {
        final int size = this.size();
        for (final E element : elements) {
            this.add(index, element);
            index++;
        }
        return size != this.size();
    }
}
