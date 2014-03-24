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
package org.jasig.portal.portlets.activity;

import com.google.visualization.datasource.base.TypeMismatchException;
import org.jasig.portal.events.aggr.AggregationInterval;
import org.jasig.portal.events.aggr.action.SearchRequestAggregation;
import org.jasig.portal.events.aggr.action.SearchRequestAggregationDao;
import org.jasig.portal.events.aggr.action.SearchRequestAggregationImpl;
import org.jasig.portal.events.aggr.concuser.ConcurrentUserAggregation;
import org.jasig.portal.events.aggr.concuser.ConcurrentUserAggregationDao;
import org.jasig.portal.events.aggr.concuser.ConcurrentUserAggregationKey;
import org.jasig.portal.events.aggr.concuser.ConcurrentUserAggregationKeyImpl;
import org.jasig.portal.events.aggr.groups.AggregatedGroupLookupDao;
import org.jasig.portal.events.aggr.groups.AggregatedGroupMapping;
import org.jasig.portal.events.aggr.login.LoginAggregation;
import org.jasig.portal.events.aggr.login.LoginAggregationDao;
import org.jasig.portal.events.aggr.login.LoginAggregationKey;
import org.jasig.portal.events.aggr.login.LoginAggregationKeyImpl;
import org.jasig.portal.groups.IEntityGroup;
import org.jasig.portal.security.IPerson;
import org.jasig.portal.services.GroupService;
import org.joda.time.DateMidnight;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.portlet.bind.annotation.RenderMapping;
import org.springframework.web.servlet.ModelAndView;

import javax.portlet.PortletPreferences;
import javax.portlet.PortletRequest;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Chris Waymire (chris@waymire.net)
 */
@Controller
@RequestMapping("VIEW")
public class ActivityController {
    private static final String PREFERENCE_PREFIX = "PortalActivity." + ActivityController.class.getSimpleName() + ".";
    private static final String PREFERENCE_MASTER_GROUP = PREFERENCE_PREFIX + "masterGroup";
    private static final String PREFERENCE_DISPLAY_GROUPS = PREFERENCE_PREFIX + "displayGroups";
    private static final String PREFERENCE_DISPLAY_OTHER = PREFERENCE_PREFIX + "displayOther";
    private static final String PREFERENCE_UNIQUE_LOGINS = PREFERENCE_PREFIX + "uniqueLogins";
    private static final String PREFERENCE_SHOW_SEACHES = PREFERENCE_PREFIX + "showSearches";
    private static final String DEFAULT_PREFERENCE_MASTER_GROUP = "Everyone";
    private static final String[] DEFAULT_PREFERENCE_DISPLAY_GROUPS = new String[]{ };
    private static final String DEFAULT_PREFERENCE_DISPLAY_OTHER = "true";
    private static final String DEFAULT_PREFERENCE_UNIQUE_LOGINS = "true";
    private static final String DEFAULT_PREFERENCE_SHOW_SEARCHES = "true";

    private static final int NOW = 1;
    private static final int TODAY = 2;
    private static final int YESTERDAY = 3;

    private AggregatedGroupLookupDao aggregatedGroupLookupDao;
    private SearchRequestAggregationDao<SearchRequestAggregation> searchRequestAggregationDao;
    private ConcurrentUserAggregationDao<ConcurrentUserAggregation> concurrentUserAggregationDao;
    private LoginAggregationDao<LoginAggregation> loginAggregationDao;

    @Autowired
    public void setAggregatedGroupLookupDao(AggregatedGroupLookupDao aggregatedGroupLookupDao)
    {
        this.aggregatedGroupLookupDao = aggregatedGroupLookupDao;
    }

    @Autowired
    public void setSearchRequestAggregationDao(SearchRequestAggregationDao<SearchRequestAggregation> searchRequestAggregationDao)
    {
        this.searchRequestAggregationDao = searchRequestAggregationDao;
    }

