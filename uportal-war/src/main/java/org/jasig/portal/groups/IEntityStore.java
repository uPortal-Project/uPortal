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

package org.jasig.portal.groups;
 
 /**
 * An interface for retrieving <code>IEntities</code>.
 * @author Dan Ellentuck
 * @version 1.0, 11/29/01
 */
public interface IEntityStore {
/**
 * @return org.jasig.portal.groups.IEntity
 * @param key java.lang.String
 */
IEntity newInstance(String key) throws GroupsException;
/**
 * @return org.jasig.portal.groups.IEntity
 * @param key java.lang.String - the entity's key
 * @param type java.lang.Class - the entity's Type
 */
IEntity newInstance(String key, Class type) throws GroupsException;
}
