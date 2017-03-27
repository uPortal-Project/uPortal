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
package org.apereo.portal.utils.jdbc;

import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import javax.sql.DataSource;
import org.apereo.portal.hibernate.DelegatingHibernateIntegrator.HibernateConfiguration;
import org.apereo.portal.hibernate.HibernateConfigurationAware;
import org.apereo.portal.utils.Tuple;
import org.hibernate.dialect.Dialect;
import org.hibernate.service.jdbc.dialect.spi.DialectResolver;
import org.hibernate.service.spi.SessionFactoryServiceRegistry;
import org.jasig.portlet.utils.jdbc.DelayedValidationQueryResolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.support.DatabaseMetaDataCallback;
import org.springframework.jdbc.support.JdbcUtils;
import org.springframework.jdbc.support.MetaDataAccessException;

public class DelayedValidationQueryResolverImpl
        implements DelayedValidationQueryResolver, HibernateConfigurationAware {
    protected final Logger logger = LoggerFactory.getLogger(getClass());

    private final List<Tuple<DataSource, ValidationQueryRegistrationHandler>> delayedDataSources =
            new ArrayList<Tuple<DataSource, ValidationQueryRegistrationHandler>>();
    private ConcurrentMap<Class<? extends Dialect>, String> validationQueryMap;
    private String persistenceUnit;
    private DialectResolver dialectResolver;

    public void setValidationQueryMap(Map<Class<? extends Dialect>, String> validationQueryMap) {
        this.validationQueryMap =
                new ConcurrentHashMap<Class<? extends Dialect>, String>(validationQueryMap);
    }

    public void setPersistenceUnit(String persistenceUnit) {
        this.persistenceUnit = persistenceUnit;
    }

    @Override
    public boolean supports(String persistenceUnit) {
        return this.persistenceUnit.equals(persistenceUnit);
    }

    @Override
    public void setConfiguration(
            String persistenceUnit, HibernateConfiguration hibernateConfiguration) {

        final SessionFactoryServiceRegistry serviceRegistry =
                hibernateConfiguration.getServiceRegistry();
        synchronized (this.delayedDataSources) {
            this.dialectResolver = serviceRegistry.getService(DialectResolver.class);

            for (final Tuple<DataSource, ValidationQueryRegistrationHandler> delayedDataSource :
                    this.delayedDataSources) {
                final String validationQuery = this.getValidationQuery(delayedDataSource.first);
                delayedDataSource.second.setValidationQuery(validationQuery);
            }
            this.delayedDataSources.clear();
        }
    }

    @Override
    public void registerValidationQueryCallback(
            DataSource dataSource,
            ValidationQueryRegistrationHandler validationQueryRegistrationHandler) {
        synchronized (this.delayedDataSources) {
            if (this.dialectResolver != null) {
                final String validationQuery = this.getValidationQuery(dataSource);
                validationQueryRegistrationHandler.setValidationQuery(validationQuery);
            } else {
                this.delayedDataSources.add(
                        new Tuple<DataSource, ValidationQueryRegistrationHandler>(
                                dataSource, validationQueryRegistrationHandler));
            }
        }
    }

    protected String getValidationQuery(DataSource dataSource) {
        final Dialect dialect = this.resolveDialect(dataSource);
        if (dialect == null) {
            return null;
        }

        final Class<? extends Dialect> dialectType = dialect.getClass();
        return resolveValidationQuery(dialectType);
    }

    protected String resolveValidationQuery(final Class<? extends Dialect> dialectType) {
        //First try a direct lookup
        String validationQuery = this.validationQueryMap.get(dialectType);
        if (validationQuery != null) {
            return validationQuery;
        }

        //Next search the mappings looking for a sublcass match
        for (final Map.Entry<Class<? extends Dialect>, String> validationQueryEntry :
                this.validationQueryMap.entrySet()) {
            final Class<? extends Dialect> dialectEntryType = validationQueryEntry.getKey();
            if (dialectEntryType.isAssignableFrom(dialectType)) {
                validationQuery = validationQueryEntry.getValue();

                //Cache the resolution for future tests
                this.validationQueryMap.put(dialectType, validationQuery);
                return validationQuery;
            }
        }

        logger.warn("Failed to resolve validation query for Dialect: " + dialectType);
        return null;
    }

    protected Dialect resolveDialect(DataSource dataSource) {
        try {
            return (Dialect)
                    JdbcUtils.extractDatabaseMetaData(
                            dataSource,
                            new DatabaseMetaDataCallback() {
                                @Override
                                public Object processMetaData(DatabaseMetaData dbmd)
                                        throws SQLException, MetaDataAccessException {
                                    return dialectResolver.resolveDialect(dbmd);
                                }
                            });
        } catch (MetaDataAccessException e) {
            logger.warn(
                    "Failed to resolve Dialect for DataSource "
                            + dataSource
                            + " no validation query will be resolved",
                    e);
            return null;
        }
    }
}
