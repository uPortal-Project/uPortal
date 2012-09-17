package org.jasig.portal.portlets.statistics;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.jasig.portal.events.aggr.AggregationInterval;
import org.jasig.portal.events.aggr.AggregationIntervalHelper;
import org.jasig.portal.events.aggr.DateDimension;
import org.jasig.portal.events.aggr.TimeDimension;
import org.jasig.portal.events.aggr.groups.AggregatedGroupLookupDao;
import org.jasig.portal.events.aggr.groups.AggregatedGroupMapping;
import org.jasig.portal.events.aggr.login.LoginAggregation;
import org.jasig.portal.events.aggr.login.LoginAggregationDao;
import org.jasig.portal.events.aggr.login.LoginAggregationKeyImpl;
import org.jasig.portal.events.aggr.login.MissingLoginDataCreator;
import org.joda.time.DateMidnight;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.DateTimeFormatterBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.portlet.ModelAndView;
import org.springframework.web.portlet.bind.annotation.RenderMapping;
import org.springframework.web.portlet.bind.annotation.ResourceMapping;

import com.google.visualization.datasource.base.TypeMismatchException;
import com.google.visualization.datasource.datatable.ColumnDescription;
import com.google.visualization.datasource.datatable.DataTable;
import com.google.visualization.datasource.datatable.TableCell;
import com.google.visualization.datasource.datatable.TableRow;
import com.google.visualization.datasource.datatable.value.DateTimeValue;
import com.google.visualization.datasource.datatable.value.NumberValue;
import com.google.visualization.datasource.datatable.value.ValueType;

/**
 * StatisticsPortletController drives stats reporting of login data.
 * 
 * @author Jen Bourey, jennifer.bourey@gmail.com
 */
@Controller
@RequestMapping("VIEW")
public class StatisticsPortletController {
    
    protected final Logger logger = LoggerFactory.getLogger(getClass());
    
    protected final static String LOGIN_FORM_NAME = "loginReportRequest";

    @Autowired
    private LoginAggregationDao<? extends LoginAggregation> loginDao;
    
    @Autowired
    private AggregationIntervalHelper intervalHelper;
    
    @Autowired
    private AggregatedGroupLookupDao aggregatedGroupDao;
    
    @RenderMapping
    public String getLoginView(Model model) throws TypeMismatchException {
        if (!model.containsAttribute(LOGIN_FORM_NAME)) {
            final LoginReportForm report = new LoginReportForm();
            report.setInterval(AggregationInterval.DAY);

            final Set<? extends AggregatedGroupMapping> groupMappings = aggregatedGroupDao.getGroupMappings();
            for (AggregatedGroupMapping mapping : groupMappings) {
                final List<Long> groupIds = report.getGroups();
                groupIds.add(mapping.getId());
            }
            
            final DateMidnight today = new DateMidnight();
            report.setStart(today.minusMonths(1));
            report.setEnd(today);
            model.addAttribute(LOGIN_FORM_NAME, report);
        }
        return "jsp/Statistics/loginTotals";
    }
    
    @ModelAttribute("intervals")
    public AggregationInterval[] getIntervals() {
        return AggregationInterval.values();
    }
    
    @ModelAttribute("groups")
    public Map<Long, String> getGroups() {
        final Map<Long, String> groups = new HashMap<Long, String>();
        final Set<? extends AggregatedGroupMapping> groupMappings = aggregatedGroupDao.getGroupMappings();
        for (AggregatedGroupMapping mapping : groupMappings) {
            groups.put(mapping.getId(), mapping.getGroupName());
        }
        return groups;
    }
    
    @InitBinder
    public void initBinder(WebDataBinder binder) {
        final DateTimeFormatter formatter = new DateTimeFormatterBuilder()
                .appendPattern("M/d/yyyy").toFormatter();
        binder.registerCustomEditor(DateMidnight.class, new CustomDateMidnightEditor(formatter, false));
    }
    
    @ResourceMapping
    public ModelAndView getLoginData(@ModelAttribute(LOGIN_FORM_NAME) LoginReportForm form) throws TypeMismatchException {

        final ModelAndView mv = new ModelAndView("json");
        
        final DataTable table = new JsonDataTable();
        table.addColumn(new ColumnDescription("date", ValueType.DATETIME, "Date"));

        final List<List<LoginAggregation>> data = new ArrayList<List<LoginAggregation>>();
        
        //Pull data out of form for per-group fetching
        final AggregationInterval interval = form.getInterval();
        final DateMidnight start = form.getStart();
        final DateMidnight end = form.getEnd();
        final DateTime startDateTime = start.toDateTime();
        final DateTime endDateTime = end.toDateTime();
        
        int num = 0;
        //Load aggregation data for each group
        for (Long groupId : form.getGroups()) {
            //Load the group data
            final AggregatedGroupMapping groupMapping = aggregatedGroupDao.getGroupMapping(groupId);
            final String groupName = groupMapping.getGroupName();
            table.addColumn(new ColumnDescription(groupName, ValueType.NUMBER, groupName));
            
            //Get all the aggregations for the group
            //TODO add API to load all aggregations for set of groups
            final List<LoginAggregation> aggrs = new ArrayList<LoginAggregation>();
            aggrs.addAll(loginDao.getAggregations(start.toDateTime(), end.toDateTime(), new LoginAggregationKeyImpl(interval, groupMapping)));
            
            logger.debug("Found {} data points for group {}", aggrs.size(), groupName);
            
            //Fill in the blanks to have a complete data set for the range
            final List<LoginAggregation> complete = intervalHelper.fillInBlanks(interval, startDateTime, endDateTime, aggrs, new MissingLoginDataCreator(groupMapping));
            data.add(complete);
            
            logger.debug("Added {} data points for group {}", complete.size(), groupName);
            
            if (num == 0) {
                num = complete.size();
            }
            else if (num != complete.size()) {
                throw new RuntimeException("LOGIC BUG: NOT ALL DATA SETS ARE OF EQUAL LENGTH, expected " + num + " but is " + complete.size() + " for group " + groupName);
            }
        }
        
        for (int i = 0; i < num; i++) {
            
            // create the row
            final TableRow row = new TableRow();

            // add the date to the first cell
            final DateDimension dateDimension = data.get(0).get(i).getDateDimension();
            final TimeDimension timeDimension = data.get(0).get(i).getTimeDimension();
            final DateTimeValue dateValue = new DateTimeValue(dateDimension.getYear(), dateDimension.getMonth()-1, dateDimension.getDay(), timeDimension.getHour(), timeDimension.getMinute(), 0, 0);
            row.addCell(new TableCell(dateValue));
            
            for (List<LoginAggregation> groupData : data) {
                // add the login count to the second cell
                final LoginAggregation loginAggregation = groupData.get(i);
                
                final int loginCount;
                if (form.isUniqueOnly()) {
                	loginCount = loginAggregation.getUniqueLoginCount();
                } else {
                	loginCount = loginAggregation.getLoginCount();
                }
                row.addCell(new TableCell(new NumberValue(loginCount)));
            }
            table.addRow(row);
        }

        mv.addObject("logins", table);
        return mv;
    }
    
}
