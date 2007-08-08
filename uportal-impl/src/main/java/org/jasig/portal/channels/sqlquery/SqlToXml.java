package org.jasig.portal.channels.sqlquery;

import java.util.Iterator;
import java.util.List;

import javax.sql.DataSource;

import org.springframework.jdbc.core.JdbcTemplate;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Executes a SQL query and builds from it a DOM.
 */
public class SqlToXml {

    JdbcTemplate template;
    
    String sql;
    
    public SqlToXml(DataSource ds, String sql) {
        this.template = new JdbcTemplate(ds);
        this.sql = sql;
    }
    
    public void populateDomFromSqlQuery(Document dom) {
        XmlRowMapper xmlRowMapper = new XmlRowMapper(dom);
        List rowElements = template.query(sql, xmlRowMapper);
        
        Element rowsElement = dom.createElement("rows");
        
        
        for (Iterator iter = rowElements.iterator(); iter.hasNext(); ) {
            Element rowElement = (Element) iter.next();
            rowsElement.appendChild(rowElement);
        }
        dom.appendChild(rowsElement);
        
    }
    
}
