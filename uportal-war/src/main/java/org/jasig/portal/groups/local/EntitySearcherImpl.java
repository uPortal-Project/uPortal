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

package org.jasig.portal.groups.local;

import org.jasig.portal.EntityIdentifier;
import org.jasig.portal.groups.GroupsException;
import org.jasig.portal.groups.IEntitySearcher;

/**
 * An IEntitySearcher implementation for the local portal group service. 
 * Uses implementations of ITypedEntitySearcher to do the dirty work.
 *
 * @author Alex Vigdor
 * @version $Revision$
 */


public class EntitySearcherImpl implements IEntitySearcher {
  protected ITypedEntitySearcher[] searchers;
  public EntitySearcherImpl(ITypedEntitySearcher[] searchers) {
    this.searchers = searchers;
  }
  public EntityIdentifier[] searchForEntities(String query, int method, Class type) throws GroupsException {
    EntityIdentifier[] r = new EntityIdentifier[0];
    for (int i = 0; i < searchers.length; i++){
       if (searchers[i].getType().equals(type)){
          r=searchers[i].searchForEntities(query,method);
          break;
       }
    }
    return r;
  }
  
}