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
package org.jasig.portal.json.rendering;

import org.jasig.portal.character.stream.events.CharacterEventTypes;

public final class JsonLayoutPlaceholderEventImpl implements JsonLayoutPlaceholderEvent {
    private static final long serialVersionUID = 1L;
    
    public static final JsonLayoutPlaceholderEvent INSTANCE = new JsonLayoutPlaceholderEventImpl();
    
    private final int hash = internalHashCode();
    
    private JsonLayoutPlaceholderEventImpl() {
    }

    @Override
    public CharacterEventTypes getEventType() {
        return CharacterEventTypes.JSON_LAYOUT;
    }

    @Override
    public String toString() {
        return "JsonLayoutPlaceholderEvent";
    }

    @Override
    public int hashCode() {
        return hash;
    }
    
    private int internalHashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + getEventType().hashCode();
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (!(obj instanceof JsonLayoutPlaceholderEvent))
            return false;
        JsonLayoutPlaceholderEvent other = (JsonLayoutPlaceholderEvent) obj;
        
        if (getEventType() == null) {
            if (other.getEventType() != null)
                return false;
        }
        else if (!getEventType().equals(other.getEventType()))
            return false;
        
        return true;
    }

}
