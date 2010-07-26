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

import java.util.HashMap;
import java.util.Map;

import org.jasig.portal.services.GroupService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


/**
 * Reference implementation of <code>IEntityNameFinder</code> for <code>IEntityGroup</code>.
 * @author Alex Vigdor
 * @version $Revision$
 */
public class EntityGroupNameFinder
        implements IEntityNameFinder {
    private static final Log log = LogFactory.getLog(EntityGroupNameFinder.class);
    private static IEntityNameFinder _instance = null;
    private Class type = null;

    protected EntityGroupNameFinder () {
        try {
            type = Class.forName("org.jasig.portal.groups.IEntityGroup");
        } catch (Exception e) {
            log.error(e, e);
        }
    }

    public synchronized static IEntityNameFinder singleton () {
        if (_instance == null) {
            _instance = new EntityGroupNameFinder();
        }
        return  _instance;
    }

    /**
     * Given the key, returns the entity's name.
     * @param key java.lang.String
     */
    public String getName (String key) throws Exception {
        IEntityGroup g = GroupService.findGroup(key);
        return  g.getName();
    }

    /**
     * Given an array of keys, returns the names of the entities.
     * @param keys java.lang.String[]
     */
    public Map getNames (String[] keys) throws Exception {
        HashMap names = new HashMap();
        for (int i = 0; i < keys.length; i++) {
            names.put(keys[i], getName(keys[i]));
        }
        return  names;
    }

    /**
     * Returns the entity type for this <code>IEntityFinder</code>.
     * @return java.lang.Class
     */
    public Class getType () {
        return  type;
    }
}



