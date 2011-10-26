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

package org.jasig.portal.permission;

/**
 * IPermissionActivity represents an activity for which permissions may be 
 * assigned under some permission owner.  Examples of activities might include
 * the ability to subscribe to a portlet, or the ability to manage a group's
 * members. IPermissionActivity represents the abstract permission itself and
 * does not contain any permission assignment information.
 * 
 * @author Jen Bourey, jbourey@unicon.net
 * @version $Revision$
 * @since 3.3
 */
public interface IPermissionActivity extends Comparable<IPermissionActivity> {

    public Long getId();
    
    /**
     * Get the unique, unchanging functional name for this permission activity.
     * This identifier should not change over time and should consist of 
     * a short, meaningful string.
     * 
     * @return
     */
    public String getFname();
    
    /**
     * Set the functional name for this permission activity.
     * 
     * @param fname
     */
    public void setFname(String fname);
    
    /**
     * Get the human-readable name for this activity.
     * 
     * @return
     */
    public String getName();
    
    /**
     * Set the human-readable name for this activity.
     * 
     * @return
     */
    public void setName(String name);
    
    /**
     * Get the description of this activity.
     * 
     * @return
     */
    public String getDescription();
    
    /**
     * Set the description of this activity.
     * 
     * @param description
     */
    public void setDescription(String description);
    
    /**
     * Get the key for the IPermissionTargetProvider implementation associated with this
     * activity.  The target provider key is a string that may be used to 
     * retrieve an IPermissionTargetProvider instance from an IPermissionTargetProvider registry.
     * This target provider should provide information about valid targets
     * for this permission activity.
     * 
     * @return
     */
    public String getTargetProviderKey();

    /**
     * Set the key of the IPermissionTargetProvider implementation associated with this
     * activity.
     * 
     * @param targetProviderKey
     */
    public void setTargetProviderKey(String targetProviderKey);

}
