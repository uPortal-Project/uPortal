/**
 * Copyright (c) 2000 The JA-SIG Collaborative.  All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. Redistributions of any form whatsoever must retain the following
 *    acknowledgment:
 *    "This product includes software developed by the JA-SIG Collaborative
 *    (http://www.jasig.org/)."
 *
 * THIS SOFTWARE IS PROVIDED BY THE JA-SIG COLLABORATIVE "AS IS" AND ANY
 * EXPRESSED OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL THE JA-SIG COLLABORATIVE OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
 * NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 * OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 */

package org.jasig.portal.tools;

import org.jasig.portal.UtilitiesBean;
import org.jasig.portal.RdbmServices;
import java.sql.Connection;
import java.sql.Statement;
import java.sql.PreparedStatement;
import java.sql.Types;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.XMLReader;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
//import javax.xml.parsers.SAXParserFactory;
//import javax.xml.parsers.SAXParser;


/**
 * <p>A tool to set up a uPortal database. This tool was created so that uPortal
 * developers would only have to maintain a single set of xml documents to define
 * the uPortal database schema and data.  Previously it was necessary to maintain
 * different scripts for each database we wanted to support.</p>
 *
 * <p>DbLoader reads the generic types that are specified in tables.xml and
 * tries to map them to local types by querying the database metadata via methods
 * implemented by the JDBC driver.  Fallback mappings can be supplied in
 * dbloader.xml for cases where the JDBC driver is not able to determine the
 * appropriate mapping.</p>
 *
 * <p>Generic data types (as defined in java.sql.Types) which may be specified
 * in tables.xml include:
 * <code>BIT, TINYINT, SMALLINT, INTEGER, BIGINT, FLOAT, REAL, DOUBLE,
 * NUMERIC, DECIMAL, CHAR, VARCHAR, LONGVARCHAR, DATE, TIME, TIMESTAMP,
 * BINARY, VARBINARY, LONGVARBINARY, NULL, OTHER, JAVA_OBJECT, DISTINCT,
 * STRUCT, ARRAY, BLOB, CLOB, REF</code>
 *
 * <p><strong>WARNING: YOU MAY WANT TO MAKE A BACKUP OF YOUR DATABASE BEFORE RUNNING DbLoader</strong></p>
 *
 * <p>DbLoader will perform the following steps:
 * <ol>
 * <li>Read configurable properties from <portal.home>/properties/dbloader.xml</li>
 * <li>Read tables.xml and issue corresponding DROP TABLE and CREATE TABLE SQL statements.</li>
 * <li>Read data.xml and issue corresponding INSERT SQL statements.</li>
 * </ol></p>
 *
 * <p>You will either need to change the <code>portalBaseDir</code> member variable
 * below and re-compile, or alternatively set the system property "portal.home"</p>
 *
 * @author Ken Weiner, kweiner@interactivebusiness.com
 * @version $Revision$
 * @see java.sql.Types
 * @since uPortal 2.0
 */
public class DbLoader
{
  // CHANGE THIS OR SET portal.home SYSTEM PROPERTY!!!
  private static String portalBaseDir = "D:\\Projects\\JA-SIG\\uPortal2\\";

  private static String propertiesUri;
  private static final String indent = "  ";
  private static final String space = " ";
  private static Connection con = null;
  private static Statement stmt = null;
  private static PreparedStatement pstmt = null;
  private static RdbmServices rdbmService = null;

  public DbLoader()
  {
  }

  public static void main(String[] args)
  {
    try
    {
      setPortalBaseDir();
      propertiesUri = UtilitiesBean.fixURI("properties" + File.separator + "dbloader.xml");
      con = rdbmService.getConnection ();

      if (con != null)
      {
        long startTime = System.currentTimeMillis();

        XMLReader parser = getXMLReader();
        doInfo();
        doProperties(parser);
        //doScriptFile();
        doTables(parser);
        doData(parser);

        System.out.println("Done!");
        long endTime = System.currentTimeMillis();
        System.out.println("Elapsed time: " + ((endTime - startTime) / 1000f) + " seconds");
      }
      else
        System.out.println("DbLoader couldn't obtain a database connection. See '" + portalBaseDir + "logs" + File.separator + "portal.log' for details.");
    }
    catch (Exception e)
    {
      e.printStackTrace();
    }
    finally
    {
      rdbmService.releaseConnection(con);
    }
  }

