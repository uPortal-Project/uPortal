/**
 * Copyright © 2001 The JA-SIG Collaborative.  All rights reserved.
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
import org.jasig.portal.utils.DTDResolver;
import org.jasig.portal.utils.XSLT;
import java.io.File;
import java.io.PrintWriter;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.StringReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.net.URL;
import java.sql.Connection;
import java.sql.Statement;
import java.sql.PreparedStatement;
import java.sql.Types;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.w3c.dom.Node;
import org.w3c.dom.Text;
import org.xml.sax.XMLReader;
import org.xml.sax.Attributes;
import org.xml.sax.AttributeList;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.ContentHandler;
import org.apache.xerces.parsers.DOMParser;
import org.apache.xerces.dom.DocumentImpl;

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
 * appropriate mapping.  Such cases will be reported to standard out.</p>
 *
 * <p>An xsl transformation is used to produce the DROP TABLE and CREATE TABLE
 * SQL statements. These statements can be altered by modifying tables.xsl</p>
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
 * <li>Get database connection from RdbmServices
 *     (reads JDBC database settings from <portal.home>/properties/rdbm.properties).</li>
 * <li>Read tables.xml and issue corresponding DROP TABLE and CREATE TABLE SQL statements.</li>
 * <li>Read data.xml and issue corresponding INSERT SQL statements.</li>
 * </ol></p>
 *
 * <p>You will need to set the system property "portal.home"  For example,
 * java -Dportal.home=/usr/local/uPortal</p>
 *
 * @author Ken Weiner, kweiner@interactivebusiness.com
 * @version $Revision$
 * @see java.sql.Types
 * @since uPortal 2.0
 */
public class DbLoader
{
  private static String portalBaseDir;
  private static String propertiesUri;
  private static String tablesUri;
  private static String tablesXslUri;
  private static Connection con;
  private static Statement stmt;
  private static PreparedStatement pstmt;
  private static RdbmServices rdbmService;
  private static Document tablesDoc;
  private static Document tablesDocGeneric;
  private static boolean createScript;
  private static boolean dropTables;
  private static boolean createTables;
  private static boolean populateTables;
  private static PrintWriter scriptOut;

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

        // Read in the properties
        XMLReader parser = getXMLReader();
        printInfo();
        readProperties(parser);

        // Read drop/create/populate table settings
        dropTables = Boolean.valueOf(PropertiesHandler.properties.getDropTables()).booleanValue();
        createTables = Boolean.valueOf(PropertiesHandler.properties.getCreateTables()).booleanValue();
        populateTables = Boolean.valueOf(PropertiesHandler.properties.getPopulateTables()).booleanValue();

        // Set up script
        createScript = Boolean.valueOf(PropertiesHandler.properties.getCreateScript()).booleanValue();
        if (createScript)
          initScript();

        try
        {
          // Read tables.xml
          DOMParser domParser = new DOMParser();
          // Eventually, write and validate against a DTD
          //domParser.setFeature ("http://xml.org/sax/features/validation", true);
          //domParser.setEntityResolver(new DTDResolver("tables.dtd"));
          tablesUri = UtilitiesBean.fixURI(PropertiesHandler.properties.getTablesUri());
          domParser.parse(tablesUri);
          tablesDoc = domParser.getDocument();
        }
        catch(Exception e)
        {
          System.out.println("Could not open " + tablesUri);
          e.printStackTrace();

          return;
        }

        // Hold on to tables xml with generic types
        tablesDocGeneric = (Document)tablesDoc.cloneNode(true);

        // Replace all generic data types with local data types
        replaceDataTypes(tablesDoc);

        // tables.xml + tables.xsl --> DROP TABLE and CREATE TABLE sql statements
        tablesXslUri = UtilitiesBean.fixURI(PropertiesHandler.properties.getTablesXslUri());
        XSLT.transform(tablesDoc, new URL(tablesXslUri), new TableHandler());

        // data.xml --> INSERT sql statements
        readData(parser);

