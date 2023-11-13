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
package org.apereo.portal.events.tincan.json;

import static org.junit.Assert.assertEquals;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableMap;
import java.net.URI;
import java.util.Locale;
import org.apereo.portal.events.tincan.UrnBuilder;
import org.apereo.portal.events.tincan.om.LocalizedString;
import org.apereo.portal.events.tincan.om.LrsActor;
import org.apereo.portal.events.tincan.om.LrsObject;
import org.apereo.portal.events.tincan.om.LrsStatement;
import org.apereo.portal.events.tincan.om.LrsVerb;
import org.junit.Before;
import org.junit.Test;

public class LrsDataModelSerializeTest {
    private ObjectMapper objectMapper;

    @Before
    public void setup() {
        this.objectMapper = new ObjectMapper();
    }

    @Test
    public void testLrsVerbSerialize() throws Exception {
        final String result = this.objectMapper.writeValueAsString(LrsVerb.INITIALIZED);
        assertEquals(
                "{\"id\":\"http://adlnet.gov/expapi/verbs/initialized\",\"display\":{\"en-us\":\"initialized\"}}",
                result);
    }

    @Test
    public void testLrsActorSerialize() throws Exception {
        final LrsActor lrsActor = new LrsActor("user@example.com", "John Doe");

        final String result = this.objectMapper.writeValueAsString(lrsActor);
        assertEquals(
                objectMapper.readTree(
                        "{\"mbox\":\"user@example.com\",\"name\":\"John Doe\",\"objectType\":\"Agent\"}"),
                objectMapper.readTree(result));
    }

    @Test
    public void testLrsObjectSerialize() throws Exception {
        final UrnBuilder idBuilder =
                new UrnBuilder("UTF-8", "tincan", "uportal", "activities", "portlet", "fname");
        final URI id = idBuilder.getUri();

        final LrsObject lrsObject =
                new LrsObject(
                        id,
                        "Activity",
                        ImmutableMap.of(
                                "name", new LocalizedString(new Locale("en", "us"), "Portlet Name"),
                                "description",
                                        new LocalizedString(
                                                new Locale("en", "us"), "Portlet Description")));

        final String result = this.objectMapper.writeValueAsString(lrsObject);

        assertEquals(
                objectMapper.readTree(
                        "{\"id\":\"urn:tincan:uportal:activities:portlet:fname\",\"objectType\":\"Activity\",\"definition\":{\"name\":{\"en-US\":\"Portlet Name\"},\"description\":{\"en-US\":\"Portlet Description\"}}}"),
                objectMapper.readTree(result));
    }

    @Test
    public void testLrsStatementSerialize() throws Exception {
        final LrsActor lrsActor = new LrsActor("user@example.com", "John Doe");

        final UrnBuilder idBuilder =
                new UrnBuilder("UTF-8", "tincan", "uportal", "activities", "portlet", "fname");
        final URI id = idBuilder.getUri();

        final LrsObject lrsObject =
                new LrsObject(
                        id,
                        "Activity",
                        ImmutableMap.of(
                                "name", new LocalizedString(new Locale("en", "us"), "Portlet Name"),
                                "description",
                                        new LocalizedString(
                                                new Locale("en", "us"), "Portlet Description")));

        final LrsStatement lrsStatement =
                new LrsStatement(lrsActor, LrsVerb.INITIALIZED, lrsObject);

        final String result = this.objectMapper.writeValueAsString(lrsStatement);

        assertEquals(
                objectMapper.readTree(
                        "{\"actor\":{\"mbox\":\"user@example.com\",\"name\":\"John Doe\",\"objectType\":\"Agent\"},\"verb\":{\"id\":\"http://adlnet.gov/expapi/verbs/initialized\",\"display\":{\"en-us\":\"initialized\"}},\"object\":{\"id\":\"urn:tincan:uportal:activities:portlet:fname\",\"objectType\":\"Activity\",\"definition\":{\"name\":{\"en-US\":\"Portlet Name\"},\"description\":{\"en-US\":\"Portlet Description\"}}}}"),
                objectMapper.readTree(result));
    }
}
