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

import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;

import com.google.visualization.datasource.base.TypeMismatchException;
import com.google.visualization.datasource.datatable.ColumnDescription;
import com.google.visualization.datasource.datatable.value.NumberValue;
import com.google.visualization.datasource.datatable.value.Value;
import org.jasig.portal.events.aggr.AggregationInterval;
import org.jasig.portal.events.aggr.BaseAggregationDao;
import org.jasig.portal.events.aggr.BaseAggregationDateTimeComparator;
import org.jasig.portal.events.aggr.groups.AggregatedGroupLookupDao;
import org.jasig.portal.events.aggr.groups.AggregatedGroupMapping;
import org.jasig.portal.events.aggr.tabrender.TabRenderAggregation;
import org.jasig.portal.events.aggr.tabrender.TabRenderAggregationDao;
import org.jasig.portal.events.aggr.tabrender.TabRenderAggregationDiscriminator;
import org.jasig.portal.events.aggr.tabrender.TabRenderAggregationDiscriminatorImpl;
import org.jasig.portal.events.aggr.tabrender.TabRenderAggregationKey;
import org.jasig.portal.events.aggr.tabrender.TabRenderAggregationKeyImpl;
import org.jasig.portal.events.aggr.tabs.AggregatedTabLookupDao;
import org.jasig.portal.events.aggr.tabs.AggregatedTabMapping;
import org.jasig.portal.events.aggr.tabs.AggregatedTabMappingNameComparator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.portlet.ModelAndView;
import org.springframework.web.portlet.bind.annotation.RenderMapping;
import org.springframework.web.portlet.bind.annotation.ResourceMapping;

/**
 * Tab render reports
 * 
 * @author James Wennmacher, jameswennmacher@gmail.com
 */
