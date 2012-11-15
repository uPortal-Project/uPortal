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
package org.jasig.portal.jgroups.protocols;

import java.io.Serializable;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;

import org.hibernate.annotations.Index;
import org.jasig.portal.jpa.BasePortalJpaDao.PortalTransactional;
import org.jasig.portal.utils.JdbcUtils;
import org.jgroups.Address;
import org.jgroups.PhysicalAddress;
import org.jgroups.util.Streamable;
import org.jgroups.util.Util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcOperations;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcOperations;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

/**
 * {@link PingDao} that uses the Spring JDBC APIs to do its work.
 * 
 * @author Eric Dalquist
 */
public class JdbcPingDao implements PingDao, InitializingBean {
    /**
     * This class is ONLY used to provide for creation of the table/index required by the {@link JdbcPingDao}.
     * Due to the JPA -> Hibernate -> Ehcache -> JGroups -> DAO_PING -> JdbcPingDao reference chain this class
     * CANNOT directly reference the JPA entity manager or transaction manager
     */
    @Entity(name = Table.NAME)
    public static class Table implements Serializable {
        private static final long serialVersionUID = 1L;
        
        static final String NAME = "UP_JGROUPS_PING";
        
        static final String CLASS_COL_SUFFIX = "_CLASS";
        static final String DATA_COL_SUFFIX = "_DATA";
        
        static final String COL_CLUSTER_NAME = "CLUSTER_NAME";
        
        static final String COL_MEMBER_ADDRESS = "MEMBER_ADDRESS";
        static final String COL_MEMBER_ADDRESS_CLASS = COL_MEMBER_ADDRESS + CLASS_COL_SUFFIX;
        static final String COL_MEMBER_ADDRESS_DATA = COL_MEMBER_ADDRESS + DATA_COL_SUFFIX;

        static final String COL_PHYSICAL_ADDRESS = "PHYSICAL_ADDRESS";
        static final String COL_PHYSICAL_ADDRESS_CLASS = COL_PHYSICAL_ADDRESS + CLASS_COL_SUFFIX;
        static final String COL_PHYSICAL_ADDRESS_DATA = COL_PHYSICAL_ADDRESS + DATA_COL_SUFFIX;

        @Id
        @Column(name=COL_CLUSTER_NAME, length=200)
        @Index(name="IDX_JGROUPS_PING")
        private final String clusterName = null;
        
        
        @Column(name=COL_MEMBER_ADDRESS, length=500)
        private final String memberAddress = null;
        
        @Id
        @Column(name=COL_MEMBER_ADDRESS_CLASS, length=200)
        private final Class<? extends Address> memberAddressClass = null;
        
        @Id
        @Column(name=COL_MEMBER_ADDRESS_DATA, length=1000)
        private final byte[] memberAddressData = null;
        
        
        @Column(name=COL_PHYSICAL_ADDRESS, length=500)
        private final String physicalAddressName = null;
        
        @Column(name=COL_PHYSICAL_ADDRESS_CLASS, length=200)
        private final Class<? extends Address> physicalAddressClass = null;
        
        @Column(name=COL_PHYSICAL_ADDRESS_DATA, length=1000)
        private final byte[] physicalAddressData = null;
    }

    
    private static final String CLASS_PRM_SUFFIX = "Class";
    private static final String DATA_PRM_SUFFIX = "Data";
    
    private static final String PRM_CLUSTER_NAME = "clusterName";
    
    private static final String PRM_MEMBER_ADDRESS = "memberAddress";
    private static final String PRM_MEMBER_ADDRESS_CLASS = PRM_MEMBER_ADDRESS + CLASS_PRM_SUFFIX;
    private static final String PRM_MEMBER_ADDRESS_DATA = PRM_MEMBER_ADDRESS + DATA_PRM_SUFFIX;
    
