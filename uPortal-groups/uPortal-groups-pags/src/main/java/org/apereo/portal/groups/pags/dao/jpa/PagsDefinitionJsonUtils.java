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
package org.apereo.portal.groups.pags.dao.jpa;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.ObjectCodec;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import java.io.IOException;
import java.net.URLEncoder;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;
import org.apereo.portal.groups.pags.dao.IPersonAttributesGroupDefinition;
import org.apereo.portal.groups.pags.dao.IPersonAttributesGroupTestDefinition;
import org.apereo.portal.groups.pags.dao.IPersonAttributesGroupTestGroupDefinition;
import org.apereo.portal.url.IPortalRequestUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Supports serialization and deserialization of PAGS definitions in various ways. The {@link
 * JsonSerializer} and {@link JsonDeserializer} classes themselves are nested types on this class,
 * which is stereotyped as a Spring Component. This approach allows the outer type to access
 * Spring-managed dependencies and make them (privately, statically) available to the nested types.
 */
@Component
public final class PagsDefinitionJsonUtils {

    /** Help other Java classes use this API well. */
    public static final String URL_FORMAT_STRING = "/api/v4-3/pags/%s.json";

    private static final Map<Class<?>, Object> beans = new HashMap<>();

    @Autowired private IPortalRequestUtils portalRequestUtils;

    /** Makes some beans from the Spring app context available to nested types. */
    @PostConstruct
    public void setUp() {
        beans.clear();
        beans.put(IPortalRequestUtils.class, portalRequestUtils);
    }

    /*
     * Nested Types
     */

    /**
     * We only want name, description, and a URL that would return the whole object (in JSON) when
     * we serialize the <code>members</code> attribute of a PAGS definition.
     */
    public static final class DefinitionLinkJsonSerializer
            extends JsonSerializer<Set<IPersonAttributesGroupDefinition>> {

        @Override
        public void serialize(
                Set<IPersonAttributesGroupDefinition> members,
                JsonGenerator jsonGenerator,
                SerializerProvider serializerProvider)
                throws JsonGenerationException, IOException {

            jsonGenerator.writeStartArray();
            for (IPersonAttributesGroupDefinition groupDef : members) {
                jsonGenerator.writeStartObject();
                jsonGenerator.writeStringField("name", groupDef.getName());
                jsonGenerator.writeStringField("description", groupDef.getDescription());
                IPortalRequestUtils portalRequestUtils =
                        (IPortalRequestUtils) beans.get(IPortalRequestUtils.class);
                HttpServletRequest req = portalRequestUtils.getCurrentPortalRequest();
                StringBuilder url = new StringBuilder();
                url.append(req.getContextPath())
                        .append(
                                String.format(
                                        URL_FORMAT_STRING,
                                        URLEncoder.encode(groupDef.getName(), "UTF-8")));
                jsonGenerator.writeStringField("url", url.toString());
                jsonGenerator.writeEndObject();
            }
            jsonGenerator.writeEndArray();
        }
    }

    /**
     * This deserializer will (when it's implemented) translate JSON references to PAGS definitions
     * into full PAGS definitions.
     */
    public static final class DefinitionLinkJsonDeserializer
            extends JsonDeserializer<Set<IPersonAttributesGroupDefinition>> {

        @Override
        public Set<IPersonAttributesGroupDefinition> deserialize(
                JsonParser jsonParser, DeserializationContext ctx)
                throws JsonProcessingException, IOException {
            ObjectCodec oc = jsonParser.getCodec();
            JsonNode node = oc.readTree(jsonParser);
            // For now, we'll only support deserializing PAGS definitions WITHOUT members...
            if (node.elements().hasNext()) {
                throw new UnsupportedOperationException("Members not yet supported");
            }
            return Collections.emptySet();
        }
    }

    public static final class TestGroupJsonDeserializer
            extends JsonDeserializer<Set<IPersonAttributesGroupTestGroupDefinition>> {

        @Override
        public Set<IPersonAttributesGroupTestGroupDefinition> deserialize(
                JsonParser jsonParser, DeserializationContext ctx)
                throws JsonProcessingException, IOException {
            Set<IPersonAttributesGroupTestGroupDefinition> result = new HashSet<>();
            ObjectCodec oc = jsonParser.getCodec();
            JsonNode json = oc.readTree(jsonParser);
            for (Iterator<JsonNode> testGroupNodes = json.elements(); testGroupNodes.hasNext(); ) {
                JsonNode testGroupNode = testGroupNodes.next();
                IPersonAttributesGroupTestGroupDefinition testGroupDef =
                        new PersonAttributesGroupTestGroupDefinitionImpl();
                Set<IPersonAttributesGroupTestDefinition> testDefs = new HashSet<>();
                for (Iterator<JsonNode> testNodes = testGroupNode.get("tests").elements();
                        testNodes.hasNext(); ) {
                    JsonNode testNode = testNodes.next();
                    IPersonAttributesGroupTestDefinition testDef =
                            new PersonAttributesGroupTestDefinitionImpl();
                    testDef.setAttributeName(testNode.get("attributeName").asText());
                    testDef.setTesterClassName(testNode.get("testerClassName").asText());
                    testDef.setTestValue(testNode.get("testValue").asText());
                    testDefs.add(testDef);
                    // NOTE:  We are also obligated to establish the backlink
                    // testDef --> testGroupDef;  arguably this backlink serves
                    // little purpose and could be removed.
                    testDef.setTestGroup(testGroupDef);
                }
                testGroupDef.setTests(testDefs);
                result.add(testGroupDef);
            }
            return result;
        }
    }
}
