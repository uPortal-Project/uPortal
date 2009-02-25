/**
 * Copyright (c) 2000-2009, Jasig, Inc.
 * See license distributed with this file and available online at
 * https://www.ja-sig.org/svn/jasig-parent/tags/rel-10/license-header.txt
 */
package org.jasig.portal.utils.compare;

import java.util.Comparator;

public class OrComparator extends BooleanComparator {
    public OrComparator(Comparator<String>[] comparators) {
        super(comparators);
    }

    /* (non-Javadoc)
     * @see org.jasig.portal.tools.checks.SystemPropertyCheck.BooleanComparator#getComparatorType()
     */
    @Override
    protected ComparatorType getComparatorType() {
        return ComparatorType.OR;
    }
}