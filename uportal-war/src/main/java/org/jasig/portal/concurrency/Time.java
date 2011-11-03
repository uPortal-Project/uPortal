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

import java.util.Collections;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import com.google.common.collect.Maps;

/**
 * A combination of a {@link TimeUnit} and a duration
 * 
 * @author Eric Dalquist
 * @version $Revision$
 */
public class Time {
    private static final char SEPARATOR = '_';
    
    private static final Map<TimeUnit, Time> ZERO_CACHE;
    
    static {
        final Map<TimeUnit, Time> zeroCacheBuilder = Maps.newEnumMap(TimeUnit.class);
        for (final TimeUnit timeUnit : TimeUnit.values()) {
            zeroCacheBuilder.put(timeUnit, new Time(0, timeUnit));
        }
        
        ZERO_CACHE = Collections.unmodifiableMap(zeroCacheBuilder);
    }
    
    private final TimeUnit timeUnit;
    private final long duration;
    private String s = null;
    private int h = 0;
    
    /**
     * Time from parsing formatted string.
     * <br/>
     * If the string is null or it does not conform to {@link Time#toString()} formatting
     * {@link IllegalArgumentException} is thrown.
     * 
     * @param s String
     * @return Time
     */
    public static Time valueOf(String s) {
        if (s == null || s.length() == 0) {
            throw new IllegalArgumentException("cannot create Time from \"null\" or \"\" String");
        }

        //Verify there either is no separator or that the separator is in a valid position
        final int split = s.indexOf(SEPARATOR);
        if (split == 0) {
            throw new IllegalArgumentException("cannot create Time from \"" + s + "\" the first character cannot be " + SEPARATOR);
        }
        if (split == s.length() - 1) {
            throw new IllegalArgumentException("cannot create Time from \"" + s + "\" the last character cannot be " + SEPARATOR);
        }

        //Just a number, assume it is in milliseconds
        if (split < 0) {
            final long duration;
            try {
                duration = Long.parseLong(s);
            }
            catch (NumberFormatException e) {
                throw new IllegalArgumentException("cannot create Time from \"" + s + "\" the string cannot be parsed as a long", e);
            }
            
            return getTime(duration);
        }
        
        final String durationPart = s.substring(0, split);
        final long duration;
        try {
            duration = Long.parseLong(durationPart);
        }
        catch (NumberFormatException e) {
            throw new IllegalArgumentException("cannot create Time from \"" + s + "\" the duration part \"" + durationPart + "\" cannot be parsed as a long", e);
        }
        
        final String unitPart = s.substring(split + 1);
        final TimeUnit timeUnit;
        try {
            timeUnit = TimeUnit.valueOf(unitPart);
        }
        catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("cannot create Time from \"" + s + "\" the TimeUnit part \"" + unitPart + "\" cannot be parsed as a TimeUnit", e);
        }
        
        return getTime(duration, timeUnit);
    }
    
    /**
     * Creates a {@link TimeUnit#MILLISECONDS} based Time
     * @param duration time in milliseconds
     */
    public static Time getTime(long duration) {
        return getTime(duration, TimeUnit.MILLISECONDS);
    }

    /**
     * @param timeUnit TimeUnit
     * @param duration time in the specified unit
     */
    public static Time getTime(long duration, TimeUnit timeUnit) {
        if (duration == 0) {
            return ZERO_CACHE.get(timeUnit);
        }
        
        return new Time(duration, timeUnit);
    }
    
    private Time(long duration, TimeUnit timeUnit) {
        if (timeUnit == null) {
            throw new NullPointerException("TimeUnit can not be null");
        }
        
        this.timeUnit = timeUnit;
        this.duration = duration;
    }
    
    public TimeUnit getTimeUnit() {
        return this.timeUnit;
    }

    public long getDuration() {
        return this.duration;
    }

    public long asNanos() {
        return this.timeUnit.toNanos(duration);
    }

    public long asMicros() {
        return this.timeUnit.toMicros(duration);
    }

    public long asMillis() {
        return this.timeUnit.toMillis(duration);
    }

    public long asSeconds() {
        return this.timeUnit.toSeconds(duration);
    }

    public long asMinutes() {
        return this.timeUnit.toMinutes(duration);
    }

    public long asHours() {
        return this.timeUnit.toHours(duration);
    }

    public long asDays() {
        return this.timeUnit.toDays(duration);
    }

    public void timedWait(Object obj) throws InterruptedException {
        this.timeUnit.timedWait(obj, this.duration);
    }

    public void timedJoin(Thread thread) throws InterruptedException {
        this.timeUnit.timedJoin(thread, this.duration);
    }

    public void sleep() throws InterruptedException {
        this.timeUnit.sleep(this.duration);
    }

    @Override
    public int hashCode() {
        int hash = h;
        if (hash == 0) {
            hash = 1;
            final int prime = 31;
            hash = prime * hash + (int) (this.duration ^ (this.duration >>> 32));
            hash = prime * hash + ((this.timeUnit == null) ? 0 : this.timeUnit.hashCode());
            h = hash;
        }
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        Time other = (Time) obj;
        if (this.duration != other.duration)
            return false;
        if (this.timeUnit != other.timeUnit)
            return false;
        return true;
    }

    @Override
    public String toString() {
        String str = s;
        if (str == null) {
            str = Long.toString(this.duration) + SEPARATOR + this.timeUnit.name();
            s = str;
        }
        return str;
    }
}
