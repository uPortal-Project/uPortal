/**
 * Copyright 2007 The JA-SIG Collaborative.  All rights reserved.
 * See license distributed with this file and
 * available online at http://www.uportal.org/license.html
 */
package org.jasig.portal.utils.compare;

import java.util.Comparator;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

abstract class BooleanComparator implements Comparator<String> {
    public static enum ComparatorType {
        OR,
        AND;
    }
    
    private final Comparator<String>[] comparators;
    
    public BooleanComparator(Comparator<String>[] comparators) {
        this.comparators = comparators;
    }
    
    /* (non-Javadoc)
     * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
     */
    public final int compare(String o1, String o2) {
        final BooleanComparator.ComparatorType comparatorType = this.getComparatorType();
        
        for (final Comparator<String> comparator : this.comparators) {
            final int result = comparator.compare(o1, o2);
            
            if (result == 0 && comparatorType == ComparatorType.OR) {
                return 0;
            }
            else if (result != 0 && comparatorType == ComparatorType.AND) {
                return result;
            }
        }
        
        return Integer.MIN_VALUE;
    }
    
    protected abstract BooleanComparator.ComparatorType getComparatorType();

    /**
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE)
            .append(this.comparators)
            .toString();
    }
}