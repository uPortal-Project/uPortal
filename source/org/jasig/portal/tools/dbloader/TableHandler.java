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
