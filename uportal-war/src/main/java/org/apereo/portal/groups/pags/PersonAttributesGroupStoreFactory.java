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
package org.apereo.portal.groups.pags;

import org.apereo.portal.groups.ComponentGroupServiceDescriptor;
import org.apereo.portal.groups.GroupsException;
import org.apereo.portal.groups.IEntityGroupStore;
import org.apereo.portal.groups.IEntityGroupStoreFactory;

/**
 * Factory class for Person Attributes Group Store
 *
 * @author Al Wold
 * @deprecated JPA (database) PAGS is required for a small but growing list of newer features.
 */
public class PersonAttributesGroupStoreFactory implements IEntityGroupStoreFactory {
  private static IEntityGroupStore groupStore;

  public static synchronized IEntityGroupStore getGroupStore() {
    if (groupStore == null) {
      groupStore = new PersonAttributesGroupStore();
    }
    return groupStore;
  }

  public IEntityGroupStore newGroupStore() throws GroupsException {
    return getGroupStore();
  }

  public IEntityGroupStore newGroupStore(ComponentGroupServiceDescriptor svcDescriptor)
      throws GroupsException {
    return getGroupStore();
  }
}
