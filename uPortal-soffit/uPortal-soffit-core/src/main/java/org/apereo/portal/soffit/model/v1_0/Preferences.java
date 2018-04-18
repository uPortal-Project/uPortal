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
package org.apereo.portal.soffit.model.v1_0;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang3.builder.ToStringBuilder;

/**
 * Collection of preferences for this Soffit and their values. Preferences are typically defined by
 * administrators at publish time.
 *
 * @since 5.0
 */
public class Preferences extends AbstractTokenizable {

    private final Map<String, List<String>> preferencesMap;

    public Preferences(String encryptedToken, Map<String, List<String>> preferencesMap) {
        super(encryptedToken);
        this.preferencesMap = Collections.unmodifiableMap(preferencesMap);
    }

    /**
     * Supports proxying a missing data model element.
     *
     * @since 5.1
     */
    protected Preferences() {
        super(null);
        this.preferencesMap = null;
    }

    public List<String> getValues(String name) {
        return preferencesMap.get(name);
    }

    public Map<String, List<String>> getPreferencesMap() {
        return preferencesMap;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + ((preferencesMap == null) ? 0 : preferencesMap.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!super.equals(obj)) return false;
        if (getClass() != obj.getClass()) return false;
        Preferences other = (Preferences) obj;
        if (preferencesMap == null) {
            if (other.preferencesMap != null) return false;
        } else if (!preferencesMap.equals(other.preferencesMap)) return false;
        return true;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("preferencesMap", this.preferencesMap)
                .append("getEncryptedToken()", this.getEncryptedToken())
                .toString();
    }
}
