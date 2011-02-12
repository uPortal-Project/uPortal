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

import java.util.Set;

/**
 * IPermissionOwner represents the owner of a set of permission activities.  
 * Permission owners in uPortal may be thought of as a kind of category of
 * permissions or as a system which owns a set of permissions that are unrelated
 * to those of other permission owners.
 * 
 * Examples of permission owners might include a subsystem in the portal such
 * as the set of permissions for subscribing to uPortal portlets or a portlet
 * running outside the portal's application context.
 * 
 * Each permission owner registers a set of permission "activities", which 
 * enumerate the permission activities the owner expects to manage.
 * 
 * The IPermissionOwner interface replaces the IPermissible interface as of
 * the uPortal 3.3 release.
 * 
 * @author Jen Bourey, jbourey@unicon.net
 * @revision $Revision$
 * @since 3.3
 */
public interface IPermissionOwner {
	
    /**
     * Get the unique numerical identifier for this permission owner.  While
     * unique, this identifier should not be expected to be constant over time
     * and may change between uPortal versions due to import/export operations.
     * 
     * @return
     */
	public Long getId();

	/**
	 * Get the unique, unchanging functional name for this permission owner.
	 * This identifier should not change over time and should consist of 
	 * a short, meaningful string.
	 * 
	 * @return
	 */
	public String getFname();

	/**
	 * Set the functional name for this permission owner.
	 * 
	 * @param fname
	 */
	public void setFname(String fname);

	/**
	 * Get the human-readable name for this permission owner.
	 * 
	 * @return
	 */
	public String getName();

	/**
	 * Set the human-readable name of this permission owner.
	 * 
	 * @param name
	 */
	public void setName(String name);

	/**
	 * Get the description of this permission owner.
	 * 
	 * @return
	 */
	public String getDescription();

	/**
	 * Set the description of this permission owner.
	 * 
	 * @param description
	 */
	public void setDescription(String description);
	
	/**
	 * Get the set of permission activities associated with this permission
	 * owner.
	 * 
	 * @return
	 */
	public Set<IPermissionActivity> getActivities();
	
	/**
	 * Set the set of permission activities to be associated with this
	 * permission owner.
	 * 
	 * @param activities
	 */
	public void setActivities(Set<IPermissionActivity> activities);
	
}
