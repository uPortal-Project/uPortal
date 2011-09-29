/**
 * Licensed to Jasig under one or more contributor license
 * agreements. See the NOTICE file distributed with this work
 * for additional information regarding copyright ownership.
 * Jasig licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a
 * copy of the License at:
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.jasig.portal.tools.dbloader;

/**
 * @author Eric Dalquist
 * @version $Revision$
 */
public interface ISchemaExport {

    /**
     * @deprecated use {@link #create(boolean, boolean, boolean, String, boolean)}
     */
    @Deprecated
    public void hbm2ddl(boolean export, boolean create, boolean drop, String outputFile, boolean haltOnError);
    
    /**
     * @param export If the database should have the SQL executed against it
     * @param create If database objects should be created
     * @param drop If existing database objects should be dropped before creating new objects
     * @param outputFile Optional file to write out the SQL to.
     * @param haltOnError If an error should cause creation to halt with an exception
     */
    public void create(boolean export, boolean create, boolean drop, String outputFile, boolean haltOnError);
    
    /**
     * @param export If the database should have the SQL executed against it
     * @param outputFile Optional file to write out the SQL to.
     * @param haltOnError If an error should cause creation to halt with an exception
     */
    public void update(boolean export, String outputFile, boolean haltOnError);

}