  private static void setPortalBaseDir()
  {
    String portalBaseDirParam = System.getProperty("portal.home");

    if (portalBaseDirParam != null)
      portalBaseDir = portalBaseDirParam;

    if (!portalBaseDir.endsWith(File.separator))
      portalBaseDir += File.separator;

    UtilitiesBean.setPortalBaseDir(portalBaseDir);
  }

  private static XMLReader getXMLReader()
  {
    // This method of getting a parser won't compile until we start using Xerces 1.2.2 and higher
    //
    // SAXParserFactory spf = SAXParserFactory.newInstance();
    // SAXParser saxParser = spf.newSAXParser();
    // XMLReader xr = saxParser.getXMLReader();

    // For now, we'll use the hard-coded instantiaiton of a Xerces SAX Parser
    XMLReader xr = new org.apache.xerces.parsers.SAXParser();

    return xr;
  }

  private static void doInfo () throws SQLException
  {
    DatabaseMetaData dbMetaData = con.getMetaData();
    String dbName = dbMetaData.getDatabaseProductName();
    String dbVersion = dbMetaData.getDatabaseProductVersion();
    String driverName = dbMetaData.getDriverName();
    String driverVersion = dbMetaData.getDriverVersion();
    System.out.println("Starting DbLoader...");
    System.out.println("Database: " + dbName + ", version: " + dbVersion);
    System.out.println("Driver: " + driverName + ", version: " + driverVersion);
  }

  private static void doProperties (XMLReader parser) throws SAXException, IOException
  {
    PropertiesHandler propertiesHandler = new PropertiesHandler();
    parser.setContentHandler(propertiesHandler);
    parser.setErrorHandler(propertiesHandler);
    parser.parse(propertiesUri);
  }

  private static void doScriptFile()
  {
    // Create or replace file
  }

  private static void doTables (XMLReader parser) throws SAXException, IOException
  {
    TableHandler tableHandler = new TableHandler();
    parser.setContentHandler(tableHandler);
    parser.setErrorHandler(tableHandler);
    parser.parse(UtilitiesBean.fixURI(PropertiesHandler.properties.getTablesUri()));
  }

  private static void doData (XMLReader parser) throws SAXException, IOException
  {
    DataHandler dataHandler = new DataHandler();
    parser.setContentHandler(dataHandler);
    parser.setErrorHandler(dataHandler);
    parser.parse(UtilitiesBean.fixURI(PropertiesHandler.properties.getDataUri()));
  }

  static class PropertiesHandler extends DefaultHandler
  {
    private static boolean insideProperties = false;
    private static boolean insideTablesUri = false;
    private static boolean insideDataUri = false;
    private static boolean insideCreateScript = false;
    private static boolean insideScriptUri = false;
    private static boolean insideStatementTerminator = false;
    private static boolean insideDbTypeMapping = false;
    private static boolean insideDbName = false;
    private static boolean insideDbVersion = false;
    private static boolean insideDriverName = false;
    private static boolean insideDriverVersion = false;
    private static boolean insideType = false;
    private static boolean insideGeneric = false;
    private static boolean insideLocal = false;

    static Properties properties;
    static DbTypeMapping dbTypeMapping;
    static Type type;

    public void startDocument ()
    {
      System.out.print("Parsing " + propertiesUri + "...");
    }

    public void endDocument ()
    {
      System.out.println("");
    }

    public void startElement (String uri, String name, String qName, Attributes atts)
    {
        if (name.equals("properties"))
        {
          insideProperties = true;
          properties = new Properties();
        }
        else if (name.equals("tables-uri"))
          insideTablesUri = true;
        else if (name.equals("data-uri"))
          insideDataUri = true;
        else if (name.equals("create-script"))
          insideCreateScript = true;
        else if (name.equals("script-uri"))
          insideScriptUri = true;
        else if (name.equals("statement-terminator"))
          insideStatementTerminator = true;
        else if (name.equals("db-type-mapping"))
        {
          insideDbTypeMapping = true;
          dbTypeMapping = new DbTypeMapping();
        }
        else if (name.equals("db-name"))
          insideDbName = true;
        else if (name.equals("db-version"))
          insideDbVersion = true;
        else if (name.equals("driver-name"))
          insideDriverName = true;
        else if (name.equals("driver-version"))
          insideDriverVersion = true;

        else if (name.equals("type"))
        {
          insideType = true;
          type = new Type();
        }
        else if (name.equals("generic"))
          insideGeneric = true;
        else if (name.equals("local"))
          insideLocal = true;
    }