        System.out.println("Done!");
        long endTime = System.currentTimeMillis();
        System.out.println("Elapsed time: " + ((endTime - startTime) / 1000f) + " seconds");
        exit();
      }
      else
        System.out.println("DbLoader couldn't obtain a database connection. See '" + portalBaseDir + "logs" + File.separator + "portal.log' for details.");

    }
    catch (Exception e)
    {
      e.printStackTrace();
    }
    finally
    // call local exit method to clean up.  This does not actually
    // do a system.exit() allowing a stack trace to the console in
    // the case of a run time error.
    {
      exit();
    }
  }

  private static void setPortalBaseDir()
  {
    String portalBaseDirParam = System.getProperty("portal.home");

    if (portalBaseDirParam != null)
    {
      portalBaseDir = portalBaseDirParam;

      if (!portalBaseDir.endsWith(File.separator))
         portalBaseDir += File.separator;

      UtilitiesBean.setPortalBaseDir(portalBaseDir);
    }
    else
    {
      System.out.println("Please set the system parameter portal.home.  For example: java -Dportal.home=/usr/local/portal");
      exit();
    }
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

  private static void printInfo () throws SQLException
  {
    DatabaseMetaData dbMetaData = con.getMetaData();
    String dbName = dbMetaData.getDatabaseProductName();
    String dbVersion = dbMetaData.getDatabaseProductVersion();
    String driverName = dbMetaData.getDriverName();
    String driverVersion = dbMetaData.getDriverVersion();
    String driverClass = rdbmService.getJdbcDriver();
    String url = rdbmService.getJdbcUrl();
    String user = rdbmService.getJdbcUser();
    System.out.println("Starting DbLoader...");
    System.out.println("Database name: '" + dbName + "'");
    System.out.println("Database version: '" + dbVersion + "'");
    System.out.println("Driver name: '" + driverName + "'");
    System.out.println("Driver version: '" + driverVersion + "'");
    System.out.println("Driver class: '" + driverClass + "'");
    System.out.println("Connection URL: '" + url + "'");
    System.out.println("User: '" + user + "'");
  }

  private static void readData (XMLReader parser) throws SAXException, IOException
  {
    DataHandler dataHandler = new DataHandler();
    parser.setContentHandler(dataHandler);
    parser.setErrorHandler(dataHandler);
    parser.parse(UtilitiesBean.fixURI(PropertiesHandler.properties.getDataUri()));
  }

  private static void initScript() throws java.io.IOException
  {
    String scriptFileName = UtilitiesBean.getPortalBaseDir() + "properties" + File.separator + PropertiesHandler.properties.getScriptFileName().replace('/', File.separatorChar);
    File scriptFile = new File(scriptFileName);
    if (scriptFile.exists())
      scriptFile.delete();
    scriptFile.createNewFile();
    scriptOut = new PrintWriter (new BufferedWriter (new FileWriter (scriptFileName, true)));
  }

  private static void replaceDataTypes (Document tablesDoc)
  {
    Element tables = tablesDoc.getDocumentElement();
    NodeList types = tables.getElementsByTagName("type");

    for (int i = 0; i < types.getLength(); i++)
    {
      Node type = (Node)types.item(i);
      NodeList typeChildren = type.getChildNodes();

      for (int j = 0; j < typeChildren.getLength(); j++)
      {
        Node text = (Node)typeChildren.item(j);
        String genericType = text.getNodeValue();

        // Replace generic type with mapped local type
        text.setNodeValue(getLocalDataTypeName(genericType));
      }
    }
  }

  private static int getJavaSqlDataTypeOfColumn(Document tablesDocGeneric, String tableName, String columnName)
  {
    int dataType = 0;

    // Find the right table element
    Element table = getTableWithName(tablesDocGeneric, tableName);

    // Find the columns element within
    Element columns = getFirstChildWithName(table, "columns");

    // Search for the first column who's name is columnName
    for (Node ch = columns.getFirstChild(); ch != null; ch = ch.getNextSibling())
    {
      if (ch instanceof Element && ch.getNodeName().equals("column"))
      {
        Element name = getFirstChildWithName((Element)ch, "name");
        if (getNodeValue(name).equals(columnName))
        {
          // Get the corresponding type and return it's type code
          Element value = getFirstChildWithName((Element)ch, "type");
          dataType = getJavaSqlType(getNodeValue(value));
        }
      }
    }

    return dataType;
  }

  private static Element getFirstChildWithName (Element parent, String name)
  {
    Element child = null;

    for (Node ch = parent.getFirstChild(); ch != null; ch = ch.getNextSibling())
    {
      if (ch instanceof Element && ch.getNodeName().equals(name))
      {
        child = (Element)ch;
        break;
      }
    }

    return child;
  }

  private static Element getTableWithName (Document tablesDoc, String tableName)
  {
    Element tableElement = null;
    NodeList tables = tablesDoc.getElementsByTagName("table");

    for (int i = 0; i < tables.getLength(); i++)
    {
      Node table = (Node)tables.item(i);

      for (Node tableChild = table.getFirstChild(); tableChild != null; tableChild = tableChild.getNextSibling())
      {
        if (tableChild instanceof Element && tableChild.getNodeName() != null && tableChild.getNodeName().equals("name"))
        {
          if (tableName.equals(getNodeValue(tableChild)))
          {
            tableElement = (Element)table;
            break;
          }
        }
      }
    }

    return tableElement;
  }

  private static String getNodeValue (Node node)
  {
    String nodeVal = null;

    for (Node ch = node.getFirstChild(); ch != null; ch = ch.getNextSibling())
    {
      if (ch instanceof Text)
        nodeVal = ch.getNodeValue();
    }

    return nodeVal;
  }

  private static String getLocalDataTypeName (String genericDataTypeName)
  {
   
    
    String localDataTypeName = null;

    try
    {
      DatabaseMetaData dbmd = con.getMetaData();
      String dbName = dbmd.getDatabaseProductName();
      String dbVersion = dbmd.getDatabaseProductVersion();
      String driverName = dbmd.getDriverName();
      String driverVersion = dbmd.getDriverVersion();

      // Check for a mapping in DbLoader.xml
      localDataTypeName = PropertiesHandler.properties.getMappedDataTypeName(dbName, dbVersion, driverName, driverVersion, genericDataTypeName);
      
      if (localDataTypeName != null)
            return localDataTypeName;

      
      // Find the type code for this generic type name
      int dataTypeCode = getJavaSqlType(genericDataTypeName);

      // Find the first local type name matching the type code
      ResultSet rs = dbmd.getTypeInfo();
      try {
        while (rs.next())
        {
          int localDataTypeCode = rs.getInt("DATA_TYPE");

          if (dataTypeCode == localDataTypeCode)
          {
            try { localDataTypeName = rs.getString("TYPE_NAME"); } catch (SQLException sqle) { }
            break;
          }
        }
      } finally {
        rs.close();
      }

      if (localDataTypeName != null)
          return localDataTypeName;
          
      // No matching type found, report an error
      System.out.println("Your database driver, '"+ driverName + "', version '" + driverVersion + "', was unable to find a local type name that matches the generic type name, '" + genericDataTypeName + "'.");
      System.out.println("Please add a mapped type for database '" + dbName + "', version '" + dbVersion + "' inside '" + propertiesUri + "' and run this program again.");
      System.out.println("Exiting...");
      exit();
    }
    catch (Exception e)
    {
      e.printStackTrace();
      exit();
    }

    return null;
  }

  private static int getJavaSqlType (String genericDataTypeName)
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

  private static void dropTable (String dropTableStatement)
  {
    System.out.print("...");

    if (createScript)
      scriptOut.println(dropTableStatement + PropertiesHandler.properties.getStatementTerminator());

    try
    {
      stmt = con.createStatement();
      try { stmt.executeUpdate(dropTableStatement); } catch (SQLException sqle) {/*Table didn't exist*/}
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

  private static void createTable (String createTableStatement)
  {
    System.out.print("...");

    if (createScript)
      scriptOut.println(createTableStatement + PropertiesHandler.properties.getStatementTerminator());

    try
    {
      stmt = con.createStatement();
      stmt.executeUpdate(createTableStatement);
    }
    catch (Exception e)
    {
      System.out.println(createTableStatement);
      e.printStackTrace();
    }
    finally
    {
      try { stmt.close(); } catch (Exception e) { }
    }
  }

  private static void readProperties (XMLReader parser) throws SAXException, IOException
  {
    PropertiesHandler propertiesHandler = new PropertiesHandler();
    parser.setContentHandler(propertiesHandler);
    parser.setErrorHandler(propertiesHandler);
    parser.parse(propertiesUri);
  }

  static class PropertiesHandler
    extends DefaultHandler
  {
    private static StringBuffer charBuff = null;

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
      charBuff = new StringBuffer();

      if (name.equals("properties"))
        properties = new Properties();
      else if (name.equals("db-type-mapping"))
        dbTypeMapping = new DbTypeMapping();
      else if (name.equals("type"))
        type = new Type();
    }

    public void endElement (String uri, String name, String qName)
    {
      if (name.equals("drop-tables")) // drop tables ("true" or "false")
        properties.setDropTables(charBuff.toString());
      else if (name.equals("create-tables")) // create tables ("true" or "false")
        properties.setCreateTables(charBuff.toString());
      else if (name.equals("populate-tables")) // populate tables ("true" or "false")
        properties.setPopulateTables(charBuff.toString());
      else if (name.equals("tables-uri")) // tables URI
        properties.setTablesUri(charBuff.toString());
      else if (name.equals("tables-xsl-uri")) // tables xsl URI
        properties.setTablesXslUri(charBuff.toString());
      else if (name.equals("data-uri")) // data xml URI
        properties.setDataUri(charBuff.toString());
      else if (name.equals("create-script")) // create script ("true" or "false")
        properties.setCreateScript(charBuff.toString());
      else if (name.equals("script-file-name")) // script file name
        properties.setScriptFileName(charBuff.toString());
      else if (name.equals("statement-terminator")) // statement terminator
        properties.setStatementTerminator(charBuff.toString());
      else if (name.equals("db-type-mapping"))
        properties.addDbTypeMapping(dbTypeMapping);
      else if (name.equals("db-name")) // database name
        dbTypeMapping.setDbName(charBuff.toString());
      else if (name.equals("db-version")) // database version
        dbTypeMapping.setDbVersion(charBuff.toString());
      else if (name.equals("driver-name")) // driver name
        dbTypeMapping.setDriverName(charBuff.toString());
      else if (name.equals("driver-version")) // driver version
        dbTypeMapping.setDriverVersion(charBuff.toString());
      else if (name.equals("type"))
        dbTypeMapping.addType(type);
      else if (name.equals("generic")) // generic type
        type.setGeneric(charBuff.toString());
      else if (name.equals("local")) // local type
        type.setLocal(charBuff.toString());
    }

    public void characters (char ch[], int start, int length)
    {
      charBuff.append(ch, start, length);
    }

    class Properties
    {
      private String dropTables;
      private String createTables;
      private String populateTables;
      private String tablesUri;
      private String tablesXslUri;
      private String dataUri;
      private String dataXslUri;
      private String createScript;
      private String scriptFileName;
      private String statementTerminator;
      private ArrayList dbTypeMappings = new ArrayList();

      public String getDropTables() { return dropTables; }
      public String getCreateTables() { return createTables; }
      public String getPopulateTables() { return populateTables; }
      public String getTablesUri() { return tablesUri; }
      public String getTablesXslUri() { return tablesXslUri; }
      public String getDataUri() { return dataUri; }
      public String getDataXslUri() { return dataXslUri; }
      public String getCreateScript() { return createScript; }
      public String getScriptFileName() { return scriptFileName; }
      public String getStatementTerminator() { return statementTerminator; }
      public ArrayList getDbTypeMappings() { return dbTypeMappings; }

      public void setDropTables(String dropTables) { this.dropTables = dropTables; }
      public void setCreateTables(String createTables) { this.createTables = createTables; }
      public void setPopulateTables(String populateTables) { this.populateTables = populateTables; }
      public void setTablesUri(String tablesUri) { this.tablesUri = tablesUri; }
      public void setTablesXslUri(String tablesXslUri) { this.tablesXslUri = tablesXslUri; }
      public void setDataUri(String dataUri) { this.dataUri = dataUri; }
      public void setDataXslUri(String dataXslUri) { this.dataXslUri = dataXslUri; }
      public void setCreateScript(String createScript) { this.createScript = createScript; }
      public void setScriptFileName(String scriptFileName) { this.scriptFileName = scriptFileName; }
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

  static class TableHandler implements ContentHandler
  {
    private static final int UNSET = -1;
    private static final int DROP = 0;
    private static final int CREATE = 1;
    private static int mode = UNSET;
    private static StringBuffer stmtBuffer;

    public void startDocument ()
    {
    }

    public void endDocument ()
    {
      System.out.println();
    }

    public void startElement (String url, String localName, String qName, Attributes atts)
    {
      if (qName.equals("statement"))
      {
        stmtBuffer = new StringBuffer(1024);
        String statementType = atts.getValue("type");

        if (mode == UNSET || mode == CREATE && statementType != null && statementType.equals("drop"))
        {
          mode = DROP;

          System.out.print("Dropping tables...");

          if (!dropTables)
            System.out.print("disabled.");
        }
        else if (mode == UNSET || mode == DROP && statementType != null && statementType.equals("create"))
        {
          mode = CREATE;

          System.out.print("\nCreating tables...");

          if (!createTables)
            System.out.print("disabled.");
        }
      }
    }

    public void endElement (String url, String localName, String qName)
    {
      if (qName.equals("statement"))
      {
        String statement = stmtBuffer.toString();

        switch (mode)
        {
          case DROP:
            if (dropTables)
              dropTable(statement);
            break;
          case CREATE:
            if (createTables)
              createTable(statement);
            break;
          default:
            break;
        }
      }
    }

    public void characters (char ch[], int start, int length)
    {
      stmtBuffer.append(ch, start, length);
    }

    public void setDocumentLocator (Locator locator)
    {
    }

    public void processingInstruction (String target, String data)
    {
    }

    public void ignorableWhitespace (char[] ch, int start, int length)
    {
    }

      public void startPrefixMapping (String prefix, String uri) throws SAXException {};
      public void endPrefixMapping (String prefix) throws SAXException  {};
      public void skippedEntity(String name) throws SAXException {};
  }

  static class DataHandler extends DefaultHandler
  {
    private static boolean insideData = false;
    private static boolean insideTable = false;
    private static boolean insideName = false;
    private static boolean insideRow = false;
    private static boolean insideColumn = false;
    private static boolean insideValue = false;
    private static boolean supportsPreparedStatements = false;

    static Table table;
    static Row row;
    static Column column;

    public void startDocument ()
    {
      System.out.print("Populating tables...");

      if (!populateTables)
        System.out.print("disabled.");

      supportsPreparedStatements = supportsPreparedStatements();
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

          if (populateTables)
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
          String value = column.getValue();

          if (value != null)
          {
            if (value.equals("SYSDATE"))
              sb.append(value);
            else if (value.equals("NULL"))
              sb.append(value);
            else
            {
              sb.append("'");
              sb.append(value.trim());
              sb.append("'");
            }
          }
          else
            sb.append("''");
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

      if (createScript)
        scriptOut.println(prepareInsertStatement(table.getName(), row, false) + PropertiesHandler.properties.getStatementTerminator());


      if (supportsPreparedStatements)
      {
        String preparedStatement = "";
        try
        {
          preparedStatement = prepareInsertStatement(table.getName(), row, true);
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

            // Get a java sql data type for column name
            int javaSqlDataType = getJavaSqlDataTypeOfColumn(tablesDocGeneric, table.getName(), column.getName());

            if (value == null)
              pstmt.setString(i, "");
            else if (value.equals("NULL"))
              pstmt.setNull(i, javaSqlDataType);
            else if (javaSqlDataType == Types.TIMESTAMP)
            {
              if (value.equals("SYSDATE"))
                pstmt.setTimestamp(i, new java.sql.Timestamp(System.currentTimeMillis()));
              else
                pstmt.setTimestamp(i, java.sql.Timestamp.valueOf(value));
            }
            else
            {
              value = value.trim(); // portal can't read xml properly without this, don't know why yet
              int valueLength = value.length();

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
          }
          pstmt.executeUpdate();
        }
        catch (SQLException sqle)
        {
          System.err.println();
          System.err.println(preparedStatement);
          sqle.printStackTrace();
        }
        catch (Exception e)
        {
          System.err.println();
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
          System.err.println();
          System.err.println(insertStatement);
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
        Statement stmt;
        stmt = con.createStatement();
        try {
          stmt.executeUpdate("CREATE TABLE PREP_TEST (A VARCHAR(1))");
        } catch (Exception e){/* Assume it already exists */
        } finally {
          try {stmt.close();} catch (Exception e) { }
        }

        pstmt = con.prepareStatement("SELECT A FROM PREP_TEST WHERE A=?");
        pstmt.clearParameters ();
        pstmt.setString(1, "D");
        pstmt.executeQuery();
     }
      catch (SQLException sqle)
      {
        supportsPreparedStatements = false;
        sqle.printStackTrace();
     }
      finally
      {
        try {
          stmt = con.createStatement();
          stmt.executeUpdate("DROP TABLE PREP_TEST");
        } catch (Exception e){/* Assume it already exists */
        } finally {
          try {stmt.close();} catch (Exception e) { }
        }

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

    if (scriptOut != null)
      scriptOut.close();
  }
}