    private static final String PRM_PHYSICAL_ADDRESS = "physicalAddress";
    private static final String PRM_PHYSICAL_ADDRESS_CLASS = PRM_PHYSICAL_ADDRESS + CLASS_PRM_SUFFIX;
    private static final String PRM_PHYSICAL_ADDRESS_DATA = PRM_PHYSICAL_ADDRESS + DATA_PRM_SUFFIX;
    
    
    private static final String UPDATE_SQL = 
            "UPDATE " + Table.NAME + " " +
            "SET " + 
                    Table.COL_PHYSICAL_ADDRESS + "=:" + PRM_PHYSICAL_ADDRESS + ", " +
                    Table.COL_PHYSICAL_ADDRESS_CLASS + "=:" + PRM_PHYSICAL_ADDRESS_CLASS + ", " +
                    Table.COL_PHYSICAL_ADDRESS_DATA + "=:" + PRM_PHYSICAL_ADDRESS_DATA + " " +
            "WHERE " + 
                    Table.COL_CLUSTER_NAME + "=:" + PRM_CLUSTER_NAME + " AND " +
                    Table.COL_MEMBER_ADDRESS_CLASS + "=:" + PRM_MEMBER_ADDRESS_CLASS + " AND " +
                    Table.COL_MEMBER_ADDRESS_DATA + "=:" + PRM_MEMBER_ADDRESS_DATA;

    private static final String INSERT_SQL = 
            "INSERT INTO " + Table.NAME + " " +
            "(" +
                Table.COL_CLUSTER_NAME + ", " +
                Table.COL_MEMBER_ADDRESS + ", " + Table.COL_MEMBER_ADDRESS_CLASS + ", " + Table.COL_MEMBER_ADDRESS_DATA + ", " +
                Table.COL_PHYSICAL_ADDRESS + ", " + Table.COL_PHYSICAL_ADDRESS_CLASS + ", " + Table.COL_PHYSICAL_ADDRESS_DATA + ") " +
            "values (" +
                ":" + PRM_CLUSTER_NAME + ", " +
                ":" + PRM_MEMBER_ADDRESS + ", :" + PRM_MEMBER_ADDRESS_CLASS + ", :" + PRM_MEMBER_ADDRESS_DATA + ", " +
                ":" + PRM_PHYSICAL_ADDRESS + ", :" + PRM_PHYSICAL_ADDRESS_CLASS + ", :" + PRM_PHYSICAL_ADDRESS_DATA + ")";

    private static final String SELECT_CLUSTER_SQL = 
            "SELECT " +
                Table.COL_MEMBER_ADDRESS_CLASS + ", " +
                Table.COL_MEMBER_ADDRESS_DATA + ", " +
                Table.COL_PHYSICAL_ADDRESS_CLASS + ", " +
                Table.COL_PHYSICAL_ADDRESS_DATA + " " +
            "FROM " + Table.NAME + " " +
            "WHERE " + Table.COL_CLUSTER_NAME + "=:" + PRM_CLUSTER_NAME;

    private static final String DELETE_SQL = 
            "DELETE FROM " + Table.NAME + " " +
            "WHERE " + Table.COL_CLUSTER_NAME + "=:" + PRM_CLUSTER_NAME;

    
    protected final Logger logger = LoggerFactory.getLogger(getClass());
    
    private JdbcOperations jdbcOperations;
    private NamedParameterJdbcOperations namedParameterJdbcOperations;
    private volatile boolean ready = false; 
    
    public void setJdbcOperations(JdbcOperations jdbcOperations) {
        this.jdbcOperations = jdbcOperations;
        this.namedParameterJdbcOperations = new NamedParameterJdbcTemplate(this.jdbcOperations);
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        DAO_PING.setPingDao(this);
    }

    @PortalTransactional
    @Override
    public void addAddress(String clusterName, Address address, PhysicalAddress physicalAddress) {
        if (!isReady()) {
            return;
        }
        
        final Map<String, Object> paramMap = new HashMap<String, Object>();
        paramMap.put(PRM_CLUSTER_NAME, clusterName);
        setStreamableParam(paramMap, PRM_MEMBER_ADDRESS, address);
        setStreamableParam(paramMap, PRM_PHYSICAL_ADDRESS, physicalAddress);
        
        final int rowCount = this.namedParameterJdbcOperations.update(UPDATE_SQL, paramMap);
        if (rowCount == 0) {
            this.namedParameterJdbcOperations.update(INSERT_SQL, paramMap);
            logger.debug("Inserted cluster address: " + paramMap);
        }
        else {
            logger.debug("Updated cluster address: " + paramMap);
        }
    }

