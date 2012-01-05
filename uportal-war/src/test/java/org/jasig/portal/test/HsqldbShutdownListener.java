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

package org.jasig.portal.test;

import java.util.Set;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

/**
 * @author Eric Dalquist
 * @version $Revision$
 */
public class HsqldbShutdownListener implements DisposableBean {
    protected final Logger logger = LoggerFactory.getLogger(getClass());
    
    @Autowired
    private Set<DataSource> dataSources;

    @Override
    public void destroy() throws Exception {
        for (DataSource dataSource : this.dataSources) {
            try {
                final JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
                jdbcTemplate.execute("SHUTDOWN");
            }
            catch (Exception e) {
                logger.info("Failed to shutdown data source: " + dataSource, e);
            }
        }
    }

    
}
