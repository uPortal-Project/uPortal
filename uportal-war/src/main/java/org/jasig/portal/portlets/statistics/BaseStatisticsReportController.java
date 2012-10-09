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
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;

import javax.portlet.ResourceURL;

import org.jasig.portal.events.aggr.AggregationInterval;
import org.jasig.portal.events.aggr.AggregationIntervalHelper;
import org.jasig.portal.events.aggr.BaseAggregation;
import org.jasig.portal.events.aggr.BaseAggregationDao;
import org.jasig.portal.events.aggr.BaseAggregationDateTimeComparator;
import org.jasig.portal.events.aggr.BaseAggregationKey;
import org.jasig.portal.events.aggr.groups.AggregatedGroupLookupDao;
import org.jasig.portal.events.aggr.groups.AggregatedGroupMapping;
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

import AggregatedGroupMapping.AggregatedGroupMappingNameComparator;

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
public abstract class BaseStatisticsReportController<T extends BaseAggregation<K>, K extends BaseAggregationKey, F extends BaseReportForm> {
    /**
     * List of intervals in the prefered report order. This is the order they are tested against
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
    private AggregatedGroupLookupDao aggregatedGroupDao;
    
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
        return this.getBaseAggregationDao().getAggregationIntervals();
    }
    
    /**
     * @return Groups that exist for the aggregation
     * @see BaseAggregationDao#getAggregatedGroupMappings()
     */
    @ModelAttribute("groups")
    public Set<AggregatedGroupMapping> getGroups() {
        return this.getBaseAggregationDao().getAggregatedGroupMappings();
    }
    
