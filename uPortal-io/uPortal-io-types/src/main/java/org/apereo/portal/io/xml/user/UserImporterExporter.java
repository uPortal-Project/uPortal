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
package org.apereo.portal.io.xml.user;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.annotation.Resource;
import javax.sql.DataSource;
import org.apereo.portal.ICounterStore;
import org.apereo.portal.io.xml.AbstractJaxbDataHandler;
import org.apereo.portal.io.xml.IPortalData;
import org.apereo.portal.io.xml.IPortalDataType;
import org.apereo.portal.io.xml.PortalDataKey;
import org.apereo.portal.io.xml.SimpleStringPortalData;
import org.apereo.portal.jpa.BasePortalJpaDao;
import org.apereo.portal.persondir.ILocalAccountDao;
import org.apereo.portal.persondir.ILocalAccountPerson;
import org.apereo.portal.utils.SafeFilenameUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.support.DataAccessUtils;
import org.springframework.jdbc.core.JdbcOperations;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;

/** */
public class UserImporterExporter extends AbstractJaxbDataHandler<UserType> {

    private static final ImmutableSet<PortalDataKey> IMPORT_DATA_KEYS =
            ImmutableSet.of(UserPortalDataType.IMPORT_40_DATA_KEY);

    private JdbcOperations jdbcOperations;

    private UserPortalDataType userPortalDataType;
    private DataSource dataSource;
    private ILocalAccountDao localAccountDao;
    private ICounterStore counterStore;

    @Autowired
    public void setUserPortalDataType(UserPortalDataType userPortalDataType) {
        this.userPortalDataType = userPortalDataType;
    }

    @Autowired
    public void setCounterStore(ICounterStore counterStore) {
        this.counterStore = counterStore;
    }

    @Resource(name = BasePortalJpaDao.PERSISTENCE_UNIT_NAME)
    public void setDataSource(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Autowired
    public void setLocalAccountDao(ILocalAccountDao localAccountDao) {
        this.localAccountDao = localAccountDao;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        super.afterPropertiesSet();

        this.jdbcOperations = new JdbcTemplate(dataSource);
    }

    @Override
    public Set<PortalDataKey> getImportDataKeys() {
        return IMPORT_DATA_KEYS;
    }

    @Override
    public IPortalDataType getPortalDataType() {
        return this.userPortalDataType;
    }

    @Override
    public Iterable<? extends IPortalData> getPortalData() {
        final List<String> userList =
                this.jdbcOperations.queryForList("SELECT USER_NAME FROM UP_USER", String.class);

        return Lists.transform(
                userList,
                (Function<String, IPortalData>)
                        userName -> new SimpleStringPortalData(userName, null, null));
    }

    @Transactional
    @Override
    public void importData(UserType userType) {
        final String username = userType.getUsername();
        final Long nextStructId = getNextStructId(username);

        // Update or Insert
        final int rowsUpdated =
                this.jdbcOperations.update(
                        "UPDATE UP_USER \n"
                                + "SET USER_DFLT_LAY_ID=1, NEXT_STRUCT_ID=? \n"
                                + "WHERE USER_NAME = ?",
                        nextStructId,
                        username);

        if (rowsUpdated != 1) {
            final int userId = this.counterStore.getNextId("UP_USER");

            this.jdbcOperations.update(
                    "INSERT INTO UP_USER(USER_ID, USER_DFLT_LAY_ID, NEXT_STRUCT_ID, USER_NAME) \n"
                            + "VALUES(?, 1, ?, ?)",
                    userId,
                    nextStructId,
                    username);
        }

        ILocalAccountPerson account = this.localAccountDao.getPerson(username);
        final String password = userType.getPassword();
        final List<Attribute> attributes = userType.getAttributes();
        if (password == null && attributes.isEmpty()) {
            // No local account data, clean up the DB
            if (account != null) {
                this.localAccountDao.deleteAccount(account);
            }
        } else {
            // Create or Update local account info
            if (account == null) {
                account = this.localAccountDao.createPerson(username);
            }
            account.setPassword(password);
            final Calendar lastPasswordChange = userType.getLastPasswordChange();
            if (lastPasswordChange != null) {
                account.setLastPasswordChange(lastPasswordChange.getTime());
            }

            account.removeAttribute(username);
            for (final Attribute attribute : attributes) {
                account.setAttribute(attribute.getName(), attribute.getValues());
            }

            this.localAccountDao.updateAccount(account);
        }
    }

    private Long getNextStructId(final String username) {

        final List<Long> maxStructIdResults =
                this.jdbcOperations.queryForList(
                        "SELECT MAX(UPLS.STRUCT_ID) AS MAX_STRUCT_ID "
                                + "FROM UP_USER UPU "
                                + "    LEFT JOIN UP_LAYOUT_STRUCT UPLS ON UPU.USER_ID = UPLS.USER_ID "
                                + "WHERE UPU.USER_NAME = ?",
                        Long.class,
                        username);
        final Long maxStructId = DataAccessUtils.singleResult(maxStructIdResults);

        if (maxStructId != null) {
            return maxStructId + 1;
        }

        return null;
    }

    @Override
    public UserType exportData(String userName) {

        final UserType userType = new ExternalUser();
        userType.setUsername(userName);

        final ILocalAccountPerson localAccountPerson = this.localAccountDao.getPerson(userName);
        if (localAccountPerson != null) {
            userType.setPassword(localAccountPerson.getPassword());

            final Date lastPasswordChange = localAccountPerson.getLastPasswordChange();
            if (lastPasswordChange != null) {
                final Calendar lastPasswordChangeCal = Calendar.getInstance();
                lastPasswordChangeCal.setTime(lastPasswordChange);
                userType.setLastPasswordChange(lastPasswordChangeCal);
            }

            final List<Attribute> externalAttributes = userType.getAttributes();
            for (final Map.Entry<String, List<Object>> attributeEntry :
                    localAccountPerson.getAttributes().entrySet()) {
                final String name = attributeEntry.getKey();
                final List<Object> values = attributeEntry.getValue();

                final Attribute externalAttribute = new Attribute();
                externalAttribute.setName(name);

                final List<String> externalValues = externalAttribute.getValues();
                for (final Object value : values) {
                    if (value != null) {
                        externalValues.add(value.toString());
                    } else {
                        externalValues.add(null);
                    }
                }

                externalAttributes.add(externalAttribute);
            }
            Collections.sort(externalAttributes, AttributeComparator.INSTANCE);
        }

        return userType;
    }

    @Override
    public String getFileName(UserType data) {
        return SafeFilenameUtils.makeSafeFilename(data.getUsername());
    }

    @Transactional
    @Override
    public ExternalUser deleteData(String id) {
        return null;
    }
}
