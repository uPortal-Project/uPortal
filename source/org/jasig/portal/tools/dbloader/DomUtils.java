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

import java.io.File;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;

/**
 * Utility class centralizing various DOM related functions used during
 * loading of the database.
 *
 * @author Ken Weiner, kweiner@unicon.net
 * @author Mark Boyd <mark.boyd@engineer.com>
 * @version $Revision$
 */
class DomUtils
{
    static void replaceDataTypes (Configuration config, Document tablesDoc)
    {
      Element tables = tablesDoc.getDocumentElement();
      NodeList types = tables.getElementsByTagName("type");

      for (int i = 0; i < types.getLength(); i++)
      {
        Node type = (Node)types.item(i);
        NodeList typeChildren = type.getChildNodes();

        for (int j = 0; j < typeChildren.getLength(); j++)
        {
          Node text = (Node)typeChildren.item(j);
          String genericType = text.getNodeValue();

          // Replace generic type with mapped local type
          text.setNodeValue(DbUtils.getLocalDataTypeName(config, genericType));
        }
      }
    }

    private static String getNodeValue (Node node)
    {
      String nodeVal = null;

      for (Node ch = node.getFirstChild(); ch != null; ch = ch.getNextSibling())
      {
        if (ch instanceof Text)
          nodeVal = ch.getNodeValue();
      }

      return nodeVal;
    }

    private static Element getTableWithName (Document tablesDoc, String tableName)
    {
      Element tableElement = null;
      NodeList tables = tablesDoc.getElementsByTagName("table");

      for (int i = 0; i < tables.getLength(); i++)
      {
        Node table = (Node)tables.item(i);

        for (Node tableChild = table.getFirstChild(); tableChild != null; tableChild = tableChild.getNextSibling())
        {
          if (tableChild instanceof Element && tableChild.getNodeName() != null && tableChild.getNodeName().equals("name"))
          {
            if (tableName.equals(getNodeValue(tableChild)))
            {
              tableElement = (Element)table;
              break;
            }
          }
        }
      }

      return tableElement;
    }
    
    static int getJavaSqlDataTypeOfColumn(
        Configuration config,
        String tableName,
        String columnName)
    {
        Document tablesDocGeneric = config.getGenericTablesDoc();
        int dataType = 0;
        String hashKey = tableName + File.separator + columnName;

    // Try to use cached version first
    if (config.getTableColumnTypes().get(hashKey) != null) {
       dataType = ((Integer)config.getTableColumnTypes().get(hashKey)).intValue();
    } else {
      // Find the right table element
      Element table = getTableWithName(tablesDocGeneric, tableName);

      // Find the columns element within
      Element columns = getFirstChildWithName(table, "columns");

      // Search for the first column who's name is columnName
      for (Node ch = columns.getFirstChild(); ch != null; ch = ch.getNextSibling())
      {
        if (ch instanceof Element && ch.getNodeName().equals("column"))
        {
          Element name = getFirstChildWithName((Element)ch, "name");
          if (getNodeValue(name).equals(columnName))
          {
            // Get the corresponding type and return it's type code
            Element value = getFirstChildWithName((Element)ch, "type");
            dataType = DbUtils.getJavaSqlType(getNodeValue(value));
          }
        }
      }

        // Store value in hashtable for next call to this method.
        // This prevents repeating xml parsing which takes a very long time
        config.getTableColumnTypes().put(hashKey, new Integer(dataType));
    }
    
      return dataType;
    }

    private static Element getFirstChildWithName (Element parent, String name)
    {
      Element child = null;
      for (Node ch = parent.getFirstChild(); ch != null; ch = ch.getNextSibling())
      {
        if (ch instanceof Element && ch.getNodeName().equals(name))
        {
          child = (Element)ch;
          break;
        }
      }

      return child;
    }
}
