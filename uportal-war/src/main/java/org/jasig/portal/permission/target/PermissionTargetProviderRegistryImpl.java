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

package org.jasig.portal.permission.target;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * PermissionTargetProviderRegistryImpl provides the default implementation
 * of the permission target provider registry interface.  This implementation
 * is a simple in-memory map.
 * 
 * TODO: We still need to add Spring auto-wiring friendliness.
 * 
 * @author Jen Bourey, jbourey@unicon.net
 * @version $Revision$
 * @since 3.3
 */
public class PermissionTargetProviderRegistryImpl implements IPermissionTargetProviderRegistry {
    
    private Map<String, IPermissionTargetProvider> providers = new HashMap<String, IPermissionTargetProvider>();

    /**
     * Default constructor
     */
    public PermissionTargetProviderRegistryImpl() { }
    
    /**
     * Construct a new target provider registry and initialize it with the 
     * supplied map of key -> provider pairs.
     * 
     * @param providers
     */
    public void setProviders(Map<String, IPermissionTargetProvider> providers) {
        this.providers.clear();
        for (Map.Entry<String, IPermissionTargetProvider> provider : providers.entrySet()) {
            this.providers.put(provider.getKey(), provider.getValue());
        }
    }
    
    /*
     * (non-Javadoc)
     * @see org.jasig.portal.permission.target.IPermissionTargetProviderRegistry#getTargetProvider(java.lang.String)
     */
    public IPermissionTargetProvider getTargetProvider(String key) {
        return providers.get(key);
    }

    /*
     * (non-Javadoc)
     * @see org.jasig.portal.permission.target.IPermissionTargetProviderRegistry#registerTargetProvider(java.lang.String, org.jasig.portal.permission.target.IPermissionTargetProvider)
     */
    public void registerTargetProvider(String key, IPermissionTargetProvider provider) {
        this.providers.put(key, provider);
    }

    /*
     * (non-Javadoc)
     * @see org.jasig.portal.permission.target.IPermissionTargetProviderRegistry#getTargetProviders()
     */
    public Collection<IPermissionTargetProvider> getTargetProviders() {
        return this.providers.values();
    }

}
