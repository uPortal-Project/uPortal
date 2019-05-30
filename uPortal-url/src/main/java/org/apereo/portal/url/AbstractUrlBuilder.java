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
package org.apereo.portal.url;

import com.google.common.collect.ObjectArrays;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang.Validate;

/** */
public abstract class AbstractUrlBuilder implements IUrlBuilder {
    private final Map<String, String[]> parameters;

    public AbstractUrlBuilder() {
        this.parameters = new HashMap<>();
    }

    @Override
    public final Map<String, String[]> getParameters() {
        return Collections.unmodifiableMap(this.parameters);
    }

    @Override
    public final void addParameter(String name, String... values) {
        Validate.notNull(values, "values can not be null");

        String[] valuesList = this.parameters.get(name);
        if (valuesList == null) {
            valuesList = Arrays.copyOf(values, values.length);
        } else {
            valuesList = ObjectArrays.concat(valuesList, values, String.class);
        }

        this.parameters.put(name, valuesList);
    }

    @Override
    public final void setParameter(String name, String... values) {
        Validate.notNull(values, "values can not be null");

        final String[] valuesList = Arrays.copyOf(values, values.length);
        this.parameters.put(name, valuesList);
    }

    @Override
    public void setParameter(String name, List<String> values) {
        Validate.notNull(values, "values can not be null");

        this.parameters.put(name, values.toArray(new String[values.size()]));
    }

    @Override
    public final void setParameters(Map<String, List<String>> parameters) {
        this.parameters.clear();
        for (final Map.Entry<String, List<String>> parameterEntry : parameters.entrySet()) {
            this.setParameter(parameterEntry.getKey(), parameterEntry.getValue());
        }
    }

    @Override
    public String toString() {
        return "AbstractPortalUrl [parameters=" + this.parameters + "]";
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((this.parameters == null) ? 0 : this.parameters.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null) return false;
        if (getClass() != obj.getClass()) return false;
        AbstractUrlBuilder other = (AbstractUrlBuilder) obj;
        if (this.parameters == null) {
            if (other.parameters != null) return false;
        } else if (!this.parameters.equals(other.parameters)) return false;
        return true;
    }
}
