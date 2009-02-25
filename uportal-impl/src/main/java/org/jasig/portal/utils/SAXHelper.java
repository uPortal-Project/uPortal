/**
 * Copyright (c) 2000-2009, Jasig, Inc.
 * See license distributed with this file and available online at
 * https://www.ja-sig.org/svn/jasig-parent/tags/rel-10/license-header.txt
 */
package  org.jasig.portal.utils;

import java.io.IOException;
import java.io.StringReader;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.ContentHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;


/**
 * Convenience methods for SAX parsing
 * @author Bernie Durfee, bdurfee@interactivebusiness.com
 * @version $Revision$
 */
public class SAXHelper {

  /**
   * Outputs SAX events to specified ContentHandler based on the content String
   * @param out the ContentHandler
   * @param content an XML string
   * @exception SAXException, IOException, ParserConfigurationException
   */
  public static synchronized void outputContent (ContentHandler out, String content) throws SAXException, IOException, ParserConfigurationException {
    XMLReader xmlReader = SAXParserFactory.newInstance().newSAXParser().getXMLReader();
    xmlReader.setContentHandler(out);
    xmlReader.parse(new InputSource(new StringReader(content)));
  }
}



