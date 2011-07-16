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

package  org.jasig.portal.groups;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


/**
 * Factory for creating <code>EntityGroupNameFinder</code>.
 * @author Alex Vigdor
 * @version $Revision$
 */
public class EntityGroupNameFinderFactory
        implements IEntityNameFinderFactory {
    private static final Log log = LogFactory.getLog(EntityGroupNameFinderFactory.class);
    public EntityGroupNameFinderFactory () {
    }

    public IEntityNameFinder newFinder () throws GroupsException {
        try {
            return  EntityGroupNameFinder.singleton();
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw new GroupsException(e);
        }
    }
}



