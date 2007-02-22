/* Copyright 2004 The JA-SIG Collaborative.  All rights reserved.
 *  See license distributed with this file and
 *  available online at http://www.uportal.org/license.html
 */

package org.jasig.portal.tools.dbloader;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

/**
 * Handles SAX events resulting from parsing of the data.xml file.
 *
 * @author Ken Weiner, kweiner@unicon.net
 * @author Mark Boyd <mark.boyd@engineer.com>
 * @version $LastChangedRevision$
 */
class SqlServerDataHandler extends DataHandler
{
    private Configuration config = null;
    private String identityInsertOffPrefix = "set identity_insert ";
    private String ON = "on";
    private String OFF = "off";
    private boolean insideTable = false;
    private boolean insideTableName = false;
    private boolean insideRows = false;
    private StringBuffer tableNameBuff = new StringBuffer();
    private String currentTable = null;
    
    public SqlServerDataHandler(Configuration config)
    {
        super(config);
        this.config = config;
    }
    
    public void startElement (String namespaceURI, String localName, String qName, Attributes atts)
    {
        if (qName.equals("table"))
        {
            insideTable = true;
            currentTable = null;
        }
        else if (insideTable && !insideRows && "name".equals(qName)) 
        {
            insideTableName = true;
            tableNameBuff.setLength(0);
        }
        else if (insideTable && "rows".equals(qName)) 
        {
            insideRows = true;
        }
        super.startElement(namespaceURI, localName, qName, atts);
    }
    
    public void endElement (String namespaceURI, String localName, String qName) throws SAXException
    {
        super.endElement(namespaceURI, localName, qName);
        if (qName.equals("table")) {
            insideTable = false;
            if (config.getScriptWriter() != null)
            {
                StringBuffer sb = new StringBuffer();
                sb.append(identityInsertOffPrefix);
                sb.append(currentTable).append(' ');
                sb.append(OFF).append(config.getStatementTerminator());
                config.getScriptWriter().println(sb.toString());
                config.getScriptWriter().println("go");
            }
            currentTable = null;
        } else if (insideTableName && "name".equals(qName)) {
            insideTableName = false;
            if (config.getScriptWriter() != null)
            {
                currentTable = tableNameBuff.toString();
                StringBuffer sb = new StringBuffer();
                sb.append(identityInsertOffPrefix);
                sb.append(currentTable).append(' ');
                sb.append(ON).append(config.getStatementTerminator());
                config.getScriptWriter().println(sb.toString());
                config.getScriptWriter().println("go");
            }
        }
        else if (insideTable && "rows".equals(qName)) 
        {
            insideRows = false;
        }
    }
    
    public void characters (char ch[], int start, int length)
    {
        super.characters(ch, start, length);
        if (insideTableName && currentTable == null) {
            tableNameBuff.append(ch, start, length);
        }
    }
    
}
