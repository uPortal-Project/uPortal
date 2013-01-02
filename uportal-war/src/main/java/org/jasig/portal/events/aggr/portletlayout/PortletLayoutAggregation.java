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
package org.jasig.portal.events.aggr.portletlayout;

import org.jasig.portal.events.aggr.BaseAggregation;
import org.jasig.portal.events.aggr.portlets.AggregatedPortletMapping;

/**
 * Tracks portlet layout change events
 *
 * @author Chris Waymire <cwaymire@unicon.net>
 */
public interface PortletLayoutAggregation extends BaseAggregation<PortletLayoutAggregationKey,PortletLayoutAggregationDiscriminator> {

    /**
     * @return Number of times the portlet was added to a layout
     */
    int getAddCount();

    /**
     * @return Number of times the portlet was removed from a layout
     */
    int getDeleteCount();

    /**
     * @return Number of times the portlet was moved to a layout
     */
    int getMoveCount();

    /**
     * @return The name of the portlet
     */
    AggregatedPortletMapping getPortletMapping();
}
