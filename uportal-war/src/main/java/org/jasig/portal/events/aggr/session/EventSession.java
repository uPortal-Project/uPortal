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

package org.jasig.portal.events.aggr.session;

import java.util.Set;

import org.jasig.portal.events.PortalEvent;
import org.jasig.portal.events.aggr.AggregatedGroupConfig;
import org.jasig.portal.events.aggr.groups.AggregatedGroupMapping;

/**
 * Defines data that is tracked across all events associated with the same {@link PortalEvent#getEventSessionId()}
 * 
 * @author Eric Dalquist
 * @version $Revision$
 */
public interface EventSession {
    
    /**
     * @see PortalEvent#getEventSessionId()
     */
    String getEventSessionId();
    
    /**
     * @return The event store resolved group mappings for the event session, immutable
     */
    Set<AggregatedGroupMapping> getGroupMappings();
    
    /**
     * @return A view of this event session that filters the results of {@link #getGroupMappings()} using {@link AggregatedGroupConfig#isIncluded(AggregatedGroupMapping)}
     */
    EventSession getFilteredEventSession(AggregatedGroupConfig groupConfig);
}
