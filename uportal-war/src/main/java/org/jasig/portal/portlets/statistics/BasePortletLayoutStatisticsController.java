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
import org.jasig.portal.events.aggr.portletlayout.PortletLayoutAggregation;
import org.jasig.portal.events.aggr.portletlayout.PortletLayoutAggregationDao;
import org.jasig.portal.events.aggr.portletlayout.PortletLayoutAggregationDiscriminator;
import org.jasig.portal.events.aggr.portletlayout.PortletLayoutAggregationDiscriminatorImpl;
import org.jasig.portal.events.aggr.portletlayout.PortletLayoutAggregationKey;
import org.jasig.portal.events.aggr.portletlayout.PortletLayoutAggregationKeyImpl;
import org.jasig.portal.events.aggr.portlets.AggregatedPortletLookupDao;
import org.jasig.portal.events.aggr.portlets.AggregatedPortletMapping;
import org.jasig.portal.events.aggr.portlets.AggregatedPortletMappingNameComparator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.portlet.ModelAndView;

import com.google.visualization.datasource.base.TypeMismatchException;
import com.google.visualization.datasource.datatable.ColumnDescription;
import com.google.visualization.datasource.datatable.value.Value;
import com.google.visualization.datasource.datatable.value.ValueType;

/**
 * @author Chris Waymire <cwaymire@unicon.net>
 */
