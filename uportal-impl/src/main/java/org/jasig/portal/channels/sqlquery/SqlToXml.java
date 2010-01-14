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

import java.util.Iterator;
import java.util.List;

import javax.sql.DataSource;

import org.springframework.jdbc.core.JdbcTemplate;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Executes a SQL query and builds from it a DOM.
 * @deprecated All IChannel implementations should be migrated to portlets
 */
@Deprecated
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
