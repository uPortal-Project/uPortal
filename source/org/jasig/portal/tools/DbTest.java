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

   public static void main(String[] args) {

      Connection con = null;
      try {
         con = RDBMServices.getConnection ();
	 if (con == null) {
            System.err.println("Unable to get a database connection");
            return;
         }
         printInfo(con);
      }
      catch (SQLException e) {
         e.printStackTrace();
      } finally {
         try { RDBMServices.releaseConnection(con); } catch (Exception e) {}
      }
   }//end main

  private static void printInfo (Connection conn ) throws SQLException
  {

    DatabaseMetaData dbMetaData = conn.getMetaData();

    String dbName = dbMetaData.getDatabaseProductName();
    String dbVersion = dbMetaData.getDatabaseProductVersion();
    String driverName = dbMetaData.getDriverName();
    String driverVersion = dbMetaData.getDriverVersion();
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

    System.out.println("Database name: '" + dbName + "'");
    System.out.println("Database version: '" + dbVersion + "'");
    System.out.println("Driver name: '" + driverName + "'");
    System.out.println("Driver version: '" + driverVersion + "'");
    System.out.println("Driver class: '" + driverClass + "'");
    System.out.println("Connection URL: '" + url + "'");
    System.out.println("User: '" + user + "'");

    System.out.println("supportsANSI92EntryLevelSQL: "+supportsANSI92EntryLevelSQL); 
    System.out.println("supportsANSI92FullSQL: "+supportsANSI92FullSQL); 

    System.out.println("supportsCoreSQLGrammar: "+supportsCoreSQLGrammar); 
    System.out.println("supportsExtendedSQLGrammar: "+supportsExtendedSQLGrammar); 

    System.out.println("supportsTransactions: "+supportsTransactions); 

    System.out.println("supportsMultipleTransactions: "+supportsMultipleTransactions); 

    System.out.println("supportsOpenCursorsAcrossCommit: "+supportsOpenCursorsAcrossCommit); 
    System.out.println("supportsOpenCursorsAcrossRollback: "+supportsOpenCursorsAcrossRollback); 
    System.out.println("supportsOpenStatementsAcrossCommit: "+supportsOpenStatementsAcrossCommit); 
    System.out.println("supportsOpenStatementsAcrossRollback: "+supportsOpenStatementsAcrossRollback); 

    System.out.println("supportsStoredProcedures: "+supportsStoredProcedures); 

    System.out.println("supportsOuterJoins: "+supportsOuterJoins); 
    System.out.println("supportsFullOuterJoins: "+supportsFullOuterJoins); 
    System.out.println("supportsLimitedOuterJoins: "+supportsLimitedOuterJoins); 
    System.out.println("supportsBatchUpdates: "+supportsBatchUpdates); 
    System.out.println("supportsColumnAliasing: "+supportsColumnAliasing); 
    System.out.println("supportsExpressionsInOrderBy: "+supportsExpressionsInOrderBy); 

    System.out.println("supportsOrderByUnrelated: "+supportsOrderByUnrelated); 
    System.out.println("supportsPositionedDelete: "+supportsPositionedDelete); 

    System.out.println("supportsSelectForUpdate: "+supportsSelectForUpdate); 

    System.out.println("supportsUnion: "+supportsUnion); 
    System.out.println("supportsUnionAll: "+supportsUnionAll); 
    System.out.println("");

    System.out.println("getMaxColumnNameLength: "+getMaxColumnNameLength);
    System.out.println("getMaxColumnsInIndex: "+getMaxColumnsInIndex);
    System.out.println("getMaxColumnsInOrderBy: "+getMaxColumnsInOrderBy);
    System.out.println("getMaxColumnsInSelect: "+getMaxColumnsInSelect);
    System.out.println("getMaxColumnsInTable: "+getMaxColumnsInTable);
    System.out.println("getMaxConnections: "+getMaxConnections);
    System.out.println("getMaxCursorNameLength: "+getMaxCursorNameLength);
    System.out.println("getMaxIndexLength: "+getMaxIndexLength);
    System.out.println("getMaxRowSize: "+getMaxRowSize);
    System.out.println("getMaxStatements: "+getMaxStatements);
    System.out.println("getMaxTableNameLength: "+getMaxTableNameLength);
    System.out.println("getMaxTablesInSelect: "+getMaxTablesInSelect);
    System.out.println("getMaxUserNameLength: "+getMaxUserNameLength);
 
    System.out.println("getSearchStringEscape: "+getSearchStringEscape);
    //System.out.println("getStringFunctions: "+getStringFunctions);
    //System.out.println("getSystemFunctions: "+getSystemFunctions);
    //System.out.println("getTimeDateFunctions: "+getTimeDateFunctions);

    System.out.println("");

    Statement stmt = null;
    String tabletypes = "";
    String typeinfo = "";
    int lastcomma = 0;
    try {
       stmt = conn.createStatement();
       getTableTypes = dbMetaData.getTableTypes();
       while (getTableTypes.next()) {
          tabletypes += getTableTypes.getString(1);
          tabletypes  += ",";
       }
       tabletypes = tabletypes.substring(0,tabletypes.length()-1);
       System.out.println("Table Types: "+tabletypes);

       getTypeInfo = dbMetaData.getTypeInfo();
       while (getTypeInfo.next()) {
          typeinfo += getTypeInfo.getString(1);
          typeinfo  += ",";
       }
       typeinfo = typeinfo.substring(0,typeinfo.length()-1);
       System.out.println("SQL Types: "+typeinfo);
    } 
    catch (SQLException ex) {}
    finally {
       try {
          if (stmt != null) stmt.close();
          if (getTableTypes != null) getTableTypes.close();
          if (getTypeInfo != null) getTypeInfo.close();
       } catch (SQLException e) {}
    } 
  } // end printInfo
}
