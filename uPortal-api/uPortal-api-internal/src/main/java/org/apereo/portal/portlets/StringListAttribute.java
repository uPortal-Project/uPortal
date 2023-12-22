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
package org.apereo.portal.portlets;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.apache.commons.lang.StringUtils;

public class StringListAttribute implements Serializable {
    private static final long serialVersionUID = 1L;
    public static final List<String> LIST_EMPTY_ITEMS = Arrays.asList("", null);
    private List<String> value = new ArrayList<String>();

    public StringListAttribute() {}

    public StringListAttribute(List<String> value) {
        removeAllEmptyItems(value);
        this.value = value;
    }

    public StringListAttribute(String[] value) {
        List<String> values = new ArrayList<String>();
        values.addAll(Arrays.<String>asList(value));
        removeAllEmptyItems(values);
        this.value = values;
    }

    /** @return the value */
    public List<String> getValue() {
        return value;
    }

    /**
     * A {@link StringListAttribute} is considered blank <i>unless</i> it has at least one
     * non-zero-length, non-whitespace entry.
     *
     * @return
     */
    public boolean isBlank() {
        boolean result = true; // default
        if (value != null) {
            for (String v : value) {
                if (StringUtils.isNotBlank(v)) {
                    result = false;
                    break;
                }
            }
        }
        return result;
    }

    /** @param value the value to set */
    public void setValue(List<String> value) {
        removeAllEmptyItems(value);
        this.value = value;
    }

    public void setValue(String[] value) {
        List<String> values = new ArrayList<String>();
        values.addAll(Arrays.<String>asList(value));
        removeAllEmptyItems(values);
        this.value = values;
    }

    private void removeAllEmptyItems(List<String> value) {
        value.removeAll(LIST_EMPTY_ITEMS);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((this.value == null) ? 0 : this.value.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null) return false;
        if (getClass() != obj.getClass()) return false;
        StringListAttribute other = (StringListAttribute) obj;
        if (this.value == null) {
            if (other.value != null) return false;
        } else if (!this.value.equals(other.value)) return false;
        return true;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return String.valueOf(this.value);
    }
}
