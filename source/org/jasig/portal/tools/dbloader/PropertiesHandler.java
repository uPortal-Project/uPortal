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
import org.xml.sax.helpers.DefaultHandler;

/**
 * Class for handling SAX events during parsing of dbloader.xml.
 * All configuration values are stored in a passed in Configuration
 * object.
 *
 * @author Ken Weiner, kweiner@unicon.net
 * @author Mark Boyd <mark.boyd@engineer.com>
 * @version $Revision$
 */
class PropertiesHandler extends DefaultHandler
{
      
    private static StringBuffer charBuff = null;

  Configuration properties = null;
  DbTypeMapping dbTypeMapping;
  Type type;

    PropertiesHandler( Configuration config )
    {
        this.properties = config;
    }
  public void startDocument ()
  {
  }

  public void endDocument ()
  {
    properties.getLog().println("");
  }

  public void startElement (String namespaceURI, String localName, String qName, Attributes atts)
  {
    charBuff = new StringBuffer();

    if (qName.equals("db-type-mapping"))
      dbTypeMapping = new DbTypeMapping();
    else if (qName.equals("type"))
      type = new Type();
  }

  public void endElement (String namespaceURI, String localName, String qName)
  {
    if (qName.equals("drop-tables")) // drop tables ("true" or "false")
      properties.setDropTables(charBuff.toString());
    else if (qName.equals("create-tables")) // create tables ("true" or "false")
      properties.setCreateTables(charBuff.toString());
    else if (qName.equals("populate-tables")) // populate tables ("true" or "false")
      properties.setPopulateTables(charBuff.toString());
    else if (qName.equals("tables-uri")) // tables URI
      properties.setTablesUri(charBuff.toString());
    else if (qName.equals("tables-xsl-uri")) // tables xsl URI
      properties.setTablesXslUri(charBuff.toString());
    else if (qName.equals("data-uri")) // data xml URI
      properties.setDataUri(charBuff.toString());
    else if (qName.equals("create-script")) // create script ("true" or "false")
      properties.setCreateScript(charBuff.toString());
    else if (qName.equals("script-file-name")) // script file name
      properties.setScriptFileName(charBuff.toString());
    else if (qName.equals("statement-terminator")) // statement terminator
      properties.setStatementTerminator(charBuff.toString());
    else if (qName.equals("db-type-mapping"))
      properties.addDbTypeMapping(dbTypeMapping);
    else if (qName.equals("db-name")) // database name
      dbTypeMapping.setDbName(charBuff.toString());
    else if (qName.equals("db-version")) // database version
      dbTypeMapping.setDbVersion(charBuff.toString());
    else if (qName.equals("driver-name")) // driver name
      dbTypeMapping.setDriverName(charBuff.toString());
    else if (qName.equals("driver-version")) // driver version
      dbTypeMapping.setDriverVersion(charBuff.toString());
    else if (qName.equals("type"))
      dbTypeMapping.addType(type);
    else if (qName.equals("generic")) // generic type
      type.setGeneric(charBuff.toString());
    else if (qName.equals("local")) // local type
      type.setLocal(charBuff.toString());
  }

  public void characters (char ch[], int start, int length)
  {
    charBuff.append(ch, start, length);
  }
}
