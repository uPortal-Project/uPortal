/**
 * Copyright 2007 The JA-SIG Collaborative.  All rights reserved.
 * See license distributed with this file and
 * available online at http://www.uportal.org/license.html
 */
package org.jasig.portal.tools.dbloader;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.sql.DataSource;

import org.apache.commons.collections.map.CaseInsensitiveMap;
import org.apache.commons.lang.ArrayUtils;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceUtils;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import edu.emory.mathcs.backport.java.util.Arrays;

/**
 * Generates and executes SQL INSERT statements as the data XML document is
 * parsed.
 * 
 * @author Eric Dalquist
 * @version $Revision$
 */
public class DataXmlHandler extends BaseDbXmlHandler {
    private final JdbcTemplate jdbcTemplate;
    private final TransactionTemplate transactionTemplate;
    private final Map<String, Map<String, Integer>> tableColumnInfo = new CaseInsensitiveMap();
    private final List<String> script = new LinkedList<String>();
    
    public DataXmlHandler(JdbcTemplate jdbcTemplate, TransactionTemplate transactionTemplate) {
        this.jdbcTemplate = jdbcTemplate;
        this.transactionTemplate = transactionTemplate;
    }
    
    public List<String> getScript() {
        return this.script;
    }
    
    private String currentTable = null;
    private String currentColumn = null;
    private String currentValue = null;
    private Map<String, String> rowData;

    /* (non-Javadoc)
     * @see org.xml.sax.helpers.DefaultHandler#startElement(java.lang.String, java.lang.String, java.lang.String, org.xml.sax.Attributes)
     */
    @Override
    public void startElement(String uri, String localName, String name, Attributes attributes) throws SAXException {
        if ("row".equals(name)) {
            this.rowData = new LinkedHashMap<String, String>();
        }
        
        this.chars = new StringBuilder();
    }
    
    /* (non-Javadoc)
     * @see org.xml.sax.helpers.DefaultHandler#endElement(java.lang.String, java.lang.String, java.lang.String)
     */
    @Override
    public void endElement(String uri, String localName, String name) throws SAXException {
        if ("name".equals(name)) {
            final String itemName = this.chars.toString().trim();
            
            if (this.currentTable == null) {
                this.currentTable = itemName;
            }
            else if (this.currentColumn == null) {
                this.currentColumn = itemName;
            }
        }
        else if ("value".equals(name)) {
            this.currentValue = this.chars.toString().trim();
        }
        else if ("column".equals(name)) {
            this.rowData.put(this.currentColumn, this.currentValue);
            this.currentColumn = null;
            this.currentValue = null;
        }
        else if ("row".equals(name)) {
            this.doInsert();
            this.rowData = null;
        }
        else if ("table".equals(name)) {
            this.currentTable = null;
        }
        
        this.chars = null;
    }
    
    protected final void doInsert() {
        if (this.rowData.size() == 0) {
            this.logger.warn("Found a row with no data for table " + this.currentTable + ", the row will be ignored");
            return;
        }
        
        final Map<String, Integer> columnInfo = this.getTableColumnTypes(this.currentTable);
        
        final StringBuilder columns = new StringBuilder();
        final StringBuilder parameters = new StringBuilder();
        final Object[] values = new Object[this.rowData.size()];
        final int[] types = new int[this.rowData.size()];
        
        int index = 0;
        for (final Iterator<Entry<String, String>> rowIterator = this.rowData.entrySet().iterator(); rowIterator.hasNext(); ) {
            final Entry<String, String> row = rowIterator.next();
            final String columnName = row.getKey();
            columns.append(columnName);
            parameters.append("?");
            
            values[index] = row.getValue();
            types[index] = columnInfo.get(columnName);
            
            if (rowIterator.hasNext()) {
                columns.append(", ");
                parameters.append(", ");
            }
            
            index++;
        }
        
        final String sql = "INSERT INTO " + this.currentTable + " (" + columns + ") VALUES (" + parameters + ")";
        if (this.logger.isInfoEnabled()) {
            this.logger.info(sql + "\t" + Arrays.asList(values) + "\t" + Arrays.asList(ArrayUtils.toObject(types)));
        }

        this.transactionTemplate.execute(new TransactionCallback() {

            /* (non-Javadoc)
             * @see org.springframework.transaction.support.TransactionCallback#doInTransaction(org.springframework.transaction.TransactionStatus)
             */
            public Object doInTransaction(TransactionStatus status) {
                return jdbcTemplate.update(sql, values, types);
            }
        });
    }
    
    protected Map<String, Integer> getTableColumnTypes(String tableName) {
        final Map<String, Integer> columnInfo;
        synchronized (this.tableColumnInfo) {
            if (this.tableColumnInfo.containsKey(tableName)) {
                columnInfo = this.tableColumnInfo.get(tableName);
                this.logger.info("Using pre-populated " + columnInfo + " for " + tableName + "'.");
            }
            else {
                columnInfo = new CaseInsensitiveMap();
                
                final DataSource dataSource = jdbcTemplate.getDataSource();
                final Connection connection = DataSourceUtils.getConnection(dataSource);
                try {
                    final DatabaseMetaData metaData = connection.getMetaData();
                    final ResultSet columns = metaData.getColumns(null, null, tableName, null);
                    this.logger.info("Have columns ResultSet for table '" + tableName + "'.");
                    try {
                        while (columns.next()) {
                            final String name = columns.getString("COLUMN_NAME");
                            final int type = columns.getInt("DATA_TYPE");
                            this.logger.info("Getting column info [name=" + name + ", type=" + type + "] for '" + tableName + "'.");
                            columnInfo.put(name, type);
                        }
                    }
                    finally {
                        columns.close();
                    }
                }
                catch (Exception e) {
                    throw new RuntimeException("Could not determine database table information for populating tables", e);
                }
                finally {
                    DataSourceUtils.releaseConnection(connection, dataSource);
                }
                
                this.logger.info("Loaded " + columnInfo + " for " + tableName + "'.");
            }
        }
        
        return columnInfo;
    }
}
