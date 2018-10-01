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
package org.apereo.portal.spring.properties;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.beans.PropertyEditorSupport;
import java.io.IOException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.Assert;

/** Converts to/from the generic {@link JsonNode} API */
public class MappingJackson2JsonTreeEditor extends PropertyEditorSupport {
    private ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Set the {@code ObjectMapper} for this view. If not set, a default {@link
     * ObjectMapper#ObjectMapper() ObjectMapper} is used.
     *
     * <p>Setting a custom-configured {@code ObjectMapper} is one way to take further control of the
     * JSON serialization process. For example, an extended <code>SerializerFactory</code> can be
     * configured that provides custom serializers for specific types. The other option for refining
     * the serialization process is to use Jackson's provided annotations on the types to be
     * serialized, in which case a custom-configured ObjectMapper is unnecessary.
     */
    @Autowired(required = false)
    public void setObjectMapper(ObjectMapper objectMapper) {
        Assert.notNull(objectMapper, "ObjectMapper must not be null");
        this.objectMapper = objectMapper;
    }

    /** Return the underlying {@code ObjectMapper} for this view. */
    public ObjectMapper getObjectMapper() {
        return this.objectMapper;
    }

    @Override
    public void setValue(Object value) {
        if (value != null && !(value instanceof JsonNode)) {
            throw new IllegalArgumentException(
                    "Only values of type "
                            + JsonNode.class.getName()
                            + " are supported. Was "
                            + value.getClass().getName());
        }

        super.setValue(value);
    }

    @Override
    public String getAsText() {
        try {
            return this.objectMapper.writeValueAsString(getValue());
        } catch (IOException e) {
            throw new RuntimeException("Failed to convert JsonNode into JSON text", e);
        }
    }

    @Override
    public void setAsText(String text) throws IllegalArgumentException {
        final JsonNode value;
        try {
            value = this.objectMapper.readTree(text);
        } catch (IOException e) {
            throw new RuntimeException("Failed to convert JSON text into JsonNode", e);
        }
        super.setValue(value);
    }
}
