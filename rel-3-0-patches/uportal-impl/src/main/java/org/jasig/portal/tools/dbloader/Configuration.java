/* Copyright 2004 The JA-SIG Collaborative.  All rights reserved.
 *  See license distributed with this file and
 *  available online at http://www.uportal.org/license.html
 */

package org.jasig.portal.tools.dbloader;

import java.io.PrintWriter;
import java.net.URL;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import org.jasig.portal.properties.PropertiesManager;
import org.w3c.dom.Document;

/**
 * Holds all configuration values used in DbLoader as part of loading a database.
 *
 * @author Ken Weiner, kweiner@unicon.net
 * @author Mark Boyd  {@link <a href="mailto:mark.boyd@engineer.com">mark.boyd@engineer.com</a>}
 * @version $Revision$
 */
public class Configuration
{
    ///////// config values needed during the DB loading process /////////
    
    private Connection con;
    private PrintWriter scriptWriter;
    private PrintWriter log;
    private Document genericTablesDoc;
    private Document tablesDoc;
   
    private URL propertiesURL;
    private URL tablesURL;
    private URL dataURL;

    private boolean dropTables;
    private boolean createTables;
    private boolean populateTables;
    private boolean createScript;
    
    private String upgradeVersion;
    private int upgradeMajor;
    private int upgradeMinor;

    private String adminLocale;
    private boolean localeAware;
    
    private String tablesUri;
    private String tablesXslUri;
    private String dataUri;
    private String dataXslUri;
    
    private String scriptFileName;
    private String statementTerminator;
    private List<DbTypeMapping> dbTypeMappings = new ArrayList<DbTypeMapping>();
    private Map localDbMetaTypeMap = null;
    private Map tableColumnTypes = new Hashtable(300);
      
      ///////// Accessor Methods /////////

    public void setLocaleAware(boolean localeAware) { this.localeAware = localeAware; }
    public void setAdminLocale(String adminLocale) { this.adminLocale = adminLocale; }

    public void setUpgradeVersion(String v) { this.upgradeVersion = v; }
    public String getUpgradeVersion() { return this.upgradeVersion; }
    public void setUpgradeMajor(int m) { this.upgradeMajor = m; }

    public int getUpgradeMajor() { return this.upgradeMajor; }
    public void setUpgradeMinor(int m) { this.upgradeMinor = m; }
    public int getUpgradeMinor() { return this.upgradeMinor; }

    public URL getPropertiesURL() { return propertiesURL; }
    public URL getTablesURL() { return tablesURL; }
    public URL getDataURL() { return dataURL; }

    public void setTablesURL(URL tablesURL) { this.tablesURL = tablesURL; }
    public void setDataURL(URL dataURL) { this.dataURL = dataURL; }
    public void setPropertiesURL(URL u) { this.propertiesURL=u; }

    public Document getGenericTablesDoc() {return this.genericTablesDoc; }
    public void setGenericTablesDoc(Document d) {this.genericTablesDoc=d; }
    public Document getTablesDoc() {return this.tablesDoc; }
    public void setTablesDoc(Document d) {this.tablesDoc=d; }
    public Connection getConnection() {return con; }
    public PrintWriter getScriptWriter() {return scriptWriter;}
    public PrintWriter getLog() {return log;}
    public void setScriptWriter(PrintWriter w) {this.scriptWriter=w;}
    public void setConnection(Connection c){this.con=c;}
    public void setLog(PrintWriter w) {this.log=w;}

    public boolean getDropTables() { return dropTables; }
    public boolean getCreateTables() { return createTables; }
    public boolean getCreateScript() { return createScript; }
    public boolean getPopulateTables() { return populateTables; }

    public String getTablesUri() { return tablesUri; }
    public String getTablesXslUri() { return tablesXslUri; }

    public String getDataUri() {
        String ret = dataUri;
        if (localeAware == true && adminLocale != null) {
            ret = ret.replaceAll("\\.xml", "_" + adminLocale + ".xml");
        }
        return ret;
    }

    public String getDataXslUri() { return dataXslUri; }
    public String getScriptFileName() { return scriptFileName; }
    public String getStatementTerminator() { return statementTerminator; }
    public List<DbTypeMapping> getDbTypeMappings() { return dbTypeMappings; }

