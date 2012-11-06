package org.jasig.portal.jgroups.auth;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collections;

import javax.sql.DataSource;

import org.hibernate.exception.ConstraintViolationException;
import org.jasig.portal.jgroups.protocols.PingDao;
import org.jasig.portal.utils.JdbcUtils;
import org.jasig.portal.utils.RandomTokenGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.support.DataAccessUtils;
import org.springframework.jdbc.core.ConnectionCallback;
import org.springframework.jdbc.core.JdbcOperations;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcOperations;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.jdbc.datasource.SingleConnectionDataSource;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionOperations;
import org.springframework.transaction.support.TransactionTemplate;

import com.google.common.collect.ImmutableMap;

/**
 * {@link PingDao} that uses the Spring JDBC APIs to do its work.
 * 
 * @author Eric Dalquist
 */
public class JdbcAuthDao implements AuthDao, InitializingBean {
    private static final String TABLE_NAME = "UP_JGROUPS_AUTH";
    
    private static final String CREATE_TABLE_SQL =
            "CREATE TABLE " + TABLE_NAME + " (" +
                "SERVICE_NAME varchar(100)," +
                "RANDOM_TOKEN varchar(500), " +
                "PRIMARY KEY (SERVICE_NAME)" +
            ")";
    
    private static final String INSERT_SQL = 
            "INSERT INTO " + TABLE_NAME + " " +
            "(SERVICE_NAME, RANDOM_TOKEN) " +
            "values (:serviceName, :randomToken)";

    private static final String SELECT_SQL = 
            "SELECT RANDOM_TOKEN " +
            "FROM " + TABLE_NAME + " " +
    		"WHERE SERVICE_NAME=:serviceName";

    
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
        }
        
        HashedDaoAuthToken.setAuthDao(this);
    }
    
    @Override
    public String getAuthToken(String serviceName) {
        final String token = DataAccessUtils.singleResult(this.namedParameterJdbcOperations.queryForList(SELECT_SQL, Collections.singletonMap("serviceName", serviceName), String.class));
        if (token != null) {
            return token;
        }
        
        createToken(serviceName);
        
        return getAuthToken(serviceName);
    }

    protected void createToken(final String serviceName) {
        try {
            this.jdbcOperations.execute(new ConnectionCallback<Object>() {
                @Override
                public Object doInConnection(Connection con) throws SQLException, DataAccessException {
                    //This is horribly hacky but we can't rely on the main uPortal TM directly or we get
                    //into a circular dependency loop from JPA to Ehcache to jGroups and back to JPA
                    final DataSource ds = new SingleConnectionDataSource(con, true);
                    final PlatformTransactionManager ptm = new DataSourceTransactionManager(ds);
                    final TransactionOperations to = new TransactionTemplate(ptm);
                    
                    to.execute(new TransactionCallbackWithoutResult() {
                        @Override
                        protected void doInTransactionWithoutResult(TransactionStatus status) {
                            logger.info("Creating jGroups auth token");
                            final String authToken = RandomTokenGenerator.INSTANCE.generateRandomToken(100);
                            final ImmutableMap<String, String> params = ImmutableMap.of("randomToken", authToken, "serviceName", serviceName);
                            namedParameterJdbcOperations.update(INSERT_SQL, params);
                        }
                    });
                    
                    return null;
                }
            });
        }
        catch (ConstraintViolationException e) {
            //Ignore, just means a concurrent token creation
        }
        catch (DataIntegrityViolationException e) {
            //Ignore, just means a concurrent token creation
        }
    }

}
