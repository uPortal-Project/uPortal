/**
 * Licensed to Apereo under one or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information regarding copyright ownership. Apereo
 * licenses this file to you under the Apache License, Version 2.0 (the "License"); you may not use
 * this file except in compliance with the License. You may obtain a copy of the License at the
 * following location:
 *
 * <p>http://www.apache.org/licenses/LICENSE-2.0
 *
 * <p>Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apereo.portal.portlets.popular;

import com.google.visualization.datasource.base.TypeMismatchException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import javax.portlet.PortletRequest;
import javax.servlet.http.HttpServletRequest;
import org.apereo.portal.EntityIdentifier;
import org.apereo.portal.events.aggr.AggregationInterval;
import org.apereo.portal.events.aggr.groups.AggregatedGroupLookupDao;
import org.apereo.portal.events.aggr.groups.AggregatedGroupMapping;
import org.apereo.portal.events.aggr.portletlayout.PortletLayoutAggregation;
import org.apereo.portal.events.aggr.portletlayout.PortletLayoutAggregationDao;
import org.apereo.portal.events.aggr.portlets.AggregatedPortletMapping;
import org.apereo.portal.groups.IEntityGroup;
import org.apereo.portal.portlet.dao.IPortletDefinitionDao;
import org.apereo.portal.portlet.om.IPortletDefinition;
import org.apereo.portal.security.IAuthorizationPrincipal;
import org.apereo.portal.security.IPerson;
import org.apereo.portal.security.IPersonManager;
import org.apereo.portal.services.AuthorizationService;
import org.apereo.portal.services.GroupService;
import org.apereo.portal.url.IPortalRequestUtils;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.portlet.bind.annotation.RenderMapping;
import org.springframework.web.portlet.bind.annotation.ResourceMapping;
import org.springframework.web.servlet.ModelAndView;

/**
 * Spring controller that returns a JSON representation of how many times users have either added
 * each portlet in the specified number of days, counting backwards from the specified day
 * (inclusive).
 *
 * <p>Request parameters:
 *
 * <ul>
 *   <li>days: Number of calendar days to include in the report; default is 30
 *   <li>fromDate: Date (inclusive) from which to count backwards; default is today
 * </ul>
 *
 */
@Controller
@RequestMapping("VIEW")
public class PopularPortletsController {
    public static final AggregationInterval AGGREGATION_INTERVAL = AggregationInterval.DAY;

    private IPersonManager personManager;
    private IPortalRequestUtils portalRequestUtils;
    private PortletLayoutAggregationDao<PortletLayoutAggregation> portletLayoutDao;
    private AggregatedGroupLookupDao aggregatedGroupLookupDao;
    private IPortletDefinitionDao portletDefinitionDao;

    @Autowired
    public void setPortletDefinitionDao(IPortletDefinitionDao portletDefinitionDao) {
        this.portletDefinitionDao = portletDefinitionDao;
    }

    @Autowired
    public void setPersonManager(IPersonManager personManager) {
        this.personManager = personManager;
    }

    @Autowired
    public void setAggregatedGroupLookupDao(AggregatedGroupLookupDao aggregatedGroupLookupDao) {
        this.aggregatedGroupLookupDao = aggregatedGroupLookupDao;
    }

    @Autowired
    public void setPortalRequestUtils(IPortalRequestUtils portalRequestUtils) {
        this.portalRequestUtils = portalRequestUtils;
    }

    @Autowired
    public void setPortletLayoutDao(
            PortletLayoutAggregationDao<PortletLayoutAggregation> portletLayoutDao) {
        this.portletLayoutDao = portletLayoutDao;
    }

    @RenderMapping
    public String getReportList() throws TypeMismatchException {
        return "jsp/PopularPortlets/listPortlets";
    }

    @ResourceMapping("popularPortletCounts")
    public ModelAndView getEventCounts(
            PortletRequest req, @RequestParam(value = "days", required = false) Integer days) {
        Map<String, Object> model = new HashMap<String, Object>();
        final List<PortletUsage> counts = getCounts(req, days);
        model.put("counts", counts);
        return new ModelAndView("jsonView", model);
    }

    private final List<PortletUsage> getCounts(PortletRequest portletRequest, Integer days) {
        final HttpServletRequest servletRequest =
                this.portalRequestUtils.getPortletHttpRequest(portletRequest);
        IPerson user = personManager.getPerson(servletRequest);
        final List<PortletUsage> resultList =
                buildEventCounts(days, user, portletRequest.getLocale());
        return resultList;
    }

    private List<PortletUsage> buildEventCounts(Integer days, IPerson user, Locale locale) {
        final DateTime end = new DateTime();
        final DateTime begin = end.minusDays(days);

        final IEntityGroup everyone = GroupService.getRootGroup(IPerson.class);
        final AggregatedGroupMapping group =
                aggregatedGroupLookupDao.getGroupMapping(everyone.getKey());
        final List<PortletLayoutAggregation> aggregations =
                portletLayoutDao.getAggregationsForAllPortlets(
                        begin, end, AGGREGATION_INTERVAL, group);

        final EntityIdentifier ei = user.getEntityIdentifier();
        final AuthorizationService authService = AuthorizationService.instance();
        final IAuthorizationPrincipal ap = authService.newPrincipal(ei.getKey(), ei.getType());

        final Map<String, PortletUsage> resultBuilder = new HashMap<String, PortletUsage>();

        for (final PortletLayoutAggregation aggregation : aggregations) {
            final AggregatedPortletMapping portlet = aggregation.getPortletMapping();
            final String fname = portlet.getFname();
            PortletUsage portletUsage = resultBuilder.get(fname);
            if (portletUsage == null) {
                final IPortletDefinition portletDefinition =
                        this.portletDefinitionDao.getPortletDefinitionByFname(fname);

                if (portletDefinition == null
                        || !ap.canSubscribe(
                                portletDefinition.getPortletDefinitionId().getStringId())) {
                    //Skip portlets that no longer exist or cannot be subscribed to
                    continue;
                }

                portletUsage =
                        new PortletUsage(
                                portletDefinition.getPortletDefinitionId().getLongId(),
                                fname,
                                portletDefinition.getTitle(locale.toString()),
                                portletDefinition.getDescription(locale.toString()));

                resultBuilder.put(fname, portletUsage);
            }

            portletUsage.incrementCount(aggregation.getAddCount());
        }

        final ArrayList<PortletUsage> results = new ArrayList<PortletUsage>(resultBuilder.values());
        Collections.sort(results);

        return results;
    }
}
