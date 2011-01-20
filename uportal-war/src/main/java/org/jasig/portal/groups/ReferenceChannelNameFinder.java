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

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jasig.portal.IBasicEntity;
import org.jasig.portal.portlet.om.IPortletDefinition;
import org.jasig.portal.portlet.registry.IPortletDefinitionRegistry;
import org.jasig.portal.spring.locator.PortletDefinitionRegistryLocator;

/**
 * Reference implementation of <code>IEntityNameFinder</code> for <code>Channels</code>.
 * @author Alex Vigdor
 * @version $Revision$
 */
public class ReferenceChannelNameFinder
        implements IEntityNameFinder {
    
    private static final Log log = LogFactory.getLog(ReferenceChannelNameFinder.class);
    
    private static IEntityNameFinder _instance = null;
    private final Class<? extends IBasicEntity> type = IPortletDefinition.class;

    protected ReferenceChannelNameFinder () {
    }

    public static synchronized IEntityNameFinder singleton () {
        if (_instance == null) {
            _instance = new ReferenceChannelNameFinder();
        }
        return  _instance;
    }

    /**
     * Given the key, returns the entity's name.
     * @param key java.lang.String
     */
    public String getName (String key) throws Exception {
    	IPortletDefinitionRegistry registry = PortletDefinitionRegistryLocator.getPortletDefinitionRegistry();
    	
        IPortletDefinition portletDefinition;
    	if (StringUtils.isNumeric(key)) {
            portletDefinition = registry.getPortletDefinition(key);
    	} else {
            portletDefinition = registry.getPortletDefinition(key.split("\\.")[1]);
    	    
    	}
        return  portletDefinition.getName();
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



