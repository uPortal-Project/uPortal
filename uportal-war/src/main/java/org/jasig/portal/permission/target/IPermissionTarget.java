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

/**
 * IPermissionTarget represents a valid target for a permission.  Examples
 * of permission targets might include a uPortal group or a static string.
 * 
 * @author Jen Bourey, jbourey@unicon.net
 * @version $Revision$
 * @since 3.3
 */
public interface IPermissionTarget {
    
    /**
     * Get the key of this permission target.  This key
     * will be used as the actual target string of a permission assignment.
     * 
     * @return
     */
    public String getKey();
    
    /**
     * Get the human-readable name of this permission target.
     * 
     * @return
     */
    public String getName();
    
}
