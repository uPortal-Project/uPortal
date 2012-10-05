package org.jasig.portal.portlets.statistics;

import java.util.Collection;
import java.util.Set;
import java.util.TreeSet;

import org.jasig.portal.events.aggr.AggregationInterval;
import org.jasig.portal.events.aggr.AggregationIntervalHelper;
import org.joda.time.DateMidnight;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.DateTimeFormatterBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.portlet.ModelAndView;
import org.springframework.web.portlet.bind.annotation.RenderMapping;
import org.springframework.web.portlet.bind.annotation.ResourceMapping;

import com.google.visualization.datasource.base.TypeMismatchException;

/**
 * StatisticsPortletController drives stats reporting
 * 
 * @author Jen Bourey, jennifer.bourey@gmail.com
 */
@Controller
@RequestMapping("VIEW")
public class StatisticsPortletController {
    protected final Logger logger = LoggerFactory.getLogger(getClass());
    
    private Set<String> statisticsReportControllerNames;
    
    @Autowired
    private AggregationIntervalHelper intervalHelper;
    
    @Value("${org.jasig.portal.portlets.statistics.maxIntervals}")
    private int maxIntervals = 4000;
    
    @Autowired
    public void setStatisticsReportControllers(Collection<BaseStatisticsReportController<?, ?, ?>> statisticsReportControllers) {
        this.statisticsReportControllerNames = new TreeSet<String>();
        
        for (final BaseStatisticsReportController<?, ?, ?> controller : statisticsReportControllers) {
            this.statisticsReportControllerNames.add(controller.getReportName());
        }
    }
    
    @InitBinder
    public void initBinder(WebDataBinder binder) {
        final DateTimeFormatter formatter = new DateTimeFormatterBuilder().appendPattern("M/d/yyyy").toFormatter();
        binder.registerCustomEditor(DateMidnight.class, new CustomDateMidnightEditor(formatter, false));
    }
    
    @RenderMapping
    public String getReportList() throws TypeMismatchException {
        return "jsp/Statistics/reportList";
    }
    
    @ModelAttribute("reports")
    public Set<String> getIntervals() {
        return this.statisticsReportControllerNames;
    }
    
    @ModelAttribute("maxIntervals")
    public Integer getMaxIntervals() {
        return this.maxIntervals;
    }
    
    @ResourceMapping("intervalCount")
    public ModelAndView getIntervalCount(@RequestParam("interval") AggregationInterval interval, @RequestParam("start") DateMidnight start, @RequestParam("end") DateMidnight end) throws TypeMismatchException {
        final int intervalsBetween = this.intervalHelper.intervalsBetween(interval, start.toDateTime(), end.plusDays(1).toDateTime().minusSeconds(1));
        return new ModelAndView("json", "intervalsBetween", intervalsBetween);
    }
}
