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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;

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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.portlet.ModelAndView;
import org.springframework.web.portlet.bind.annotation.RenderMapping;
import org.springframework.web.portlet.bind.annotation.ResourceMapping;

import com.google.visualization.datasource.base.TypeMismatchException;
import com.google.visualization.datasource.datatable.ColumnDescription;
import com.google.visualization.datasource.datatable.value.NumberValue;
import com.google.visualization.datasource.datatable.value.Value;
import com.google.visualization.datasource.datatable.value.ValueType;

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
    private PortletExecutionAggregationDao<PortletExecutionAggregation> portletExecutionDao;

    @Autowired
    private AggregatedGroupLookupDao aggregatedGroupLookupDao;

    @Autowired
    private AggregatedPortletLookupDao aggregatedPortletLookupDao;
    
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

        // If multiple of all selected, the report title does not change.
        if (portletSize > 1 && executionTypeSize > 1 && groupSize > 1) {
            return null;
        }

        // Look up names in case we need them.  They should be in cache so no real performance hit.
        String firstPortletName = this.aggregatedPortletLookupDao.getMappedPortletForFname(form.getPortlets().iterator().next()).getFname();
        Long firstGroupId = form.getGroups().iterator().next().longValue();
        String firstGroupName = this.aggregatedGroupLookupDao.getGroupMapping(firstGroupId).getGroupName();
        String firstExecutionType = form.getExecutionTypeNames().iterator().next();

        // Default to show portlet name else group name in title
        String augmentedTitle = groupSize == 1 && portletSize > 1 ?
                firstGroupName : firstPortletName;

        // Use only the execution Type or append it.
        if (executionTypeSize == 1) {
            if (groupSize > 1 && portletSize > 1) {
                augmentedTitle = firstExecutionType;
            } else {
                augmentedTitle += " (" + firstExecutionType + ")";
            }
        }

        // Last scenario:  multiple execution type and single portlet and group
        if (executionTypeSize > 1 && portletSize == 1 && groupSize == 1) {
            augmentedTitle = String.format("%s - %s", firstPortletName, firstGroupName);
        }

        return augmentedTitle;
    }

    /**
     * Create column descriptions for the portlet report.  The default column description is
     * the group name possibly with the executionType, but those items that are
     * singular will move to the report title and only those that have 2 or more values
     * selected on the form will be in the column title.
     * Format:  P (E) - G
     *          P (E)
     *          G (E)
     *          E
     *          P - G
     * where P is portlet name, G is group name, E is Execution Type
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

        String description = null;
        if (showFullColumnHeaderDescriptions(form) ||
                (groupSize > 1 && portletSize > 1 && executionTypeSize > 1)) {
            description = String.format("%s (%s) - %s", portletName, executionTypeName, groupName);
        } else {
            // Default to group name, else portlet name
            description = groupSize == 1 && portletSize > 1 ?
                    portletName : groupName;

            // Does ExecutionType replace default column name or add to it?
            if (executionTypeSize > 1) {
                if (groupSize == 1 && portletSize == 1) {
                    description = executionTypeName;
                } else {
                    description += " (" + executionTypeName + ")";
                }
            }
            // Last scenario: 1 execution type and multiple portlet and group
            if (groupSize > 1 && portletSize > 1 && executionTypeSize == 1) {
                description = String.format("%s - %s", portletName, groupName);
            }
        }

        final List<ColumnDescription> columnDescriptions = new ArrayList<ColumnDescription>();
        columnDescriptions.add(new ColumnDescription(description, ValueType.NUMBER, description));
        return columnDescriptions;
    }

    @Override
    protected List<Value> createRowValues(PortletExecutionAggregation aggr, PortletExecutionReportForm form) {
        int count = aggr != null ? aggr.getExecutionCount() : 0;
        return Collections.<Value>singletonList(new NumberValue(count));
    }
}
