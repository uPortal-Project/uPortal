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

package org.jasig.portal.rest;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Pattern;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.sql.DataSource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jasig.portal.EntityIdentifier;
import org.jasig.portal.events.aggr.AggregationInterval;
import org.jasig.portal.events.aggr.BaseAggregationDateTimeComparator;
import org.jasig.portal.events.aggr.groups.AggregatedGroupLookupDao;
import org.jasig.portal.events.aggr.groups.AggregatedGroupMapping;
import org.jasig.portal.events.aggr.portletlayout.PortletLayoutAggregation;
import org.jasig.portal.events.aggr.portletlayout.PortletLayoutAggregationDao;
import org.jasig.portal.events.aggr.portletlayout.PortletLayoutAggregationKey;
import org.jasig.portal.events.aggr.portletlayout.PortletLayoutAggregationKeyImpl;
import org.jasig.portal.events.aggr.portlets.AggregatedPortletLookupDao;
import org.jasig.portal.events.aggr.portlets.AggregatedPortletMapping;
import org.jasig.portal.portlet.dao.IPortletDefinitionDao;
import org.jasig.portal.portlet.om.IPortletDefinition;
import org.jasig.portal.security.AdminEvaluator;
import org.jasig.portal.security.IAuthorizationPrincipal;
import org.jasig.portal.security.IPerson;
import org.jasig.portal.security.IPersonManager;
import org.jasig.portal.services.AuthorizationService;
import org.jasig.portal.utils.cache.CacheFactory;
import org.joda.time.DateTime;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

/**
 * Spring controller that returns a JSON representation of how many times users 
 * have either added each portlet in the specified number of days, counting 
 * backwards from the specified day (inclusive). 
 * <p>Request parameters:</p>
 * <ul>
 *   <li>days: Number of calendar days to include in the report; default is 30</li>
 *   <li>fromDate: Date (inclusive) from which to count backwards; default is today</li>
 * </ul>
 *
 * @author Drew Wills, drew@unicon.net
 */
@Controller
public class StatsLayoutModificationsController implements InitializingBean {

    private static final int MIN_DAYS = 0;
    
    private DataSource dataSource;
    private EventCountFactory factory;
    private final Pattern datePattern = Pattern.compile("\\d+/\\d+/\\d+");
    private final DateFormat format = DateFormat.getDateInstance(DateFormat.SHORT);
    private IPersonManager personManager;
    private CacheFactory cacheFactory;
    private final Log log = LogFactory.getLog(getClass());

    @Autowired
    protected PortletLayoutAggregationDao<PortletLayoutAggregation> portletLayoutDao;

    @Autowired
    protected AggregatedPortletLookupDao aggregatedPortletLookupDao;

    @Autowired
    protected AggregatedGroupLookupDao aggregatedGroupLookupDao;

    @Autowired
    protected IPortletDefinitionDao portletDefinitionDao;

    @Autowired
    public void setPersonManager(IPersonManager personManager) {
        this.personManager = personManager;
    }

    @Autowired
    public void setCacheFactory(CacheFactory cacheFactory) {
        this.cacheFactory = cacheFactory;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        this.factory = new EventCountFactory(portletLayoutDao, aggregatedPortletLookupDao,aggregatedGroupLookupDao,portletDefinitionDao,cacheFactory);
    }

    @RequestMapping(value="/userLayoutModificationsCounts")
    public ModelAndView getEventCounts(HttpServletRequest req, HttpServletResponse res) throws ServletException {
        Map<String, Object> model = new HashMap<String, Object>();
        Set<AggregationInterval> intervals = getIntervals();

        String intervalParam = req.getParameter("interval");
        AggregationInterval interval = null;

        if((intervalParam != null) && (intervalParam.length() > 0)) {
            interval = AggregationInterval.valueOf(req.getParameter("interval"));
        } else {
            interval = intervals.iterator().next();
        }

        model.put("intervals",intervals);
        model.put("counts",getCounts(req,interval));
        return new ModelAndView("jsonView",model);
    }

