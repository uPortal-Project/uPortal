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
          } else if (columnType[i] == java.sql.Types.TIMESTAMP) {
            java.sql.Timestamp ts = rs.getTimestamp(i+1);
            if (!rs.wasNull()) {
              value = ts.toString();
            }
          } else {
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
      if (con == null) {
        System.err.println("Unable to get a database connection");
        return;
      }
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
    } catch (Exception e) {
      e.printStackTrace();
      System.exit(1);
    } finally {
      try { RDBMServices.releaseConnection(con); } catch (Exception e) {}
    }
  }
}
