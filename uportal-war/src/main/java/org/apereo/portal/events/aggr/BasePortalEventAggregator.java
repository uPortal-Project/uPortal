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
package org.apereo.portal.events.aggr;

import org.apereo.portal.events.PortalEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Base {@link PortalEvent} aggregator, useful for aggregations that extend from {@link
 * BaseAggregationImpl}
 *
 * @param <E> The {@link PortalEvent} type handled by this aggregator
 */
public abstract class BasePortalEventAggregator<E extends PortalEvent>
        implements IPortalEventAggregator<E> {

    protected final Logger logger = LoggerFactory.getLogger(getClass());

    @Override
    public boolean supports(PortalEvent event) {
        return supports(event.getClass());
    }
}
