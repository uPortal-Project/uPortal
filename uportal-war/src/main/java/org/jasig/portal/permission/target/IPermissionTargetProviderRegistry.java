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

/**
 * IPermissionTargetProviderRegistry provides a registry of target provider
 * instances.  This registry can be used to retrieve provider instances
 * associated with specified provider keys.
 * 
 * @author Jen Bourey, jbourey@unicon.net
 * @version $Revision$
 * @since 3.3
 */
public interface IPermissionTargetProviderRegistry {
    
    /**
     * Register a new permission target provider under the specified key.
     * 
     * @param key
     * @param provider
     */
    public void registerTargetProvider(String key, IPermissionTargetProvider provider);
    
    /**
     * Retrieve a permission target provider instance for the given key.  If
     * no provider can be located matching the given key, this method will
     * return <code>null</code>. 
     * 
     * @param key
     * @return
     */
    public IPermissionTargetProvider getTargetProvider(String key);

    /**
     * Get the collection of all registered target providers.
     * 
     * @return
     */
    public Collection<IPermissionTargetProvider> getTargetProviders();

}