    public void endElement (String uri, String name, String qName)
    {
        if (name.equals("properties"))
          insideProperties = false;
        else if (name.equals("tables-uri"))
          insideTablesUri = false;
        else if (name.equals("data-uri"))
          insideDataUri = false;
        else if (name.equals("create-script"))
          insideCreateScript = false;
        else if (name.equals("script-uri"))
          insideScriptUri = false;
        else if (name.equals("statement-terminator"))
          insideStatementTerminator = false;
        else if (name.equals("db-type-mapping"))
        {
          insideDbTypeMapping = false;
          properties.addDbTypeMapping(dbTypeMapping);
        }
        else if (name.equals("db-name"))
          insideDbName = false;
        else if (name.equals("db-version"))
          insideDbVersion = false;
        else if (name.equals("driver-name"))
          insideDriverName = false;
        else if (name.equals("driver-version"))
          insideDriverVersion = false;
        else if (name.equals("type"))
        {
          insideType = false;
          dbTypeMapping.addType(type);
        }
        else if (name.equals("generic"))
          insideGeneric = false;
        else if (name.equals("local"))
          insideLocal = false;
    }

    public void characters (char ch[], int start, int length)
    {
      if (insideTablesUri) // tables xml URI
        properties.setTablesUri(new String(ch, start, length));
      else if (insideDataUri) // data xml URI
        properties.setDataUri(new String(ch, start, length));
      else if (insideCreateScript) // create script ("yes" or "no")
        properties.setCreateScript(new String(ch, start, length));
      else if (insideScriptUri) // script URI
        properties.setScriptUri(new String(ch, start, length));
      else if (insideStatementTerminator) // statement terminator
        properties.setStatementTerminator(new String(ch, start, length));
      else if (insideDbTypeMapping && insideDbName) // database name
        dbTypeMapping.setDbName(new String(ch, start, length));
      else if (insideDbTypeMapping && insideDbVersion) // database version
        dbTypeMapping.setDbVersion(new String(ch, start, length));
      else if (insideDbTypeMapping && insideDriverName) // driver name
        dbTypeMapping.setDriverName(new String(ch, start, length));
      else if (insideDbTypeMapping && insideDriverVersion) // driver version
        dbTypeMapping.setDriverVersion(new String(ch, start, length));
      else if (insideDbTypeMapping && insideType && insideGeneric) // generic type
        type.setGeneric(new String(ch, start, length));
      else if (insideDbTypeMapping && insideType && insideLocal) // local type
        type.setLocal(new String(ch, start, length));
    }

    class Properties
    {
      private String tablesUri;
      private String dataUri;
      private String createScript;
      private String scriptUri;
      private String statementTerminator;
      private ArrayList dbTypeMappings = new ArrayList();

      public String getTablesUri() { return tablesUri; }
      public String getDataUri() { return dataUri; }
      public String getCreateScript() { return createScript; }
      public String getScriptUri() { return scriptUri; }
      public String getStatementTerminator() { return statementTerminator; }
      public ArrayList getDbTypeMappings() { return dbTypeMappings; }

      public void setTablesUri(String tablesUri) { this.tablesUri = tablesUri; }
      public void setDataUri(String dataUri) { this.dataUri = dataUri; }
      public void setCreateScript(String createScript) { this.createScript = createScript; }
      public void setScriptUri(String scriptUri) { this.scriptUri = scriptUri; }
      public void setStatementTerminator(String statementTerminator) { this.statementTerminator = statementTerminator; }
      public void addDbTypeMapping(DbTypeMapping dbTypeMapping) { dbTypeMappings.add(dbTypeMapping); }

