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

package org.jasig.portal.portlets.groupadmin;

import org.apache.commons.lang.StringUtils;
import org.springframework.binding.message.MessageBuilder;
import org.springframework.binding.message.MessageContext;

/**
 * GroupFormValidator validates GroupForm objects in the group editing
 * Spring Web Flow.
 * 
 * @author Jen Bourey, jbourey@unicon.net
 * @version $Revision$
 */
public class GroupFormValidator {

	/**
	 * Validate the detail editing group view
	 * 
	 * @param group	
	 * @param context   
	 */
	public void validateEditDetails(GroupForm group, MessageContext context) {
		
		// ensure the group name is set
		if(StringUtils.isBlank(group.getName())) {
			context.addMessage(new MessageBuilder().error().source("name")
					.code("please.enter.name").build());
		}
		
	}

}
