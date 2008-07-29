/**
 * Copyright 2007 The JA-SIG Collaborative.  All rights reserved.
 * See license distributed with this file and
 * available online at http://www.uportal.org/license.html
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