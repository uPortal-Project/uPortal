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

/**
 * 
 */
package org.jasig.portal.layout;

import javax.sql.DataSource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jasig.portal.IUserIdentityStore;
import org.jasig.portal.IUserProfile;
import org.jasig.portal.PortalException;
import org.jasig.portal.security.PersonFactory;
import org.jasig.portal.security.provider.RestrictedPerson;
import org.jasig.portal.spring.locator.UserLayoutStoreLocator;
import org.jasig.services.persondir.IPersonAttributes;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.simple.SimpleJdbcDaoSupport;

/**
 * Helper class for reset-layout based web flows.
 * 
 * Depends on uPortal's {@link DataSource}.
 * 
 * @author Nicholas Blair, npblair@wisc.edu
 * @author Susan Bramhall, susan.bramhall@yale.edu
 *
 */
public class UserLayoutHelperImpl extends SimpleJdbcDaoSupport implements IUserLayoutHelper {

	protected static final String DEFAULT_LAYOUT_FNAME = "default";
	
	protected final Log logger = LogFactory.getLog(this.getClass());
	private IUserIdentityStore userIdentityStore;

	/**
	 * @param userIdentityStore the userIdentityStore to set
	 */
	@Autowired
	public void setUserIdentityStore(IUserIdentityStore userIdentityStore) {
		this.userIdentityStore = userIdentityStore;
	}

	/**
	 * @param personAttributes
	 * @see org.jasig.portal.layout.IUserLayoutHelper#resetUserLayout(org.jasig.services.persondir.IPersonAttributes)
	 */
	public void resetUserLayout(final IPersonAttributes personAttributes) { 
		// Create an empty RestrictedPerson object
		RestrictedPerson person = PersonFactory.createRestrictedPerson();       

		// populate the person with the supplied attributes
		person.setAttributes(personAttributes.getAttributes());

		// get the integer uid into the person object without creating any new person data       
		int uid = userIdentityStore.getPortalUID( person, false );
		person.setID(uid);

		IUserLayoutStore userLayoutStore = UserLayoutStoreLocator.getUserLayoutStore();
		try {
			// determine user profile            
			IUserProfile userProfile = userLayoutStore.getUserProfileByFname(person, DEFAULT_LAYOUT_FNAME);

			// Finally set the layout id to 0.  This orphans the existing layout but it will be replaced by the default 
			// when the user logs in
			userProfile.setLayoutId(0);            

			// persist the change
			userLayoutStore.updateUserProfile(person, userProfile);
			logger.info("resetUserLayout complete for " + person);
		} catch (Exception e) {
			final String msg = "Exception caught during resetUserLayout for " + person;
			logger.error(msg, e);
			throw new PortalException(msg, e);
		}

	}
}