public abstract class BasePortletLayoutStatisticsController<F extends BasePortletLayoutReportForm> extends
        BaseStatisticsReportController<PortletLayoutAggregation, PortletLayoutAggregationKey,
                PortletLayoutAggregationDiscriminator, F> {

    @Autowired
    protected PortletLayoutAggregationDao<PortletLayoutAggregation> portletLayoutDao;

    @Autowired
    protected AggregatedGroupLookupDao aggregatedGroupLookupDao;

    @Autowired
    protected AggregatedPortletLookupDao aggregatedPortletLookupDao;

    public String getLoginView() throws TypeMismatchException {
        return "jsp/Statistics/reportGraph";
    }

    public ModelAndView renderPortletAddAggregationReport(F form) throws TypeMismatchException {
        return renderAggregationReport(form);
    }

    @Override
    public abstract String getReportName();

    @Override
    public abstract String getReportDataResourceId();

    @Override
    protected void initReportForm(F report) {
        super.initReportForm(report);
        
        selectFormDefaultPortlet(report);
    }

    /**
     * Select the first portlet name by default for the form
     */
    protected void selectFormDefaultPortlet(final F report) {
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
        final Set<AggregatedPortletMapping> groupMappings = this.aggregatedPortletLookupDao.getPortletMappings();

        final Set<AggregatedPortletMapping> sortedGroupMappings = new TreeSet<AggregatedPortletMapping>(AggregatedPortletMappingNameComparator.INSTANCE);
        sortedGroupMappings.addAll(groupMappings);
        return sortedGroupMappings;
    }

    //public abstract Set<AggregatedPortletMapping> getPortlets();

    @Override
    protected BaseAggregationDao<PortletLayoutAggregation, PortletLayoutAggregationKey> getBaseAggregationDao() {
        return this.portletLayoutDao;
    }

    @Override
    protected Set<PortletLayoutAggregationKey> createAggregationsQueryKeyset(
            Set<PortletLayoutAggregationDiscriminator> columnDiscriminators, F form) {
        // Create keys (that exclude the temporal date/time information) from the interval
        // and the data in the column discriminators.
        final AggregationInterval interval = form.getInterval();
        final HashSet<PortletLayoutAggregationKey> keys = new HashSet<PortletLayoutAggregationKey>();
        for (PortletLayoutAggregationDiscriminator discriminator : columnDiscriminators) {
            keys.add(new PortletLayoutAggregationKeyImpl(interval, discriminator.getAggregatedGroup(),
                    discriminator.getPortletMapping()));
        }
        return keys;
    }


    @Override
    protected Comparator<? super PortletLayoutAggregationDiscriminator> getDiscriminatorComparator() {
        return PortletLayoutAggregationDiscriminatorImpl.Comparator.INSTANCE;
    }

    protected Map<PortletLayoutAggregationDiscriminator, SortedSet<PortletLayoutAggregation>> createColumnDiscriminatorMap
            (F form) {
        //Collections used to track the queried groups and the results
        final Map<PortletLayoutAggregationDiscriminator, SortedSet<PortletLayoutAggregation>> groupedAggregations =
                new TreeMap<PortletLayoutAggregationDiscriminator,
                        SortedSet<PortletLayoutAggregation>>(PortletLayoutAggregationDiscriminatorImpl.Comparator.INSTANCE);

        //Get concrete group mapping objects that are being queried for
        List<Long> groups = form.getGroups();
        Set<String> portletFNames = form.getPortlets();
        for (final Long queryGroupId : groups) {
            AggregatedGroupMapping groupMapping = this.aggregatedGroupLookupDao.getGroupMapping(queryGroupId);
            for (final String portletFName : portletFNames) {
                AggregatedPortletMapping tabMapping = this.aggregatedPortletLookupDao.getMappedPortletForFname(portletFName);
                final PortletLayoutAggregationDiscriminator mapping =
                        new PortletLayoutAggregationDiscriminatorImpl(groupMapping, tabMapping);
                //Create the set the aggregations for this report column will be stored in, sorted chronologically
                final SortedSet<PortletLayoutAggregation> aggregations =
                        new TreeSet<PortletLayoutAggregation>(BaseAggregationDateTimeComparator.INSTANCE);

                //Map the group to the set
                groupedAggregations.put(mapping, aggregations);
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
    protected String getReportTitleAugmentation(F form) {
        int groupSize = form.getGroups().size();
        int portletSize = form.getPortlets().size();

        // If multiple of all selected, the report title does not change.
        if (portletSize > 1 && groupSize > 1) {
            return null;
        }

        // Look up names in case we need them.  They should be in cache so no real performance hit.
        String firstPortletName = this.aggregatedPortletLookupDao.getMappedPortletForFname(form.getPortlets().iterator().next()).getFname();
        Long firstGroupId = form.getGroups().iterator().next().longValue();
        String firstGroupName = this.aggregatedGroupLookupDao.getGroupMapping(firstGroupId).getGroupName();

        // Default to show portlet name else group name in title
        String augmentedTitle = groupSize == 1 && portletSize > 1 ? firstGroupName : firstPortletName;

        return augmentedTitle;
    }

    /**
     * Create column descriptions for the portlet report.  The default column description is
     * the group name, but those items that are singular will move to the report title and
     * only those that have 2 or more values selected on the form will be in the column title.
     * Format:  P - G
     *          P
     *          G
     *          P - G
     * where P is portlet name, G is group name
     *
     * @param reportColumnDiscriminator
     * @param form The original query form
     * @return
     */
    @Override
    protected List<ColumnDescription> getColumnDescriptions(PortletLayoutAggregationDiscriminator reportColumnDiscriminator,
                                                            F form) {
        int groupSize = form.getGroups().size();
        int portletSize = form.getPortlets().size();

        String portletName = reportColumnDiscriminator.getPortletMapping().getFname();
        String groupName = reportColumnDiscriminator.getAggregatedGroup().getGroupName();

        String description = null;
        if (showFullColumnHeaderDescriptions(form) || (groupSize > 1 && portletSize > 1)) {
            description = String.format("%s - %s", portletName, groupName);
        } else {
            // Default to group name, else portlet name
            description = groupSize == 1 && portletSize > 1 ? portletName : groupName;
        }

        final List<ColumnDescription> columnDescriptions = new ArrayList<ColumnDescription>();
        columnDescriptions.add(new ColumnDescription(description, ValueType.NUMBER, description));
        return columnDescriptions;
    }

    @Override
    protected abstract List<Value> createRowValues(PortletLayoutAggregation aggr, F form);
}