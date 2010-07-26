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

package org.jasig.portal.layout.dlm;

import org.jasig.portal.PortalException;

/**
 * Represents a layout processing action that should be applied to a user's
 * layout and contains the ability to apply that specific action. Node changes
 * that need to be made to the ILF and PLF of a user must not be made until all
 * changes have been identified and reviewed to see if they are allowed by an
 * owning fragment and additionally in the case of channels by the channel
 * definition. After all such actions have been identified and approved then
 * each implementation of this interface applies its changes in an appropriate
 * manner based on whether the node is incorporated from a fragment or owned by
 * the user.
 * 
 * @author mboyd@sungardsct.com
 */
public interface ILayoutProcessingAction
{
    public void perform() throws PortalException;
}
