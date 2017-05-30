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
package org.apereo.portal.dao.usertype;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import java.io.IOException;
import org.apereo.portal.spring.beans.factory.ObjectMapperFactoryBean;
import org.jadira.usertype.spi.shared.AbstractStringColumnMapper;

/**
 * Mapper that read/writes objects to JSON. Uses a 2 step serialization/deserialization process so
 * the object's type can be recorded in the JSON data
 *
 */
public class JacksonColumnMapper extends AbstractStringColumnMapper<Object> {
    private static final long serialVersionUID = 1L;

    private final LoadingCache<Class<?>, ObjectWriter> typedObjectWriters =
            CacheBuilder.newBuilder()
                    .build(
                            new CacheLoader<Class<?>, ObjectWriter>() {
                                @Override
                                public ObjectWriter load(Class<?> key) throws Exception {
                                    return objectWriter.withType(key);
                                }
                            });
    private final LoadingCache<Class<?>, ObjectReader> typedObjectReaders =
            CacheBuilder.newBuilder()
                    .build(
                            new CacheLoader<Class<?>, ObjectReader>() {
                                @Override
                                public ObjectReader load(Class<?> key) throws Exception {
                                    return objectReader.withType(key);
                                }
                            });

    private final ObjectWriter objectWriter;
    private final ObjectReader objectReader;

    public JacksonColumnMapper() {
        final ObjectMapper mapper;
        try {
            final ObjectMapperFactoryBean omfb = new ObjectMapperFactoryBean();
            omfb.afterPropertiesSet();
            mapper = omfb.getObject();
        } catch (Exception e) {
            throw new RuntimeException("Failed to create ObjectMapper", e);
        }

        customizeObjectMapper(mapper);

        this.objectWriter = this.createObjectWriter(mapper).withType(JsonWrapper.class);
        this.objectReader = this.createObjectReader(mapper).withType(JsonWrapper.class);
    }

    protected void customizeObjectMapper(ObjectMapper mapper) {}

    protected ObjectWriter createObjectWriter(ObjectMapper mapper) {
        return mapper.writer();
    }

    protected ObjectReader createObjectReader(ObjectMapper mapper) {
        return mapper.reader();
    }

    @Override
    public final Object fromNonNullValue(String s) {
        try {
            final JsonWrapper jsonWrapper = objectReader.readValue(s);
            final ObjectReader typeReader = typedObjectReaders.getUnchecked(jsonWrapper.getType());
            return typeReader.readValue(jsonWrapper.getValue());
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("Could not read from JSON: " + s, e);
        } catch (IOException e) {
            throw new IllegalArgumentException("Could not read from JSON: " + s, e);
        }
    }

    @Override
    public final String toNonNullValue(Object value) {
        try {
            final Class<? extends Object> type = value.getClass();
            final ObjectWriter typeWriter = typedObjectWriters.getUnchecked(type);
            final String valueAsString = typeWriter.writeValueAsString(value);

            return objectWriter.writeValueAsString(new JsonWrapper(type, valueAsString));
        } catch (JsonGenerationException e) {
            throw new IllegalArgumentException("Could not write to JSON: " + value, e);
        } catch (JsonMappingException e) {
            throw new IllegalArgumentException("Could not write to JSON: " + value, e);
        } catch (IOException e) {
            throw new IllegalArgumentException("Could not write to JSON: " + value, e);
        }
    }

    public static final class JsonWrapper {
        private Class<?> t;
        private String v;

        public JsonWrapper() {}

        public JsonWrapper(Class<?> type, String value) {
            this.t = type;
            this.v = value;
        }

        public Class<?> getType() {
            return t;
        }

        public void setType(Class<?> type) {
            this.t = type;
        }

        public String getValue() {
            return v;
        }

        public void setValue(String value) {
            this.v = value;
        }

        @Override
        public String toString() {
            return "JsonWrapper [type=" + t + ", value=" + v + "]";
        }
    }
}
