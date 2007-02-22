/* Copyright 2004 The JA-SIG Collaborative.  All rights reserved.
 *  See license distributed with this file and
 *  available online at http://www.uportal.org/license.html
 */

package org.jasig.portal.tools.dbloader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
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
 * @version $LastChangedRevision$
 */

public class PostgresDbUnload {
    
    private static int tableChunkSize = 100000;
    
    static void dumpTable(PrintWriter xmlOut, Connection conn, String tableName,
        int minOid, int maxOid)
    throws Exception {
        int start = minOid;
        int end = Math.min(start + tableChunkSize - 1, maxOid);
        while (start < maxOid) {
            __dumpTable(xmlOut, conn, tableName, start, end);
            start=end+1;
            end=Math.min(start+ tableChunkSize - 1, maxOid);
        }
    }
    
    static void __dumpTable(PrintWriter xmlOut, Connection conn, String tableName,
        int startOid, int endOid)
    throws Exception {
        System.out.println("fetching " + tableName + " range [" + startOid + ", " + endOid + "]");
        StringBuffer sql = new StringBuffer();
        sql.append("SELECT * FROM ").append(tableName).
        	append(" where oid >= ? and oid <= ?");
        ResultSet rs = null;
        PreparedStatement ps = null;
        
        xmlOut.println("  <table>");
        xmlOut.println("    <name>" + tableName.toUpperCase() + "</name>");
        xmlOut.println("    <rows>");
        try {
            ps = conn.prepareStatement(sql.toString());
            int j=1;
            ps.setInt(j++, startOid);
            ps.setInt(j++, endOid);
            rs = ps.executeQuery();
            
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
                    } else if (columnType[i] == java.sql.Types.DATE) {
                        java.sql.Date dt = rs.getDate(i+1);
                        if (!rs.wasNull()) {
                            value = dt.toString();
                        }
                    } else if (columnType[i] == java.sql.Types.BIT &&
                        RDBMServices.getJdbcDriver().indexOf("postgres") >= 0) {
                        
                        // postgres returns boolean column types as BIT
                        value = Boolean.toString(rs.getBoolean(i+1));
                    } else if (columnType[i] == java.sql.Types.BOOLEAN) {
                        value = Boolean.toString(rs.getBoolean(i+1));
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
        } catch (Error e) {
            e.printStackTrace();
            throw new Exception(e);
        } finally {
            RDBMServices.closeResultSet(rs);
            rs = null;
            RDBMServices.closePreparedStatement(ps);
            ps = null;
        }
        xmlOut.println("    </rows>");
        xmlOut.println("  </table>");
        xmlOut.println();
    }
    
    public static int getMinOID(Connection conn, String tableName) throws Exception {
        int rowCount = 0;
        
        PreparedStatement ps = null;
        ResultSet rs = null;
        StringBuffer sql = new StringBuffer();
        
        try {
            sql.append("select min(oid) from ").append(tableName);
            ps = conn.prepareStatement(sql.toString());
            rs = ps.executeQuery();
            if (!rs.next()) {
                throw new Exception("Failed to get row count from " + tableName);
            }
            rowCount = rs.getInt(1);
        } finally {
            RDBMServices.closeResultSet(rs);
            rs = null;
            RDBMServices.closeStatement(ps);
            ps = null;
        }
        
        return rowCount;
    }
    public static int getMaxOID(Connection conn, String tableName) throws Exception {
        int rowCount = 0;
        
        PreparedStatement ps = null;
        ResultSet rs = null;
        StringBuffer sql = new StringBuffer();
        
        try {
            sql.append("select max(oid) from ").append(tableName);
            ps = conn.prepareStatement(sql.toString());
            rs = ps.executeQuery();
            if (!rs.next()) {
                throw new Exception("Failed to get row count from " + tableName);
            }
            rowCount = rs.getInt(1);
        } finally {
            RDBMServices.closeResultSet(rs);
            rs = null;
            RDBMServices.closeStatement(ps);
            ps = null;
        }
        
        return rowCount;
    }
    
    public static void main(String[] args) {
        if (args.length < 2) {
            System.err.println("Usage \"dbunload <table>... <out xmlfile>\"");
            return;
        }
        String chunkSizeStr = System.getProperty("dbunload.table.chunksize");
        if (chunkSizeStr != null) {
            try {
                tableChunkSize = Integer.parseInt(chunkSizeStr);
            } catch (NumberFormatException e) {
                System.out.println("Invalid table chunk size, using default: "
                    + tableChunkSize);
            }
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
            xmlOut.println("<?xml version=\"1.0\"?>");
            xmlOut.println();
            xmlOut.println("<data>");
                for (int i = 0; i < args.length - 1; i++) {
                    int minOid = getMinOID(con, args[i].toUpperCase());
                    int maxOid = getMaxOID(con, args[i].toUpperCase());
                    dumpTable(xmlOut, con, args[i].toUpperCase(), minOid, maxOid);
                }
            xmlOut.println("</data>");
            xmlOut.close();
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        } catch (Throwable t) {
            t.printStackTrace();
            System.exit(1);
        } finally {
            try { RDBMServices.releaseConnection(con);
            } catch (Throwable t) {
                t.printStackTrace();
            }
        }
    }
}