    /**
     * @return The default report request form to use, populates the inital form view
     */
    @ModelAttribute("reportRequest")
    public final F getReportForm() {
        final F report = createReportFormRequest();
        
        setReportFormDateRangeAndInterval(report);
        
        setReportFormGroups(report);

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
     * Set the groups to have selected by default
     */
    protected void setReportFormGroups(final F report) {
        final List<Long> groups = report.getGroups();
        for (final AggregatedGroupMapping group : this.getGroups()) {
            groups.add(group.getId());
        }
    }
    
    /**
     * Set the start/end date and the interval to have selected by default
     */
    protected void setReportFormDateRangeAndInterval(final F report) {
        //Determine default interval based on the intervals available for this aggregation
        report.setInterval(AggregationInterval.DAY);
        final Set<AggregationInterval> intervals = this.getIntervals();
        for (final AggregationInterval preferredInterval : PREFERRED_INTERVAL_ORDER) {
            if (intervals.contains(preferredInterval)) {
                report.setInterval(preferredInterval);
                break;
            }
        }
        
        //Set the report end date as today
        final DateMidnight today = new DateMidnight();
        report.setEnd(today);
        
        //Determine the best start date based on the selected interval
        final DateMidnight start;
        switch (report.getInterval()) {
            case MINUTE: {
                start = today.minusDays(1);
                break;
            }
            case FIVE_MINUTE: {
                start = today.minusDays(2);
                break;
            }
            case HOUR: {
                start = today.minusWeeks(1);
                break;
            }
            case DAY: {
                start = today.minusMonths(1);
                break;
            }
            case WEEK: {
                start = today.minusMonths(3);
                break;
            }
            case MONTH: {
                start = today.minusYears(1);
                break;
            }
            case ACADEMIC_TERM: {
                start = today.minusYears(2);
                break;
            }
            case CALENDAR_QUARTER: {
                start = today.minusYears(2);
                break;
            }
            case YEAR: {
                start = today.minusYears(10);
                break;
            }
            default: {
                start = today.minusWeeks(1);
            }
        }
        
        report.setStart(start);
    }
    
    /**
     * @return The {@link BaseReportForm} implementation used to populate the initial view
     */
    protected abstract F createReportFormRequest();
    
    /**
     * @return The dao for the aggregation
     */
    protected abstract BaseAggregationDao<T, K> getBaseAggregationDao();
    
    /**
     * Create the key used to execute {@link BaseAggregationDao#getAggregations(DateTime, DateTime, BaseAggregationKey, AggregatedGroupMapping...)}
     * 
     * @param groups The groups being queried for
     * @param form The original query form
     * @return The partial key to query with
     */
    protected abstract K createAggregationsQueryKey(Set<AggregatedGroupMapping> groups, F form);
    
    /**
     * Get the column descriptors to use for each group in the report. The order of the returned columns 
     * is VERY important and must match the order of values as retuned by {@link #createRowValues(BaseAggregation, BaseReportForm)}
     * 
     * @param group The group to crate the column descriptors for
     * @param form The original query form
     * @return List of column descriptors for the group
     */
    protected abstract List<ColumnDescription> getColumnDescriptions(AggregatedGroupMapping group, F form);
    
    /**
     * Convert the aggregation into report values, the order of the values returned must match the column descriptions
     * returned by {@link #getColumnDescriptions(AggregatedGroupMapping, BaseReportForm)}.
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
        
        return new ModelAndView(view, "table", table);
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
        
        final Map<AggregatedGroupMapping, SortedSet<T>> groupedAggregations = loadGroupMappings(form.getGroups());
        
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
        final Set<AggregatedGroupMapping> queryGroups = groupedAggregations.keySet();
        for (final AggregatedGroupMapping groupMapping : queryGroups) {
            final Collection<ColumnDescription> columnDescriptions = this.getColumnDescriptions(groupMapping, form);
            table.addColumns(columnDescriptions);
        }
        
        //Query for all aggregation data in the time range for all groups
        final K key = this.createAggregationsQueryKey(queryGroups, form);
        final BaseAggregationDao<T, K> baseAggregationDao = this.getBaseAggregationDao();
        final Collection<T> aggregations = baseAggregationDao.getAggregations(
                startDateTime, 
                endDateTime, 
                key,
                queryGroups.toArray(new AggregatedGroupMapping[0]));

        //Organize the results by group and sort them chronologically by adding them to the sorted set
        for (final T aggregation : aggregations) {
            final AggregatedGroupMapping aggregatedGroup = aggregation.getAggregatedGroup();
            final SortedSet<T> results = groupedAggregations.get(aggregatedGroup);
            results.add(aggregation);
        }
        
        //Build Map from group mapping to result iterator
        final Map<AggregatedGroupMapping, PeekingIterator<T>> groupedAggregationIterators = new TreeMap<AggregatedGroupMapping, PeekingIterator<T>>(AggregatedGroupMappingNameComparator.INSTANCE);
        for (final Entry<AggregatedGroupMapping, SortedSet<T>> groupedAggregationEntry : groupedAggregations.entrySet()) {
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

    /**
     * Convert the list of aggregated group ids to a map of {@link AggregatedGroupMapping}s to the {@link SortedSet} that
     * will be used to collate the results
     */
    private Map<AggregatedGroupMapping, SortedSet<T>> loadGroupMappings(List<Long> groups) {
        //Collections used to track the queried groups and the results
        final Map<AggregatedGroupMapping, SortedSet<T>> groupedAggregations = new TreeMap<AggregatedGroupMapping, SortedSet<T>>(AggregatedGroupMappingNameComparator.INSTANCE);
        
        //Get concrete group mapping objects that are being queried for
        for (final Long queryGroupId : groups) {
            final AggregatedGroupMapping groupMapping = this.aggregatedGroupDao.getGroupMapping(queryGroupId);

            //Create the set the aggregations for this group will be stored in, sorted chronologically 
            final SortedSet<T> aggregations = new TreeSet<T>(BaseAggregationDateTimeComparator.INSTANCE);
            
            //Map the group to the set
            groupedAggregations.put(groupMapping, aggregations);
        }

        return groupedAggregations;
    }
}
