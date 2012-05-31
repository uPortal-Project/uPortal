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

package org.jasig.portal.events.aggr.tabrender;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.sf.ehcache.Ehcache;
import net.sf.ehcache.Element;

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
 * Event aggregator that uses {@link TabRenderAggregationPrivateDao} to aggregate tab renders 
 * 
 * @author Eric Dalquist
 * @version $Revision$
 */
public class TabRenderAggregator extends BasePortalEventAggregator<PortalRenderEvent, TabRenderAggregationImpl, TabRenderAggregationKey> {
    private TabRenderAggregationPrivateDao tabRenderAggregationDao;
    private JdbcOperations portalJdbcOperations;
    private Ehcache layoutNodeIdNameResolutionCache;
    
    @Autowired
    @Qualifier("org.jasig.portal.events.aggr.tabrender.TabRenderAggregator.layoutNodeIdNameResolver")
    public void setLayoutNodeIdNameResolutionCache(Ehcache layoutNodeIdNameResolutionCache) {
        this.layoutNodeIdNameResolutionCache = layoutNodeIdNameResolutionCache;
    }

    @Autowired
    @Qualifier(BasePortalJpaDao.PERSISTENCE_UNIT_NAME)
    public void setPortalJdbcOperations(JdbcOperations portalJdbcOperations) {
        this.portalJdbcOperations = portalJdbcOperations;
    }

    @Autowired
    public void setTabRenderAggregationDao(TabRenderAggregationPrivateDao tabRenderAggregationDao) {
        this.tabRenderAggregationDao = tabRenderAggregationDao;
    }

    @Override
    public boolean supports(Class<? extends PortalEvent> type) {
        return PortalRenderEvent.class.isAssignableFrom(type);
    }

    @Override
    protected BaseAggregationPrivateDao<TabRenderAggregationImpl, TabRenderAggregationKey> getAggregationDao() {
        return this.tabRenderAggregationDao;
    }

    @Override
    protected void updateAggregation(PortalRenderEvent e, AggregationIntervalInfo intervalInfo, TabRenderAggregationImpl aggregation) {
        final long executionTime = e.getExecutionTimeNano();
        final int duration = intervalInfo.getDurationTo(e.getTimestampAsDate());
        aggregation.setDuration(duration);
        aggregation.addValue(executionTime);
    }
    
    private final Pattern DLM_NODE = Pattern.compile("^u(\\d+)l(\\d+)s(\\d+)$");

    @Override
    protected TabRenderAggregationKey createAggregationKey(AggregationIntervalInfo intervalInfo,
            AggregatedGroupMapping aggregatedGroup, PortalRenderEvent event) {
        
        final TimeDimension timeDimension = intervalInfo.getTimeDimension();
        final DateDimension dateDimension = intervalInfo.getDateDimension();
        final AggregationInterval aggregationInterval = intervalInfo.getAggregationInterval();
        
        final String targetedLayoutNodeId = event.getTargetedLayoutNodeId();
        final String tabName = resolveTabName(targetedLayoutNodeId);
        
        return new TabRenderAggregationKeyImpl(dateDimension, timeDimension, aggregationInterval, aggregatedGroup, tabName);
    }

    protected final String resolveTabName(final String targetedLayoutNodeId) {
        //Check the cache first
        final Element element = layoutNodeIdNameResolutionCache.get(targetedLayoutNodeId);
        if (element != null) {
            return (String)element.getValue();
        }
        
        final String tabName;
        if (targetedLayoutNodeId == null) {
            //No layout node id, return null placeholder
            tabName = TabRenderAggregationKey.NO_TAB_NAME;
        }
        else {
            final Matcher nodeIdMatcher = DLM_NODE.matcher(targetedLayoutNodeId);
            if (nodeIdMatcher.matches()) {
                final int userId = Integer.parseInt(nodeIdMatcher.group(1));
                final int layoutId = Integer.parseInt(nodeIdMatcher.group(2));
                final int nodeId = Integer.parseInt(nodeIdMatcher.group(3));
                
                final List<String> result = this.portalJdbcOperations.queryForList(
                        "SELECT NAME FROM UP_LAYOUT_STRUCT where USER_ID = ? AND LAYOUT_ID = ? AND STRUCT_ID = ?", 
                        String.class, 
                        userId, layoutId, nodeId);
                
                if (result.isEmpty()) {
                    //No tab name found, fall back to using the bare layout node id
                    tabName = targetedLayoutNodeId;
                }
                else {
                    //Use the found tab name
                    tabName = result.iterator().next();
                }
            }
            else {
                //Node isn't from DLM return personal placeholder
                tabName = TabRenderAggregationKey.PERSONAL_TAB_NAME;
            }
        }

        //cache the resolution
        layoutNodeIdNameResolutionCache.put(new Element(targetedLayoutNodeId, tabName));
        
        return tabName;
    }
}
