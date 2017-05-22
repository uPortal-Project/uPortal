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

import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import org.apache.commons.lang.Validate;

/**
 * Base class to handle encoding strings
 *
 */
public abstract class BaseEncodedStringBuilder implements Serializable, Cloneable {
    private static final long serialVersionUID = 1L;

    private final String encoding;

    public BaseEncodedStringBuilder(String encoding) {
        Validate.notNull(encoding, "encoding can not be null");
        this.checkEncoding(encoding);

        this.encoding = encoding;
    }

    protected final void checkEncoding(String encoding) {
        try {
            URLEncoder.encode("", encoding);
        } catch (UnsupportedEncodingException e) {
            throw new IllegalArgumentException("Encoding '" + encoding + "' is not supported", e);
        }
    }

    protected final String encode(String s) {
        try {
            return URLEncoder.encode(s, this.encoding);
        } catch (UnsupportedEncodingException e) {
            throw new IllegalStateException(
                    "Encoding '"
                            + this.encoding
                            + "' is not supported. This should have been caught in the consructor.",
                    e);
        }
    }

    public String getEncoding() {
        return encoding;
    }
}
