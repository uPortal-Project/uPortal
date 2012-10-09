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
import java.util.Set;

import org.jasig.portal.events.aggr.AggregationInterval;
import org.jasig.portal.events.aggr.BaseAggregationDao;
import org.jasig.portal.events.aggr.groups.AggregatedGroupMapping;
import org.jasig.portal.events.aggr.login.LoginAggregation;
import org.jasig.portal.events.aggr.login.LoginAggregationDao;
import org.jasig.portal.events.aggr.login.LoginAggregationKey;
import org.jasig.portal.events.aggr.login.LoginAggregationKeyImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.portlet.ModelAndView;
import org.springframework.web.portlet.bind.annotation.RenderMapping;
import org.springframework.web.portlet.bind.annotation.ResourceMapping;

import com.google.common.collect.ImmutableList;
import com.google.visualization.datasource.base.TypeMismatchException;
import com.google.visualization.datasource.datatable.ColumnDescription;
import com.google.visualization.datasource.datatable.value.NumberValue;
import com.google.visualization.datasource.datatable.value.Value;
import com.google.visualization.datasource.datatable.value.ValueType;

/**
 * Login reports
 * 
 * @author Jen Bourey, jennifer.bourey@gmail.com
 */
@Controller
@RequestMapping(value="VIEW")
public class LoginTotalsStatisticsController extends BaseStatisticsReportController<LoginAggregation, LoginAggregationKey, LoginReportForm> {
    private static final String DATA_TABLE_RESOURCE_ID = "loginData";
    private final static String REPORT_NAME = "login.totals";

    @Autowired
    private LoginAggregationDao<LoginAggregation> loginDao;
    
    @RenderMapping(value="MAXIMIZED", params="report=" + REPORT_NAME)
    public String getLoginView() throws TypeMismatchException {
        return "jsp/Statistics/reportGraph";
    }
    
    @ResourceMapping(DATA_TABLE_RESOURCE_ID)
    public ModelAndView renderLoginAggregationReport(LoginReportForm form) throws TypeMismatchException {
        return renderAggregationReport(form);
    }
    
    @Override
    protected LoginReportForm createReportFormRequest() {
        return new LoginReportForm();
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
    protected BaseAggregationDao<LoginAggregation, LoginAggregationKey> getBaseAggregationDao() {
        return this.loginDao;
    }

    @Override
    protected LoginAggregationKey createAggregationsQueryKey(Set<AggregatedGroupMapping> groups, LoginReportForm form) {
        final AggregationInterval interval = form.getInterval();
        return new LoginAggregationKeyImpl(interval, groups.iterator().next());
    }
    
    @Override
    protected List<ColumnDescription> getColumnDescriptions(AggregatedGroupMapping group, LoginReportForm form) {
        final String groupName = group.getGroupName();
        
        if (form.isTotalLogins() && form.isUniqueLogins()) {
            return ImmutableList.of(
                    //THE ORDER OF RETURNED COLUMNS HERE MUST MATCH THE ORDER OF THE VALUES RETURNED IN createRowValues
                    new ColumnDescription(groupName + "-uniqueLogins", ValueType.NUMBER, groupName + " - Unique Logins"),
                    new ColumnDescription(groupName + "-totalLogins", ValueType.NUMBER, groupName + " - Total Logins")
                );
        }
        else if (form.isUniqueLogins()) {
            return Collections.singletonList(new ColumnDescription(groupName + "-uniqueLogins", ValueType.NUMBER, groupName + " - Unique Logins"));
        }
        else {
            return Collections.singletonList(new ColumnDescription(groupName + "-totalLogins", ValueType.NUMBER, groupName + " - Total Logins"));
        }
    }

    @Override
    protected List<Value> createRowValues(LoginAggregation aggr, LoginReportForm form) {
        final int loginCount;
        final int uniqueLoginCount;
        if (aggr == null) {
            loginCount = 0;
            uniqueLoginCount = 0;
        }
        else {
            loginCount = aggr.getLoginCount();
            uniqueLoginCount = aggr.getUniqueLoginCount();
        }
        
        if (form.isTotalLogins() && form.isUniqueLogins()) {
            return ImmutableList.<Value>of(
                    //THE ORDER OF RETURNED VALUES HERE MUST MATCH THE ORDER OF THE COLUMNS RETURNED IN getColumnDescriptions
                    new NumberValue(uniqueLoginCount),
                    new NumberValue(loginCount)
                );
        }
        else if (form.isUniqueLogins()) {
            return Collections.<Value>singletonList(new NumberValue(uniqueLoginCount));
        }
        else {
            return Collections.<Value>singletonList(new NumberValue(loginCount));
        }
    }
}
