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
package org.apereo.portal.events.tincan.json;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import java.io.IOException;
import java.util.Locale;
import org.apereo.portal.events.tincan.om.LocalizedString;

public class LocalizedStringSerializer extends JsonSerializer<LocalizedString> {

    @Override
    public void serialize(LocalizedString value, JsonGenerator jgen, SerializerProvider provider)
            throws IOException, JsonProcessingException {

        final Locale locale = value.getLocale();

        jgen.writeStartObject();
        jgen.writeStringField(toString(locale), value.getValue());
        jgen.writeEndObject();
    }

    private String toString(Locale locale) {
        final String language = locale.getLanguage();
        final String country = locale.getCountry();
        final String variant = locale.getVariant();

        final boolean l = language.length() != 0;
        final boolean c = country.length() != 0;
        final boolean v = variant.length() != 0;

        final StringBuilder result = new StringBuilder(language);

        if (c || (l && v)) {
            result.append('-').append(country);
        }

        if (v && (l || c)) {
            result.append('-').append(variant);
        }

        return result.toString();
    }
}
