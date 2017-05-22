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
package org.apereo.portal.tools.dbloader;

import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import org.apache.commons.lang.ArrayUtils;
import org.springframework.jdbc.core.JdbcOperations;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionOperations;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

/**
 * Generates and executes SQL INSERT statements as the data XML document is parsed.
 *
 */
public class DataXmlHandler extends BaseDbXmlHandler {
    private final JdbcOperations jdbcOperations;
    private final TransactionOperations transactionOperations;
    private final Map<String, Map<String, Integer>> tableColumnInfo;
    private final List<String> script = new LinkedList<String>();

    public DataXmlHandler(
            JdbcOperations jdbcOperations,
            TransactionOperations transactionOperations,
            Map<String, Map<String, Integer>> tableColumnTypes) {
        this.jdbcOperations = jdbcOperations;
        this.transactionOperations = transactionOperations;
        this.tableColumnInfo = tableColumnTypes;
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
    public void startElement(String uri, String localName, String name, Attributes attributes)
            throws SAXException {
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
            } else if (this.currentColumn == null) {
                this.currentColumn = itemName;
            }
        } else if ("value".equals(name)) {
            this.currentValue = this.chars.toString().trim();
        } else if ("column".equals(name)) {
            this.rowData.put(this.currentColumn, this.currentValue);
            this.currentColumn = null;
            this.currentValue = null;
        } else if ("row".equals(name)) {
            this.doInsert();
            this.rowData = null;
        } else if ("table".equals(name)) {
            this.currentTable = null;
        }

        this.chars = null;
    }

    protected final void doInsert() {
        if (this.rowData.size() == 0) {
            this.logger.warn(
                    "Found a row with no data for table "
                            + this.currentTable
                            + ", the row will be ignored");
            return;
        }

        final Map<String, Integer> columnInfo = this.tableColumnInfo.get(this.currentTable);

        final StringBuilder columns = new StringBuilder();
        final StringBuilder parameters = new StringBuilder();
        final Object[] values = new Object[this.rowData.size()];
        final int[] types = new int[this.rowData.size()];

        int index = 0;
        for (final Iterator<Entry<String, String>> rowIterator = this.rowData.entrySet().iterator();
                rowIterator.hasNext();
                ) {
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

        final String sql =
                "INSERT INTO "
                        + this.currentTable
                        + " ("
                        + columns
                        + ") VALUES ("
                        + parameters
                        + ")";
        if (this.logger.isInfoEnabled()) {
            this.logger.info(
                    sql
                            + "\t"
                            + Arrays.asList(values)
                            + "\t"
                            + Arrays.asList(ArrayUtils.toObject(types)));
        }

        this.transactionOperations.execute(
                new TransactionCallbackWithoutResult() {
                    @Override
                    protected void doInTransactionWithoutResult(TransactionStatus status) {
                        jdbcOperations.update(sql, values, types);
                    }
                });
    }
}
