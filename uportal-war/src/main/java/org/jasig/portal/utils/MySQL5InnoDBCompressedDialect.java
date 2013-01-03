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
package org.jasig.portal.utils;

import org.hibernate.dialect.MySQL5InnoDBDialect;

/**
 * Uses the COMPRESSED row format in an InnoDB engine, needed for long index support with UTF-8 
 * 
 * @author Raymond Bourges
 */
public class MySQL5InnoDBCompressedDialect extends MySQL5InnoDBDialect {

	public String getTableTypeString() {
		return " ENGINE=InnoDB ROW_FORMAT=COMPRESSED";
	}
	
}
