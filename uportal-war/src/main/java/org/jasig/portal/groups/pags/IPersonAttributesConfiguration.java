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

package org.jasig.portal.groups.pags;

import java.util.Map;

/**
 * Interface for configuration providers for the Person Attributes Group Store.
 * Portal implementors may choose to override the default implementation of 
 * this type, <code>XMLPersonAttributesConfiguration</code>, in order to 
 * provide a list of group definitions for the PAGS to use.
 * 
 * @author Al wold
 * @version $Revision$
 */
public interface IPersonAttributesConfiguration {
   /**
    * Get the group definitions for the store.  Implementations
    * should initialize a Map of PersonAttributesGroupStore.GroupDefinition 
    * objects.
    * 
    * @return Map consisting of group definitions, keyed by group key
    */
   public Map getConfig();
}
