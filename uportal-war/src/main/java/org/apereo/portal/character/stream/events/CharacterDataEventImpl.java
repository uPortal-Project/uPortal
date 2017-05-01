/**
 * Licensed to Apereo under one or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information regarding copyright ownership. Apereo
 * licenses this file to you under the Apache License, Version 2.0 (the "License"); you may not use
 * this file except in compliance with the License. You may obtain a copy of the License at the
 * following location:
 *
 * <p>http://www.apache.org/licenses/LICENSE-2.0
 *
 * <p>Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apereo.portal.character.stream.events;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import org.springframework.util.Assert;

/**
 */
public final class CharacterDataEventImpl implements CharacterDataEvent {
    private static final long serialVersionUID = 1L;

    /** Character data for an empty string */
    public static final CharacterDataEvent EMPTY_CHARACTER_DATA = new CharacterDataEventImpl("");

    //Since the cache using weak refs for the events it should never be a cause for object retention therefor no max-size is needed
    private static final LoadingCache<String, CharacterDataEvent> WEAK_EVENT_CACHE =
            CacheBuilder.newBuilder()
                    .weakValues()
                    .recordStats()
                    .build(
                            new CacheLoader<String, CharacterDataEvent>() {
                                @Override
                                public CharacterDataEvent load(String data) throws Exception {
                                    return new CharacterDataEventImpl(data);
                                }
                            });

    public static CharacterDataEvent create(String data) {
        if (data.length() == 0) {
            return EMPTY_CHARACTER_DATA;
        }

        CharacterDataEvent event = WEAK_EVENT_CACHE.getIfPresent(data);
        if (event == null) {
            //Make sure the String reference is using a minimal char[]
            data = new String(data);
            event = WEAK_EVENT_CACHE.getUnchecked(data);
        }
        return event;
    }

    static Cache<String, CharacterDataEvent> getEventCache() {
        return WEAK_EVENT_CACHE;
    }

    private final String data;
    private int hash = 0;

    private CharacterDataEventImpl(String data) {
        Assert.notNull(data);
        this.data = data;
    }

    /* (non-Javadoc)
     * @see org.apereo.portal.character.stream.events.CharacterDataEvent#getData()
     */
    @Override
    public String getData() {
        return this.data;
    }

    /* (non-Javadoc)
     * @see org.apereo.portal.character.stream.events.CharacterEvent#getEventType()
     */
    @Override
    public CharacterEventTypes getEventType() {
        return CharacterEventTypes.CHARACTER;
    }

    @Override
    public String toString() {
        return "CharacterDataEvent [data=" + this.data + "]";
    }

    @Override
    public int hashCode() {
        int h = hash;
        if (h == 0) {
            h = internalHashCode();
            hash = h;
        }
        return h;
    }

    private int internalHashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + getEventType().hashCode();
        result = prime * result + ((data == null) ? 0 : data.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null) return false;
        if (!(obj instanceof CharacterDataEvent)) return false;
        CharacterDataEvent other = (CharacterDataEvent) obj;

        if (data == null) {
            if (other.getData() != null) return false;
        } else if (!data.equals(other.getData())) return false;

        if (getEventType() == null) {
            if (other.getEventType() != null) return false;
        } else if (!getEventType().equals(other.getEventType())) return false;

        return true;
    }
}