@Controller
@RequestMapping(value="VIEW")
public class TabRenderStatisticsController extends
        BaseStatisticsReportController<TabRenderAggregation, TabRenderAggregationKey,
                TabRenderAggregationDiscriminator, TabRenderReportForm> {
    private static final String DATA_TABLE_RESOURCE_ID = "tabRenderData";
    private final static String REPORT_NAME = "tabRender.totals";

    @Autowired
    private ReportTitleAndColumnDescriptionStrategy titleAndColumnDescriptionStrategy;

    @Autowired
    @Qualifier(value = "jpaTabRenderAggregationDao")
    private TabRenderAggregationDao<TabRenderAggregation> tabRenderDao;

    @Autowired
    @Qualifier(value = "jpaAggregatedGroupLookupDao")
    private AggregatedGroupLookupDao aggregatedGroupDao;

    @Autowired
    private AggregatedTabLookupDao aggregatedTabLookupDao;

    public void setTitleAndColumnDescriptionStrategy(ReportTitleAndColumnDescriptionStrategy titleAndColumnDescriptionStrategy) {
        this.titleAndColumnDescriptionStrategy = titleAndColumnDescriptionStrategy;
    }

    @RenderMapping(value="MAXIMIZED", params="report=" + REPORT_NAME)
    public String getLoginView() throws TypeMismatchException {
        return "jsp/Statistics/reportGraph";
    }
    
    @ResourceMapping(DATA_TABLE_RESOURCE_ID)
    public ModelAndView renderTabRenderAggregationReport(TabRenderReportForm form) throws TypeMismatchException {
        return renderAggregationReport(form);
    }
    
    @Override
    protected void initReportForm(TabRenderReportForm report) {
        setReportFormTabs(report);
    }

    /**
     * Set the tab names to have first selected by default
     */
    private void setReportFormTabs(final TabRenderReportForm report) {
        if (!report.getTabs().isEmpty()) {
            //Tabs are already set, do nothing
            return;
        }
        
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
    protected Set<TabRenderAggregationKey> createAggregationsQueryKeyset(
            Set<TabRenderAggregationDiscriminator> columnDiscriminators, TabRenderReportForm form) {
        // Create keys (that exclude the temporal date/time information) from the interval
        // and the data in the column discriminators.
        final AggregationInterval interval = form.getInterval();
        final HashSet<TabRenderAggregationKey> keys = new HashSet<TabRenderAggregationKey>();
        for (TabRenderAggregationDiscriminator discriminator : columnDiscriminators) {
            keys.add(new TabRenderAggregationKeyImpl(interval, discriminator.getAggregatedGroup(),
                    discriminator.getTabMapping()));
        }
        return keys;
    }

    @Override
    protected Comparator<? super TabRenderAggregationDiscriminator> getDiscriminatorComparator() {
        return TabRenderAggregationDiscriminatorImpl.Comparator.INSTANCE;
    }

    protected Map<TabRenderAggregationDiscriminator, SortedSet<TabRenderAggregation>> createColumnDiscriminatorMap
            (TabRenderReportForm form) {
        //Collections used to track the queried groups and the results
        final Map<TabRenderAggregationDiscriminator, SortedSet<TabRenderAggregation>> groupedAggregations =
                new TreeMap<TabRenderAggregationDiscriminator,
                        SortedSet<TabRenderAggregation>>(TabRenderAggregationDiscriminatorImpl.Comparator.INSTANCE);

        //Get concrete group mapping objects that are being queried for
        List<Long> groups = form.getGroups();
        List<Long> tabs = form.getTabs();
        for (final Long queryGroupId : groups) {
            AggregatedGroupMapping groupMapping = this.aggregatedGroupDao.getGroupMapping(queryGroupId);
            for (final Long tabId : tabs) {
                AggregatedTabMapping tabMapping = this.aggregatedTabLookupDao.getTabMapping(tabId);
                final TabRenderAggregationDiscriminator mapping =
                        new TabRenderAggregationDiscriminatorImpl(groupMapping, tabMapping);
                //Create the set the aggregations for this report column will be stored in, sorted chronologically
                final SortedSet<TabRenderAggregation> aggregations =
                        new TreeSet<TabRenderAggregation>(BaseAggregationDateTimeComparator.INSTANCE);

                //Map the group to the set
                groupedAggregations.put(mapping, aggregations);
            }

        }

        return groupedAggregations;
    }

    @Override
    protected String getReportTitleAugmentation(TabRenderReportForm form) {
        Long tabId = form.getTabs().iterator().next().longValue();
        String firstTabname = this.aggregatedTabLookupDao.getTabMapping(tabId).getDisplayString();

        Long groupId = form.getGroups().iterator().next().longValue();
        String firstGroupName = this.aggregatedGroupDao.getGroupMapping(groupId).getGroupName();

        ReportTitleAndColumnDescriptionStrategy.TitleAndCount[] items = new ReportTitleAndColumnDescriptionStrategy.TitleAndCount[] {
                new ReportTitleAndColumnDescriptionStrategy.TitleAndCount(firstTabname, form.getTabs().size()),
                new ReportTitleAndColumnDescriptionStrategy.TitleAndCount(firstGroupName, form.getGroups().size())
        };

        return titleAndColumnDescriptionStrategy.getReportTitleAugmentation(items);
    }

    @Override
    protected List<ColumnDescription> getColumnDescriptions(TabRenderAggregationDiscriminator reportColumnDiscriminator,
                                                            TabRenderReportForm form) {
        String tabName = reportColumnDiscriminator.getTabMapping().getDisplayString();
        String groupName = reportColumnDiscriminator.getAggregatedGroup().getGroupName();

        ReportTitleAndColumnDescriptionStrategy.TitleAndCount[] items = new ReportTitleAndColumnDescriptionStrategy.TitleAndCount[] {
                new ReportTitleAndColumnDescriptionStrategy.TitleAndCount(tabName, form.getTabs().size()),
                new ReportTitleAndColumnDescriptionStrategy.TitleAndCount(groupName, form.getGroups().size())
        };

        return titleAndColumnDescriptionStrategy.getColumnDescriptions(items, showFullColumnHeaderDescriptions(form), form);
    }

    @Override
    protected List<Value> createRowValues(TabRenderAggregation aggr, TabRenderReportForm form) {
        int count = aggr != null ? aggr.getRenderCount() : 0;
        return Collections.<Value>singletonList(new NumberValue(count));
    }

}
