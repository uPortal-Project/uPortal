/**
 * Copyright (c) 2000-2009, Jasig, Inc.
 * See license distributed with this file and available online at
 * https://www.ja-sig.org/svn/jasig-parent/tags/rel-10/license-header.txt
 */

package org.jasig.portal.utils;

import java.util.Comparator;

/**
 * Base comparator that is used for comparing two objects that are not directly comparable.
 * 
 * Implementations have a custom {@link #getComparable(Object)} method that returns an appropriate
 * {@link Comparable} for the object.
 * 
 * @author Eric Dalquist
 * @version $Revision$
 */
public abstract class ComparableExtractingComparator<T, C extends Comparable<C>> implements Comparator<T> {
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
        
        if (c1 == null && c1== c2) {
            return 0;
        }
        
        return c1.compareTo(c2);
    }
    
    protected abstract C getComparable(T o);
}
