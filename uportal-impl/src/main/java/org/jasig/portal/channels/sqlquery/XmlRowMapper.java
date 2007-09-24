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
            if (columnType == Types.VARCHAR || columnType == Types.LONGVARCHAR) {
                columnValue = resultSet.getString(i);
            } else if (columnType == Types.DOUBLE || columnType == Types.FLOAT) {
                double columnValueDouble = resultSet.getDouble(i);
                columnValue = Double.toString(columnValueDouble);
            } else if (columnType == Types.INTEGER) {
                int columnValueInt = resultSet.getInt(i);
                columnValue = Integer.toString(columnValueInt);
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
