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
package org.jasig.portal.version;

import java.util.Map;

import javax.annotation.Resource;

import org.jasig.portal.version.dao.VersionDao;
import org.jasig.portal.version.om.Version;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContextException;
import org.springframework.stereotype.Component;

import com.google.common.collect.ImmutableMap;

/**
 * Bean that verifies the product version numbers in the configuration versus the database on startup
 * 
 * @author Eric Dalquist
 */
@Component
public class VersionVerifier implements InitializingBean {
	protected final Logger logger = LoggerFactory.getLogger(getClass());
	
    private Map<String, Version> requiredProductVersions;
    private VersionDao versionDao;
    
    @Resource(name = "productVersions")
    public void setRequiredProductVersions(Map<String, Version> requiredProductVersions) {
        this.requiredProductVersions = ImmutableMap.copyOf(requiredProductVersions);
    }
    
    @Autowired
    public void setVersionDao(VersionDao versionDao) {
        this.versionDao = versionDao;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        for (final Map.Entry<String, Version> productVersionEntry : this.requiredProductVersions.entrySet()) {
            final String product = productVersionEntry.getKey();
            final Version dbVersion = this.versionDao.getVersion(product);
            if (dbVersion == null) {
                throw new ApplicationContextException("No Version exists for " + product + " in the database. Please run 'ant db-update'");
            }
            
            final Version expectedVersion = productVersionEntry.getValue();
			if (dbVersion.isBefore(expectedVersion)) {
            	throw new ApplicationContextException("Database Version for " + product + " is " + dbVersion + " but the code version is " + expectedVersion + ". Please run 'ant db-update'");
            }
			
			logger.info("Versions {} match for {}", dbVersion, product);
        }
    }
}
