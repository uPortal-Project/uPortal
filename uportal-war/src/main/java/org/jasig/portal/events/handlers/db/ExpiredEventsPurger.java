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

package org.jasig.portal.events.handlers.db;

import java.text.DateFormat;
import java.util.Calendar;
import java.util.Date;
import javax.sql.DataSource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.simple.SimpleJdbcTemplate;

public class ExpiredEventsPurger {

    private static final String PURGE_RENDER_TIME_SQL = 
        "DELETE FROM stats_render_time srt " +
            "WHERE srt.event_id IN (SELECT se.id FROM stats_event se WHERE se.act_date < ?)";

    private static final String PURGE_FOLDER_SQL = 
        "DELETE FROM stats_folder sf " +
            "WHERE sf.event_id IN (SELECT se.id FROM stats_event se WHERE se.act_date < ?)";

    private static final String PURGE_CHANNEL_SQL = 
        "DELETE FROM stats_channel sc " +
            "WHERE sc.event_id IN (SELECT se.id FROM stats_event se WHERE se.act_date < ?)";

    private static final String PURGE_EVENT_SQL = 
        "DELETE FROM stats_event se WHERE se.act_date < ?";
    
    // NB: Includes the date argument only so all statements have the same arguments
    private static final String PURGE_SESSION_GROUPS_SQL = 
        "DELETE FROM stats_session_groups ssg " +
            "WHERE ssg.session_id NOT IN (SELECT se.session_id FROM stats_event se WHERE se.act_date > ?)";

    // NB: Includes the date argument only so all statements have the same arguments
    private static final String PURGE_SESSION_SQL = 
        "DELETE FROM stats_session ss " +
            "WHERE ss.id NOT IN (SELECT se.session_id FROM stats_event se WHERE se.act_date > ?)";

    // Establishes a viable order for executing the statements
    private static final String[] SQL = new String[] {
        PURGE_RENDER_TIME_SQL,
        PURGE_FOLDER_SQL,
        PURGE_CHANNEL_SQL,
        PURGE_EVENT_SQL,
        PURGE_SESSION_GROUPS_SQL,
        PURGE_SESSION_SQL
    };

    // Instance Members
    private SimpleJdbcTemplate simpleJdbcTemplate;
    private int expirationThresholdDays = 366;
    private final DateFormat format = DateFormat.getDateInstance(DateFormat.SHORT);
    private final Log log = LogFactory.getLog(getClass());
    
    /*
     * Public API.
     */
    
    @Autowired
    @Qualifier("StatsDB")
    public void setDataSource(DataSource dataSource) {
        this.simpleJdbcTemplate = new SimpleJdbcTemplate(dataSource);
    }
    
    public void setExpirationThresholdDays(int expirationThresholdDays) {
        this.expirationThresholdDays = expirationThresholdDays;
    }
    
    public void purge() {
        
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DATE, -expirationThresholdDays);
        Date threshold = cal.getTime();

        if (log.isInfoEnabled()) {
            log.info("Purging events before the following date: " + format.format(threshold));
        }

        for (String sql : SQL) {
            simpleJdbcTemplate.update(sql, threshold);
        }

    }

}