      public String getMappedDataTypeName(String dbName, String dbVersion, String driverName, String driverVersion, String genericDataTypeName)
      {
        String mappedDataTypeName = null;
        Iterator iterator = dbTypeMappings.iterator();

        while (iterator.hasNext())
        {
          DbTypeMapping dbTypeMapping = (DbTypeMapping)iterator.next();
          String dbNameProp = dbTypeMapping.getDbName();
          String dbVersionProp = dbTypeMapping.getDbVersion();
          String driverNameProp = dbTypeMapping.getDriverName();
          String driverVersionProp = dbTypeMapping.getDriverVersion();

          if (dbNameProp.equalsIgnoreCase(dbName) && dbVersionProp.equalsIgnoreCase(dbVersion) &&
              driverNameProp.equalsIgnoreCase(driverName) && driverVersionProp.equalsIgnoreCase(driverVersion))
          {
            // Found a matching database/driver combination
            mappedDataTypeName = dbTypeMapping.getMappedDataTypeName(genericDataTypeName);
          }
        }
        return mappedDataTypeName;
      }

    }

    class DbTypeMapping
    {
      String dbName;
      String dbVersion;
      String driverName;
      String driverVersion;
      ArrayList types = new ArrayList();

      public String getDbName() { return dbName; }
      public String getDbVersion() { return dbVersion; }
      public String getDriverName() { return driverName; }
      public String getDriverVersion() { return driverVersion; }
      public ArrayList getTypes() { return types; }

      public void setDbName(String dbName) { this.dbName = dbName; }
      public void setDbVersion(String dbVersion) { this.dbVersion = dbVersion; }
      public void setDriverName(String driverName) { this.driverName = driverName; }
      public void setDriverVersion(String driverVersion) { this.driverVersion = driverVersion; }
      public void addType(Type type) { types.add(type); }

      public String getMappedDataTypeName(String genericDataTypeName)
      {
        String mappedDataTypeName = null;
        Iterator iterator = types.iterator();

        while (iterator.hasNext())
        {
          Type type = (Type)iterator.next();

          if (type.getGeneric().equalsIgnoreCase(genericDataTypeName))
            mappedDataTypeName = type.getLocal();
        }
        return mappedDataTypeName;
      }
    }

    class Type
    {
      String genericType; // "generic" is a Java reserved word
      String local;

      public String getGeneric() { return genericType; }
      public String getLocal() { return local; }

      public void setGeneric(String genericType) { this.genericType = genericType; }
      public void setLocal(String local) { this.local = local; }
    }
  }

  static class TableHandler extends DefaultHandler
  {
    private static boolean insideTables = false;
    private static boolean insideTable = false;
    private static boolean insideName = false;
    private static boolean insideType = false;
    private static boolean insideColumn = false;
    private static boolean insideParam = false;
    private static boolean insidePrimaryKey = false;

    static HashMap tables;
    static Table table;
    static Column column;

    public void startDocument ()
    {
      System.out.print("Parsing " + PropertiesHandler.properties.getTablesUri() + "...");
    }

    public void endDocument ()
    {
      System.out.println("");
    }

    public void startElement (String uri, String name, String qName, Attributes atts)
    {
        if (name.equals("tables"))
        {
          insideTables = true;
          tables = new HashMap();
        }
        else if (name.equals("table"))
        {
          insideTable = true;
          table = new Table();
        }
        else if (name.equals("name"))
          insideName = true;
        else if (name.equals("column"))
        {
          insideColumn = true;
          column = new Column();
        }
        else if (name.equals("type"))
          insideType = true;
        else if (name.equals("param"))
          insideParam = true;
        else if (name.equals("primary-key"))
          insidePrimaryKey = true;
    }

    public void endElement (String uri, String name, String qName)
    {
        if (name.equals("tables"))
          insideTables = false;
        else if (name.equals("table"))
        {
          insideTable = false;
          replaceTable(table);
          tables.put(table.getName(), table);
        }
        else if (name.equals("name"))
          insideName = false;
        else if (name.equals("column"))
        {
          insideColumn = false;
          table.addColumn(column);
        }
        else if (name.equals("type"))
          insideType = false;
        else if (name.equals("param"))
          insideParam = false;
        else if (name.equals("primary-key"))
          insidePrimaryKey = false;
    }

