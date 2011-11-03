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
package org.jasig.portal.concurrency;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.concurrent.TimeUnit;

import org.junit.Test;

/**
 * @author Eric Dalquist
 * @version $Revision$
 */
public class TimeTest {
    
    @Test(expected=IllegalArgumentException.class)
    public void testNullTimeParse() {
        Time.valueOf(null);
    }
    
    @Test(expected=IllegalArgumentException.class)
    public void testEmptyTimeParse() {
        Time.valueOf("");
    }
    
    @Test(expected=IllegalArgumentException.class)
    public void testSepStartTimeParse() {
        Time.valueOf("_1");
    }
    
    @Test(expected=IllegalArgumentException.class)
    public void testSepEndTimeParse() {
        Time.valueOf("1_");
    }
    
    @Test
    public void testNumberOnlyTimeParse() {
        final Time t = Time.valueOf("1234567890123456789");
        
        assertNotNull(t);
        assertEquals(1234567890123456789l, t.asMillis());
        assertEquals(Time.getTime(1234567890123456789l), t);
        assertEquals(Time.getTime(1234567890123456789l, TimeUnit.MILLISECONDS), t);
        assertEquals("1234567890123456789_MILLISECONDS", t.toString());
    }
    
    @Test
    public void testNumberAndUnitTimeParse() {
        final Time t = Time.valueOf("1234567890123456789_MICROSECONDS");
        
        assertNotNull(t);
        assertEquals(1234567890123456l, t.asMillis());
        assertEquals(Time.getTime(1234567890123456789l, TimeUnit.MICROSECONDS), t);
        assertEquals("1234567890123456789_MICROSECONDS", t.toString());
    }
    
    @Test
    public void testZeroNumberOnlyTimeParse() {
        final Time t = Time.valueOf("0");
        
        assertNotNull(t);
        assertEquals(0, t.asMillis());
        assertEquals(Time.getTime(0), t);
        assertEquals(Time.getTime(0, TimeUnit.MILLISECONDS), t);
        assertEquals("0_MILLISECONDS", t.toString());
    }
    
    @Test
    public void testZeroNumberAndUnitTimeParse() {
        final Time t = Time.valueOf("0_MICROSECONDS");
        
        assertNotNull(t);
        assertEquals(0, t.asMillis());
        assertEquals(Time.getTime(0, TimeUnit.MICROSECONDS), t);
        assertEquals("0_MICROSECONDS", t.toString());
    }
    
    @Test(expected=IllegalArgumentException.class)
    public void testNumberAndInvalidUnitTimeParse() {
        Time.valueOf("1_PARSEC");
    }
}
