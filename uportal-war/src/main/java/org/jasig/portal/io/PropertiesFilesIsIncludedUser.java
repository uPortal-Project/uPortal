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

package org.jasig.portal.io;

import java.util.Map;
import java.util.Set;

import org.jasig.portal.layout.IUserLayoutStore;
import org.springframework.beans.factory.annotation.Autowired;

import com.google.common.base.Function;

/**
 * Checks if the username is contained in the set.
 * 
 * Users that are fragment owners as determined by {@link IUserLayoutStore#isFragmentOwner(String)} are also included
 * 
 * @author Eric Dalquist
 * @version $Revision$
 */
public class PropertiesFilesIsIncludedUser implements Function<String, Boolean> {
	//userLayoutStore.isFragmentOwner
	private IUserLayoutStore userLayoutStore;
	private Set<String> userNames;
	private boolean allUsers = false;

	@Autowired
	public void setUserLayoutStore(IUserLayoutStore userLayoutStore) {
		this.userLayoutStore = userLayoutStore;
	}

	/**
	 * @param userNames the userNames to set
	 */
	public void setUserNames(Set<String> userNames) {
		this.userNames = userNames;
		this.allUsers = this.userNames.contains("*");
	}
	
	/**
	 * @param userNames just the {@link Map#keySet()} is used.
	 */
	public void setUserNamesMap(Map<String, Object> userNames) {
		this.userNames = userNames.keySet();
		this.allUsers = this.userNames.contains("*");
	}

	/* (non-Javadoc)
	 * @see com.google.common.base.Function#apply(java.lang.Object)
	 */
	@Override
	public Boolean apply(String username) {
		if (this.allUsers) {
			return true;
		}
		
		if (userLayoutStore.isFragmentOwner(username)) {
			return true;
		}
		
		return this.userNames.contains(username);
	}
}
