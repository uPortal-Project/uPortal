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

package org.jasig.portal.portlets.groupadmin;

import org.apache.commons.lang.StringUtils;
import org.jasig.portal.groups.IEntityGroup;
import org.jasig.portal.services.GroupService;
import org.springframework.binding.message.MessageBuilder;
import org.springframework.binding.message.MessageContext;

/**
 * Validates {@link AdHocPagsForm} instances in the group editing submitted from
 * the special UI that exists for managing them..
 *
 * @author Benito J. Gonzalez <bgonzalez@unicon.net>
 * @since   4.3
 */
public final class AdHocPagsFormValidator {

    /**
     * Validate ad hoc groups form.
     * <ul>
     *     <li>Check that the group name is set</li>
     *     <li>Check that the group name is either un-used or is an existing PAGS group name</li>
     *     <li>Check that at least one group has been selected for either
     *         {@code includes} or {@code excludes} list</li>
     * </ul>
     * 
     * @param group        Group information submitted by the user
     * @param context   Validation messages to be sent back to the user
     */
    public void validateEditDetails(AdHocPagsForm group, MessageContext context) {

        // check the group name is set
        if (StringUtils.isBlank(group.getName())) {
            context.addMessage(new MessageBuilder().error().source("name")
                    .code("please.enter.name").build());
        }

        // check that the group name is either available or a PAGS group name
        IEntityGroup entityGroup = GroupService.findGroup(group.getName());
        if (entityGroup != null && !entityGroup.getServiceName().equals("pags")) {
            context.addMessage(new MessageBuilder().error().source("name")
                    .code("please.enter.unique.name").build());
        }

        // check that at least one group has been specified in the includes or excludes lists
        int testGroupCount = group.getIncludes().size() + group.getExcludes().size();
        if (testGroupCount == 0) {
            context.addMessage(new MessageBuilder().error().source("includes")
                    .code("please.choose.at.least.one.group").build());
        }

    }

}
