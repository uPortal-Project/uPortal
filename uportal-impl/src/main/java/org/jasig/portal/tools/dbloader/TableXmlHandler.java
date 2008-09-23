/**
 * Copyright 2007 The JA-SIG Collaborative.  All rights reserved.
 * See license distributed with this file and
 * available online at http://www.uportal.org/license.html
 */
package org.jasig.portal.tools.dbloader;

import java.lang.reflect.Field;
import java.sql.Types;
import java.util.LinkedHashMap;
import java.util.Map;

import org.hibernate.MappingException;
import org.hibernate.dialect.Dialect;
import org.hibernate.mapping.Column;
import org.hibernate.mapping.Index;
import org.hibernate.mapping.PrimaryKey;
import org.hibernate.mapping.SimpleValue;
import org.hibernate.mapping.Table;
import org.hibernate.mapping.UniqueKey;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

/**
 * Builds an object model of Hibernate mapping objects for tables based on an
 * XML definition file. Once parsing is complete the generated objects are availabe
 * via {@link #getTables()}
 * 
 * @author Eric Dalquist
 * @version $Revision$
 */
public class TableXmlHandler extends BaseDbXmlHandler {
    private final Dialect dialect;
    
    public TableXmlHandler(Dialect dialect) {
        this.dialect = dialect;
    }
    
    public Map<String, Table> getTables() {
        return this.tables;
    }
    
    private Map<String, Table> tables = new LinkedHashMap<String, Table>();
    private Table currentTable = null;
    private Map<String, Column> currentColumns = null;
    private Column currentColumn = null;
    private PrimaryKey primaryKey = null;
    private Index currentIndex = null;
    private UniqueKey currentUnique = null;
    
    /* (non-Javadoc)
     * @see org.xml.sax.helpers.DefaultHandler#startElement(java.lang.String, java.lang.String, java.lang.String, org.xml.sax.Attributes)
     */
    @Override
    public void startElement(String uri, String localName, String name, Attributes attributes) throws SAXException {
        if ("table".equals(name)) {
            this.currentColumns = new LinkedHashMap<String, Column>();
        }
        else if ("index".equals(name)) {
            this.currentIndex = new Index();
            this.currentIndex.setTable(this.currentTable);
        }
        else if ("unique".equals(name)) {
            this.currentUnique = new UniqueKey();
            this.currentUnique.setTable(this.currentTable);
        }
        
        this.chars = new StringBuilder();
    }
    
    /* (non-Javadoc)
     * @see org.xml.sax.helpers.DefaultHandler#endElement(java.lang.String, java.lang.String, java.lang.String)
     */
    @Override
    public void endElement(String uri, String localName, String name) throws SAXException {
        if ("table".equals(name)) {
            for (final Column column : this.currentColumns.values()) {
                this.currentTable.addColumn(column);
            }
            
            if (this.primaryKey != null) {
                this.currentTable.setPrimaryKey(this.primaryKey);
            }
            
            this.tables.put(this.currentTable.getName(), this.currentTable);
            this.primaryKey = null;
            this.currentColumns = null;
            this.currentTable = null;
        }
        else if ("column".equals(name)) {
            this.currentColumns.put(this.currentColumn.getName(), this.currentColumn);
            this.currentColumn = null;
        }
        else if ("name".equals(name)) {
            final String itemName = this.chars.toString().trim();

            if (this.currentIndex != null) {
                this.currentIndex.setName(itemName);
            }
            else if (this.currentUnique != null) {
                this.currentUnique.setName(itemName);
            }
            else if (this.currentTable == null) {
                this.currentTable = new Table(itemName);
            }
            else if (this.currentColumn == null) {
                this.currentColumn = new Column(itemName);
            }
        }
        else if ("desc".equals(name)) {
            final String description = this.chars.toString().trim();
            
            if (this.currentColumn != null) {
                this.currentColumn.setComment(description);
            }
            else if (this.currentTable != null) {
                this.currentTable.setComment(description);
            }
        }
        else if ("type".equals(name)) {
            final String sqlTypeName = this.chars.toString().trim();
            
            final String hibType = this.getHibernateType(sqlTypeName);

            final SimpleValue value = new SimpleValue(this.currentTable);
            value.setTypeName(hibType);
            
            this.currentColumn.setValue(value);
        }
        else if ("param".equals(name)) {
            final String param = this.chars.toString().trim();
            
            final Integer length = Integer.valueOf(param);
            this.currentColumn.setLength(length);
        }
        else if ("primary-key".equals(name)) {
            final String columnName = this.chars.toString().trim();
            
            if (this.primaryKey == null) {
                this.primaryKey = new PrimaryKey();
            }
            
            final Column column = this.currentColumns.get(columnName);
            this.primaryKey.addColumn(column);
        }
        else if ("not-null".equals(name)) {
            final String columnName = this.chars.toString().trim();
            final Column column = this.currentColumns.get(columnName);
            column.setNullable(false);
        }
        else if ("column-ref".equals(name)) {
            final String columnName = this.chars.toString().trim();
            final Column column = this.currentColumns.get(columnName);
            
            if (this.currentIndex != null) {
                this.currentIndex.addColumn(column);
            }
            else if (this.currentUnique != null) {
                this.currentUnique.addColumn(column);
            }
        }
        else if ("index".equals(name)) {
            this.currentTable.addIndex(this.currentIndex);
            this.currentIndex = null;
        }
        else if ("unique".equals(name)) {
            this.currentTable.addUniqueKey(this.currentUnique);
            this.currentUnique = null;
        }
        else if ("key".equals(name)) {
            this.logger.warn("the 'key' element is ignored, use the table level 'primary-key' element instead");
        }
        
        this.chars = null;
    }

    protected String getHibernateType(final String sqlTypeName) {
        final int sqlType;
        try {
            final Field sqlTypeField = Types.class.getField(sqlTypeName);
            sqlType = sqlTypeField.getInt(null);
        }
        catch (SecurityException e) {
            throw new RuntimeException("Cannot access field '" + sqlTypeName + "' on " + Types.class + " for column '" + this.currentColumn.getName() + "'", e);
        }
        catch (NoSuchFieldException e) {
            throw new IllegalArgumentException("No SQL Type field '" + sqlTypeName + "' on " + Types.class + " for column '" + this.currentColumn.getName() + "'", e);
        }
        catch (IllegalArgumentException e) {
            throw new RuntimeException("Cannot access field '" + sqlTypeName + "' on " + Types.class + " for column '" + this.currentColumn.getName() + "'", e);
        }
        catch (IllegalAccessException e) {
            throw new RuntimeException("Cannot access field '" + sqlTypeName + "' on " + Types.class + " for column '" + this.currentColumn.getName() + "'", e);
        }
        
        final String hibType;
        try {
            hibType = this.dialect.getHibernateTypeName(sqlType);
        }
        catch (MappingException e) {
            throw new IllegalArgumentException("No mapped hibernate type found for '" + sqlTypeName + "' Types value=" + sqlType + " for column '" + this.currentColumn.getName() + "'", e);
        }

        return hibType;
    }
}
