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

package org.jasig.portal.channels.sqlquery;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Types;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.jdbc.core.RowMapper;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
/**
* @deprecated All IChannel implementations should be migrated to portlets
*/
@Deprecated
public class XmlRowMapper implements RowMapper {

    protected final Log log = LogFactory.getLog(getClass());
    
    private Document document;
    
    public XmlRowMapper(Document parentDoc) {
        this.document = parentDoc;
    }
    
    public Object mapRow(ResultSet resultSet, int row) throws SQLException {
        
        ResultSetMetaData md = resultSet.getMetaData();
        
        Element rowElement = this.document.createElement("row");
        
        for (int i = 1; i < md.getColumnCount() + 1; i++) {
            Element columnElement = this.document.createElement("column");
            Element columnNameElement = this.document.createElement("name");
            String columnName = md.getColumnName(i);
            columnNameElement.setTextContent(columnName);
            columnElement.appendChild(columnNameElement);
            
            
            int columnType = md.getColumnType(i);
            String columnValue = null;
            if (columnType != Types.ARRAY && columnType != Types.BLOB && 
                columnType != Types.CLOB && columnType != Types.STRUCT &&
                columnType != Types.REF && columnType != Types.JAVA_OBJECT /*&&
                columnType != Types.ROWID && columnType != Types.NCLOB &&
                columnType != Types.SQLXML */) {
              columnValue = resultSet.getString(i);
            } else {
                throw new RuntimeException(
                        "Encountered unsupported column type " + 
                        columnType + " for column [" + columnName + "]");
            }
            
            if (columnValue != null) {
                Element columnValueElement = this.document.createElement("value");
                columnValueElement.setTextContent(columnValue);
                columnElement.appendChild(columnValueElement);
            }
            
            if (log.isTraceEnabled()) {
                log.trace("<column><name>" + columnName + "</name><value>" + columnValue + "</value></column>");
            }
            
            rowElement.appendChild(columnElement);
        }
        
        return rowElement;
    }

}
