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
package org.apereo.portal.events.tincan.om;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.apereo.portal.events.tincan.json.LrsVerbSerializer;

/** A list of verbs from http://www.adlnet.gov/expapi/verbs/ */
@JsonSerialize(using = LrsVerbSerializer.class)
public enum LrsVerb {
    ANSWERED,
    ASKED,
    ATTEMPTED,
    ATTENDED,
    COMMENTED,
    COMPLETED,
    EXITED,
    EXPERIENCED,
    FAILED,
    IMPORTED,
    INITIALIZED,
    INTERACTED,
    LAUNCHED,
    MASTERED,
    PASSED,
    PREFERRED,
    PROGRESSED,
    REGISTERED,
    RESPONDED,
    RESUMED,
    SCORED,
    SHARED,
    SUSPENDED,
    TERMINATED,
    VOIDED;

    public static final String BASE_URI = "http://adlnet.gov/expapi/verbs/";
    private final String uri;

    private LrsVerb() {
        this.uri = BASE_URI + this.name().toLowerCase();
    }

    /** @return The URI for this verb */
    public String getUri() {
        return uri;
    }
}
