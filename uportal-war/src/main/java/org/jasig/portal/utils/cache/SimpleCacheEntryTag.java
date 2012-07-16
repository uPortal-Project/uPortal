package org.jasig.portal.utils.cache;

/**
 * Simple cache entry tag that uses two strings
 * 
 * @author Eric Dalquist
 */
public class SimpleCacheEntryTag implements CacheEntryTag {
    private final String tagType;
    private final String tagValue;
    private final int hash;
    
    public SimpleCacheEntryTag(String tagType, String tagValue) {
        this.tagType = tagType;
        this.tagValue = tagValue;
        this.hash = internalHashCode();
    }

    @Override
    public String getTagType() {
        return this.tagType;
    }

    public String getTagValue() {
        return tagValue;
    }
    
    @Override
    public int hashCode() {
        return this.hash;
    }
    
    private int internalHashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((tagType == null) ? 0 : tagType.hashCode());
        result = prime * result + ((tagValue == null) ? 0 : tagValue.hashCode());
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
        SimpleCacheEntryTag other = (SimpleCacheEntryTag) obj;
        if (tagType == null) {
            if (other.tagType != null)
                return false;
        }
        else if (!tagType.equals(other.tagType))
            return false;
        if (tagValue == null) {
            if (other.tagValue != null)
                return false;
        }
        else if (!tagValue.equals(other.tagValue))
            return false;
        return true;
    }

    @Override
    public String toString() {
        return "SimpleCacheEntryTag [tagType=" + tagType + ", tagValue=" + tagValue + "]";
    }
}
