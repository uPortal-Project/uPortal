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
