/**
 * Licensed to Jasig under one or more contributor license
 * agreements. See the NOTICE file distributed with this work
 * for additional information regarding copyright ownership.
 * Jasig licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a
 * copy of the License at:
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
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



