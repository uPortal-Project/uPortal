/**
 * Copyright © 2002 The JA-SIG Collaborative.  All rights reserved.
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

import java.io.StringReader;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;

import org.jasig.portal.services.SequenceGenerator;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * Handles SAX events resulting from parsing of the data.xml file.
 *
 * @author Ken Weiner, kweiner@interactivebusiness.com
 * @author Mark Boyd <mark.boyd@engineer.com>
 * @version $Revision$
*/
  class DataHandler extends DefaultHandler
  {
    private Configuration config = null;
    private StringBuffer charBuff = null;

    private boolean insideData = false;
    private boolean insideTable = false;
    private boolean insideName = false;
    private boolean insideRow = false;
    private boolean insideColumn = false;
    private boolean insideValue = false;
    private static boolean insideSequence = false;
    private boolean supportsPreparedStatements = false;
    private static String sequenceId;
    
    private static Map sequences = new HashMap();

    Table table;
    Row row;
    Column column;
    String action;  //determines sql function for a table row
    String type;    //determines type of column

    public DataHandler(Configuration config)
    {
        this.config = config;
    }
    public void startDocument ()
    {
      supportsPreparedStatements = supportsPreparedStatements(config);
    }

    public void endDocument ()
    {
      config.getLog().println("");
    }

    public void startElement (String namespaceURI, String localName, String qName, Attributes atts)
    {
      charBuff = new StringBuffer();

      if (qName.equals("data"))
        insideData = true;
      else if (qName.equals("table"))
      {
        insideTable = true;
        table = new Table();
        action = atts.getValue("action");
        if (atts.getValue("sinceMajor") != null && atts.getValue("sinceMinor") != null) {
          table.setSince(atts.getValue("sinceMajor"), atts.getValue("sinceMinor"));
        }
      }
      else if (qName.equals("name"))
        insideName = true;
      else if (qName.equals("row"))
      {
        insideRow = true;
        row = new Row();
        if (atts.getValue("sinceMajor") != null) {
           row.setSinceMajor(Integer.parseInt(atts.getValue("sinceMajor")));
      }
        if (atts.getValue("sinceMinor") != null) {
           row.setSinceMinor(Integer.parseInt(atts.getValue("sinceMinor")));
        }
      }
      else if (qName.equals("column"))
      {
        insideColumn = true;
        column = new Column();
        type = atts.getValue("type");
      }
      else if (qName.equals("value"))
        insideValue = true;
      else if (qName.equals("sequence"))
      {
        sequenceId = atts.getValue("id");
        insideSequence = true;
      }
    }

    public void endElement (String namespaceURI, String localName, String qName) throws SAXException
    {
      if (qName.equals("data"))
        insideData = false;
      else if (qName.equals("table"))
        insideTable = false;
      else if (qName.equals("name"))
      {
        insideName = false;

        if (!insideColumn) // table name, log which table
        {
            table.setName(charBuff.toString());
            config.getLog().println("  " + table.getName());
        }
        else // column name
          column.setName(charBuff.toString());
      }
      else if (qName.equals("row"))
      {
        insideRow = false;

        int sinceMajor = row.getSinceMajor();
        int sinceMinor = row.getSinceMinor();
        
        if (sinceMajor == -1) {
           sinceMajor = table.getSinceMajor();
           sinceMinor = table.getSinceMinor();
        }
        if ((sinceMajor == -1 || config.getUpgradeMajor() == -1)
            || (sinceMajor > config.getUpgradeMajor()
                || sinceMinor > config.getUpgradeMinor()))
        {
            if (action != null)
            {
                if (action.equals("delete"))
                    executeSQL(table, row, "delete");
                else if (action.equals("modify"))
                    executeSQL(table, row, "modify");
                else if (action.equals("add"))
                    executeSQL(table, row, "insert");
            }
            else if (config.getPopulateTables())
                executeSQL(table, row, "insert");
        }
      }
      else if (qName.equals("column"))
      {
        insideColumn = false;
        if (type != null) column.setType(type);
        row.addColumn(column);
      }
      else if (qName.equals("value"))
      {
        insideValue = false;

        if (insideColumn) // column value
          column.setValue(charBuff.toString());
      }
      else if (qName.equals("sequence"))
      {
        insideSequence = false;
        if (insideValue)
        {
          // if it's already been generated, return it, otherwise get the next one
          String name = charBuff.toString();
          if (sequences.get(name) != null) {
            charBuff = (StringBuffer)sequences.get(name);
          } else {
            try {
               charBuff = new StringBuffer(SequenceGenerator.instance().getNext(sequenceId));
            } catch (Exception e) {
               String msg = "Error generating next ID in sequence: "+e.getMessage();
               config.getLog().println(msg);
               e.printStackTrace(config.getLog());
               throw new SAXException(msg);
      }
            sequences.put(name, charBuff);
    }
        }
      }
    }

    public void characters (char ch[], int start, int length)
    {
      charBuff.append(ch, start, length);
    }

    private String prepareInsertStatement (Row row, boolean preparedStatement)
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
            else if (DomUtils.getJavaSqlDataTypeOfColumn(config, table.getName(), column.getName()) == Types.INTEGER)
              // this column is an integer, so don't put quotes (Sybase cares about this)
              sb.append(value);
            else
            {
              sb.append("'");
              sb.append(sqlEscape(value.trim()));
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

    private String prepareDeleteStatement (Row row, boolean preparedStatement)
    {

      StringBuffer sb = new StringBuffer("DELETE FROM ");
      sb.append(table.getName()).append(" WHERE ");

      ArrayList columns = row.getColumns();
      Iterator iterator = columns.iterator();
      Column column;

      while (iterator.hasNext())
      {
        column = (Column) iterator.next();
        if (preparedStatement)
          sb.append(column.getName() + " = ? and ");
        else if (DomUtils.getJavaSqlDataTypeOfColumn(config, table.getName(), column.getName()) == Types.INTEGER)
          sb.append(column.getName() + " = " + sqlEscape(column.getValue().trim()) + " and ");
        else
          sb.append(column.getName() + " = " + "'" + sqlEscape(column.getValue().trim()) + "' and ");
      }

      sb.deleteCharAt(sb.length() - 1);
      sb.deleteCharAt(sb.length() - 1);
      sb.deleteCharAt(sb.length() - 1);
      sb.deleteCharAt(sb.length() - 1);

      if (!preparedStatement)
        sb.deleteCharAt(sb.length() - 1);

                 return sb.toString();

    }

    private String prepareUpdateStatement (Row row)
    {

      StringBuffer sb = new StringBuffer("UPDATE ");
      sb.append(table.getName()).append(" SET ");

      ArrayList columns = row.getColumns();
      Iterator iterator = columns.iterator();

      Hashtable setPairs = new Hashtable();
      Hashtable wherePairs = new Hashtable();
      String type;
      Column column;

      while (iterator.hasNext())
      {
        column = (Column) iterator.next();
        type = column.getType();

                   if (type != null && type.equals("select"))
        {
          if (DomUtils.getJavaSqlDataTypeOfColumn(config, table.getName(), column.getName()) == Types.INTEGER)
            wherePairs.put(column.getName(), column.getValue().trim());
          else
            wherePairs.put(column.getName(), "'" + column.getValue().trim() + "'");
        }
        else
        {
          if (DomUtils.getJavaSqlDataTypeOfColumn(config, table.getName(), column.getName()) == Types.INTEGER)
            setPairs.put(column.getName(), column.getValue().trim());
          else
            setPairs.put(column.getName(), "'" + column.getValue().trim() + "'");
        }
      }

      String nm;
      String val;

      Enumeration sKeys = setPairs.keys();
      while (sKeys.hasMoreElements())
      {
        nm = (String) sKeys.nextElement();
        val = (String) setPairs.get(nm);
        sb.append( nm + " = " + sqlEscape(val) + ", ");
      }
      sb.deleteCharAt(sb.length() - 1);
      sb.deleteCharAt(sb.length() - 1);

      sb.append(" WHERE ");

      Enumeration wKeys = wherePairs.keys();
      while (wKeys.hasMoreElements())
      {
        nm = (String) wKeys.nextElement();
        val = (String) wherePairs.get(nm);
        sb.append( nm + "=" + sqlEscape(val) + " and ");
      }
      sb.deleteCharAt(sb.length() - 1);
      sb.deleteCharAt(sb.length() - 1);
      sb.deleteCharAt(sb.length() - 1);
      sb.deleteCharAt(sb.length() - 1);
      sb.deleteCharAt(sb.length() - 1);

      return sb.toString();

    }

    /**
     * Make a string SQL safe
     * @param sql the string containing sql to escape
     * @return SQL safe string
     */
    public static final String sqlEscape (String sql) {
      if (sql == null) {
        return  "";
      }
      else {
        int primePos = sql.indexOf("'");
        if (primePos == -1) {
          return  sql;
        }
        else {
          StringBuffer sb = new StringBuffer(sql.length() + 4);
          int startPos = 0;
          do {
            sb.append(sql.substring(startPos, primePos + 1));
            sb.append("'");
            startPos = primePos + 1;
            primePos = sql.indexOf("'", startPos);
          } while (primePos != -1);
          sb.append(sql.substring(startPos));
          return  sb.toString();
        }
      }
    }

    private void executeSQL (Table table, Row row, String action)
    {
        if (config.getScriptWriter() != null)
      {
        if (action.equals("delete"))
          config.getScriptWriter().println(prepareDeleteStatement(row, false) + config.getStatementTerminator());
        else if (action.equals("modify"))
        config.getScriptWriter().println(prepareUpdateStatement(row) + config.getStatementTerminator());
        else if (action.equals("insert"))
        config.getScriptWriter().println(prepareInsertStatement(row, false) + config.getStatementTerminator());
      }

      if (supportsPreparedStatements)
      {
        String preparedStatement = "";
        PreparedStatement pstmt = null;

        try
        {
          if (action.equals("delete"))
            preparedStatement = prepareDeleteStatement(row, true);
          else if (action.equals("modify"))
            preparedStatement = prepareUpdateStatement(row);
          else if (action.equals("insert"))
          preparedStatement = prepareInsertStatement(row, true);
          //config.getLog().println(preparedStatement);
          pstmt = config.getConnection().prepareStatement(preparedStatement);
          pstmt.clearParameters ();

          // Loop through parameters and set them, checking for any that excede 4k
          ArrayList columns = row.getColumns();
          Iterator iterator = columns.iterator();

          for (int i = 1; iterator.hasNext(); i++)
          {
            Column column = (Column)iterator.next();
            String value = column.getValue();

            // Get a java sql data type for column name
            int javaSqlDataType = DomUtils.getJavaSqlDataTypeOfColumn(config, table.getName(), column.getName());
            if (value==null || (value!=null && value.equalsIgnoreCase("NULL")))
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
          config.getLog().println();
          config.getLog().println(preparedStatement);
          sqle.printStackTrace(config.getLog());
        }
        catch (Exception e)
        {
          config.getLog().println();
          e.printStackTrace(config.getLog());
        }
        finally
        {
          try { if (pstmt != null) pstmt.close();  } catch (Exception e) {}
        }
      }
      else
      {
        // If prepared statements aren't supported, try a normal sql statement
        String statement = "";
        if (action.equals("delete"))
          statement = prepareDeleteStatement(row, false);
        else if (action.equals("modify"))
          statement = prepareUpdateStatement(row);
        else if (action.equals("insert"))
          statement = prepareInsertStatement(row, false);
        Statement stmt = null;

        try
        {
          stmt = config.getConnection().createStatement();
          stmt.executeUpdate(statement);
        }
        catch (Exception e)
        {
          config.getLog().println();
          config.getLog().println(statement);
          e.printStackTrace(config.getLog());
        }
        finally
        {
          try { if (stmt != null ) stmt.close(); } catch (Exception e) {}
        }
      }
    }

    private static boolean supportsPreparedStatements(Configuration config)
    {
      boolean supportsPreparedStatements = true;
      PreparedStatement pstmt = null;

      try
      {
        // Issue a prepared statement to see if database/driver accepts them.
        // The assumption is that if a SQLException is thrown, it doesn't support them.
        // I don't know of any other way to check if the database/driver accepts
        // prepared statements.  If you do, please change this method!
        Statement stmt;
        stmt = config.getConnection().createStatement();
        try {
          stmt.executeUpdate("CREATE TABLE PREP_TEST (A VARCHAR(1))");
        } catch (Exception e){/* Assume it already exists */
        } finally {
          try {stmt.close();} catch (Exception e) { }
        }

        pstmt = config.getConnection().prepareStatement("SELECT A FROM PREP_TEST WHERE A=?");
        pstmt.clearParameters ();
        pstmt.setString(1, "D");
        ResultSet rs = pstmt.executeQuery();
        rs.close();
     }
      catch (SQLException sqle)
      {
        supportsPreparedStatements = false;
        sqle.printStackTrace(config.getLog());
     }
      finally
      {
          Statement stmt = null;
        try {
          stmt = config.getConnection().createStatement();
          stmt.executeUpdate("DROP TABLE PREP_TEST");
        } catch (Exception e){/* Assume it already exists */
        } finally {
          try { if (stmt != null) stmt.close();} catch (Exception e) { }
        }

        try { pstmt.close(); } catch (Exception e) { }
      }
      return supportsPreparedStatements;
    }

    class Table
    {
      private String name;
      private int sinceMajor = -1;
      private int sinceMinor = -1;

      public String getName() { return name; }
      public void setName(String name) { this.name = name; }
      public void setSince(String major, String minor) {
         this.sinceMajor = Integer.parseInt(major);
         this.sinceMinor = Integer.parseInt(minor);
      }
      public int getSinceMajor() { return sinceMajor; }
      public int getSinceMinor() { return sinceMinor; }
    }

    class Row
    {
      ArrayList columns = new ArrayList();
      private int sinceMajor = -1;
      private int sinceMinor = -1;

      public ArrayList getColumns() { return columns; }
      public void addColumn(Column column) { columns.add(column); }
      public void setSinceMajor(int sinceMajor) { this.sinceMajor = sinceMajor; }
      public void setSinceMinor(int sinceMinor) { this.sinceMinor = sinceMinor; }
      public int getSinceMajor() { return sinceMajor; }
      public int getSinceMinor() { return sinceMinor; }
    }

    class Column
    {
      private String name;
      private String value;
      private String type;

      public String getName() { return name; }
      public String getValue() { return value; }
      public String getType() { return type; }
      public void setName(String name) { this.name = name; }
      public void setValue(String value) { this.value = value; }
      public void setType(String type) { this.type = type; }
    }
  }