    @Autowired
    public void setConcurrentUserAggregationDao(ConcurrentUserAggregationDao<ConcurrentUserAggregation> concurrentUserAggregationDao)
    {
        this.concurrentUserAggregationDao = concurrentUserAggregationDao;
    }

    @Autowired
    public void setLoginAggregationDao(LoginAggregationDao<LoginAggregation> loginAggregationDao)
    {
        this.loginAggregationDao = loginAggregationDao;
    }

    @RenderMapping
    public ModelAndView summary(PortletRequest request) throws TypeMismatchException {
        final Map<String, Object> model = new HashMap<String, Object>();
        final PortalActivity now = buildPortalActivity(request, NOW);
        final PortalActivity today = buildPortalActivity(request, TODAY);
        final PortalActivity yesterday = buildPortalActivity(request, YESTERDAY);
        model.put("usageNow", now);
        model.put("usageToday", today);
        model.put("usageYesterday", yesterday);

        // Searches
        List<SearchInfo> popularSearchTerms = Collections.emptyList();  // default
        final PortletPreferences prefs = request.getPreferences();
        final Boolean showSearches = Boolean.valueOf(prefs.getValue(PREFERENCE_SHOW_SEACHES, DEFAULT_PREFERENCE_SHOW_SEARCHES));
        if (showSearches) {
            popularSearchTerms = getPopularSearchTerms();
        }
        model.put("showSearches", showSearches);
        model.put("popularSearchTerms", popularSearchTerms);

        return new ModelAndView("jsp/Activity/activity", model);
    }

