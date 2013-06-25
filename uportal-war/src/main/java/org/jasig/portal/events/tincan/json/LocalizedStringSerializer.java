package org.jasig.portal.events.tincan.json;

import java.io.IOException;
import java.util.Locale;

import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.JsonProcessingException;
import org.codehaus.jackson.map.JsonSerializer;
import org.codehaus.jackson.map.SerializerProvider;
import org.jasig.portal.events.tincan.om.LocalizedString;

public class LocalizedStringSerializer extends JsonSerializer<LocalizedString> {

    @Override
    public void serialize(LocalizedString value, JsonGenerator jgen,
            SerializerProvider provider) throws IOException,
            JsonProcessingException {

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
        
        if (c||(l&&v)) {
            result.append('-').append(country);
        }
        
        if (v&&(l||c)) {
            result.append('-').append(variant);
        }
        
        return result.toString();
    }
}
