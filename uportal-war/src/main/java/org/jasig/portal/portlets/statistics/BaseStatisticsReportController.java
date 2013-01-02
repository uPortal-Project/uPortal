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

import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;

import javax.portlet.ResourceURL;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterators;
import com.google.common.collect.PeekingIterator;
import com.google.visualization.datasource.base.TypeMismatchException;
import com.google.visualization.datasource.datatable.ColumnDescription;
import com.google.visualization.datasource.datatable.DataTable;
import com.google.visualization.datasource.datatable.TableCell;
import com.google.visualization.datasource.datatable.TableRow;
import com.google.visualization.datasource.datatable.value.DateTimeValue;
import com.google.visualization.datasource.datatable.value.DateValue;
import com.google.visualization.datasource.datatable.value.TimeOfDayValue;
import com.google.visualization.datasource.datatable.value.Value;
import com.google.visualization.datasource.datatable.value.ValueType;
import org.apache.commons.lang.StringUtils;
import org.jasig.portal.events.aggr.AggregationInterval;
import org.jasig.portal.events.aggr.AggregationIntervalHelper;
import org.jasig.portal.events.aggr.BaseAggregation;
import org.jasig.portal.events.aggr.BaseAggregationDao;
import org.jasig.portal.events.aggr.BaseAggregationDateTimeComparator;
import org.jasig.portal.events.aggr.BaseAggregationKey;
import org.jasig.portal.events.aggr.BaseGroupedAggregationDiscriminator;
import org.jasig.portal.events.aggr.groups.AggregatedGroupLookupDao;
import org.jasig.portal.events.aggr.groups.AggregatedGroupMapping;
import org.jasig.portal.events.aggr.groups.AggregatedGroupMappingNameComparator;
import org.joda.time.DateMidnight;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.DateTimeFormatterBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.portlet.ModelAndView;

/**
 * Base class for reporting on portal statistics. Does most of the heavy lifting for reporting against {@link BaseAggregation} subclasses.
 * Implementations should call {@link #renderAggregationReport(BaseReportForm)} from their resource request handling method. This will
 * generate the {@link DataTable} of the results and render the correct view.
 * 
 * @author Eric Dalquist
 * @param <T> The type of aggregation being reported on
 * @param <K> The aggregation query key
 * @param <F> The form used to query for data
 */
