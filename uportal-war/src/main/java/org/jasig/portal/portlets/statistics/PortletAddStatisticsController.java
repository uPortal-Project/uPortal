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

import com.google.visualization.datasource.base.TypeMismatchException;
import com.google.visualization.datasource.datatable.ColumnDescription;
import com.google.visualization.datasource.datatable.value.NumberValue;
import com.google.visualization.datasource.datatable.value.Value;
import com.google.visualization.datasource.datatable.value.ValueType;
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
import org.jasig.portal.utils.ComparableExtractingComparator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.portlet.ModelAndView;
import org.springframework.web.portlet.bind.annotation.RenderMapping;
import org.springframework.web.portlet.bind.annotation.ResourceMapping;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;

/**
 * @author Chris Waymire <cwaymire@unicon.net>
 */
@Controller
@RequestMapping(value="VIEW")
public class PortletAddStatisticsController extends BasePortletLayoutStatisticsController<PortletAddReportForm> {
    private static final String DATA_TABLE_RESOURCE_ID = "portletAddData";
    private final static String REPORT_NAME = "portletAdd.totals";

    @RenderMapping(value="MAXIMIZED", params="report=" + REPORT_NAME)
    public String getLoginView() throws TypeMismatchException {
        return super.getLoginView();
    }

    @ResourceMapping(DATA_TABLE_RESOURCE_ID)
    public ModelAndView renderPortletAddAggregationReport(PortletAddReportForm form) throws TypeMismatchException {
        return super.renderPortletAddAggregationReport(form);
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
    protected List<Value> createRowValues(PortletLayoutAggregation aggr, PortletAddReportForm form) {
        int count = aggr != null ? aggr.getAddCount() : 0;
        return Collections.<Value>singletonList(new NumberValue(count));
    }
}