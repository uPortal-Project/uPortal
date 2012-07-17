package org.jasig.portal.utils;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ConnectionCallback;
import org.springframework.jdbc.core.JdbcOperations;

import com.google.common.base.Function;
import com.google.common.base.Functions;

/**
 * Utilities for JDBC operations
 * 
 * @author Eric Dalquist
 */
public final class JdbcUtils {
    private JdbcUtils() {
    }
    
    /**
     * Check if the named table exists, if it does drop it, calling the preDropCallback first
     * 
     * @param jdbcOperations {@link JdbcOperations} used to check if the table exists and execute the drop
     * @param table The name of the table to drop, case insensitive
     * @param preDropCallback The callback to execute immediately before the table is dropped
     * @return The result returned from the callback
     */
    public static final <T> T dropTableIfExists(JdbcOperations jdbcOperations, final String table, final Function<JdbcOperations, T> preDropCallback) {
        final boolean tableExists = jdbcOperations.execute(new ConnectionCallback<Boolean>() {
            @Override
            public Boolean doInConnection(Connection con) throws SQLException,
                    DataAccessException {
                
                final DatabaseMetaData metaData = con.getMetaData();
                final ResultSet tables = metaData.getTables(null, null, null, new String[] {"TABLE"});
                while (tables.next()) {
                    final String dbTableName = tables.getString("TABLE_NAME");
                    if (table.equalsIgnoreCase(dbTableName)) {
                        return true;
                    }
                }
                
                return false;
            }
        });
        
        if (tableExists) {
            final T ret = preDropCallback.apply(jdbcOperations);
            jdbcOperations.execute("DROP TABLE " + table);
            return ret;
        }
        
        return null;
    }
    
    /**
     * @see #dropTableIfExists(JdbcOperations, String, Function)
     */
    public static final void dropTableIfExists(JdbcOperations jdbcOperations, final String table) {
        dropTableIfExists(jdbcOperations, table, Functions.<JdbcOperations>identity());
    }
}
