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
import org.jasig.portal.events.aggr.portletexec.PortletExecutionAggregation;
import org.jasig.portal.events.aggr.portletexec.PortletExecutionAggregationDao;
import org.jasig.portal.events.aggr.portletexec.PortletExecutionAggregationDiscriminator;
import org.jasig.portal.events.aggr.portletexec.PortletExecutionAggregationDiscriminatorImpl;
import org.jasig.portal.events.aggr.portletexec.PortletExecutionAggregationKey;
import org.jasig.portal.events.aggr.portletexec.PortletExecutionAggregationKey.ExecutionType;
import org.jasig.portal.events.aggr.portletexec.PortletExecutionAggregationKeyImpl;
import org.jasig.portal.events.aggr.portlets.AggregatedPortletLookupDao;
import org.jasig.portal.events.aggr.portlets.AggregatedPortletMapping;
import org.jasig.portal.events.aggr.portlets.AggregatedPortletMappingNameComparator;
import org.jasig.portal.portlets.statistics.ReportTitleAndColumnDescriptionStrategy.TitleAndCount;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.portlet.ModelAndView;
import org.springframework.web.portlet.bind.annotation.RenderMapping;
import org.springframework.web.portlet.bind.annotation.ResourceMapping;

/**
 * Portlet Execution reports
 * 
 * @author James Wennmacher, jameswennmacher@gmail.com
 */
