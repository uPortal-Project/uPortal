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