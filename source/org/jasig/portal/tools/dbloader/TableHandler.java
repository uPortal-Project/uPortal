/* Copyright 2001 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/

package org.jasig.portal.tools.dbloader;

import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;

/**
 * Class for handling SAX events during parsing of tables.xml.
 *
 * @author Ken Weiner, kweiner@unicon.net
 * @author Mark Boyd <mark.boyd@engineer.com>
 * @version $Revision$
 */
public class TableHandler implements ContentHandler
{
    private Configuration config = null;
  private static final int UNSET = -1;
  private static final int DROP = 0;
  private static final int CREATE = 1;
  private int mode = UNSET;
  private StringBuffer stmtBuffer;
  private String tableName = null;

  public TableHandler(Configuration config)
  {
      this.config = config;
  }
  public void startDocument ()
  {
      config.getLog().println();
  }

  public void endDocument ()
  {
    config.getLog().println();
  }

  public void startElement (String namespaceURI, String localName, String qName, Attributes atts)
  {
    if (qName.equals("statement"))
    {
      stmtBuffer = new StringBuffer(1024);
      String statementType = atts.getValue("type");
      tableName = atts.getValue("name");

    if (mode == UNSET
        || mode == CREATE
        && statementType != null
        && statementType.equals("drop"))
      {
        mode = DROP;

        if (config.getDropTables())
          config.getLog().println("Dropping tables...");
        else
          config.getLog().println("Dropping tables...disabled.");
      }
    else if (
        mode == UNSET
            || mode == DROP
            && statementType != null
            && statementType.equals("create"))
      {
        mode = CREATE;
        config.getLog().println();
        

        if (config.getCreateTables())
          config.getLog().println("Creating tables...");
        else
          config.getLog().println("Creating tables...disabled.");
      }
    }
  }

  public void endElement (String namespaceURI, String localName, String qName)
  {
    if (qName.equals("statement"))
    {
      String statement = stmtBuffer.toString();

      switch (mode)
      {
        case DROP:
          if (config.getDropTables())
          {
              config.getLog().println("  " + tableName);
              DbUtils.dropTable(config, statement);
          }
          break;
        case CREATE:
          if (config.getCreateTables())
          {
              config.getLog().println("  " + tableName);
              DbUtils.createTable(config, statement);
          }
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
