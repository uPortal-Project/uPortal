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

package org.jasig.portal.io.xml.user;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.Resource;
import javax.sql.DataSource;

import org.jasig.portal.IUserIdentityStore;
import org.jasig.portal.io.xml.AbstractJaxbDataHandler;
import org.jasig.portal.io.xml.IPortalData;
import org.jasig.portal.io.xml.IPortalDataType;
import org.jasig.portal.io.xml.PortalDataKey;
import org.jasig.portal.io.xml.SimpleStringPortalData;
import org.jasig.portal.persondir.ILocalAccountDao;
import org.jasig.portal.persondir.ILocalAccountPerson;
import org.jasig.portal.utils.ICounterStore;
import org.jasig.portal.utils.SafeFilenameUtils;
import org.jasig.portal.utils.Tuple;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.dao.support.DataAccessUtils;
import org.springframework.jdbc.core.JdbcOperations;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.transaction.annotation.Transactional;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;

/**
 * @author Nicholas Blair
 * @version $Id$
 */
public class UserImporterExporter extends
		AbstractJaxbDataHandler<UserType> {
    
    private static final ImmutableSet<PortalDataKey> IMPORT_DATA_KEYS = ImmutableSet.of(UserPortalDataType.IMPORT_40_DATA_KEY, TemplateUserPortalDataType.IMPORT_40_DATA_KEY);

	private JdbcOperations jdbcOperations;

	private UserPortalDataType userPortalDataType;
	private boolean forceDefaultUserName = false;
	private String defaultUserName;
	private DataSource dataSource;
    private ILocalAccountDao localAccountDao;
    private ICounterStore counterStore;
    private IUserIdentityStore userIdentityStore;
    
    public void setForceDefaultUserName(boolean forceDefaultUserName) {
        this.forceDefaultUserName = forceDefaultUserName;
    }
    
    public void setDefaultUserName(String defaultUserName) {
        this.defaultUserName = defaultUserName;
    }

    @Autowired
	public void setUserIdentityStore(IUserIdentityStore userIdentityStore) {
		this.userIdentityStore = userIdentityStore;
	}

	@Autowired
    public void setUserPortalDataType(UserPortalDataType userPortalDataType) {
        this.userPortalDataType = userPortalDataType;
    }

    @Autowired
    public void setCounterStore(ICounterStore counterStore) {
        this.counterStore = counterStore;
    }

    @Resource(name="PortalDb")
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
	    final List<String> userList = this.jdbcOperations.queryForList("SELECT USER_NAME FROM UP_USER", String.class);
	    
	    return Lists.transform(userList, new Function<String, IPortalData>() {
            @Override
            public IPortalData apply(final String userName) {
                return new SimpleStringPortalData(userName, null, null);
            }
        });
	}
	
	@Transactional
	@Override
	public void importData(UserType userType) {
	    final String username = userType.getUsername();
	    final String defaultUsername = getDefaultUsername(userType);
	    final Tuple<Integer, Long> defaultUserInfo = getDefaultUserInfo(defaultUsername);
        final Long nextStructId = getNextStructId(username, defaultUserInfo);
	    
        final Integer defaultUserId = defaultUserInfo != null ? defaultUserInfo.first : null;
        
        //Update or Insert
        final int rowsUpdated = this.jdbcOperations.update(
	            "UPDATE UP_USER \n" + 
	    		"SET USER_DFLT_USR_ID = ?, USER_DFLT_LAY_ID=1, NEXT_STRUCT_ID=? \n" + 
	    		"WHERE USER_NAME = ?", 
	    		defaultUserId, nextStructId, username);
	    
	    if (rowsUpdated != 1) {
	        final int userId = this.counterStore.getNextId("UP_USER");
	        
	        this.jdbcOperations.update(
	                "INSERT INTO UP_USER(USER_ID, USER_DFLT_USR_ID, USER_DFLT_LAY_ID, NEXT_STRUCT_ID, USER_NAME) \n" + 
	                "VALUES(?, ?, 1, ?, ?)",
	                userId, defaultUserId, nextStructId, username);
	    }

	    
	    ILocalAccountPerson account = this.localAccountDao.getPerson(username);
	    final String password = userType.getPassword();
	    final List<Attribute> attributes = userType.getAttributes();
	    if (password == null && attributes.isEmpty()) {
	        //No local account data, clean up the DB
	        if (account != null) {
	            this.localAccountDao.deleteAccount(account);
	        }
	    }
	    else {
    	    //Create or Update local account info
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

    protected Long getNextStructId(final String username, final Tuple<Integer, Long> defaultUserInfo) {
        final List<Long> maxStructIdResults = this.jdbcOperations.queryForList(
	            "SELECT MAX(UPLS.STRUCT_ID) AS MAX_STRUCT_ID " + 
	            "FROM UP_USER UPU " + 
	            "    LEFT JOIN UP_LAYOUT_STRUCT UPLS ON UPU.USER_ID = UPLS.USER_ID " + 
	            "WHERE UPU.USER_NAME = ?",
	            Long.class,
	            username);
	    final Long maxStructId = DataAccessUtils.singleResult(maxStructIdResults);
	    
	    if (maxStructId != null) {
	        return maxStructId + 1;
	    }
	    else if (defaultUserInfo != null) {
	        return defaultUserInfo.second;
	    }

	    return null;
    }

    //TODO cache the lookup of default user data
    protected Tuple<Integer, Long> getDefaultUserInfo(String defaultUsername) {
    	if (defaultUsername == null) {
    		return null;
    	}

	    final List<Tuple<Integer, Long>> defaultUserInfoResults = this.jdbcOperations.query(
	            "SELECT USER_ID, NEXT_STRUCT_ID " + 
	            "FROM UP_USER " + 
	            "WHERE USER_NAME = ?",
	            new RowMapper<Tuple<Integer, Long>>() {
                    @Override
                    public Tuple<Integer, Long> mapRow(ResultSet rs, int rowNum) throws SQLException {
                        final int userId = rs.getInt("USER_ID");
                        final Long nextStructId = rs.getLong("NEXT_STRUCT_ID");
                        final boolean nullNextStructId = rs.wasNull();
                        return new Tuple<Integer, Long>(userId, nullNextStructId ? null : nextStructId);
                    }
	            },
	            defaultUsername);
	    try {
	    	return DataAccessUtils.requiredSingleResult(defaultUserInfoResults);
	    }
	    catch (EmptyResultDataAccessException e) {
	    	throw new RuntimeException("No user data found for default-user: " + defaultUsername, e);
	    }
    }

    protected String getDefaultUsername(UserType data) {
        final String defaultUser = data.getDefaultUser();
        if (defaultUser == null || this.forceDefaultUserName) {
            return this.defaultUserName;
        }
        
        return defaultUser;
    }

	/*
	 * (non-Javadoc)
	 * @see org.jasig.portal.io.xml.IDataImporterExporter#exportData(java.lang.String)
	 */
	@Override
	public UserType exportData(String userName) {
        final String defaultUserName = getDefaultUserName(userName);
        
        final boolean isDefaultUser = this.userIdentityStore.isDefaultUser(userName);
	    final UserType userType;
	    if (isDefaultUser) {
	        userType = new ExternalTemplateUser();
	    }
	    else {
	        userType = new ExternalUser();
	    }
	    userType.setVersion("4.0");
	    userType.setUsername(userName);
	    userType.setDefaultUser(defaultUserName);
	    
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
            for (final Map.Entry<String, List<Object>> attributeEntry : localAccountPerson.getAttributes().entrySet()) {
                final String name = attributeEntry.getKey();
                final List<Object> values = attributeEntry.getValue();
                
                final Attribute externalAttribute = new Attribute();
                externalAttribute.setName(name);
                
                final List<String> externalValues = externalAttribute.getValues();
                for (final Object value : values) {
                    if (value != null) {
                        externalValues.add(value.toString());
                    }
                    else {
                        externalValues.add(null);
                    }
                }
                
                externalAttributes.add(externalAttribute);
            }
	    }
	    
	    return userType;
	}
	
	protected String getDefaultUserName(String userName) {
	    final List<Integer> defaultUserIdResults = this.jdbcOperations.queryForList(
                "SELECT user_dflt_usr_id FROM up_user WHERE user_name = ?", Integer.class, userName);
        final Integer defaultUserId = DataAccessUtils.singleResult(defaultUserIdResults);
        
        if (defaultUserId != null) {
            final List<String> defaultUserNameResults = this.jdbcOperations.queryForList(
                    "SELECT user_name FROM up_user WHERE user_id = ?", String.class, defaultUserId);
            return DataAccessUtils.singleResult(defaultUserNameResults);
        }
        
        return null;
	}
    

	@Override
    public String getFileName(UserType data) {
        return SafeFilenameUtils.makeSafeFilename(data.getUsername());
    }

    /*
	 * (non-Javadoc)
	 * @see org.jasig.portal.io.xml.IDataImporterExporter#deleteData(java.lang.String)
	 */
	@Transactional
	@Override
	public ExternalUser deleteData(String id) {
//		IPortletType portletType = this.portletTypeRegistry.getPortletType(Integer.parseInt(id));
//		if(null == portletType) {
//			return null;
//		} else {
//			ExternalPortletType result = convert(portletType);
//			this.portletTypeRegistry.deleteChannelType(portletType);
//			return result;
//		}
	    return null;
	}
}
