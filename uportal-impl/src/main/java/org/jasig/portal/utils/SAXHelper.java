/* Copyright 2001 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
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