    public void characters (char ch[], int start, int length)
    {
      // Implicitly inside <tables> and <table>
      if (insideName && !insideColumn) // table name
        table.setName(new String(ch, start, length));
      else if (insideColumn && insideName) // column name
        column.setName(new String(ch, start, length));
      else if (insideColumn && insideType) // column type
      {
        column.setGenericType(new String(ch, start, length));
        column.setLocalType(getLocalDataTypeName(new String(ch, start, length)));
      }
      else if (insideColumn && insideParam) // column param
        column.setParam(new String(ch, start, length));
      else if (insidePrimaryKey) // a primary key
        table.setPrimaryKey(new String(ch, start, length));
    }

    private String getLocalDataTypeName (String genericDataTypeName)
    {
      // Find the type code for this generic type name
      int dataTypeCode = getJavaSqlType(genericDataTypeName);

      // Find the first local type name matching the type code
      String localDataTypeName = null;

      try
      {
        DatabaseMetaData dbmd = con.getMetaData();
        ResultSet rs = dbmd.getTypeInfo();

        while (rs.next())
        {
          int localDataTypeCode = rs.getInt("DATA_TYPE");
          //String createParams = rs.getString("CREATE_PARAMS");

          if (dataTypeCode == localDataTypeCode)
          {
            try { localDataTypeName = rs.getString("TYPE_NAME"); } catch (SQLException sqle) { }
            break;
          }
        }

        // If a local data type wasn't found, look in properties file
        // for a mapped data type name
        if (localDataTypeName == null)
        {
          DatabaseMetaData dbMetaData = con.getMetaData();
          String dbName = dbMetaData.getDatabaseProductName();
          String dbVersion = dbMetaData.getDatabaseProductVersion();
          String driverName = dbMetaData.getDriverName();
          String driverVersion = dbMetaData.getDriverVersion();

          localDataTypeName = PropertiesHandler.properties.getMappedDataTypeName(dbName, dbVersion, driverName, driverVersion, genericDataTypeName);

          if (localDataTypeName == null)
          {
            System.out.println("Your database driver, '"+ driverName + "', version '" + driverVersion + "', was unable to find a local type name that matches the generic type name, '" + genericDataTypeName + "'.");
            System.out.println("Please add a mapped type for database '" + dbName + "', version '" + dbVersion + "' inside '" + propertiesUri + "' and run this program again.");
            System.out.println("Exiting...");
            exit();
          }
        }
      }
      catch (Exception e)
      {
        e.printStackTrace();
        exit();
      }

      return localDataTypeName;
    }

