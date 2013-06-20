package org.jasig.portal.events.tincan.om;

import java.util.Locale;

import org.codehaus.jackson.map.annotate.JsonSerialize;
import org.jasig.portal.events.tincan.json.LocalizedStringSerializer;

/**
 * A String with an associated Locale
 * 
 * @author Eric Dalquist
 */
@JsonSerialize(using=LocalizedStringSerializer.class)
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
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        LocalizedString other = (LocalizedString) obj;
        if (locale == null) {
            if (other.locale != null)
                return false;
        } else if (!locale.equals(other.locale))
            return false;
        if (value == null) {
            if (other.value != null)
                return false;
        } else if (!value.equals(other.value))
            return false;
        return true;
    }

    @Override
    public String toString() {
        return "LocalizedString [locale=" + locale + ", value=" + value + "]";
    }
}