@Controller
@RequestMapping(value="VIEW")
public class PortletExecutionStatisticsController
        extends BaseStatisticsReportController<
            PortletExecutionAggregation, 
            PortletExecutionAggregationKey,
            PortletExecutionAggregationDiscriminator, 
            PortletExecutionReportForm> {

    private static final String DATA_TABLE_RESOURCE_ID = "portletExecutionData";
    private final static String REPORT_NAME = "portletExecution.totals";

    @Autowired
    private ReportTitleAndColumnDescriptionStrategy titleAndColumnDescriptionStrategy;

    @Autowired
    @Qualifier(value = "jpaPortletExecutionAggregationDao")
    private PortletExecutionAggregationDao<PortletExecutionAggregation> portletExecutionDao;

    @Autowired
    private AggregatedGroupLookupDao aggregatedGroupLookupDao;

    @Autowired
    private AggregatedPortletLookupDao aggregatedPortletLookupDao;

    public void setTitleAndColumnDescriptionStrategy(ReportTitleAndColumnDescriptionStrategy titleAndColumnDescriptionStrategy) {
        this.titleAndColumnDescriptionStrategy = titleAndColumnDescriptionStrategy;
    }

    @RenderMapping(value="MAXIMIZED", params="report=" + REPORT_NAME)
    public String getLoginView() throws TypeMismatchException {
        return "jsp/Statistics/reportGraph";
    }
    
    @ResourceMapping(DATA_TABLE_RESOURCE_ID)
    public ModelAndView renderPortletExecutionAggregationReport(PortletExecutionReportForm form) throws TypeMismatchException {
        return renderAggregationReport(form);
    }
    
    
    @Override
    protected void initReportForm(PortletExecutionReportForm report) {
        selectFormDefaultPortlet(report);
        selectFormExecutionType(report);
    }

    /**
     * Select the first portlet name by default for the form
     */
    private void selectFormDefaultPortlet(final PortletExecutionReportForm report) {
        if (!report.getPortlets().isEmpty()) {
            //Portlets already selected, do nothin
            return;
        }
        
        final Set<AggregatedPortletMapping> portlets = this.getPortlets();
        if (!portlets.isEmpty()) {
            report.getPortlets().add(portlets.iterator().next().getFname());
        }
    }

    /**
     * @return List of Portlets that exist for the aggregation
     */
    @ModelAttribute("portlets")
    public Set<AggregatedPortletMapping> getPortlets() {
        final Set<AggregatedPortletMapping> groupMappings = aggregatedPortletLookupDao.getPortletMappings();

        final Set<AggregatedPortletMapping> sortedGroupMappings = new TreeSet<AggregatedPortletMapping>(AggregatedPortletMappingNameComparator.INSTANCE);
        sortedGroupMappings.addAll(groupMappings);
        return sortedGroupMappings;
    }

    @ModelAttribute("executionTypes")
    public ExecutionType[] getExecutionTypes() {
        return ExecutionType.values();
    }

    /**
     * Select the XXXX execution type by default for the form
     */
    private void selectFormExecutionType(final PortletExecutionReportForm report) {
        if (!report.getExecutionTypeNames().isEmpty()) {
            //Already execution types set, do nothing
            return;
        }
        
        report.getExecutionTypeNames().add(ExecutionType.RENDER.name());
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
    protected BaseAggregationDao<PortletExecutionAggregation, PortletExecutionAggregationKey> getBaseAggregationDao() {
        return this.portletExecutionDao;
    }

    @Override
    protected Set<PortletExecutionAggregationKey> createAggregationsQueryKeyset(
            Set<PortletExecutionAggregationDiscriminator> columnDiscriminators, PortletExecutionReportForm form) {
        // Create keys (that exclude the temporal date/time information) from the interval
        // and the data in the column discriminators.
        final AggregationInterval interval = form.getInterval();
        final HashSet<PortletExecutionAggregationKey> keys = new HashSet<PortletExecutionAggregationKey>();
        for (PortletExecutionAggregationDiscriminator discriminator : columnDiscriminators) {
            keys.add(new PortletExecutionAggregationKeyImpl(interval, discriminator.getAggregatedGroup(),
                    discriminator.getPortletMapping(), discriminator.getExecutionType()));
        }
        return keys;
    }

    @Override
    protected Comparator<? super PortletExecutionAggregationDiscriminator> getDiscriminatorComparator() {
        return PortletExecutionAggregationDiscriminatorImpl.Comparator.INSTANCE;
    }

    protected Map<PortletExecutionAggregationDiscriminator, SortedSet<PortletExecutionAggregation>> createColumnDiscriminatorMap
            (PortletExecutionReportForm form) {
        //Collections used to track the queried groups and the results
        final Map<PortletExecutionAggregationDiscriminator, SortedSet<PortletExecutionAggregation>> groupedAggregations =
                new TreeMap<PortletExecutionAggregationDiscriminator,
                        SortedSet<PortletExecutionAggregation>>(PortletExecutionAggregationDiscriminatorImpl.Comparator.INSTANCE);

        //Get concrete group mapping objects that are being queried for
        List<Long> groups = form.getGroups();
        Set<String> portletFNames = form.getPortlets();
        Set<String> executionTypes = form.getExecutionTypeNames();
        for (final Long queryGroupId : groups) {
            AggregatedGroupMapping groupMapping = this.aggregatedGroupLookupDao.getGroupMapping(queryGroupId);
            for (final String portletFName : portletFNames) {
                AggregatedPortletMapping tabMapping = this.aggregatedPortletLookupDao.getMappedPortletForFname(portletFName);
                for (String executionType : executionTypes) {
                    final PortletExecutionAggregationDiscriminator mapping =
                            new PortletExecutionAggregationDiscriminatorImpl(groupMapping, tabMapping,
                                    ExecutionType.valueOf(executionType));
                    //Create the set the aggregations for this report column will be stored in, sorted chronologically
                    final SortedSet<PortletExecutionAggregation> aggregations =
                            new TreeSet<PortletExecutionAggregation>(BaseAggregationDateTimeComparator.INSTANCE);

                    //Map the group to the set
                    groupedAggregations.put(mapping, aggregations);
                }
            }

        }

        return groupedAggregations;
    }

    /**
     * Create report title.  Criteria that have a single value selected are put
     * into the title.  Format and possible options are:
     * <ul>
     * <li>null (no change needed)</li>
     * <li>portlet</li>
     * <li>portlet (execution)</li>
     * <li>group</li>
     * <li>group (execution)</li>
     * <li>execution</li>
     * <li>portlet - group (also displayed if one of each criteria selected)</li>
     * </ul>
     * @param form the form
     * @return report title
     */
    @Override
    protected String getReportTitleAugmentation(PortletExecutionReportForm form) {
        int groupSize = form.getGroups().size();
        int portletSize = form.getPortlets().size();
        int executionTypeSize = form.getExecutionTypeNames().size();

        // Look up names in case we need them.  They should be in cache so no real performance hit.
        String firstPortletName = this.aggregatedPortletLookupDao.getMappedPortletForFname(form.getPortlets().iterator().next()).getFname();
        Long firstGroupId = form.getGroups().iterator().next().longValue();
        String firstGroupName = this.aggregatedGroupLookupDao.getGroupMapping(firstGroupId).getGroupName();
        String firstExecutionType = form.getExecutionTypeNames().iterator().next();

        TitleAndCount[] items = new TitleAndCount[] {
                new TitleAndCount(firstPortletName, portletSize),
                new TitleAndCount(firstExecutionType, executionTypeSize),
                new TitleAndCount(firstGroupName, groupSize)
        };

        return titleAndColumnDescriptionStrategy.getReportTitleAugmentation(items);
    }

    /**
     * Create column descriptions for the portlet report using the configured report
     * labelling strategy.
     *
     * @param reportColumnDiscriminator
     * @param form The original query form
     * @return
     */
    @Override
    protected List<ColumnDescription> getColumnDescriptions(PortletExecutionAggregationDiscriminator reportColumnDiscriminator,
                                                            PortletExecutionReportForm form) {
        int groupSize = form.getGroups().size();
        int portletSize = form.getPortlets().size();
        int executionTypeSize = form.getExecutionTypeNames().size();

        String portletName = reportColumnDiscriminator.getPortletMapping().getFname();
        String groupName = reportColumnDiscriminator.getAggregatedGroup().getGroupName();
        String executionTypeName = reportColumnDiscriminator.getExecutionType().getName();

        TitleAndCount[] items = new TitleAndCount[] {
                new TitleAndCount(portletName, portletSize),
                new TitleAndCount(executionTypeName, executionTypeSize),
                new TitleAndCount(groupName, groupSize)
        };

        return titleAndColumnDescriptionStrategy.getColumnDescriptions(items, showFullColumnHeaderDescriptions(form), form);
    }

    @Override
    protected List<Value> createRowValues(PortletExecutionAggregation aggr, PortletExecutionReportForm form) {
        int count = aggr != null ? aggr.getExecutionCount() : 0;
        return Collections.<Value>singletonList(new NumberValue(count));
    }

}
