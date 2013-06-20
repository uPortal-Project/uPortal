package org.jasig.portal.events.tincan.om;

import java.util.Map;

import org.jasig.portal.url.UrlStringBuilder;

import com.google.common.collect.ImmutableMap;

/**
 * Target of an {@link LrsVerb}
 * 
 * @author Eric Dalquist
 */
public class LrsObject {
    private final String id;
    private final String objectType;
    private final Map<String, LocalizedString> definition;
    
    public LrsObject(UrlStringBuilder id, String objectType, Map<String, LocalizedString> definition) {
        this.id = id.toString();
        if (this.id.startsWith("/")) {
            throw new IllegalArgumentException("id must be an absolute URL: " + id);
        }
        this.objectType = objectType;
        this.definition = ImmutableMap.copyOf(definition);
    }

    public String getId() {
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
        result = prime * result
                + ((definition == null) ? 0 : definition.hashCode());
        result = prime * result + ((id == null) ? 0 : id.hashCode());
        result = prime * result
                + ((objectType == null) ? 0 : objectType.hashCode());
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
        LrsObject other = (LrsObject) obj;
        if (definition == null) {
            if (other.definition != null)
                return false;
        } else if (!definition.equals(other.definition))
            return false;
        if (id == null) {
            if (other.id != null)
                return false;
        } else if (!id.equals(other.id))
            return false;
        if (objectType == null) {
            if (other.objectType != null)
                return false;
        } else if (!objectType.equals(other.objectType))
            return false;
        return true;
    }

    @Override
    public String toString() {
        return "LrsObject [id=" + id + ", objectType=" + objectType
                + ", definition=" + definition + "]";
    }
}
