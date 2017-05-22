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
package org.apereo.portal.version;

import com.google.common.collect.ImmutableMap;
import java.util.Map;
import javax.annotation.Resource;
import org.apereo.portal.shell.PortalShellBuildHelper;
import org.apereo.portal.version.dao.VersionDao;
import org.apereo.portal.version.om.Version;
import org.apereo.portal.version.om.Version.Field;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContextException;
import org.springframework.stereotype.Component;

/**
 * Bean that verifies the product version numbers in the configuration versus the database on
 * startup
 *
 */
@Component
public class VersionVerifier implements InitializingBean {
    protected final Logger logger = LoggerFactory.getLogger(getClass());

    private Map<String, Version> requiredProductVersions;
    private VersionDao versionDao;
    private Version.Field updatePolicy = Version.Field.LOCAL;
    private PortalShellBuildHelper portalShellBuildHelper;

    @Resource(name = "productVersions")
    public void setRequiredProductVersions(Map<String, Version> requiredProductVersions) {
        this.requiredProductVersions = ImmutableMap.copyOf(requiredProductVersions);
    }

    @Autowired
    public void setVersionDao(VersionDao versionDao) {
        this.versionDao = versionDao;
    }

    @Autowired
    public void setPortalShellBuildHelper(PortalShellBuildHelper portalShellBuildHelper) {
        this.portalShellBuildHelper = portalShellBuildHelper;
    }

    @Value("${org.apereo.portal.version.autoUpdatePolicy:LOCAL}")
    public void setUpdatePolicy(Version.Field updatePolicy) {
        if (updatePolicy != null
                && updatePolicy != Version.Field.LOCAL
                && updatePolicy != Version.Field.PATCH) {
            throw new IllegalArgumentException("Only null, LOCAL, and PATCH updates are allowed");
        }
        this.updatePolicy = updatePolicy;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        for (final Map.Entry<String, Version> productVersionEntry :
                this.requiredProductVersions.entrySet()) {
            final String product = productVersionEntry.getKey();
            final Version dbVersion = this.versionDao.getVersion(product);
            if (dbVersion == null) {
                throw new ApplicationContextException(
                        "No Version exists for "
                                + product
                                + " in the database. Please check the upgrade instructions for this release.");
            }

            final Version codeVersion = productVersionEntry.getValue();
            final Field mostSpecificMatchingField =
                    VersionUtils.getMostSpecificMatchingField(dbVersion, codeVersion);

            switch (mostSpecificMatchingField) {
                    //Versions completely match
                case LOCAL:
                    {
                        logger.info(
                                "Software and Database versions are both {} for {}",
                                dbVersion,
                                product);
                        continue;
                    }
                    //Versions match except for local part
                case PATCH:
                    //Versions match except for patch.local part
                case MINOR:
                    {
                        //If db is before code and auto-update is enabled run hibernate-update
                        final Field upgradeField = mostSpecificMatchingField.getLessImportant();

                        if (dbVersion.isBefore(codeVersion)
                                && this.updatePolicy != null
                                && (upgradeField.equals(this.updatePolicy)
                                        || upgradeField.isLessImportantThan(this.updatePolicy))) {
                            logger.info(
                                    "Automatically updating database from {} to {} for {}",
                                    dbVersion,
                                    codeVersion,
                                    product);

                            this.portalShellBuildHelper.hibernateUpdate(
                                    "automated-hibernate-update", product, true, null);
                            continue;
                        } else if (codeVersion.isBefore(dbVersion)) {
                            //It is ok to run older code on a newer DB within the local/patch range
                            continue;
                        }
                    }
                    //Versions match except for minor.patch.local part
                case MAJOR:
                    //Versions do not match at all
                default:
                    {
                        if (dbVersion.isBefore(codeVersion)) {
                            throw new ApplicationContextException(
                                    "Database Version for "
                                            + product
                                            + " is "
                                            + dbVersion
                                            + " but the code version is "
                                            + codeVersion
                                            + ". Please check the upgrade instructions for this release");
                        } else {
                            throw new ApplicationContextException(
                                    "Database Version for "
                                            + product
                                            + " is "
                                            + dbVersion
                                            + " but the code version is "
                                            + codeVersion
                                            + ". It is not possible to run ");
                        }
                    }
            }
        }
    }
}