    private int getJavaSqlType (String genericDataTypeName)
    {
      // Find the type code for this generic type name
      int dataTypeCode = 0;

      if (genericDataTypeName.equalsIgnoreCase("BIT"))
        dataTypeCode = Types.BIT; // -7
      else if (genericDataTypeName.equalsIgnoreCase("TINYINT"))
        dataTypeCode = Types.TINYINT; // -6
      else if (genericDataTypeName.equalsIgnoreCase("SMALLINT"))
        dataTypeCode = Types.SMALLINT; // 5
      else if (genericDataTypeName.equalsIgnoreCase("INTEGER"))
        dataTypeCode = Types.INTEGER; // 4
      else if (genericDataTypeName.equalsIgnoreCase("BIGINT"))
        dataTypeCode = Types.BIGINT; // -5

      else if (genericDataTypeName.equalsIgnoreCase("FLOAT"))
        dataTypeCode = Types.FLOAT; // 6
      else if (genericDataTypeName.equalsIgnoreCase("REAL"))
        dataTypeCode = Types.REAL; // 7
      else if (genericDataTypeName.equalsIgnoreCase("DOUBLE"))
        dataTypeCode = Types.DOUBLE; // 8

      else if (genericDataTypeName.equalsIgnoreCase("NUMERIC"))
        dataTypeCode = Types.NUMERIC; // 2
      else if (genericDataTypeName.equalsIgnoreCase("DECIMAL"))
        dataTypeCode = Types.DECIMAL; // 3

      else if (genericDataTypeName.equalsIgnoreCase("CHAR"))
        dataTypeCode = Types.CHAR; // 1
      else if (genericDataTypeName.equalsIgnoreCase("VARCHAR"))
        dataTypeCode = Types.VARCHAR; // 12
      else if (genericDataTypeName.equalsIgnoreCase("LONGVARCHAR"))
        dataTypeCode = Types.LONGVARCHAR; // -1

      else if (genericDataTypeName.equalsIgnoreCase("DATE"))
        dataTypeCode = Types.DATE; // 91
      else if (genericDataTypeName.equalsIgnoreCase("TIME"))
        dataTypeCode = Types.TIME; // 92
      else if (genericDataTypeName.equalsIgnoreCase("TIMESTAMP"))
        dataTypeCode = Types.TIMESTAMP; // 93

      else if (genericDataTypeName.equalsIgnoreCase("BINARY"))
        dataTypeCode = Types.BINARY; // -2
      else if (genericDataTypeName.equalsIgnoreCase("VARBINARY"))
        dataTypeCode = Types.VARBINARY; // -3
      else if (genericDataTypeName.equalsIgnoreCase("LONGVARBINARY"))
        dataTypeCode = Types.LONGVARBINARY;  // -4

      else if (genericDataTypeName.equalsIgnoreCase("NULL"))
        dataTypeCode = Types.NULL; // 0

      else if (genericDataTypeName.equalsIgnoreCase("OTHER"))
        dataTypeCode = Types.OTHER; // 1111

      else if (genericDataTypeName.equalsIgnoreCase("JAVA_OBJECT"))
        dataTypeCode = Types.JAVA_OBJECT; // 2000
      else if (genericDataTypeName.equalsIgnoreCase("DISTINCT"))
        dataTypeCode = Types.DISTINCT; // 2001
      else if (genericDataTypeName.equalsIgnoreCase("STRUCT"))
        dataTypeCode = Types.STRUCT; // 2002

      else if (genericDataTypeName.equalsIgnoreCase("ARRAY"))
        dataTypeCode = Types.ARRAY; // 2003
      else if (genericDataTypeName.equalsIgnoreCase("BLOB"))
        dataTypeCode = Types.BLOB; // 2004
      else if (genericDataTypeName.equalsIgnoreCase("CLOB"))
        dataTypeCode = Types.CLOB; // 2005
      else if (genericDataTypeName.equalsIgnoreCase("REF"))
        dataTypeCode = Types.REF; // 2006

      return dataTypeCode;
    }

    private String prepareDropTableStatement (Table table)
    {
      StringBuffer sb = new StringBuffer("DROP TABLE ");
      sb.append(table.getName());
      return sb.toString();
    }

    private String prepareCreateTableStatement (Table table)
    {
      StringBuffer sb = new StringBuffer("CREATE TABLE ");
      sb.append(table.getName()).append("\n(\n");

      ArrayList columns = table.getColumns();
      Iterator iterator = columns.iterator();

      while (iterator.hasNext())
      {
        Column column = (Column)iterator.next();
        sb.append(indent).append(column.getName());
        sb.append(space).append(column.getLocalType());

        String param = column.getParam();

        if (param != null)
          sb.append("(").append(param).append(")");

        sb.append(",\n");
      }

      String primaryKey = table.getPrimaryKey();

      if (primaryKey != null)
        sb.append(indent).append("PRIMARY KEY (").append(primaryKey).append("),\n");

      sb.append(")");

      // Delete comma after last line (kind of sloppy, but it works)
      sb.deleteCharAt(sb.length() - 3);

      return sb.toString();
    }

    private void replaceTable (Table table)
    {
      System.out.print("...");
      String dropTableStatement = prepareDropTableStatement(table);
      String createTableStatement = prepareCreateTableStatement(table);
      //System.out.println(dropTableStatement);
      //System.out.println(createTableStatement);

      try
      {
        stmt = con.createStatement();

        try { stmt.executeUpdate(dropTableStatement); } catch (SQLException sqle) {/*Table didn't exist*/}
        stmt.executeUpdate(createTableStatement);
      }
      catch (Exception e)
      {
        e.printStackTrace();
      }
      finally
      {
        try { stmt.close(); } catch (Exception e) { }
      }
    }