    private final Set<AggregationInterval> getIntervals() {
        final Set<AggregationInterval> unsorted = this.portletLayoutDao.getAggregationIntervals();
        final Set<AggregationInterval> sorted = new TreeSet<AggregationInterval>(unsorted);
        return sorted;
    }

    private final List<CountingTuple> getCounts(HttpServletRequest req,AggregationInterval interval) {
        List<CountingTuple> completeList = factory.getEventCounts(interval);
        IPerson user = personManager.getPerson(req);
        List<CountingTuple> filteredList = filterByPermissions(user, completeList);
        return filteredList;
    }

    /*
     * Implementation
     */
    
    private List<CountingTuple> filterByPermissions(IPerson user, List<CountingTuple> completeList) {
        
        // Assertions
        if (user == null) {
            String msg = "Argument 'user' cannot be null";
            throw new IllegalArgumentException(msg);
        }
        if (completeList == null) {
            String msg = "Argument 'completeList' cannot be null";
            throw new IllegalArgumentException(msg);
        }
        
        if(AdminEvaluator.isAdmin(user)) {
            // Admins may see the complete list
            return completeList;
        }
        
        EntityIdentifier ei = user.getEntityIdentifier();
        IAuthorizationPrincipal ap = AuthorizationService.instance().newPrincipal(ei.getKey(), ei.getType());

        List<CountingTuple> rslt = new ArrayList<CountingTuple>();
        for (CountingTuple tuple : completeList) {
            if (ap.canSubscribe(String.valueOf(tuple.getId()))) {
                rslt.add(tuple);
            }
        }
        
        return rslt;
        
    }

    /*
     * Nested Types
     */
    
    public static final class CountingTuple implements Comparable<CountingTuple> {
        
        private final int id;
        private final String portletFName;
        private final String portletTitle;
        private String portletDescription = "[no description available]";  // default
        private final int count;
        
        public CountingTuple(int id, String portletFName, String portletTitle, String portletDescription, int count) {

            // Assertions
            if (portletFName == null) {
                String msg = "Argument 'portletFName' cannot be null";
                throw new IllegalArgumentException(msg);
            }
            if (portletTitle == null) {
                String msg = "Argument 'portletTitle' cannot be null";
                throw new IllegalArgumentException(msg);
            }
            // NB:  'portletDescription' actually can be null

            this.id = id;
            this.portletFName = portletFName;
            this.portletTitle = portletTitle;
            if (portletDescription != null) {
                this.portletDescription = portletDescription;
            }
            this.count = count;
        }

        public int getId() {
            return id;
        }

        public String getPortletFName() {
            return portletFName;
        }

        public String getPortletTitle() {
            return portletTitle;
        }

        public String getPortletDescription() {
            return portletDescription;
        }

        public int getCount() {
            return count;
        }

        @Override
        public int compareTo(CountingTuple tuple) {
            // Natural order for these is count
            return new Integer(count).compareTo(tuple.getCount());
        }
        
    }
    
    private static final class EventCountFactory {
        
        private static final String PORTLETS_ADDED_CACHE_KEY =
            "PortalStats.org.jasig.portal.rest.StatsLayoutModificationsController.portletsAdded";

        private final PortletLayoutAggregationDao<PortletLayoutAggregation> portletLayoutDao;
        private final AggregatedPortletLookupDao aggregatedPortletLookupDao;
        private final AggregatedGroupLookupDao aggregatedGroupLookupDao;
        private final IPortletDefinitionDao portletDefinitionDao;
        private final Map<AggregationInterval,List<CountingTuple>> cache;
        