    public void setDropTables(String dropTables) { this.setDropTables(toBoolean(dropTables)); }
    public void setDropTables(boolean dropTables) { this.dropTables = dropTables; }
    public void setCreateTables(String createTables) { this.setCreateTables(toBoolean(createTables)); }
    public void setCreateTables(boolean createTables) { this.createTables = createTables; }
        // CP Mod
    public void setPopulateTables(String populateTables) { this.setPopulateTables(toBoolean(populateTables)); }
    public void setPopulateTables(boolean populateTables) { this.populateTables = populateTables; }
    public void setTablesUri(String tablesUri) { this.tablesUri = tablesUri; }
    public void setTablesXslUri(String tablesXslUri) { this.tablesXslUri = tablesXslUri; }
    public void setDataUri(String dataUri) { this.dataUri = dataUri; }
    public void setDataXslUri(String dataXslUri) { this.dataXslUri = dataXslUri; }
    public void setCreateScript(String createScript) { this.setCreateScript(toBoolean(createScript)); }
    public void setCreateScript(boolean createScript) { this.createScript = createScript; }
    public void setScriptFileName(String scriptFileName) { this.scriptFileName = scriptFileName; }
    public void setStatementTerminator(String statementTerminator) { this.statementTerminator = statementTerminator; }
    public void addDbTypeMapping(DbTypeMapping dbTypeMapping) { dbTypeMappings.add(dbTypeMapping); }
      
    public void setLocalTypeMap(Map m){this.localDbMetaTypeMap = m;}
    public Map getLocalTypeMap(){return this.localDbMetaTypeMap;}
    public Map getTableColumnTypes(){return this.tableColumnTypes;}

    public Configuration()
    {
        System.setProperty("org.xml.sax.driver", PropertiesManager.getProperty("org.xml.sax.driver"));
        this.setLog(new PrintWriter(System.out, true));
    }
    /**
     * Utility method for converting boolean strings to boolean values.
     * @param booleanString 
     * @return boolean
     */
    static boolean toBoolean(String booleanString)
    {
        return Boolean.valueOf(booleanString).booleanValue();
    }
    
    public DbTypeMapping getDbTypeMapping(String dbName, String dbVersion, String driverName, String driverVersion) {
        for (final DbTypeMapping dbTypeMapping : this.dbTypeMappings) {
            final String dbNameProp = dbTypeMapping.getDbName();
            final String dbVersionProp = dbTypeMapping.getDbVersion();
            final String driverNameProp = dbTypeMapping.getDriverName();
            final String driverVersionProp = dbTypeMapping.getDriverVersion();

            if (dbNameProp.equalsIgnoreCase(dbName) 
                    && dbVersionProp.equalsIgnoreCase(dbVersion)
                    && driverNameProp.equalsIgnoreCase(driverName)
                    && (driverVersionProp.equalsIgnoreCase(driverVersion) || driverVersion == null)) {

                // Found a matching database/driver combination
                return dbTypeMapping;
            }
        }
        
        throw new IllegalStateException("*************** No <db-type-mapping> exists for the current configuration. ********************\n" +
                "<db-type-mapping>\n" +
                "    <db-name>" + dbName + "</db-name>\n" +
                "    <db-version>" + dbVersion + "</db-version>\n" +
                "    <driver-name>" + driverName + "</driver-name>\n" +
                "    <driver-version>" + driverVersion + "</driver-version>\n" +
                "</db-type-mapping>\n" +
                "Add this EXACT (minus any '[java] ' prefix if there is one) type mapping fragment to 'uportal-impl/src/main/resources/properties/db/dbloader.xml' along with any needed <type> mappings and re-run the command.\n" +
                "For example <type> mappings look for existing <db-type-mapping> entries for the same database you are using.");
    }

    public String getMappedDataTypeName(String dbName, String dbVersion, String driverName, String driverVersion, String genericDataTypeName) {
        final DbTypeMapping dbTypeMapping = this.getDbTypeMapping(dbName, dbVersion, driverName, driverVersion);
        final String mappedDataTypeName = dbTypeMapping.getMappedDataTypeName(genericDataTypeName);
        return mappedDataTypeName;
    }
}