    class Table
    {
      private String name;
      private ArrayList columns = new ArrayList();
      private String primaryKey;

      public String getName() { return name; }
      public ArrayList getColumns() { return columns; }
      public String getPrimaryKey() { return primaryKey; }
      public void setName(String name) { this.name = name; }
      public void setPrimaryKey(String primaryKey) { this.primaryKey = primaryKey; }
      public void addColumn(Column column) { columns.add(column); }

      public int getSqlType (String columnName)
      {
        int type = 0;
        Iterator iterator = columns.iterator();

        while (iterator.hasNext())
        {
          Column column = (Column)iterator.next();

          if (column.getName().equalsIgnoreCase(columnName))
          {
            String columnType = column.getGenericType();
            type = getJavaSqlType(columnType);
            break;
          }
        }
        return type;
      }
    }

    class Column
    {
      private String name;
      private String localType;
      private String genericType;
      private String param;

      public String getName() { return name; }
      public String getLocalType() { return localType; }
      public String getGenericType() { return genericType; }
      public String getParam() { return param; }
      public void setName(String name) { this.name = name; }
      public void setLocalType(String localType) { this.localType = localType; }
      public void setGenericType(String genericType) { this.genericType = genericType; }
      public void setParam(String param) { this.param = param; }
    }
  }

  static class DataHandler extends DefaultHandler
  {
    private static boolean insideData = false;
    private static boolean insideTable = false;
    private static boolean insideName = false;
    private static boolean insideRow = false;
    private static boolean insideColumn = false;
    private static boolean insideValue = false;

    static Table table;
    static Row row;
    static Column column;

    public void startDocument ()
    {
      System.out.print("Parsing " + PropertiesHandler.properties.getDataUri() + "...");
    }

    public void endDocument ()
    {
      System.out.println("");
    }

    public void startElement (String uri, String name, String qName, Attributes atts)
    {
        if (name.equals("data"))
          insideData = true;
        else if (name.equals("table"))
        {
          insideTable = true;
          table = new Table();
        }
        else if (name.equals("name"))
          insideName = true;
        else if (name.equals("row"))
        {
          insideRow = true;
          row = new Row();
        }
        else if (name.equals("column"))
        {
          insideColumn = true;
          column = new Column();
        }
        else if (name.equals("value"))
          insideValue = true;
    }

    public void endElement (String uri, String name, String qName)
    {
        if (name.equals("data"))
          insideData = false;
        else if (name.equals("table"))
          insideTable = false;
        else if (name.equals("name"))
          insideName = false;
        else if (name.equals("row"))
        {
          insideRow = false;
          insertRow(table, row);
        }
        else if (name.equals("column"))
        {
          insideColumn = false;
          row.addColumn(column);
        }
        else if (name.equals("value"))
          insideValue = false;
    }

    public void characters (char ch[], int start, int length)
    {
      // Implicitly inside <data> and <table>
      if (insideName && !insideColumn) // table name
        table.setName(new String(ch, start, length));
      else if (insideColumn && insideName) // column name
        column.setName(new String(ch, start, length));
      else if (insideColumn && insideValue) // column value
        column.setValue(new String(ch, start, length));
    }

    private String prepareInsertStatement (String tableName, Row row, boolean preparedStatement)
    {
      StringBuffer sb = new StringBuffer("INSERT INTO ");
      sb.append(table.getName()).append(" (");

      ArrayList columns = row.getColumns();
      Iterator iterator = columns.iterator();

      while (iterator.hasNext())
      {
        Column column = (Column)iterator.next();
        sb.append(column.getName()).append(", ");
      }

      // Delete comma and space after last column name (kind of sloppy, but it works)
      sb.deleteCharAt(sb.length() - 1);
      sb.deleteCharAt(sb.length() - 1);

      sb.append(") VALUES (");
      iterator = columns.iterator();

      while (iterator.hasNext())
      {
        Column column = (Column)iterator.next();

        if (preparedStatement)
           sb.append("?");
        else
        {
          sb.append("'");
          String value = column.getValue();
          sb.append(value != null ? value.trim() : "");
          sb.append("'");
        }

        sb.append(", ");
      }

      // Delete comma and space after last value (kind of sloppy, but it works)
      sb.deleteCharAt(sb.length() - 1);
      sb.deleteCharAt(sb.length() - 1);

      sb.append(")");

      return sb.toString();
    }

