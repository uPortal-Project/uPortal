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

package org.jasig.portal.events.aggr.tab;

import org.jasig.portal.events.PortalEvent;
import org.jasig.portal.events.PortalRenderEvent;
import org.jasig.portal.events.aggr.AggregationInterval;
import org.jasig.portal.events.aggr.AggregationIntervalInfo;
import org.jasig.portal.events.aggr.BaseAggregationPrivateDao;
import org.jasig.portal.events.aggr.BasePortalEventAggregator;
import org.jasig.portal.events.aggr.DateDimension;
import org.jasig.portal.events.aggr.TimeDimension;
import org.jasig.portal.events.aggr.groups.AggregatedGroupMapping;
import org.jasig.portal.jpa.BasePortalJpaDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcOperations;

/**
 * Event aggregator that uses {@link TabRenderAggregationPrivateDao} to aggregate concurrent user data 
 * 
 * @author Eric Dalquist
 * @version $Revision$
 */
public class TabRenderAggregator extends BasePortalEventAggregator<PortalRenderEvent, TabRenderAggregationImpl, TabRenderAggregationKey> {
    private TabRenderAggregationPrivateDao concurrentUserAggregationDao;
    private JdbcOperations portalJdbcOperations;
    
    @Autowired
    @Qualifier(BasePortalJpaDao.PERSISTENCE_UNIT_NAME)
    public void setPortalJdbcOperations(JdbcOperations portalJdbcOperations) {
        this.portalJdbcOperations = portalJdbcOperations;
    }

    @Autowired
    public void setConcurrentUserAggregationDao(TabRenderAggregationPrivateDao concurrentUserAggregationDao) {
        this.concurrentUserAggregationDao = concurrentUserAggregationDao;
    }

    @Override
    public boolean supports(Class<? extends PortalEvent> type) {
        return PortalRenderEvent.class.isAssignableFrom(type);
    }

    @Override
    protected BaseAggregationPrivateDao<TabRenderAggregationImpl, TabRenderAggregationKey> getAggregationDao() {
        return this.concurrentUserAggregationDao;
    }

    @Override
    protected void updateAggregation(PortalRenderEvent e, AggregationIntervalInfo intervalInfo, TabRenderAggregationImpl aggregation) {
        final long executionTime = e.getExecutionTime();
        final int duration = intervalInfo.getDurationTo(e.getTimestampAsDate());
        aggregation.setDuration(duration);
        aggregation.countRender(executionTime);
    }

    @Override
    protected TabRenderAggregationKey createAggregationKey(AggregationIntervalInfo intervalInfo,
            AggregatedGroupMapping aggregatedGroup, PortalRenderEvent event) {
        
        final TimeDimension timeDimension = intervalInfo.getTimeDimension();
        final DateDimension dateDimension = intervalInfo.getDateDimension();
        final AggregationInterval aggregationInterval = intervalInfo.getAggregationInterval();
        
        //TODO resolve tab name
        final String targetedLayoutNodeId = event.getTargetedLayoutNodeId();
        
//        this.portalJdbcOperations.se
        
        return new TabRenderAggregationKeyImpl(dateDimension, timeDimension, aggregationInterval, aggregatedGroup, targetedLayoutNodeId);
    }
}
