/**
 * Licensed to Apereo under one or more contributor license
 * agreements. See the NOTICE file distributed with this work
 * for additional information regarding copyright ownership.
 * Apereo licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License.  You may obtain a
 * copy of the License at the following location:
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.jasig.portal.security.provider;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jasig.portal.security.IPerson;
import org.jasig.portal.security.PersonFactory;
import org.jasig.portal.security.PortalSecurityException;
import org.jasig.services.persondir.IPersonAttributes;
import org.jasig.services.persondir.support.merger.IAttributeMerger;
import org.jasig.services.persondir.support.merger.MultivaluedAttributeMerger;

/**
 * Manages the storage of an IPerson object in a user's session, and add request attributes to the user guest.
 * @author GIP RECIA - Julien Gribonvald
 * 23 avr. 2013
 */
public class ExtendedPersonManager extends AbstractPersonManager {

	/** Logger. */
	private static final Log log = LogFactory.getLog(ExtendedPersonManager.class);

	/**
	 * Strategy for merging together the results from successive PersonAttributeDaos.
	 */
	protected IAttributeMerger merger = new MultivaluedAttributeMerger();

	/**
	 * Attributes to add to user guest from a descriptor.
	 */
	private IPersonAttributes descriptors;

	/**
	 * Retrieve an IPerson object for the incoming request
	 * @param request the servlet request object
	 * @return the IPerson object for the incoming request
	 * @throws PortalSecurityException
	 */
	public IPerson getPerson (HttpServletRequest request) throws PortalSecurityException {
		HttpSession session = request.getSession(false);
		IPerson person = null;
		// Return the person object if it exists in the user's session
		if (session != null)
			person = (IPerson)session.getAttribute(PERSON_SESSION_KEY);
		if (person == null) {
			try {
				// Create a guest person
				person = PersonFactory.createGuestPerson();
				merger.mergeAttributes(person.getAttributeMap(), descriptors.getAttributes());
			} catch (Exception e) {
				// Log the exception
				log.error("Exception creating guest person.", e);
			}
			// Add this person object to the user's session
			if (person != null && session != null)
				session.setAttribute(PERSON_SESSION_KEY, person);
		}
		return person;
	}

	/**
	 * Getter of member merger.
	 * @return <code>IAttributeMerger</code> the attribute merger
	 */
	public IAttributeMerger getMerger() {
		return merger;
	}

	/**
	 * Setter of attribute merger.
	 * @param merger the attribute merger to set
	 */
	public void setMerger(final IAttributeMerger merger) {
		this.merger = merger;
	}

	/**
	 * Getter of member descriptors.
	 * @return <code>IPersonAttributes</code> the attribute descriptors
	 */
	public IPersonAttributes getDescriptors() {
		return descriptors;
	}

	/**
	 * Setter of attribute descriptors.
	 * @param descriptors the attribute descriptors to set
	 */
	public void setDescriptors(final IPersonAttributes descriptors) {
		this.descriptors = descriptors;
	}

}



