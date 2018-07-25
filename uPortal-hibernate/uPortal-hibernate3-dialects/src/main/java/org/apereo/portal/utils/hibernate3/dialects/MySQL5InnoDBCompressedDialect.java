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
package org.apereo.portal.utils.hibernate3.dialects;

import org.hibernate.dialect.MySQL5InnoDBDialect;

/** Uses the COMPRESSED row format in an InnoDB engine, needed for long index support with UTF-8 */
public class MySQL5InnoDBCompressedDialect extends MySQL5InnoDBDialect {

    public MySQL5InnoDBCompressedDialect() {
        super();
    }

    @Override
    public String getTableTypeString() {
        return super.getTableTypeString() + " ROW_FORMAT=COMPRESSED";
    }
}