        public EventCountFactory(PortletLayoutAggregationDao<PortletLayoutAggregation> portletLayoutDao,
                                 AggregatedPortletLookupDao aggregatedPortletLookupDao,
                                 AggregatedGroupLookupDao aggregatedGroupLookupDao,
                                 IPortletDefinitionDao portletDefinitionDao,
                                 CacheFactory cacheFactory) {

            // Assertions
            if (portletLayoutDao == null) {
                String msg = "Argument 'portletLayoutDao' cannot be null";
                throw new IllegalArgumentException(msg);
            }
            if (aggregatedPortletLookupDao == null) {
                String msg = "Argument 'aggregatedPortletLookupDao' cannot be null";
                throw new IllegalArgumentException(msg);
            }
            if (aggregatedGroupLookupDao == null) {
                String msg = "Argument 'aggregatedGroupLookupDao' cannot be null";
                throw new IllegalArgumentException(msg);
            }
            if (portletDefinitionDao == null) {
                String msg = "Argument 'portletDefinitionDao' cannot be null";
                throw new IllegalArgumentException(msg);
            }
            if (cacheFactory == null) {
                String msg = "Argument 'cacheFactory' cannot be null";
                throw new IllegalArgumentException(msg);
            }

            this.portletLayoutDao = portletLayoutDao;
            this.aggregatedPortletLookupDao = aggregatedPortletLookupDao;
            this.aggregatedGroupLookupDao = aggregatedGroupLookupDao;
            this.portletDefinitionDao = portletDefinitionDao;
            this.cache = cacheFactory.getCache(PORTLETS_ADDED_CACHE_KEY);

        }

        public List<CountingTuple> getEventCounts(AggregationInterval interval) {
            List<CountingTuple> rslt = (List<CountingTuple>) cache.get(interval);
            if(rslt == null) {
                rslt = buildEventCounts(interval);
                cache.put(interval, rslt);
            }
            return rslt;
        }
        
        private List<CountingTuple> buildEventCounts(AggregationInterval interval) {
            DateTime[] dates = getDateRange(interval);

            Set<AggregatedPortletMapping> portletMappings = aggregatedPortletLookupDao.getPortletMappings();
            AggregatedGroupMapping group = aggregatedGroupLookupDao.getGroupMapping("local","Everyone");
            List<CountingTuple> completeList =  new ArrayList<CountingTuple>();

            for(AggregatedPortletMapping portletMapping : portletMappings) {
                IPortletDefinition def = portletDefinitionDao.getPortletDefinitionByFname(portletMapping.getFname());
                PortletLayoutAggregationKey key = new PortletLayoutAggregationKeyImpl(interval,group,portletMapping);
                List<PortletLayoutAggregation> aggregations = portletLayoutDao.getAggregations(dates[0],dates[1],key,group);

                if((aggregations != null) && (!aggregations.isEmpty())) {
                    Collections.sort(aggregations,BaseAggregationDateTimeComparator.INSTANCE);
                    DateTime now = new DateTime();
                    DateTime compval = null;
                    switch(interval) {
                        case MINUTE: {
                            compval = now.minusMinutes(1);
                            break;
                        }
                        case FIVE_MINUTE: {
                            compval = now.minusMinutes(5);
                            break;
                        }
                        case HOUR: {
                            compval = now.minusHours(1);
                            break;
                        }
                        case DAY: {
                            compval = now.minusDays(1);
                            break;
                        }
                        case WEEK: {
                            compval =  now.minusWeeks(1);
                            break;
                        }
                        case MONTH: {
                            compval = now.minusMonths(1);
                            break;
                        }
                        case CALENDAR_QUARTER: {
                            compval = now.minusMonths(3);
                            break;
                        }
                        case YEAR: {
                            compval = now.minusYears(1);
                            break;
                        }
                        default: {
                            compval = now.minusYears(1);
                        }
                    }

                    for(int index=aggregations.size()-1; index >= 0; index--) {
                        PortletLayoutAggregation aggregation = aggregations.get(index);
                        if(!aggregation.getDateTime().isAfter(compval) && !aggregation.getDateTime().equals(compval)) {
                            break;
                        }

                        int count = aggregation.getAddCount() - aggregation.getDeleteCount();
                        if(count > 0) {
                            int id = (int)def.getPortletDefinitionId().getLongId();
                            completeList.add(new CountingTuple(id, def.getFName(), def.getTitle(), def.getDescription(), count));
                        }
                    }
                }
            }
            return completeList;
        }

        private DateTime[] getDateRange(AggregationInterval interval) {
            DateTime[] dates =  new DateTime[2];

            //Set the report end date as today
            final DateTime today = new DateTime();
            dates[1] = today;

            //Determine the best start date based on the selected interval
            final DateTime start;
            switch (interval) {
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

            dates[0] = start;
            return dates;
        }
    }
}
