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

package org.jasig.portal.tools;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.jasig.portal.RDBMServices;

/**
 * Title:        DbTest
 * Description:  Displays database metadata information
 * Company:
 * @author  John Fereira
 * @version $Revision$
 */

public class DbTest {

   public static void main(String[] args) throws Exception {
        Connection con = null;
        try {
            con = RDBMServices.getConnection();
            printInfo(con);
        }
        finally {
            try {
                if (con != null) {
                    RDBMServices.releaseConnection(con);
                }
            }
            catch (Exception e) {
            }
        }
    }//end main

  private static void printInfo (Connection conn ) throws Exception
  {
    DatabaseMetaData dbMetaData = conn.getMetaData();
    
    String dbName = dbMetaData.getDatabaseProductName();
    String dbVersion = dbMetaData.getDatabaseProductVersion();
    String driverName = dbMetaData.getDriverName();
    String driverVersion = dbMetaData.getDriverVersion();
    final int databaseMajorVersion = dbMetaData.getDatabaseMajorVersion();
    final int databaseMinorVersion = dbMetaData.getDatabaseMinorVersion();
    final int driverMajorVersion = dbMetaData.getDriverMajorVersion();
    final int driverMinorVersion = dbMetaData.getDriverMinorVersion();
    
    String driverClass = RDBMServices.getJdbcDriver();
    String url = RDBMServices.getJdbcUrl();
    String user = RDBMServices.getJdbcUser();

    boolean supportsANSI92EntryLevelSQL = dbMetaData.supportsANSI92EntryLevelSQL(); 
    boolean supportsANSI92FullSQL = dbMetaData.supportsANSI92FullSQL(); 
    boolean supportsBatchUpdates = dbMetaData.supportsBatchUpdates(); 
    boolean supportsColumnAliasing = dbMetaData.supportsColumnAliasing(); 
    boolean supportsCoreSQLGrammar = dbMetaData.supportsCoreSQLGrammar(); 
    boolean supportsExtendedSQLGrammar = dbMetaData.supportsExtendedSQLGrammar(); 
    boolean supportsExpressionsInOrderBy = dbMetaData.supportsExpressionsInOrderBy(); 
    boolean supportsOuterJoins = dbMetaData.supportsOuterJoins(); 
    boolean supportsFullOuterJoins = dbMetaData.supportsFullOuterJoins(); 
    boolean supportsLimitedOuterJoins = dbMetaData.supportsLimitedOuterJoins(); 

    boolean supportsMultipleTransactions = dbMetaData.supportsMultipleTransactions(); 
    boolean supportsOpenCursorsAcrossCommit = dbMetaData.supportsOpenCursorsAcrossCommit(); 
    boolean supportsOpenCursorsAcrossRollback = dbMetaData.supportsOpenCursorsAcrossRollback(); 
    boolean supportsOpenStatementsAcrossCommit = dbMetaData.supportsOpenStatementsAcrossCommit(); 
    boolean supportsOpenStatementsAcrossRollback = dbMetaData.supportsOpenStatementsAcrossRollback(); 

    boolean supportsOrderByUnrelated = dbMetaData.supportsOrderByUnrelated(); 
    boolean supportsPositionedDelete = dbMetaData.supportsPositionedDelete(); 

    boolean supportsSelectForUpdate = dbMetaData.supportsSelectForUpdate(); 
    boolean supportsStoredProcedures = dbMetaData.supportsStoredProcedures(); 
    boolean supportsTransactions = dbMetaData.supportsTransactions(); 
    boolean supportsUnion = dbMetaData.supportsUnion(); 
    boolean supportsUnionAll = dbMetaData.supportsUnionAll(); 

    int getMaxColumnNameLength = dbMetaData.getMaxColumnNameLength();
    int getMaxColumnsInIndex = dbMetaData.getMaxColumnsInIndex();
    int getMaxColumnsInOrderBy = dbMetaData.getMaxColumnsInOrderBy();
    int getMaxColumnsInSelect = dbMetaData.getMaxColumnsInSelect();
    int getMaxColumnsInTable = dbMetaData.getMaxColumnsInTable();
    int getMaxConnections = dbMetaData.getMaxConnections();
    int getMaxCursorNameLength = dbMetaData.getMaxCursorNameLength();
    int getMaxIndexLength = dbMetaData.getMaxIndexLength();
    int getMaxRowSize = dbMetaData.getMaxRowSize();
    int getMaxStatements = dbMetaData.getMaxStatements();
    int getMaxTableNameLength = dbMetaData.getMaxTableNameLength();
    int getMaxTablesInSelect = dbMetaData.getMaxTablesInSelect();
    int getMaxUserNameLength = dbMetaData.getMaxUserNameLength();
 
    String getSearchStringEscape = dbMetaData.getSearchStringEscape();
    String getStringFunctions = dbMetaData.getStringFunctions();
    String getSystemFunctions = dbMetaData.getSystemFunctions();
    String getTimeDateFunctions = dbMetaData.getTimeDateFunctions();

    ResultSet getTableTypes = null;
    ResultSet getTypeInfo = null;

    System.out.println();
    System.out.println("Database name:    '" + dbName + "'");
    System.out.println("Database version: '" + dbVersion + "' (" + databaseMajorVersion + "." + databaseMinorVersion + ")");
    System.out.println("Driver name:      '" + driverName + "'");
    System.out.println("Driver version:   '" + driverVersion + "' (" + driverMajorVersion + "." + driverMinorVersion + ")");
    System.out.println("Driver class:     '" + driverClass + "'");
    System.out.println("Connection URL:   '" + url + "'");
    System.out.println("User:             '" + user + "'");
    System.out.println();
    
    System.out.println("supportsANSI92EntryLevelSQL: "+supportsANSI92EntryLevelSQL); 
    System.out.println("supportsANSI92FullSQL:       "+supportsANSI92FullSQL); 
    System.out.println("supportsCoreSQLGrammar:     "+supportsCoreSQLGrammar); 
    System.out.println("supportsExtendedSQLGrammar: "+supportsExtendedSQLGrammar); 
    System.out.println();
    
    System.out.println("supportsTransactions:         "+supportsTransactions); 
    System.out.println("supportsMultipleTransactions: "+supportsMultipleTransactions); 

    System.out.println("supportsOpenCursorsAcrossCommit:      "+supportsOpenCursorsAcrossCommit); 
    System.out.println("supportsOpenCursorsAcrossRollback:    "+supportsOpenCursorsAcrossRollback); 
    System.out.println("supportsOpenStatementsAcrossCommit:   "+supportsOpenStatementsAcrossCommit); 
    System.out.println("supportsOpenStatementsAcrossRollback: "+supportsOpenStatementsAcrossRollback); 
    System.out.println();

    System.out.println("supportsStoredProcedures:     "+supportsStoredProcedures); 
    System.out.println("supportsOuterJoins:           "+supportsOuterJoins); 
    System.out.println("supportsFullOuterJoins:       "+supportsFullOuterJoins); 
    System.out.println("supportsLimitedOuterJoins:    "+supportsLimitedOuterJoins); 
    System.out.println("supportsBatchUpdates:         "+supportsBatchUpdates); 
    System.out.println("supportsColumnAliasing:       "+supportsColumnAliasing); 
    System.out.println("supportsExpressionsInOrderBy: "+supportsExpressionsInOrderBy); 
    System.out.println("supportsOrderByUnrelated:     "+supportsOrderByUnrelated); 
    System.out.println("supportsPositionedDelete:     "+supportsPositionedDelete); 
    System.out.println("supportsSelectForUpdate:      "+supportsSelectForUpdate); 
    System.out.println("supportsUnion:                "+supportsUnion); 
    System.out.println("supportsUnionAll:             "+supportsUnionAll); 
    System.out.println();

    System.out.println("getMaxColumnNameLength: "+getMaxColumnNameLength);
    System.out.println("getMaxColumnsInIndex:   "+getMaxColumnsInIndex);
    System.out.println("getMaxColumnsInOrderBy: "+getMaxColumnsInOrderBy);
    System.out.println("getMaxColumnsInSelect:  "+getMaxColumnsInSelect);
    System.out.println("getMaxColumnsInTable:   "+getMaxColumnsInTable);
    System.out.println("getMaxConnections:      "+getMaxConnections);
    System.out.println("getMaxCursorNameLength: "+getMaxCursorNameLength);
    System.out.println("getMaxIndexLength:      "+getMaxIndexLength);
    System.out.println("getMaxRowSize:          "+getMaxRowSize);
    System.out.println("getMaxStatements:       "+getMaxStatements);
    System.out.println("getMaxTableNameLength:  "+getMaxTableNameLength);
    System.out.println("getMaxTablesInSelect:   "+getMaxTablesInSelect);
    System.out.println("getMaxUserNameLength:   "+getMaxUserNameLength);
    System.out.println("getSearchStringEscape:  "+getSearchStringEscape);
    System.out.println("getStringFunctions:     "+getStringFunctions);
    System.out.println("getSystemFunctions:     "+getSystemFunctions);
    System.out.println("getTimeDateFunctions:   "+getTimeDateFunctions);
    System.out.println();

    Statement stmt = null;
    StringBuilder tabletypes = new StringBuilder();
    StringBuilder typeinfo = new StringBuilder();
    try {
        stmt = conn.createStatement();
        getTableTypes = dbMetaData.getTableTypes();
        while (getTableTypes.next()) {
            tabletypes.append(getTableTypes.getString(1)).append(",");
        }
        tabletypes = tabletypes.deleteCharAt(tabletypes.length() - 1);
        System.out.println("Table Types: " + tabletypes);

        getTypeInfo = dbMetaData.getTypeInfo();
        while (getTypeInfo.next()) {
            typeinfo.append(getTypeInfo.getString(1)).append(",");
        }
        typeinfo = typeinfo.deleteCharAt(typeinfo.length() - 1);
        System.out.println("SQL Types:   " + typeinfo);
    }
    catch (SQLException ex) {
    }
    finally {
        try {
            if (stmt != null)
                stmt.close();
            if (getTableTypes != null)
                getTableTypes.close();
            if (getTypeInfo != null)
                getTypeInfo.close();
        }
        catch (SQLException e) {
        }
    } 
  } // end printInfo
}
