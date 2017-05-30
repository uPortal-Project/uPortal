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

import java.lang.reflect.Field;
import java.sql.Types;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.TreeMap;
import org.hibernate.MappingException;
import org.hibernate.cfg.Configuration;
import org.hibernate.cfg.Mappings;
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
 * Builds an object model of Hibernate mapping objects for tables based on an XML definition file.
 * Once parsing is complete the generated objects are availabe via {@link #getTables()}
 *
 */
public class TableXmlHandler extends BaseDbXmlHandler implements ITableDataProvider {
    private final Mappings mappings = new Configuration().createMappings();
    private final Dialect dialect;

    public TableXmlHandler(Dialect dialect) {
        this.dialect = dialect;
    }

    @Override
    public Map<String, Table> getTables() {
        return this.tables;
    }

    @Override
    public Map<String, Map<String, Integer>> getTableColumnTypes() {
        return tableColumnTypes;
    }

    private Map<String, Table> tables = new LinkedHashMap<String, Table>();
    private Map<String, Map<String, Integer>> tableColumnTypes =
            new TreeMap<String, Map<String, Integer>>(String.CASE_INSENSITIVE_ORDER);

    private Table currentTable = null;
    private Map<String, Column> currentColumns = null;
    private Map<String, Integer> currentColumnTypes = null;
    private Column currentColumn = null;
    private PrimaryKey primaryKey = null;
    private Index currentIndex = null;
    private UniqueKey currentUnique = null;

    /* (non-Javadoc)
     * @see org.xml.sax.helpers.DefaultHandler#startElement(java.lang.String, java.lang.String, java.lang.String, org.xml.sax.Attributes)
     */
    @Override
    public void startElement(String uri, String localName, String name, Attributes attributes)
            throws SAXException {
        if ("table".equals(name)) {
            this.currentColumns = new LinkedHashMap<String, Column>();
            this.currentColumnTypes = new TreeMap<String, Integer>(String.CASE_INSENSITIVE_ORDER);
        } else if ("index".equals(name)) {
            this.currentIndex = new Index();
            this.currentIndex.setTable(this.currentTable);
        } else if ("unique".equals(name)) {
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
            this.tableColumnTypes.put(this.currentTable.getName(), this.currentColumnTypes);
            this.primaryKey = null;
            this.currentColumns = null;
            this.currentColumnTypes = null;
            this.currentTable = null;
        } else if ("column".equals(name)) {
            this.currentColumns.put(this.currentColumn.getName(), this.currentColumn);
            this.currentColumn = null;
        } else if ("name".equals(name)) {
            final String itemName = this.chars.toString().trim();

            if (this.currentIndex != null) {
                this.currentIndex.setName(itemName);
            } else if (this.currentUnique != null) {
                this.currentUnique.setName(itemName);
            } else if (this.currentTable == null) {
                this.currentTable = new Table(itemName);
            } else if (this.currentColumn == null) {
                this.currentColumn = new Column(itemName);
            }
        } else if ("type".equals(name)) {
            final String sqlTypeName = this.chars.toString().trim();

            final int sqlType = this.getSqlType(sqlTypeName);
            this.currentColumnTypes.put(this.currentColumn.getName(), sqlType);

            final String hibType = this.getHibernateType(sqlType);

            final SimpleValue value = new SimpleValue(this.mappings, this.currentTable);
            value.setTypeName(hibType);

            this.currentColumn.setValue(value);
        } else if ("param".equals(name)) {
            final String param = this.chars.toString().trim();

            final Integer length = Integer.valueOf(param);
            this.currentColumn.setLength(length);
        } else if ("primary-key".equals(name)) {
            final String columnName = this.chars.toString().trim();

            if (this.primaryKey == null) {
                this.primaryKey = new PrimaryKey();
            }

            final Column column = this.currentColumns.get(columnName);
            this.primaryKey.addColumn(column);
        } else if ("not-null".equals(name)) {
            final String columnName = this.chars.toString().trim();
            final Column column = this.currentColumns.get(columnName);
            column.setNullable(false);
        } else if ("column-ref".equals(name)) {
            final String columnName = this.chars.toString().trim();
            final Column column = this.currentColumns.get(columnName);

            if (this.currentIndex != null) {
                this.currentIndex.addColumn(column);
            } else if (this.currentUnique != null) {
                this.currentUnique.addColumn(column);
            }
        } else if ("index".equals(name)) {
            this.currentTable.addIndex(this.currentIndex);
            this.currentIndex = null;
        } else if ("unique".equals(name)) {
            this.currentTable.addUniqueKey(this.currentUnique);
            this.currentUnique = null;
        } else if ("key".equals(name)) {
            this.logger.warn(
                    "the 'key' element is ignored, use the table level 'primary-key' element instead");
        }

        this.chars = null;
    }

    protected int getSqlType(final String sqlTypeName) {
        try {
            final Field sqlTypeField = Types.class.getField(sqlTypeName);
            return sqlTypeField.getInt(null);
        } catch (SecurityException e) {
            throw new RuntimeException(
                    "Cannot access field '"
                            + sqlTypeName
                            + "' on "
                            + Types.class
                            + " for column '"
                            + this.currentColumn.getName()
                            + "'",
                    e);
        } catch (NoSuchFieldException e) {
            throw new IllegalArgumentException(
                    "No SQL Type field '"
                            + sqlTypeName
                            + "' on "
                            + Types.class
                            + " for column '"
                            + this.currentColumn.getName()
                            + "'",
                    e);
        } catch (IllegalArgumentException e) {
            throw new RuntimeException(
                    "Cannot access field '"
                            + sqlTypeName
                            + "' on "
                            + Types.class
                            + " for column '"
                            + this.currentColumn.getName()
                            + "'",
                    e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(
                    "Cannot access field '"
                            + sqlTypeName
                            + "' on "
                            + Types.class
                            + " for column '"
                            + this.currentColumn.getName()
                            + "'",
                    e);
        }
    }

    protected String getHibernateType(final int sqlType) {
        final String hibType;
        try {
            hibType = this.dialect.getHibernateTypeName(sqlType);
        } catch (MappingException e) {
            throw new IllegalArgumentException(
                    "No mapped hibernate type found for '"
                            + sqlType
                            + "' Types value="
                            + sqlType
                            + " for column '"
                            + this.currentColumn.getName()
                            + "'",
                    e);
        }

        return hibType;
    }
}