    @Override
    public Map<Address, PhysicalAddress> getAddresses(String clusterName) {
        if (!isReady()) {
            return Collections.emptyMap();
        }
        
        final Map<String, Object> paramMap = new HashMap<String, Object>();
        paramMap.put(PRM_CLUSTER_NAME, clusterName);
        
        return this.namedParameterJdbcOperations.query(SELECT_CLUSTER_SQL, paramMap, new ResultSetExtractor<Map<Address, PhysicalAddress>>() {
            @Override
            public Map<Address, PhysicalAddress> extractData(ResultSet rs) throws SQLException, DataAccessException {
                final Map<Address, PhysicalAddress> result = new HashMap<Address, PhysicalAddress>();
                
                while (rs.next()) {
                    final Address memberAddress = getStreamableParam(rs, Table.COL_MEMBER_ADDRESS);
                    final PhysicalAddress physicalAddress = getStreamableParam(rs, Table.COL_PHYSICAL_ADDRESS);
                    
                    result.put(memberAddress, physicalAddress);
                }
                
                logger.debug("Found {} addresses in cluster: {}", result.size(), result);
                return result;
            }
        });
    }

    @PortalTransactional
    @Override
    public void purgeOtherAddresses(String clusterName, Collection<Address> includedAddresses) {
        if (!isReady()) {
            return;
        }
        
        final Map<String, Object> paramMap = new HashMap<String, Object>();
        paramMap.put(PRM_CLUSTER_NAME, clusterName);
        
        final StringBuilder deleteSqlBuilder = new StringBuilder(DELETE_SQL);
        
        for (final Address address : includedAddresses) {
            final String paramPrefix = PRM_MEMBER_ADDRESS + paramMap.size();
            
            deleteSqlBuilder.append(" AND (")
                .append(Table.COL_MEMBER_ADDRESS_CLASS).append(" <> :").append(paramPrefix).append(CLASS_PRM_SUFFIX)
                .append(" OR ").append(Table.COL_MEMBER_ADDRESS_DATA).append(" <> :").append(paramPrefix).append(DATA_PRM_SUFFIX)
            .append(")");
            
            setStreamableParam(paramMap, paramPrefix, address);
        }
        
        final int purged = this.namedParameterJdbcOperations.update(deleteSqlBuilder.toString(), paramMap);
        logger.debug("Purged {} addresses from '{}' cluster while retaining: {}", purged, clusterName, includedAddresses);
    }
    
    protected boolean isReady() {
        boolean r = this.ready;
        if (!r) {
            r = JdbcUtils.doesTableExist(this.jdbcOperations, Table.NAME);
            
            if (r) {
                this.ready = r;
            }
        }
        
        return r;
    }
    
    @SuppressWarnings("unchecked")
    protected <T extends Streamable> T getStreamableParam(ResultSet rs, String columnPrefix) throws SQLException {
        final String className = rs.getString(columnPrefix + Table.CLASS_COL_SUFFIX);
        final Class<? extends Streamable> cl;
        try {
            cl = (Class<? extends Streamable>) Class.forName(className);
        }
        catch (ClassNotFoundException e) {
            throw new RuntimeException("Failed to find class '" + className + "'", e);
        }
        
        final byte[] data = rs.getBytes(columnPrefix + Table.DATA_COL_SUFFIX);
        try {
            return (T)Util.streamableFromByteBuffer(cl, data);
        } 
        catch (Exception e) {
            throw new RuntimeException("Failed to convert byte[] back into '" + cl + "'", e);
        }
    }
    
    protected void setStreamableParam(Map<String, Object> paramMap, String paramPrefix, Streamable s) {
        paramMap.put(paramPrefix + CLASS_PRM_SUFFIX, s.getClass().getName());
        try {
            paramMap.put(paramPrefix + DATA_PRM_SUFFIX, Util.streamableToByteBuffer(s));
        } 
        catch (Exception e) {
            throw new RuntimeException("Failed to convert '" + s + "' into a byte[] for persistence", e);
        }
        paramMap.put(paramPrefix, s.toString());
    }

}
