package org.jasig.portal.jgroups.protocols;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

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
    private static final String TABLE_NAME = "UP_JGROUPS_PING";
    
    private static final String CREATE_TABLE_SQL =
            "CREATE TABLE " + TABLE_NAME + " (" +
                "CLUSTER_NAME varchar(200), " +
                "MEMBER_ADDRESS_STR varchar(100), "  +                    
                "MEMBER_ADDRESS_CLASS varchar(100), " +
                "MEMBER_ADDRESS_DATA varbinary(1000), " +
                "PHYSICAL_ADDRESS_STR varchar(100), " +                    
                "PHYSICAL_ADDRESS_CLASS varchar(100), " +
                "PHYSICAL_ADDRESS_DATA varbinary(1000), " +
                "PRIMARY KEY (CLUSTER_NAME, MEMBER_ADDRESS_CLASS, MEMBER_ADDRESS_DATA) " +
            ")";
    
    private static final String CREATE_INDEX_SQL = "create index IDX_JGROUPS_PING_CL_NAME on " + TABLE_NAME + " (CLUSTER_NAME)";

    private static final String UPDATE_SQL = 
            "UPDATE " + TABLE_NAME + " " +
            "SET PHYSICAL_ADDRESS_STR=:physicalAddressStr, PHYSICAL_ADDRESS_CLASS=:physicalAddressClass, PHYSICAL_ADDRESS_DATA=:physicalAddressData " +
            "WHERE CLUSTER_NAME=:clusterName AND MEMBER_ADDRESS_CLASS=:memberAddressClass AND MEMBER_ADDRESS_DATA=:memberAddressData";

    private static final String INSERT_SQL = 
            "INSERT INTO " + TABLE_NAME + " " +
            "(CLUSTER_NAME, MEMBER_ADDRESS_STR, MEMBER_ADDRESS_CLASS, MEMBER_ADDRESS_DATA, PHYSICAL_ADDRESS_STR, PHYSICAL_ADDRESS_CLASS, PHYSICAL_ADDRESS_DATA) " +
            "values (:clusterName, :memberAddressStr, :memberAddressClass, :memberAddressData, :physicalAddressStr, :physicalAddressClass, :physicalAddressData)";

    private static final String SELECT_CLUSTER_SQL = 
            "SELECT MEMBER_ADDRESS_CLASS, MEMBER_ADDRESS_DATA, PHYSICAL_ADDRESS_CLASS, PHYSICAL_ADDRESS_DATA " +
            "FROM " + TABLE_NAME + " " +
            "WHERE CLUSTER_NAME=:clusterName";

    private static final String DELETE_SQL = 
            "DELETE FROM " + TABLE_NAME + " " +
            "WHERE CLUSTER_NAME=:clusterName";

    
    protected final Logger logger = LoggerFactory.getLogger(getClass());
    
    private JdbcOperations jdbcOperations;
    private NamedParameterJdbcOperations namedParameterJdbcOperations;

    
    public void setJdbcOperations(JdbcOperations jdbcOperations) {
        this.jdbcOperations = jdbcOperations;
        this.namedParameterJdbcOperations = new NamedParameterJdbcTemplate(this.jdbcOperations);
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        final boolean tableExists = JdbcUtils.doesTableExist(this.jdbcOperations, TABLE_NAME);
        if (!tableExists) {
            logger.info("Creating jGroups PingDao table: {}", TABLE_NAME);
            this.jdbcOperations.execute(CREATE_TABLE_SQL);
            try {
                this.jdbcOperations.execute(CREATE_INDEX_SQL);
            }
            catch (Exception e) {
                //ignore, not fatal
            }
        }
        
        DAO_PING.setPingDao(this);
    }

    @PortalTransactional
    @Override
    public void addAddress(String clusterName, Address address, PhysicalAddress physicalAddress) {
        final Map<String, Object> paramMap = new HashMap<String, Object>();
        paramMap.put("clusterName", clusterName);
        setStreamableParam(paramMap, "memberAddress", address);
        setStreamableParam(paramMap, "physicalAddress", physicalAddress);
        
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
        final Map<String, Object> paramMap = new HashMap<String, Object>();
        paramMap.put("clusterName", clusterName);
        
        return this.namedParameterJdbcOperations.query(SELECT_CLUSTER_SQL, paramMap, new ResultSetExtractor<Map<Address, PhysicalAddress>>() {
            @Override
            public Map<Address, PhysicalAddress> extractData(ResultSet rs) throws SQLException, DataAccessException {
                final Map<Address, PhysicalAddress> result = new HashMap<Address, PhysicalAddress>();
                
                while (rs.next()) {
                    final Address memberAddress = getStreamableParam(rs, "MEMBER_ADDRESS");
                    final PhysicalAddress physicalAddress = getStreamableParam(rs, "PHYSICAL_ADDRESS");
                    
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
        final Map<String, Object> paramMap = new HashMap<String, Object>();
        paramMap.put("clusterName", clusterName);
        
        final StringBuilder deleteSqlBuilder = new StringBuilder(DELETE_SQL);
        
        for (final Address address : includedAddresses) {
            final String paramPrefix = "memberAddress" + paramMap.size();
            
            deleteSqlBuilder.append(" AND (MEMBER_ADDRESS_CLASS <> :").append(paramPrefix).append("Class OR MEMBER_ADDRESS_DATA <> :").append(paramPrefix).append("Data)");
            setStreamableParam(paramMap, paramPrefix, address);
        }
        
        final int purged = this.namedParameterJdbcOperations.update(deleteSqlBuilder.toString(), paramMap);
        logger.debug("Purged {} addresses from '{}' cluster while retaining: {}", purged, clusterName, includedAddresses);
    }
    
    @SuppressWarnings("unchecked")
    protected <T extends Streamable> T getStreamableParam(ResultSet rs, String columnPrefix) throws SQLException {
        final String className = rs.getString(columnPrefix + "_CLASS");
        final Class<? extends Streamable> cl;
        try {
            cl = (Class<? extends Streamable>) Class.forName(className);
        }
        catch (ClassNotFoundException e) {
            throw new RuntimeException("Failed to find class '" + className + "'", e);
        }
        
        final byte[] data = rs.getBytes(columnPrefix + "_DATA");
        try {
            return (T)Util.streamableFromByteBuffer(cl, data);
        } 
        catch (Exception e) {
            throw new RuntimeException("Failed to convert byte[] back into '" + cl + "'", e);
        }
    }
    
    protected void setStreamableParam(Map<String, Object> paramMap, String paramPrefix, Streamable s) {
        paramMap.put(paramPrefix + "Class", s.getClass().getName());
        try {
            paramMap.put(paramPrefix + "Data", Util.streamableToByteBuffer(s));
        } 
        catch (Exception e) {
            throw new RuntimeException("Failed to convert '" + s + "' into a byte[] for persistence", e);
        }
        paramMap.put(paramPrefix + "Str", s.toString());
    }

}
