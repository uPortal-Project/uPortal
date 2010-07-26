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

package org.jasig.portal;

/**
 * A very basic interface for returning oids.
 *
 * <p>
 *    <code>getNext()</code>
 *    <code>getNext(String name)</code>
 * <p>
 *
 * @author Dan Ellentuck
 * @version $Revision$
 */
public interface IOIDGenerator {
/**
 * @return java.lang.String
 * @exception java.lang.Exception The exception description.
 */
public String getNext() throws java.lang.Exception;
/**
 * @param name String 
 * @return java.lang.String
 * @exception java.lang.Exception
 */
public String getNext(String name) throws java.lang.Exception;
}
