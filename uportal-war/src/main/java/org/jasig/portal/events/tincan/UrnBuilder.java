package org.jasig.portal.events.tincan;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import org.jasig.portal.url.BaseEncodedStringBuilder;


/**
 * Builds a valid URN
 * 
 * @author Eric Dalquist
 */
public final class UrnBuilder extends BaseEncodedStringBuilder {
    private static final long serialVersionUID = 1L;
    
    private final List<String> parts = new LinkedList<String>();
    
    public UrnBuilder(String encoding, String... parts) {
        super(encoding);
        
        add(parts);
    }
    
    public UrnBuilder(String encoding, Collection<String> parts) {
        super(encoding);
        
        add(parts);
    }
    
    public UrnBuilder add(String... parts) {
        for (String part : parts) {
            this.parts.add(part);
        }
        return this;
    }
    
    public UrnBuilder add(Collection<String> parts) {
        this.parts.addAll(parts);
        return this;
    }
    
    public URI getUri() {
        final String uriString = toString();
        try {
            return new URI(uriString);
        } catch (URISyntaxException e) {
            throw new IllegalStateException("Failed to convert '" + uriString + "' to a URI, this should not be possible", e);
        }
    }
    
    @Override
    public boolean equals(Object obj) {
        if (!this.getClass().isInstance(obj)) {
            return false;
        }
        
        return this.toString().equals(obj.toString());
    }

    @Override
    public int hashCode() {
        return this.toString().hashCode();
    }

    @Override
    public String toString() {
        final StringBuilder uriString = new StringBuilder("urn");

        for (final String part : this.parts) {
            uriString.append(':');
            uriString.append(this.encode(part));
        }
        
        return uriString.toString();
    }
}
