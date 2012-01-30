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

package org.jasig.portal.events.aggr.groups;

/**
 * Used to map portal group keys to a more static group mapping object
 * 
 * @author Eric Dalquist
 * @version $Revision$
 */
public interface AggregatedGroupLookupDao {
    /**
     * Get the group mapping object for the specified group key
     */
    public AggregatedGroupMapping getGroupMapping(String portalGroupKey);
    
    /**
     * Get the group mapping for the specified service and name
     */
    public AggregatedGroupMapping getGroupMapping(String groupService, String groupName);
}
