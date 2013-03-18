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

import java.util.Collections;
import java.util.List;

import org.jasig.portal.events.aggr.portletlayout.PortletLayoutAggregation;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.portlet.ModelAndView;
import org.springframework.web.portlet.bind.annotation.RenderMapping;
import org.springframework.web.portlet.bind.annotation.ResourceMapping;

import com.google.visualization.datasource.base.TypeMismatchException;
import com.google.visualization.datasource.datatable.value.NumberValue;
import com.google.visualization.datasource.datatable.value.Value;

/**
 * @author Chris Waymire <cwaymire@unicon.net>
 */
@Controller
@RequestMapping(value="VIEW")
public class PortletMoveStatisticsController extends BasePortletLayoutStatisticsController<PortletMoveReportForm> {
    private static final String DATA_TABLE_RESOURCE_ID = "portletMoveData";
    private static final String REPORT_NAME = "portletMove.totals";

    @RenderMapping(value="MAXIMIZED", params="report=" + REPORT_NAME)
    public String getLoginView() throws TypeMismatchException {
        return super.getLoginView();
    }

    @ResourceMapping(DATA_TABLE_RESOURCE_ID)
    public ModelAndView renderPortletAddAggregationReport(PortletMoveReportForm form) throws TypeMismatchException {
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
    protected List<Value> createRowValues(PortletLayoutAggregation aggr, PortletMoveReportForm form) {
        int count = aggr != null ? aggr.getMoveCount() : 0;
        return Collections.<Value>singletonList(new NumberValue(count));
    }
}