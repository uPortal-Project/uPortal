/* Copyright 2001 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/

package org.jasig.portal.tools.dbloader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;

import org.jasig.portal.RDBMServices;
import org.jasig.portal.utils.XMLEscaper;
import org.springframework.dao.DataAccessException;
/**
 * Title:        DbUnload
 * Description:  Dump database table(s) into a xml format
 * @author George Lindholm
 * @version $Revision$
 */

public class DbUnload {
  static void dumpTable(PrintWriter xmlOut, Statement stmt, String tableName) throws Exception {
    String sql = "SELECT * FROM " + tableName;
    ResultSet rs;
    try {
       rs = stmt.executeQuery(sql);
    } catch (SQLException e) {
      System.err.println("Problem accessing table " + tableName + ": " + e);
      return;
    }

    xmlOut.println("  <table>");
    xmlOut.println("    <name>" + tableName.toUpperCase() + "</name>");
    xmlOut.println("    <rows>");
    try {
      ResultSetMetaData rsmd = rs.getMetaData();
      int columnCount = rsmd.getColumnCount();
      int[] columnType = new int[columnCount];
      String[] columnName = new String[columnCount];
      for (int i = 0; i < columnCount; i++) {
        columnType[i] = rsmd.getColumnType(i+1);
        columnName[i] = rsmd.getColumnName(i+1);
      }
      while(rs.next()) {
        xmlOut.println("      <row>");
        for (int i = 0; i < rsmd.getColumnCount(); i++) {
          String value = "";
          if (columnType[i] == java.sql.Types.VARCHAR ||
            columnType[i] == java.sql.Types.LONGVARCHAR||
            columnType[i] == java.sql.Types.CHAR) {
            value = rs.getString(i+1);
            value = XMLEscaper.escape(value);
            if (value != null && value.startsWith("<?xml ")) {
              value = "<![CDATA[\n" + value + "\n]]>";
            }
          } else if (columnType[i] == java.sql.Types.NUMERIC ||
            columnType[i] == java.sql.Types.INTEGER) {
            value = rs.getInt(i+1) + "";
          } else if (columnType[i] == java.sql.Types.BIGINT) {
            value = rs.getLong(i+1) + "";
          } else if (columnType[i] == java.sql.Types.TIMESTAMP) {
            java.sql.Timestamp ts = rs.getTimestamp(i+1);
            if (!rs.wasNull()) {
              value = ts.toString();
            }
          } else if (columnType[i] == java.sql.Types.DATE)
          {
			java.sql.Date dt= rs.getDate(i+1);
			if (!rs.wasNull()) {
				java.sql.Timestamp ts = new java.sql.Timestamp(dt.getTime());
			  	value = ts.toString();
			}
          }
          else{
            throw new Exception("Unrecognized column type " + columnType[i] + " for column " + (i + 1) +
            " in table " + tableName);
          }
          if (rs.wasNull()) 
            xmlOut.println("        <column><name>" + columnName[i].toUpperCase() + "</name></column>");
          else
            xmlOut.println("        <column><name>" + columnName[i].toUpperCase() + "</name><value>" + value + "</value></column>");
        }
        xmlOut.println("      </row>");
      }
    } finally {
      rs.close();
    }
    xmlOut.println("    </rows>");
    xmlOut.println("  </table>");
    xmlOut.println();
  }

  public static void main(String[] args) {
    if (args.length < 2) {
      System.err.println("Usage \"dbunload <table>... <out xmlfile>\"");
      return;
    }
    Connection con = null;
    try {
      PrintWriter xmlOut;

      if (!args[args.length-1].equals("-")) {
        File xmlFile = new File(args[args.length-1]);
        xmlFile.createNewFile();

        xmlOut = new PrintWriter(new BufferedWriter(new FileWriter(args[args.length-1], true)));
      } else { // stdout
        xmlOut = new PrintWriter(System.out);
      }

      con = RDBMServices.getConnection ();
      

      Statement stmt = con.createStatement();
      xmlOut.println("<?xml version=\"1.0\"?>");
      xmlOut.println();
      xmlOut.println("<data>");
      try {
        for (int i = 0; i < args.length - 1; i++) {
          dumpTable(xmlOut, stmt, args[i].toUpperCase());
        }
      } finally {
        stmt.close();
      }
      xmlOut.println("</data>");
      xmlOut.close();
    } catch (DataAccessException dae) {
       // we know this was thrown by RDBMServices.getConnection()
        if (con == null) {
          System.err.println("Unable to get a database connection");
          return;
        }
    } catch (Exception e) {
      e.printStackTrace();
      System.exit(1);
    } finally {
      try { RDBMServices.releaseConnection(con); } catch (Exception e) {}
    }
  }
}
