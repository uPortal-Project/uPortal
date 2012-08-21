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

import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;

import java.util.Collections;
import java.util.Set;

import org.jasig.portal.events.aggr.AggregationInterval;
import org.junit.Test;

import com.google.common.collect.ImmutableSet;

public class IncludeExcludeUtilsTest {
    @Test
    public void testNoIncExc() {
        final Set<AggregationInterval> includes = Collections.emptySet();
        final Set<AggregationInterval> excludes = Collections.emptySet();
        
        assertTrue(IncludeExcludeUtils.included(AggregationInterval.MINUTE, includes, excludes));
        assertTrue(IncludeExcludeUtils.included(AggregationInterval.FIVE_MINUTE, includes, excludes));
        assertTrue(IncludeExcludeUtils.included(AggregationInterval.HOUR, includes, excludes));
        assertTrue(IncludeExcludeUtils.included(AggregationInterval.DAY, includes, excludes));
        assertTrue(IncludeExcludeUtils.included(AggregationInterval.WEEK, includes, excludes));
        assertTrue(IncludeExcludeUtils.included(AggregationInterval.MONTH, includes, excludes));
        assertTrue(IncludeExcludeUtils.included(AggregationInterval.ACADEMIC_TERM, includes, excludes));
        assertTrue(IncludeExcludeUtils.included(AggregationInterval.CALENDAR_QUARTER, includes, excludes));
        assertTrue(IncludeExcludeUtils.included(AggregationInterval.YEAR, includes, excludes));
    }
    
    @Test
    public void testSingleInclude() {
        final Set<AggregationInterval> includes = ImmutableSet.of(AggregationInterval.FIVE_MINUTE);
        final Set<AggregationInterval> excludes = Collections.emptySet();
        
        assertFalse(IncludeExcludeUtils.included(AggregationInterval.MINUTE, includes, excludes));
        assertTrue(IncludeExcludeUtils.included(AggregationInterval.FIVE_MINUTE, includes, excludes));
        assertFalse(IncludeExcludeUtils.included(AggregationInterval.HOUR, includes, excludes));
        assertFalse(IncludeExcludeUtils.included(AggregationInterval.DAY, includes, excludes));
        assertFalse(IncludeExcludeUtils.included(AggregationInterval.WEEK, includes, excludes));
        assertFalse(IncludeExcludeUtils.included(AggregationInterval.MONTH, includes, excludes));
        assertFalse(IncludeExcludeUtils.included(AggregationInterval.ACADEMIC_TERM, includes, excludes));
        assertFalse(IncludeExcludeUtils.included(AggregationInterval.CALENDAR_QUARTER, includes, excludes));
        assertFalse(IncludeExcludeUtils.included(AggregationInterval.YEAR, includes, excludes));
    }
    
    @Test
    public void testSingleExclude() {
        final Set<AggregationInterval> includes = Collections.emptySet();
        final Set<AggregationInterval> excludes = ImmutableSet.of(AggregationInterval.FIVE_MINUTE);
        
        assertTrue(IncludeExcludeUtils.included(AggregationInterval.MINUTE, includes, excludes));
        assertFalse(IncludeExcludeUtils.included(AggregationInterval.FIVE_MINUTE, includes, excludes));
        assertTrue(IncludeExcludeUtils.included(AggregationInterval.HOUR, includes, excludes));
        assertTrue(IncludeExcludeUtils.included(AggregationInterval.DAY, includes, excludes));
        assertTrue(IncludeExcludeUtils.included(AggregationInterval.WEEK, includes, excludes));
        assertTrue(IncludeExcludeUtils.included(AggregationInterval.MONTH, includes, excludes));
        assertTrue(IncludeExcludeUtils.included(AggregationInterval.ACADEMIC_TERM, includes, excludes));
        assertTrue(IncludeExcludeUtils.included(AggregationInterval.CALENDAR_QUARTER, includes, excludes));
        assertTrue(IncludeExcludeUtils.included(AggregationInterval.YEAR, includes, excludes));
    }
}
