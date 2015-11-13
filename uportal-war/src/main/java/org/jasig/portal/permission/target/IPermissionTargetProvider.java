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
 * IPermissionTargetProvider provides an interface for retrieving and validating
 * potential targets for a permission activity.
 * 
 * @author Jen Bourey, jbourey@unicon.net
 * @version $Revision$
 * @since 3.3
 */
public interface IPermissionTargetProvider {
    
    /**
     * Return the permission target associated with the specified key under
     * this provider.  If no target with the given key is valid for this 
     * provider, return <code>null</code>. 
     * 
     * @param key
     * @return
     */
    public IPermissionTarget getTarget(String key);
    
    /**
     * Search this provider for a particular target using a single string
     * search term.  Each target provider implementation is responsible for
     * determining the definition a "matching" target.
     * 
     * @param term  search term
     * @return      collection of matching targets
     */
    public Collection<IPermissionTarget> searchTargets(String term);

}
