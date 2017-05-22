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
package org.apereo.portal.tools.dbloader;

/**
 * Handles portal schema drop/create/update
 *
 */
public interface ISchemaExport {

    /** @return The name of the JPA persistence unit this bean functions against */
    public String getPersistenceUnitName();

    /**
     * Drop all objects, does not fail if drop statements fail to execute
     *
     * @param export If the database should have the SQL executed against it
     * @param outputFile Optional file to write out the SQL to.
     * @param append If the output file should be appended to or overwritten
     */
    public void drop(boolean export, String outputFile, boolean append);

    /**
     * Create all objects, fails if any create statement fails to execute
     *
     * @param export If the database should have the SQL executed against it
     * @param outputFile Optional file to write out the SQL to.
     * @param append If the output file should be appended to or overwritten
     */
    public void create(boolean export, String outputFile, boolean append);

    /**
     * Update all objects to match the current required schema, fails if any statement fails to
     * execute.
     *
     * @param export If the database should have the SQL executed against it
     * @param outputFile Optional file to write out the SQL to.
     * @param append If the output file should be appended to or overwritten
     */
    public void update(boolean export, String outputFile, boolean append);
}
