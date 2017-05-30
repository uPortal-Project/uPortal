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
package org.apereo.portal.layout.om;

/**
 * Commons properties for all stylesheet properties/attributes/parameters
 *
 */
public interface IStylesheetData {
    public enum Scope {
        PERSISTENT,
        SESSION,
        REQUEST;
    }

    /** @return Unique ID of the descriptor */
    public long getId();

    /** Name of the attribute/parameter */
    public String getName();

    /** @param defaultValue Default value */
    public void setDefaultValue(String defaultValue);

    public String getDefaultValue();

    /** @param scope Where the value is stored */
    public void setScope(Scope scope);

    public Scope getScope();

    /** @param description Description */
    public void setDescription(String description);

    public String getDescription();
}
