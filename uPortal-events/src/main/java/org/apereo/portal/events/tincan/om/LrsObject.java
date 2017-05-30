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
package org.apereo.portal.events.tincan.om;

import com.google.common.collect.ImmutableMap;
import java.net.URI;
import java.util.Map;

/**
 * Target of an {@link LrsVerb}
 *
 */
public class LrsObject {
    private final URI id;
    private final String objectType;
    private final Map<String, LocalizedString> definition;

    public LrsObject(URI id, String objectType, Map<String, LocalizedString> definition) {
        this.id = id;
        this.objectType = objectType;
        this.definition = ImmutableMap.copyOf(definition);
    }

    public URI getId() {
        return id;
    }

    public String getObjectType() {
        return objectType;
    }

    public Map<String, LocalizedString> getDefinition() {
        return definition;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((definition == null) ? 0 : definition.hashCode());
        result = prime * result + ((id == null) ? 0 : id.hashCode());
        result = prime * result + ((objectType == null) ? 0 : objectType.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null) return false;
        if (getClass() != obj.getClass()) return false;
        LrsObject other = (LrsObject) obj;
        if (definition == null) {
            if (other.definition != null) return false;
        } else if (!definition.equals(other.definition)) return false;
        if (id == null) {
            if (other.id != null) return false;
        } else if (!id.equals(other.id)) return false;
        if (objectType == null) {
            if (other.objectType != null) return false;
        } else if (!objectType.equals(other.objectType)) return false;
        return true;
    }

    @Override
    public String toString() {
        return "LrsObject [id="
                + id
                + ", objectType="
                + objectType
                + ", definition="
                + definition
                + "]";
    }
}
