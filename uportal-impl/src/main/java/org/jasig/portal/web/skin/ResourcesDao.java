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

/**
 * 
 */
package org.jasig.portal.web.skin;

/**
 * Interface to locate {@link Resources} (skin configuration).
 * 
 * @author Nicholas Blair, npblair@wisc.edu
 *
 */
public interface ResourcesDao {

	/**
	 * Load a {@link Resources} from the specified {@link String} path.
	 * Implementations will likely need to resolve the path against the
	 * ServletContext, as implementation at time of writing locates the resources
	 * configuration in the same directory as the javascript/css it documents.
	 * 
	 * @param pathToSkinXml
	 * @return
	 */
	Resources getResources(String pathToSkinXml);
}
