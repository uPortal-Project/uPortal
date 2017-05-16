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
package org.apereo.portal.utils.cache;

/**
 * Simple cache entry tag that uses two strings
 *
 */
public class SimpleCacheEntryTag<V> implements CacheEntryTag {
    private static final long serialVersionUID = 1L;

    private final String tagType;
    private final V tagValue;
    private final int hash;

    public SimpleCacheEntryTag(String tagType, V tagValue) {
        this.tagType = tagType;
        this.tagValue = tagValue;
        this.hash = internalHashCode();
    }

    @Override
    public String getTagType() {
        return this.tagType;
    }

    public V getTagValue() {
        return tagValue;
    }

    @Override
    public int hashCode() {
        return this.hash;
    }

    private int internalHashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((tagType == null) ? 0 : tagType.hashCode());
        result = prime * result + ((tagValue == null) ? 0 : tagValue.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null) return false;
        if (getClass() != obj.getClass()) return false;
        SimpleCacheEntryTag<?> other = (SimpleCacheEntryTag<?>) obj;
        if (tagType == null) {
            if (other.tagType != null) return false;
        } else if (!tagType.equals(other.tagType)) return false;
        if (tagValue == null) {
            if (other.tagValue != null) return false;
        } else if (!tagValue.equals(other.tagValue)) return false;
        return true;
    }

    @Override
    public String toString() {
        return "SimpleCacheEntryTag [tagType=" + tagType + ", tagValue=" + tagValue + "]";
    }
}