    private PortalActivity buildPortalActivity(PortletRequest request,int timeframe)
    {
        PortletPreferences prefs = request.getPreferences();
        DateTime begin, end;
        final AggregationInterval interval;
        final List<PortalGroupActivity> groupActivities = new ArrayList<PortalGroupActivity>();

        switch(timeframe)
        {
            case NOW:
            {
                end = new DateTime();
                begin = end.minusHours(1);
                interval = AggregationInterval.FIVE_MINUTE;
                break;
            }
            case TODAY:
            {
                begin = new DateMidnight().toDateTime();
                end = begin.plusDays(1);
                interval = AggregationInterval.DAY;
                break;
            }
            case YESTERDAY:
            {
                end = new DateMidnight().toDateTime().minusSeconds(1);
                begin = end.minusDays(1);
                interval = AggregationInterval.DAY;
                break;
            }
            default:
            {
                end = new DateTime();
                begin = end.minusHours(1);
                interval = AggregationInterval.HOUR;
                break;
            }
        }

        String masterGroup = prefs.getValue(PREFERENCE_MASTER_GROUP,DEFAULT_PREFERENCE_MASTER_GROUP);
        List<String> displayGroups = Arrays.asList(prefs.getValues(PREFERENCE_DISPLAY_GROUPS,DEFAULT_PREFERENCE_DISPLAY_GROUPS));
        boolean displayOther = Boolean.valueOf(prefs.getValue(PREFERENCE_DISPLAY_OTHER,DEFAULT_PREFERENCE_DISPLAY_OTHER));
        int masterTotal = 0;
        int absTotal = 0;
        int subTotal = 0;

        switch(timeframe)
        {
            case NOW:
                for(AggregatedGroupMapping group : concurrentUserAggregationDao.getAggregatedGroupMappings())
                {

                    ConcurrentUserAggregationKey key = new ConcurrentUserAggregationKeyImpl(interval, group);
                    final List<ConcurrentUserAggregation> aggregations = concurrentUserAggregationDao.getAggregations(begin, end, key);
                    
                    // NB:  We only care about the most recent entry (??)
                    if (aggregations.size() != 0) {
                        final ConcurrentUserAggregation aggregation = aggregations.get(0);
                        int groupTotal = aggregation.getConcurrentUsers();
                        absTotal += aggregation.getConcurrentUsers();
                        if (group.getGroupName().equalsIgnoreCase(masterGroup))
                        {
                            masterTotal = groupTotal;
                        } else {
                            subTotal += groupTotal;
                        }

                        if(!group.getGroupName().equals(masterGroup))
                        {
                            if (displayGroups.isEmpty() || displayGroups.contains(group.getGroupName()))
                            {
                                final PortalGroupActivity groupActivity = new PortalGroupActivity(group.getGroupName(), groupTotal);
                                groupActivities.add(groupActivity);
                            }
                        }
                    }
                }
                break;
            default:
                String uniqueLoginsPref = prefs.getValue(PREFERENCE_UNIQUE_LOGINS,DEFAULT_PREFERENCE_UNIQUE_LOGINS);
                Boolean uniqueLogins = Boolean.valueOf(uniqueLoginsPref);

                for(AggregatedGroupMapping group : loginAggregationDao.getAggregatedGroupMappings())
                {
                    
                    final LoginAggregationKey key = new LoginAggregationKeyImpl(interval, group);
                    final List<LoginAggregation> aggregations = loginAggregationDao.getAggregations(begin,end,key);
                    
                    // NB:  We only care about the most recent entry (??)
                    if (aggregations.size() != 0) {
                        final LoginAggregation aggregation = aggregations.get(0);
                        int groupTotal = getAggregationLoginCount(aggregation,uniqueLogins);
                        absTotal += groupTotal;
                        if (group.getGroupName().equalsIgnoreCase(masterGroup))
                        {
                            masterTotal = groupTotal;
                        } else {
                            subTotal += groupTotal;
                        }

                        if(!group.getGroupName().equals(masterGroup))
                        {
                            if (displayGroups.isEmpty() || displayGroups.contains(group.getGroupName()))
                            {
                                PortalGroupActivity groupActivity = new PortalGroupActivity(group.getGroupName(), groupTotal);
                                groupActivities.add(groupActivity);
                            }
                        }
                    }
                }
                break;
        }

        if(displayOther)
        {
            int otherTotal = masterTotal - subTotal;
            if(otherTotal > 0)
            {
                PortalGroupActivity otherGroup = new PortalGroupActivity("Other",otherTotal);
                groupActivities.add(otherGroup);
            }
        }

        Collections.sort(groupActivities);
        Collections.reverse(groupActivities);
        int total = masterTotal > 0 ? masterTotal : absTotal;
        final PortalActivity activity = new PortalActivity(total,groupActivities);
        return activity;

    }

    private List<SearchInfo> getPopularSearchTerms()
    {
        DateTime end = new DateTime();
        DateTime begin = end.minusDays(1);
        final IEntityGroup everyone = GroupService.getRootGroup(IPerson.class);
        final AggregatedGroupMapping group = aggregatedGroupLookupDao.getGroupMapping(everyone.getKey());
        List<SearchRequestAggregationImpl> aggregations = searchRequestAggregationDao.getAggregations(begin,end,AggregationInterval.FIVE_MINUTE,group);
        Map<String,SearchInfo> resultBuilder = new HashMap<String,SearchInfo>();
        for(SearchRequestAggregationImpl aggregation : aggregations)
        {
            SearchInfo info = resultBuilder.get(aggregation.getSearchTerm());
            if(info == null)
            {
                info = new SearchInfo(aggregation.getSearchTerm(),aggregation.getCount());
                resultBuilder.put(aggregation.getSearchTerm(),info);
            } else {
                info.incrementCount(aggregation.getCount());
            }
        }
        List<SearchInfo> results = new ArrayList<SearchInfo>(resultBuilder.values());
        Collections.sort(results);
        Collections.reverse(results);
        return results.size() > 10 ? results.subList(0,9) : results;
    }

    private int getAggregationLoginCount(LoginAggregation aggregation,boolean unique)
    {
        return unique ? aggregation.getUniqueLoginCount() : aggregation.getLoginCount();
    }
}
