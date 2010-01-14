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

package  org.jasig.portal.services.entityproperties;

import org.jasig.portal.EntityIdentifier;

/**
 * The interface for an entity property store; the EntityChannelRegistry
 * must be configured with exactly one store, but can have multiple finders.
 *
 * @author Alex Vigdor av317@columbia.edu
 * @version $Revision$
 */
public interface IEntityPropertyStore extends IEntityPropertyFinder{

    // will add property to store without affecting finder results
    void storeProperty(EntityIdentifier entityID, String name, String value);

    // will remove property from the local store, but not from finders
    void unStoreProperty(EntityIdentifier entityID, String name);
}



