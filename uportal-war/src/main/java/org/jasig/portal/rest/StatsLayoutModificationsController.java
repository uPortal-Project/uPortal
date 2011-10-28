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

import java.io.Serializable;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import javax.annotation.Resource;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.sql.DataSource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jasig.portal.EntityIdentifier;
import org.jasig.portal.security.AdminEvaluator;
import org.jasig.portal.security.IAuthorizationPrincipal;
import org.jasig.portal.security.IPerson;
import org.jasig.portal.security.IPersonManager;
import org.jasig.portal.services.AuthorizationService;
import org.jasig.portal.utils.cache.CacheFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.simple.ParameterizedRowMapper;
import org.springframework.jdbc.core.simple.SimpleJdbcTemplate;
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
    private int maxDays = 365;  // default
    private final Pattern datePattern = Pattern.compile("\\d+/\\d+/\\d+");
    private final DateFormat format = DateFormat.getDateInstance(DateFormat.SHORT);
    private IPersonManager personManager;
    private CacheFactory cacheFactory;
    private final Log log = LogFactory.getLog(getClass());
    
    @Resource(name="RawEventsDB")
    public void setDataSource(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public void setMaxDays(int maxDays) {
        this.maxDays = maxDays;
    }

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
        this.factory = new EventCountFactory(dataSource, cacheFactory);
    }

    @RequestMapping(value="/userLayoutModificationsCounts")
    public ModelAndView getEventCounts(HttpServletRequest req, HttpServletResponse res) throws ServletException {
        
        // Days parameter
        int days = 30;  // default
        if (req.getParameter("days") != null) {
            String daysParam = req.getParameter("days").trim();
            try {
                days = Integer.parseInt(daysParam);
            } catch (NumberFormatException nfe) {
                String msg = "Unrecognizable days parameter (must be a valid integer): " + daysParam;
                log.warn(msg, nfe);
                throw new ServletException(msg, nfe);
            }
        }
        
        // fromDate parameter
        Calendar dateOnOrBefore = Calendar.getInstance();  
        dateOnOrBefore.set(Calendar.HOUR_OF_DAY, 0);
        dateOnOrBefore.set(Calendar.MINUTE, 0);
        dateOnOrBefore.set(Calendar.SECOND, 0);
        dateOnOrBefore.set(Calendar.MILLISECOND, 0);
        dateOnOrBefore.roll(Calendar.DATE, true); // default is tomorrow with time fields cleared
        if (req.getParameter("fromDate") != null) {
            String fromDateParam = req.getParameter("fromDate").trim();
            // If the user doesn't enter a date, the UI sends "today" (or other 
            // string), so ignore anything that's not even close...
            if (datePattern.matcher(fromDateParam).matches()) {
                try {
                    Date fromDate = format.parse(fromDateParam);
                    // Need to add one day, since the report is inclusive
                    Calendar cal = Calendar.getInstance();
                    cal.setTime(fromDate);
                    cal.add(Calendar.DATE, 1);
                    dateOnOrBefore = cal;
                } catch (ParseException pe) {
                    // Passing a bad date is ok, it just results in the default
                    if (log.isInfoEnabled()) {
                        String msg = "Unrecognizable fromDate parameter (format 'mm/dd/yyyy'): " + fromDateParam;
                        log.info(msg, pe);
                    }
                }
            }
        }

        // Be certain days is within prescribed limits
        if (days < MIN_DAYS) {
            days = MIN_DAYS;
        } else if (days > maxDays) {
            days = maxDays;
        }
        
        List<CountingTuple> completeList = factory.getEventCounts(dateOnOrBefore.getTime(), days);

        IPerson user = personManager.getPerson(req);
        List<CountingTuple> filteredList = filterByPermissions(user, completeList);
        
        return new ModelAndView("jsonView", "counts", filteredList);

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
        
        private static final String ADDED_PORTLETS_SQL = 
            "SELECT uppd.portlet_def_id, uppd.portlet_fname, uppd.portlet_title, uppd.portlet_desc, (SELECT COUNT(*) " +
                "FROM stats_event ste, stats_event_type stet, stats_channel stc " +
                "WHERE ste.type_id = stet.id " +
                "AND stet.type = 'LAYOUT_CHANNEL_ADDED' " +
                "AND ste.id = stc.event_id " +
                "AND stc.definition_id = uppd.portlet_def_id " +
                "AND ste.act_date >= ? AND ste.act_date <= ?) " +
            "FROM up_portlet_def uppd";
        private static final String PORTLETS_ADDED_CACHE_KEY = 
            "PortalStats.org.jasig.portal.rest.StatsLayoutModificationsController.portletsAdded";

        private final SimpleJdbcTemplate simpleJdbcTemplate;
        private final RowMapperImpl rowMapper = new RowMapperImpl();
        private final Map<CacheTuple,List<CountingTuple>> cache;
        
        public EventCountFactory(DataSource dataSource, CacheFactory cacheFactory) {

            // Assertions
            if (dataSource == null) {
                String msg = "Argument 'dataSource' cannot be null";
                throw new IllegalArgumentException(msg);
            }
            if (cacheFactory == null) {
                String msg = "Argument 'cacheFactory' cannot be null";
                throw new IllegalArgumentException(msg);
            }

            this.simpleJdbcTemplate = new SimpleJdbcTemplate(dataSource);
            this.cache = cacheFactory.getCache(PORTLETS_ADDED_CACHE_KEY);

        }

        public List<CountingTuple> getEventCounts(Date dateOnOrBefore, int days) {
            CacheTuple tuple = new CacheTuple(dateOnOrBefore.getTime(), days);
            List<CountingTuple> rslt = (List<CountingTuple>) cache.get(tuple);
            if(rslt == null) {
                rslt = buildEventCounts(dateOnOrBefore, days);
                cache.put(tuple, rslt);
            }
            return rslt;
        }
        
        private List<CountingTuple> buildEventCounts(Date dateOnOrBefore, int days) {

            Calendar cal = Calendar.getInstance();
            cal.add(Calendar.DATE, -days);
            Date dateOnOrAfter = cal.getTime();
            
            List<CountingTuple> rslt = simpleJdbcTemplate.query(
                        ADDED_PORTLETS_SQL, 
                        rowMapper, 
                        dateOnOrAfter, dateOnOrBefore);
            // Remove rows with zero count... not interested in those
            int index = 0;
            while (rslt.size() > index) {
                if (rslt.get(index).getCount() == 0) {
                    rslt.remove(index);
                } else {
                    index += 1;
                }
            }
            // We're interested in descending order 
            Collections.sort(rslt, Collections.reverseOrder());

            return rslt;

        }

    }
    
    private static final class RowMapperImpl implements ParameterizedRowMapper<CountingTuple> {

        @Override
        public CountingTuple mapRow(ResultSet rs, int index) throws SQLException {
            return new CountingTuple(
                    rs.getInt("portlet_def_id"), 
                    rs.getString("portlet_fname"), 
                    rs.getString("portlet_title"), 
                    rs.getString("portlet_desc"), 
                    rs.getInt(5));
        }
        
    }
    
    private static final class CacheTuple implements Serializable {
        
        private static final long serialVersionUID = 1L;

        private final long time;
        private final int days;
        
        public CacheTuple(long time, int days) {
            this.time = time;
            this.days = days;
        }
        
        @Override
        public boolean equals(Object o) {
            boolean rslt = false;
            if (o instanceof CacheTuple) {
                CacheTuple p = (CacheTuple) o;
                rslt = p.time == time && p.days == days;
            }
            return rslt;
        }
        
    }

}
