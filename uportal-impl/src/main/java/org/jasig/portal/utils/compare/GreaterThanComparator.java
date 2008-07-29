/**
 * Copyright 2007 The JA-SIG Collaborative.  All rights reserved.
 * See license distributed with this file and
 * available online at http://www.uportal.org/license.html
 */
package org.jasig.portal.utils.compare;

import java.math.BigDecimal;
import java.util.Comparator;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

public class GreaterThanComparator implements Comparator<String> {
    /* (non-Javadoc)
     * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
     */
    public int compare(String o1, String o2) {
        final BigDecimal v1 = new BigDecimal(o1);
        final BigDecimal v2 = new BigDecimal(o2);
        
        if (v1.compareTo(v2) > 0) {
            return 0;
        }

        return -1;
    }

    /**
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE)
            .toString();
    }
}