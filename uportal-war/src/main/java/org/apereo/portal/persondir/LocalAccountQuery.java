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
package org.apereo.portal.persondir;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * LocalAccountQuery provides the persondirectory query object for local accounts.
 *
 */
public class LocalAccountQuery {

    private String username;

    private final Map<String, List<String>> attributes = new HashMap<String, List<String>>();

    public String getName() {
        return username;
    }

    public void setUserName(String name) {
        this.username = name;
    }

    public Map<String, List<String>> getAttributes() {
        return attributes;
    }

    public void setAttribute(String name, List<String> values) {
        this.attributes.put(name, values);
    }
}
