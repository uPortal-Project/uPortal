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
package org.apereo.portal.events.tincan;

import java.util.List;
import javax.annotation.Resource;
import org.apereo.portal.events.PortalEvent;
import org.apereo.portal.events.aggr.BasePortalEventAggregator;
import org.apereo.portal.events.aggr.SimplePortalEventAggregator;
import org.apereo.portal.events.aggr.session.EventSession;
import org.apereo.portal.events.tincan.om.LrsStatement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;

/**
 * Event aggregator that processes events and converts them to xAPI events.
 *
 */
public class TinCanPortalEventAggregator extends BasePortalEventAggregator<PortalEvent>
        implements SimplePortalEventAggregator<PortalEvent> {
    private static final Logger log = LoggerFactory.getLogger(TinCanPortalEventAggregator.class);
    private ITinCanEventScheduler tinCanEventScheduler;
    private List<IPortalEventToLrsStatementConverter> statementFactories;
    private boolean tinCanAPIEnabled = false;

    @Resource(name = "tinCanEventConverters")
    public void setStatementFactories(
            final List<IPortalEventToLrsStatementConverter> statementFactories) {
        this.statementFactories = statementFactories;
    }

    @Autowired
    @Qualifier("tinCanEventScheduler")
    public void setTinCanEventScheduler(final ITinCanEventScheduler tinCanEventScheduler) {
        this.tinCanEventScheduler = tinCanEventScheduler;
    }

    @Value("${org.apereo.portal.tincan-api.enabled:false}")
    public void setTinCanAPIEnabled(final boolean enabled) {
        this.tinCanAPIEnabled = enabled;
    }

    @Override
    public boolean supports(Class<? extends PortalEvent> type) {
        return true;
    }

    @Override
    public void aggregateEvent(PortalEvent e, EventSession eventSession) {
        if (!tinCanAPIEnabled) {
            return;
        }

        try {
            for (IPortalEventToLrsStatementConverter factory : statementFactories) {
                if (factory.supports(e)) {
                    LrsStatement lrsStatement = factory.toLrsStatement(e);
                    if (lrsStatement != null) {
                        tinCanEventScheduler.scheduleEvent(lrsStatement);
                        // for now, assume a max of 1 handler per event.
                        break;
                    }
                }
            }
        } catch (Exception ex) {
            log.error("Error sending xAPI event", ex);
        }
    }
}
