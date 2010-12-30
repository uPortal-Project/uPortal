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

package org.jasig.portal.groups.local.searchers;

import java.util.List;
import java.util.ListIterator;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jasig.portal.EntityIdentifier;
import org.jasig.portal.IBasicEntity;
import org.jasig.portal.groups.GroupsException;
import org.jasig.portal.groups.local.ITypedEntitySearcher;
import org.jasig.portal.portlet.om.IPortletDefinition;
import org.jasig.portal.portlet.registry.IPortletDefinitionRegistry;
import org.jasig.portal.spring.locator.PortletDefinitionRegistryLocator;

/**
 * Searches the portal DB for channels. Used by EntitySearcherImpl
 * 
 * @author Alex Vigdor
 * @version $Revision$
 */

public class RDBMChannelDefSearcher implements ITypedEntitySearcher {
	private static final Log log = LogFactory
			.getLog(RDBMChannelDefSearcher.class);

	public RDBMChannelDefSearcher() {
	}

	public EntityIdentifier[] searchForEntities(String query, int method)
			throws GroupsException {
		IPortletDefinitionRegistry registry = PortletDefinitionRegistryLocator
				.getPortletDefinitionRegistry();
		boolean allowPartial = true;

		switch (method) {
			case IS:
				allowPartial = false;
				break;
			case STARTS_WITH:
				query = query + "%";
				break;
			case ENDS_WITH:
				query = "%" + query;
				break;
			case CONTAINS:
				query = "%" + query + "%";
				break;
			default:
				throw new GroupsException("Unknown search type");
		}

		// get the list of matching portlet definitions
		List<IPortletDefinition> definitions = registry.searchForPortlets(query,
				allowPartial);
		if (log.isDebugEnabled()) {
			log.debug("Found " + definitions.size() + " matching definitions for query " + query);
		}

		// initialize an appropriately-sized array of EntityIdentifiers
		EntityIdentifier[] identifiers = new EntityIdentifier[definitions
				.size()];

		// add an identifier for each matching portlet
		for (ListIterator<IPortletDefinition> defIter = definitions
				.listIterator(); defIter.hasNext();) {
			IPortletDefinition definition = defIter.next();
			identifiers[defIter.previousIndex()] = new EntityIdentifier(
					definition.getPortletDefinitionId().getStringId(),
					getType());
		}

		return identifiers;

	}

	@Override
	public Class<? extends IBasicEntity> getType() {
		return IPortletDefinition.class;
	}

}