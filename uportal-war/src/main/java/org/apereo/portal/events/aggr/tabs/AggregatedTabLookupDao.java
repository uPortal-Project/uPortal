/**
 * Licensed to Apereo under one or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information regarding copyright ownership. Apereo
 * licenses this file to you under the Apache License, Version 2.0 (the "License"); you may not use
 * this file except in compliance with the License. You may obtain a copy of the License at the
 * following location:
 *
 * <p>http://www.apache.org/licenses/LICENSE-2.0
 *
 * <p>Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apereo.portal.events.aggr.tabs;

import java.util.Set;

/**
 * Used to map portal group keys to a more static group mapping object
 *
 */
public interface AggregatedTabLookupDao {

    /** Get the tab mapping object for the specified tab mapping id */
    AggregatedTabMapping getTabMapping(long tabMappingId);

    /** Get the tab mapping object for the specified layout node id */
    AggregatedTabMapping getMappedTabForLayoutId(String layoutNodeId);

    /** Get the tab mapping for the specified tab name */
    AggregatedTabMapping getTabMapping(String fragmentName, String tabName);

    /** All tabs that have aggregated data */
    Set<AggregatedTabMapping> getTabMappings();
}
