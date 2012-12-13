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
package org.jasig.portal.portlets.statistics;

import com.google.visualization.datasource.base.TypeMismatchException;
import com.google.visualization.datasource.datatable.ColumnDescription;
import com.google.visualization.datasource.datatable.value.NumberValue;
import com.google.visualization.datasource.datatable.value.Value;
import com.google.visualization.datasource.datatable.value.ValueType;
import org.jasig.portal.events.aggr.AggregationInterval;
import org.jasig.portal.events.aggr.BaseAggregationDao;
import org.jasig.portal.events.aggr.groups.AggregatedGroupMapping;
import org.jasig.portal.events.aggr.tabrender.TabRenderAggregation;
import org.jasig.portal.events.aggr.tabrender.TabRenderAggregationDao;
import org.jasig.portal.events.aggr.tabrender.TabRenderAggregationKey;
import org.jasig.portal.events.aggr.tabrender.TabRenderAggregationKeyImpl;
import org.jasig.portal.events.aggr.tabs.AggregatedTabLookupDao;
import org.jasig.portal.events.aggr.tabs.AggregatedTabMapping;
import org.jasig.portal.events.aggr.tabs.AggregatedTabMappingNameComparator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.portlet.ModelAndView;
import org.springframework.web.portlet.bind.annotation.RenderMapping;
import org.springframework.web.portlet.bind.annotation.ResourceMapping;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

/**
 * Tab render reports
 * 
 * @author James Wennmacher, jameswennmacher@gmail.com
 */
@Controller
@RequestMapping(value="VIEW")
public class TabRenderStatisticsController extends BaseStatisticsReportController<TabRenderAggregation, TabRenderAggregationKey, TabRenderReportForm> {
    private static final String DATA_TABLE_RESOURCE_ID = "tabRenderData";
    private final static String REPORT_NAME = "tabRender.totals";

    @Autowired
    private TabRenderAggregationDao<TabRenderAggregation> tabRenderDao;

    @Autowired
    private AggregatedTabLookupDao aggregatedTabLookupDao;
    
    @RenderMapping(value="MAXIMIZED", params="report=" + REPORT_NAME)
    public String getLoginView() throws TypeMismatchException {
        return "jsp/Statistics/reportGraph";
    }
    
    @ResourceMapping(DATA_TABLE_RESOURCE_ID)
    public ModelAndView renderTabRenderAggregationReport(TabRenderReportForm form) throws TypeMismatchException {
        return renderAggregationReport(form);
    }
    
    @Override
    protected TabRenderReportForm createReportFormRequest() {
        TabRenderReportForm form = new TabRenderReportForm();
        setReportFormTabs(form);
        return form;
    }

    /**
     * Set the tab names to have selected by default
     */
    protected void setReportFormTabs(final TabRenderReportForm report) {
        final Set<AggregatedTabMapping> tabs = this.getTabs();
        if (!tabs.isEmpty()) {
            report.getTabs().add(tabs.iterator().next().getId());
        }
    }

    /**
     * @return Tabs that exist for the aggregation
     */
    @ModelAttribute("tabs")
    public Set<AggregatedTabMapping> getTabs() {
        final Set<AggregatedTabMapping> groupMappings = aggregatedTabLookupDao.getTabMappings();

        final Set<AggregatedTabMapping> sortedGroupMappings = new TreeSet<AggregatedTabMapping>(AggregatedTabMappingNameComparator.INSTANCE);
        sortedGroupMappings.addAll(groupMappings);
        return sortedGroupMappings;
    }

    @Override
    public String getReportName() {
        return REPORT_NAME;
    }

    @Override
    public String getReportDataResourceId() {
        return DATA_TABLE_RESOURCE_ID;
    }

    @Override
    protected BaseAggregationDao<TabRenderAggregation, TabRenderAggregationKey> getBaseAggregationDao() {
        return this.tabRenderDao;
    }

    @Override
    protected TabRenderAggregationKey createAggregationsQueryKey(Set<AggregatedGroupMapping> groups, TabRenderReportForm form) {
        final AggregationInterval interval = form.getInterval();
        final AggregatedTabMapping tabMapping = aggregatedTabLookupDao.getTabMapping(form.getTabs().get(0));
        return new TabRenderAggregationKeyImpl(interval, groups.iterator().next(), tabMapping);
    }
    
    @Override
    protected List<ColumnDescription> getColumnDescriptions(AggregatedGroupMapping group, TabRenderReportForm form) {
        final String groupName = group.getGroupName();
        final AggregatedTabMapping tabMapping = aggregatedTabLookupDao.getTabMapping(form.getTabs().get(0));

        return Collections.singletonList(new ColumnDescription(groupName + "-" + tabMapping.getFragmentName() + "-" + tabMapping.getTabName() + "-renders",
                ValueType.NUMBER, groupName + "/" + tabMapping.getFragmentName() + "/" + tabMapping.getTabName() + " - Total Renders"));

    }

    @Override
    protected List<Value> createRowValues(TabRenderAggregation aggr, TabRenderReportForm form) {
        int count = aggr != null ? aggr.getRenderCount() : 0;
        return Collections.<Value>singletonList(new NumberValue(count));
    }
}
