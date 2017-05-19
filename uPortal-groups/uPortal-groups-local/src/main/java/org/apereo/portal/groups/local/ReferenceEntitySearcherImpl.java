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
package org.apereo.portal.groups.local;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import org.apereo.portal.EntityIdentifier;
import org.apereo.portal.groups.GroupsException;
import org.apereo.portal.groups.IEntitySearcher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * An IEntitySearcher implementation for the local portal group service. Uses implementations of
 * ITypedEntitySearcher to do the dirty work.
 *
 */
@Service("referenceEntitySearcher")
public class ReferenceEntitySearcherImpl implements IEntitySearcher {
    private List<ITypedEntitySearcher> typedEntitySearchers;

    @Autowired
    public void setTypedEntitySearchers(List<ITypedEntitySearcher> typedEntitySearchers) {
        this.typedEntitySearchers = typedEntitySearchers;
    }

    @Override
    public EntityIdentifier[] searchForEntities(String query, int method, Class type)
            throws GroupsException {
        final List<EntityIdentifier> entityIdentifiers = new LinkedList<EntityIdentifier>();

        for (final ITypedEntitySearcher typedEntitySearcher : this.typedEntitySearchers) {
            if (typedEntitySearcher.getType().equals(type)) {
                final EntityIdentifier[] results =
                        typedEntitySearcher.searchForEntities(query, method);
                Collections.addAll(entityIdentifiers, results);
            }
        }

        return entityIdentifiers.toArray(new EntityIdentifier[entityIdentifiers.size()]);
    }
}
