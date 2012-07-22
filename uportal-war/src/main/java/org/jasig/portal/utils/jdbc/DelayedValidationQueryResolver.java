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
package org.jasig.portal.utils.jdbc;

import javax.sql.DataSource;

import org.jasig.portal.concurrency.FunctionWithoutResult;

/**
 * Allows for a DataSource to have the appropriate validation query found
 * 
 * @author Eric Dalquist
 */
public interface DelayedValidationQueryResolver {

	/**
	 * @param dataSource The {@link DataSource} to find the validation query for
	 * @param validationQueryCallback The callback to execute when the validation query is found
	 */
	void registerValidationQueryCallback(DataSource dataSource,
			FunctionWithoutResult<String> validationQueryCallback);

}