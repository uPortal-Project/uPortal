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
package org.jasig.portal.test;

import java.util.TimeZone;

import org.joda.time.DateTimeZone;
import org.junit.AfterClass;
import org.junit.BeforeClass;

/**
 * Utility for setting the TZ during a unit test. {@link #beforeTest()} should be called in a
 * {@link BeforeClass} method and {@link #afterTest()} should be called in a {@link AfterClass}
 * block
 * 
 * @author Eric Dalquist
 * @version $Revision$
 */
public final class TimeZoneTestUtils {
    private final DateTimeZone testTimeZone;

    private TimeZone defaultTimeZone;
    private DateTimeZone defaultDateTimeZone;
    private String defaultUserTimezone;
    
    
    public TimeZoneTestUtils() {
        this(DateTimeZone.UTC);
    }
    
    public TimeZoneTestUtils(DateTimeZone testTimeZone) {
        this.testTimeZone = testTimeZone;
    }

    public void beforeTest() {
        defaultTimeZone = TimeZone.getDefault();
        defaultDateTimeZone = DateTimeZone.getDefault();
        defaultUserTimezone = System.getProperty("user.timezone");
        
        TimeZone.setDefault(testTimeZone.toTimeZone());
        DateTimeZone.setDefault(testTimeZone);
        System.setProperty("user.timezone", testTimeZone.getID());
    }
    
    public void afterTest() {
        if (defaultTimeZone != null) {
            TimeZone.setDefault(defaultTimeZone);
        }
        if (defaultDateTimeZone != null) {
            DateTimeZone.setDefault(defaultDateTimeZone);
        }
        if (defaultUserTimezone != null) {
            System.setProperty("user.timezone", defaultUserTimezone);
        }
    }
}
