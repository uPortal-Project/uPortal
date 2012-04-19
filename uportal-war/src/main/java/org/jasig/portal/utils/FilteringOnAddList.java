package org.jasig.portal.utils;

import java.util.Collection;

import com.google.common.base.Function;
import com.google.common.collect.ForwardingList;

/**
 * List that runs each element being added through a fitler before adding it
 * 
 * @author Eric Dalquist
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