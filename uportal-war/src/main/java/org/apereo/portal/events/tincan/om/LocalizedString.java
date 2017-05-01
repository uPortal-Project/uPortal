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

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import java.util.Locale;
import org.apereo.portal.events.tincan.json.LocalizedStringSerializer;

/**
 * A String with an associated Locale
 *
 */
@JsonSerialize(using = LocalizedStringSerializer.class)
public class LocalizedString {
    private final Locale locale;
    private final String value;

    public LocalizedString(Locale locale, String value) {
        this.locale = locale;
        this.value = value;
    }

    public Locale getLocale() {
        return locale;
    }

    public String getValue() {
        return value;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((locale == null) ? 0 : locale.hashCode());
        result = prime * result + ((value == null) ? 0 : value.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null) return false;
        if (getClass() != obj.getClass()) return false;
        LocalizedString other = (LocalizedString) obj;
        if (locale == null) {
            if (other.locale != null) return false;
        } else if (!locale.equals(other.locale)) return false;
        if (value == null) {
            if (other.value != null) return false;
        } else if (!value.equals(other.value)) return false;
        return true;
    }

    @Override
    public String toString() {
        return "LocalizedString [locale=" + locale + ", value=" + value + "]";
    }
}
