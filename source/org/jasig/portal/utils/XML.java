/**
 * Copyright © 2001 The JA-SIG Collaborative.  All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or withoutu
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

package org.jasig.portal.utils;

import java.io.IOException;
import java.io.StringWriter;

import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.sax.SAXResult;

import org.jasig.portal.serialize.OutputFormat;
import org.jasig.portal.serialize.XMLSerializer;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.ContentHandler;


/**
 * This utility provides useful XML helper methods.
 *
 * @author Ken Weiner, kweiner@interactivebusiness.com
 * @version $Revision$
 */
public class XML {
  /**
   * Gets the text value of an Element. For example, if an element nameElement
   * looks like this: <name>Fred</name>, then getElementText(nameElement) would
   * return "Fred".
   * @param e the Element with a text value
   * @return the the text value of the element
   */
  public static String getElementText(Element e) {
    String val = null;
    for (Node n = e.getFirstChild(); n != null; n = n.getNextSibling()) {
      if (n.getNodeType() == Node.TEXT_NODE) {
        val = n.getNodeValue();
        break;
      }
    }
    if (val == null) {
      val = new String();
    }
    return val;
  }
  
  /**
   * Gets the text value of a child Element.  For example, if an element nameElement
   * looks like this: <name><first>Fred</first><last>Flinstone</last></name>, then
   * getChildElementText(nameElement, "first") would return "Fred".
   * @param e the Element to search under
   * @param childElementName the name of the child Element
   * @return the text value of the child element
   */
  public static String getChildElementText(Element e, String childElementName) {
    String val = null;
    for (Node n = e.getFirstChild(); n != null; n = n.getNextSibling()) {
       if (n.getNodeType() == Node.ELEMENT_NODE && 
           n.getNodeName() != null &&
           n.getNodeName().equals(childElementName)) {
         Element childElement = (Element)n;
         val = getElementText(childElement);
       }
    }
    return val;
  }

  /**
   * Gets the contents of an XML Document or Element as a nicely formatted string.
   * This method is useful for debugging.
   * @param node the Node to print; must be of type Document or Element
   * @return a nicely formatted String suitable for printing
   */
  public static String serializeNode(Node node) {
    OutputFormat format = new OutputFormat();
    format.setOmitXMLDeclaration(true);
    format.setIndenting(true);
    return serializeNode(node,format);
  }

  /**
   * Gets the contents of an XML Document or Element as a formatted string.
   * This method is useful for debugging.
   * @param node the Node to print; must be of type Document or Element
   * @param format controls the formatting of the string
   * @return a nicely formatted String suitable for printing
   */
  public static String serializeNode(Node node, OutputFormat format ) {
    String returnString = null;
    StringWriter outString = new StringWriter();
    XMLSerializer xsl = new XMLSerializer(outString, format);
    try {
      if (node.getNodeType() == Node.DOCUMENT_NODE) {
        xsl.serialize((Document)node);
        returnString = outString.toString();
      } else if (node.getNodeType() == Node.ELEMENT_NODE) {
        xsl.serialize((Element)node);
        returnString = outString.toString();
      } else {
        returnString = "The node you passed to getNodeAsString() must be of type org.w3c.dom.Document or org.w3c.dom.Element in order to be serialized.";
      }
    } catch (IOException ioe) {
      returnString = "Error occurred while trying to serialize node: " + ioe.getMessage();
    }

    return returnString;
  }

  /**
   * This is only being kept around for backward compatibility. Callers
   * should now be using Document.cloneNode(true).
   * @param olddoc the original document
   * @return a clone of the original document with preserved ID tables
   */
  public static Document cloneDocument(Document olddoc) {
    return (Document)olddoc.cloneNode(true);
  }

    /**
     * Outputs a dom document into a sax stream.
     *
     * @param dom a dom <code>Node</code> value
     * @param sax a sax <code>ContentHandler</code> value
     */
    public static void dom2sax(Node dom,ContentHandler sax) throws TransformerConfigurationException, TransformerException {
        TransformerFactory tFactory = TransformerFactory.newInstance();
        Transformer emptytr = tFactory.newTransformer();
        emptytr.transform(new DOMSource(dom), new SAXResult(sax));
    }
}



