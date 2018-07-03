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
package org.apereo.portal.groups.grouper;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apereo.portal.groups.GroupsException;
import org.apereo.portal.groups.IEntitySearcher;
import org.apereo.portal.groups.IEntitySearcherFactory;

/** Returns an instance of the Grouper entity searcher. */
public class GrouperEntitySearcherFactory implements IEntitySearcherFactory {

    /** The logger to use. */
    protected final Log LOGGER = LogFactory.getLog(GrouperEntitySearcherFactory.class);

    /**
     * Creates an instance of EntitySearcher.
     *
     * @return The instance.
     * @see IEntitySearcherFactory#newEntitySearcher()
     */
    @Override
    public IEntitySearcher newEntitySearcher() throws GroupsException {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Creating New Grouper GrouperEntitySearcherFactory");
        }
        return (IEntitySearcher) new GrouperEntityGroupStoreFactory().newGroupStore();
    }
}
