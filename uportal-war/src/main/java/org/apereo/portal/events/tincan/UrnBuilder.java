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
package org.apereo.portal.events.tincan;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import org.apereo.portal.url.BaseEncodedStringBuilder;

/**
 * Builds a valid URN
 *
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
            throw new IllegalStateException(
                    "Failed to convert '" + uriString + "' to a URI, this should not be possible",
                    e);
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