    private void insertRow (Table table, Row row)
    {
      System.out.print("...");
      boolean supportsPreparedStatements = supportsPreparedStatements();

      if (supportsPreparedStatements)
      {
        try
        {
          String preparedStatement = prepareInsertStatement(table.getName(), row, true);
          //System.out.println(preparedStatement);
          pstmt = con.prepareStatement(preparedStatement);
          pstmt.clearParameters ();

          // Loop through parameters and set them, checking for any that excede 4k
          ArrayList columns = row.getColumns();
          Iterator iterator = columns.iterator();

          for (int i = 1; iterator.hasNext(); i++)
          {
            Column column = (Column)iterator.next();
            String value = column.getValue();

            if (value != null)
            {
              value = value.trim(); // portal can't read xml properly without this, don't know why yet
              int valueLength = value.length();
              TableHandler.Table aTable = (TableHandler.Table)TableHandler.tables.get(table.getName());
              int javaSqlDataType = aTable.getSqlType(column.getName());

              if (valueLength <= 4000)
              {
                try
                {
                  // Needed for Sybase and maybe others
                  pstmt.setObject(i, value, javaSqlDataType);
                }
                catch (Exception e)
                {
                  // Needed for Oracle and maybe others
                  pstmt.setObject(i, value);
                }
              }
              else
              {
                try
                {
                  try
                  {
                    // Needed for Sybase and maybe others
                    pstmt.setObject(i, value, javaSqlDataType);
                  }
                  catch (Exception e)
                  {
                   // Needed for Oracle and maybe others
                   pstmt.setObject(i, value);
                  }
                }
                catch (SQLException sqle)
                {
                  // For Oracle and maybe others
                  pstmt.setCharacterStream(i, new StringReader(value), valueLength);
                }
              }
            }
            else
              pstmt.setString(i, "");
          }
          pstmt.executeUpdate();
        }
        catch (Exception e)
        {
          e.printStackTrace();
        }
        finally
        {
          try { pstmt.close();  } catch (Exception e) { }
        }
      }
      else
      {
        // If prepared statements aren't supported, try a normal insert statement
        String insertStatement = prepareInsertStatement(table.getName(), row, false);
        //System.out.println(insertStatement);

        try
        {
          stmt = con.createStatement();
          stmt.executeUpdate(insertStatement);
        }
        catch (Exception e)
        {
          e.printStackTrace();
        }
        finally
        {
          try { stmt.close(); } catch (Exception e) { }
        }
      }
    }

    private static boolean supportsPreparedStatements()
    {
      boolean supportsPreparedStatements = true;

      try
      {
        // Issue a prepared statement to see if database/driver accepts them.
        // The assumption is that if a SQLException is thrown, it doesn't support them.
        // I don't know of any other way to check if the database/driver accepts
        // prepared statements.  If you do, please change this method!
        pstmt = con.prepareStatement("SELECT * FROM UP_USERS WHERE USER_NAME=?");
        pstmt.clearParameters ();
        pstmt.setString(1, "DUMMY_VALUE");
        pstmt.execute();
      }
      catch (SQLException sqle)
      {
        supportsPreparedStatements = false;
        sqle.printStackTrace();
        exit();
      }
      finally
      {
        try { pstmt.close(); } catch (Exception e) { }
      }

      return supportsPreparedStatements;
    }

    class Table
    {
      private String name;

      public String getName() { return name; }
      public void setName(String name) { this.name = name; }
    }

    class Row
    {
      ArrayList columns = new ArrayList();

      public ArrayList getColumns() { return columns; }
      public void addColumn(Column column) { columns.add(column); }
    }

    class Column
    {
      private String name;
      private String value;

      public String getName() { return name; }
      public String getValue() { return value; }
      public void setName(String name) { this.name = name; }
      public void setValue(String value) { this.value = value; }
    }
  }

  private static void exit()
  {
    rdbmService.releaseConnection(con);
    System.exit(1);
  }
}