public abstract class BaseStatisticsReportController<
                T extends BaseAggregation<K, D>, 
                K extends BaseAggregationKey,
                D extends BaseGroupedAggregationDiscriminator, 
                F extends BaseReportForm> {
    /**
     * List of intervals in the preferred report order. This is the order they are tested against
     * the results of {@link #getIntervals()}. The first hit is used to populate the default form. 
     */
    private static final List<AggregationInterval> PREFERRED_INTERVAL_ORDER = ImmutableList.of(
            AggregationInterval.DAY,
            AggregationInterval.HOUR,
            AggregationInterval.FIVE_MINUTE,
            AggregationInterval.MINUTE,
            AggregationInterval.WEEK,
            AggregationInterval.MONTH,
            AggregationInterval.ACADEMIC_TERM,
            AggregationInterval.CALENDAR_QUARTER,
            AggregationInterval.YEAR
    );
    protected final Logger logger = LoggerFactory.getLogger(getClass());
    
    @Autowired
    private AggregationIntervalHelper intervalHelper;
    
    @Autowired
    protected AggregatedGroupLookupDao aggregatedGroupDao;
    
    @org.springframework.beans.factory.annotation.Value("${org.jasig.portal.portlets.statistics.maxIntervals}")
    private int maxIntervals = 4000;
    
    @InitBinder
    public void initBinder(WebDataBinder binder) {
        final DateTimeFormatter formatter = new DateTimeFormatterBuilder().appendPattern("M/d/yyyy").toFormatter();
        binder.registerCustomEditor(DateMidnight.class, new CustomDateMidnightEditor(formatter, false));
    }
    
    @ModelAttribute("maxIntervals")
    public final Integer getMaxIntervals() {
        return this.maxIntervals;
    }
    
    /**
     * @return Intervals that exist for the aggregation
     * @see BaseAggregationDao#getAggregationIntervals()
     */
    @ModelAttribute("intervals")
    public final Set<AggregationInterval> getIntervals() {
        final Set<AggregationInterval> intervals = this.getBaseAggregationDao().getAggregationIntervals();
        
        final Set<AggregationInterval> sortedIntervals = new TreeSet<AggregationInterval>();
        sortedIntervals.addAll(intervals);
        return sortedIntervals;
    }
    
    /**
     * @return Groups that exist for the aggregation
     * @see BaseAggregationDao#getAggregatedGroupMappings()
     */
    @ModelAttribute("groups")
    public final Set<AggregatedGroupMapping> getGroups() {
        final Set<AggregatedGroupMapping> groupMappings = this.getBaseAggregationDao().getAggregatedGroupMappings();
        
        final Set<AggregatedGroupMapping> sortedGroupMappings = new TreeSet<AggregatedGroupMapping>(AggregatedGroupMappingNameComparator.INSTANCE);
        sortedGroupMappings.addAll(groupMappings);
        return sortedGroupMappings;
    }
    
    /**
     * @return The default report request form to use, populates the initial form view
     */
    @ModelAttribute("reportRequest")
    public final F getReportForm(F report) {
        setReportFormDateRangeAndInterval(report);
        
        setReportFormGroups(report);
        
        initReportForm(report);

        return report;
    }
    
    /**
     * @return The name of the report, used by users to choose the report from the set of available reports
     */
    @ModelAttribute("reportName")
    public abstract String getReportName();
    
    /**
     * @return The {@link ResourceURL#setResourceID(String)} value used to get the {@link DataTable} for the report
     */
    @ModelAttribute("reportDataResourceId")
    public abstract String getReportDataResourceId();

    /**
     * Set the groups to have selected by default if not already set
     */
    protected final void setReportFormGroups(final F report) {
        if (!report.getGroups().isEmpty()) {
            return;
        }
        
        final Set<AggregatedGroupMapping> groups = this.getGroups();
        if (!groups.isEmpty()) {
            report.getGroups().add(groups.iterator().next().getId());
        }
    }
    
    /**
     * Set the start/end date and the interval to have selected by default if they
     * are not already set
     */
    protected final void setReportFormDateRangeAndInterval(final F report) {
        //Determine default interval based on the intervals available for this aggregation
        if (report.getInterval() == null) {
            report.setInterval(AggregationInterval.DAY);
            final Set<AggregationInterval> intervals = this.getIntervals();
            for (final AggregationInterval preferredInterval : PREFERRED_INTERVAL_ORDER) {
                if (intervals.contains(preferredInterval)) {
                    report.setInterval(preferredInterval);
                    break;
                }
            }
        }
        
        //Set the report end date as today
        final DateMidnight reportEnd;
        if (report.getEnd() == null) {
            reportEnd = new DateMidnight();
            report.setEnd(reportEnd);
        }
        else {
            reportEnd = report.getEnd();
        }
        
        //Determine the best start date based on the selected interval
        if (report.getStart() == null) {
            final DateMidnight start;
            switch (report.getInterval()) {
                case MINUTE: {
                    start = reportEnd.minusDays(1);
                    break;
                }
                case FIVE_MINUTE: {
                    start = reportEnd.minusDays(2);
                    break;
                }
                case HOUR: {
                    start = reportEnd.minusWeeks(1);
                    break;
                }
                case DAY: {
                    start = reportEnd.minusMonths(1);
                    break;
                }
                case WEEK: {
                    start = reportEnd.minusMonths(3);
                    break;
                }
                case MONTH: {
                    start = reportEnd.minusYears(1);
                    break;
                }
                case ACADEMIC_TERM: {
                    start = reportEnd.minusYears(2);
                    break;
                }
                case CALENDAR_QUARTER: {
                    start = reportEnd.minusYears(2);
                    break;
                }
                case YEAR: {
                    start = reportEnd.minusYears(10);
                    break;
                }
                default: {
                    start = reportEnd.minusWeeks(1);
                }
            }
            
            report.setStart(start);
        }
    }
    
    /**
     * Optional for initializing the report form, note that implementers should check
     * to see if the form has alredy been populated before overwriting fields.
     */
    protected void initReportForm(F report) {
    }
    
    /**
     * @return The dao for the aggregation
     */
    protected abstract BaseAggregationDao<T, K> getBaseAggregationDao();


    /**
     * Create a set of keys used to execute {@link BaseAggregationDao#getAggregations(DateTime, DateTime, Set, AggregatedGroupMapping...)}.
     * Returns a set for those entities, such as Tab Render reports where the user can select one or more tabs to report on.
     * 
     * @param groups The groups being queried for
     * @param form The original query form
     * @return A set of partial keys to query with
     */
    protected abstract Set<K> createAggregationsQueryKeyset(Set<D> groups, F form);

        /**
        * Get the column descriptors to use for each group in the report. The order of the returned columns
        * is VERY important and must match the order of values as returned by {@link #createRowValues(BaseAggregation, BaseReportForm)}
        *
        * @param group The group to create the column descriptors for
        * @param form The original query form
        * @return List of column descriptors for the group
        */
    protected abstract List<ColumnDescription> getColumnDescriptions(D group, F form);

    /**
     * Get a discriminator comparator for the appropriate type of statistics data we are reporting on.
     * @return
     */
    protected abstract Comparator<? super D> getDiscriminatorComparator();

    /**
     * Create a map of the report column discriminators based on the submitted form to collate the aggregation
     * data into each column of a report.  * The map entries are a time-ordered sorted set of aggregation data points.
     * Subclasses may override this method to obtain more from the form than just AggregatedGroupMappings as
     * report columns.
     *
     * @param form Form submitted by the user
     * @return Map of report column discriminators to sorted set of time-based aggregation data
     */
    protected abstract Map<D, SortedSet<T>> createColumnDiscriminatorMap (F form);

    /**
     * Convert the aggregation into report values, the order of the values returned must match the column descriptions
     * returned by {@link #getColumnDescriptions(BaseGroupedAggregationDiscriminator, BaseReportForm)}.
     * 
     * @param aggr The aggregation data point to convert
     * @param form The original query form
     * @return List of row values for the aggregation
     */
    protected abstract List<Value> createRowValues(T aggr, F form);
    
    /**
     * @param form The form submitted by the user
     * @return The model and view to render
     */
    protected final ModelAndView renderAggregationReport(F form) throws TypeMismatchException {
        final DataTable table = buildAggregationReport(form);
        
        final String view;
        switch (form.getFormat()) {
            case csv: {
                view = "dataTableCsvView";
                break;
            }
            case html: {
                view = "dataTableHtmlView";
                break;
            }
            default: {
                view = "json";
            }
        }
        ModelAndView modelAndView = new ModelAndView(view, "table", table);
        String titleAugmentation = getReportTitleAugmentation(form);
        if (StringUtils.isNotBlank(titleAugmentation)) {
            modelAndView.addObject("titleAugmentation", getReportTitleAugmentation(form));
        }
        return  modelAndView;
    }

    /**
     * Return additional data to attach to the title of the form. This is used when
     * the user selects a single value of a multi-valued set and
     * you don't want to include the selected value in the report columns since they'd
     * be redundant; e.g. why have a graph with data showing "PortletA - Everyone",
     * "PortletB - Everyone", "PortletC - Everyone".
     *
     * Default behavior is to return null and not alter the report title.
     *
     * @param form the form
     * @return Formatted string to attach to the title of the form.  Null to
     *         not change the title of the report based on form selections.
     */
    protected String getReportTitleAugmentation(F form) {
        return null;
    }

    /**
     * Returns true to indicate report format is only data table and doesn't have
     * report graph titles, etc. so the report columns needs to fully describe
     * the data columns.  CSV and HTML tables require full column header
     * descriptions.
     *
     * @param form the form
     * @return True if report columns should have full header descriptions.
     */
    protected final boolean showFullColumnHeaderDescriptions(F form) {
        boolean showFullHeaderDescriptions = false;
        switch (form.getFormat()) {
            case csv: {
                showFullHeaderDescriptions = true;
                break;
            }
            case html: {
                showFullHeaderDescriptions = true;
                break;
            }
            default: {
                showFullHeaderDescriptions = false;
            }
        }
        return showFullHeaderDescriptions;
    }

    /**
     * Build the aggregation {@link DataTable}
     */
    protected final DataTable buildAggregationReport(F form) throws TypeMismatchException {
        //Pull data out of form for per-group fetching
        final AggregationInterval interval = form.getInterval();
        final DateMidnight start = form.getStart();
        final DateMidnight end = form.getEnd();
        
        final DateTime startDateTime = start.toDateTime();
        //Use a query end of the end date at 23:59:59
        final DateTime endDateTime = end.plusDays(1).toDateTime().minusSeconds(1);

        //Get the list of DateTimes used on the X axis in the report
        final List<DateTime> reportTimes = this.intervalHelper.getIntervalStartDateTimesBetween(interval, startDateTime, endDateTime, maxIntervals);

        final Map<D, SortedSet<T>> groupedAggregations = createColumnDiscriminatorMap(form);

        //Determine the ValueType of the date/time column. Use the most specific column type possible
        final ValueType dateTimeColumnType;
        if (interval.isHasTimePart()) {
            //If start/end are the same day just display the time
            if (startDateTime.toDateMidnight().equals(endDateTime.toDateMidnight())) {
                dateTimeColumnType = ValueType.TIMEOFDAY;
            }
            //interval has time data and start/end are on different days, show full date time
            else {
                dateTimeColumnType = ValueType.DATETIME;
            }
        }
        //interval is date only
        else {
            dateTimeColumnType = ValueType.DATE;
        }
        
        //Setup the date/time column description
        final ColumnDescription dateTimeColumn;
        switch (dateTimeColumnType) {
            case TIMEOFDAY: {
                dateTimeColumn = new ColumnDescription("time", dateTimeColumnType, "Time");
                break;
            }
            default: {
                dateTimeColumn = new ColumnDescription("date", dateTimeColumnType, "Date");
            }
        }
        
        final DataTable table = new JsonDataTable();
        table.addColumn(dateTimeColumn);

        //Setup columns in the DataTable 
        final Set<D> columnGroups = groupedAggregations.keySet();
        for (final D columnMapping : columnGroups) {
            final Collection<ColumnDescription> columnDescriptions = this.getColumnDescriptions(columnMapping, form);
            table.addColumns(columnDescriptions);
        }
        
        //Query for all aggregation data in the time range for all groups.  Only the
        //interval and discriminator data is used from the keys.
        final Set<K> keys = createAggregationsQueryKeyset(columnGroups, form);
        final BaseAggregationDao<T, K> baseAggregationDao = this.getBaseAggregationDao();
        final Collection<T> aggregations = baseAggregationDao.getAggregations(
                startDateTime,
                endDateTime,
                keys,
                extractGroupsArray(columnGroups));

        //Organize the results by group and sort them chronologically by adding them to the sorted set
        for (final T aggregation : aggregations) {
            final D discriminator = aggregation.getAggregationDiscriminator();
            final SortedSet<T> results = groupedAggregations.get(discriminator);
            results.add(aggregation);
        }
        
        //Build Map from discriminator column mapping to result iterator to allow putting results into
        //the correct column AND the correct time slot in the column
        Comparator<? super D> comparator = getDiscriminatorComparator();
        final Map<D, PeekingIterator<T>> groupedAggregationIterators =
                new TreeMap<D, PeekingIterator<T>>((comparator));
        for (final Entry<D, SortedSet<T>> groupedAggregationEntry : groupedAggregations.entrySet()) {
            groupedAggregationIterators.put(groupedAggregationEntry.getKey(), Iterators.peekingIterator(groupedAggregationEntry.getValue().iterator()));
        }
        
        /*
         * populate the data, filling in blank spots. The full list of interval DateTimes is used to create every row in the
         * query range. Then the iterator
         */
        for (final DateTime rowTime : reportTimes) {
            // create the row
            final TableRow row = new TableRow();

            // add the date to the first cell
            final Value dateTimeValue;
            switch (dateTimeColumnType) {
                case DATE: {
                    dateTimeValue = new DateValue(rowTime.getYear(), rowTime.getMonthOfYear()-1, rowTime.getDayOfMonth());
                    break;
                }
                case TIMEOFDAY: {
                    dateTimeValue = new TimeOfDayValue(rowTime.getHourOfDay(), rowTime.getMinuteOfHour(), 0);
                    break;
                }
                default: {
                    dateTimeValue = new DateTimeValue(rowTime.getYear(), rowTime.getMonthOfYear()-1, rowTime.getDayOfMonth(), rowTime.getHourOfDay(), rowTime.getMinuteOfHour(), 0, 0);
                    break;
                }
            }
            row.addCell(new TableCell(dateTimeValue));

            for (final PeekingIterator<T> groupedAggregationIteratorEntry : groupedAggregationIterators.values()) {
                List<Value> values = null;

                if (groupedAggregationIteratorEntry.hasNext()) {
                    final T aggr = groupedAggregationIteratorEntry.peek();
                    if (rowTime.equals(aggr.getDateTime())) {
                        //Data is for the correct time slot, advance the iterator
                        groupedAggregationIteratorEntry.next();
                        
                        values = createRowValues(aggr, form);
                    }
                }
                
                //Gap in the data, fill it in using a null aggregation
                if (values == null) {
                    values = createRowValues(null, form);
                }
                
                //Add the values to the row
                for (final Value value : values) {
                    row.addCell(value);
                }
            }
            
            table.addRow(row);
        }
        
        return table;
    }

    // Return the set of AggregatedGroupMappings based upon the set of column groups.
    // Since an AggregatedGroupMapping may occur multiple times in the column groups,
    // use a Set to filter down to unique values.
    private AggregatedGroupMapping[] extractGroupsArray(Set<D> columnGroups) {
        Set<AggregatedGroupMapping> groupMappings = new HashSet<AggregatedGroupMapping>();
        for (D discriminator : columnGroups) {
            groupMappings.add(discriminator.getAggregatedGroup());
        }
        return groupMappings.toArray(new AggregatedGroupMapping[0]);
    }
}
