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
import java.util.Enumeration;
import java.util.Hashtable;

import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.sax.SAXResult;

import org.apache.xerces.dom.AttrImpl;
import org.apache.xerces.dom.DocumentImpl;
import org.apache.xerces.dom.DocumentTypeImpl;
import org.apache.xerces.dom.EntityImpl;
import org.apache.xerces.dom.NodeImpl;
import org.apache.xerces.dom.NotationImpl;
import org.apache.xerces.dom.ProcessingInstructionImpl;
import org.apache.xml.serialize.OutputFormat;
import org.apache.xml.serialize.XMLSerializer;
import org.w3c.dom.Document;
import org.w3c.dom.DocumentType;
import org.w3c.dom.Element;
import org.w3c.dom.Entity;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.Notation;
import org.xml.sax.ContentHandler;


/**
 * <p>This utility provides useful XML helper methods</p>
 * @author Ken Weiner, kweiner@interactivebusiness.com
 * @version $Revision$
 */
public class XML {
  /**
   * Gets the text value of an Element. For example, if an element nameElement
   * looks like this: <name>Fred</name>, then getElementText(nameElement) would
   * return "Fred".
   * @param element the Element with a text value
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
    return val;
  }

  /**
   * Gets the contents of an XML Document or Element as a nicely formatted string.
   * This method is useful for debugging.
   * @param node the Node to print; must be of type Document or Element
   * @return a nicely formatted String suitable for printing
   */
  public static String serializeNode(Node node) {
    String returnString = null;
    StringWriter outString = new StringWriter();
    OutputFormat format = new OutputFormat();
    format.setOmitXMLDeclaration(true);
    format.setIndenting(true);
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
   * This allows you to create a deep copy of a DocumentImpl that preserves
   * ID tables, so that getElementById() would work on the returned clone
   * @param olddoc the original document
   * @return a clone of the original document with preserved ID tables
   */
  public static DocumentImpl cloneDocument(DocumentImpl olddoc) {
    DocumentImpl newdoc = new DocumentImpl();

    // Construct the idTable which is a reverse lookup table of
    // the identifier table in NodeImpl
    Hashtable idTable = new Hashtable();
    for (Enumeration e = olddoc.getIdentifiers(); e.hasMoreElements();) {
      String id = (String)e.nextElement();
      Element element = (Element)olddoc.getIdentifier(id);
      // Store the identifier - this is a hack - please fix!!!
      // Originally:
      //   idTable.put(element, id);
      // The line above didn't work because the hash codes of
      // the elements in the identifiers table didn't match the
      // hash codes of the elements in the actual document.
      // Instead, I am using the serialized Element as a key
      // to store the id.
      // -Ken      
      idTable.put(serializeNode(element), id);
    }

    for (NodeImpl n = (NodeImpl)olddoc.getFirstChild(); n != null; n = (NodeImpl)n.getNextSibling()) {
      newdoc.appendChild(importNodeWithId(newdoc, idTable, n));
    }

    return newdoc;
  }

  /**
   * This is similar to the importNode() method of DocumentImpl, except that the current method
   * preserves the ID table of the document.
   * @param doc the DocumentImpl
   * @param idTable the Hashtable of IDs
   * @param source the Node to import
   * @return the NodeImpl
   */
  protected static NodeImpl importNodeWithId(DocumentImpl doc,Hashtable idTable, Node source) {
    NodeImpl newnode = null;
    int type = source.getNodeType();

    switch (type) {

      case DocumentImpl.ELEMENT_NODE: {
        Element newelement = doc.createElement(source.getNodeName());
        
        // Copy the identifier - this is a hack - please fix!!!
        // Originally:
        //   String id=(String)idTable.get((Element) source);
        // The line above didn't work because the hash codes of
        // the elements in the identifiers table didn't match the
        // hash codes of the elements in the actual document.
        // Instead, I am using the serialized Element to lookup
        // the id.
        // -Ken
        String id=(String)idTable.get(serializeNode(source));
        
        if (id != null)
          doc.putIdentifier(id, newelement);

        NamedNodeMap srcattr = source.getAttributes();
        if (srcattr != null) {
          for(int i = 0; i < srcattr.getLength(); i++) {
            newelement.setAttributeNode((AttrImpl)importNodeWithId(doc, idTable,srcattr.item(i)));
          }
        }
        newnode = (NodeImpl)newelement;
        break;
      }

      case DocumentImpl.ATTRIBUTE_NODE: {
        newnode = (NodeImpl)doc.createAttribute(source.getNodeName());
        // Kids carry value
        break;
      }

      case DocumentImpl.TEXT_NODE: {
        newnode = (NodeImpl)doc.createTextNode(source.getNodeValue());
        break;
      }

      case DocumentImpl.CDATA_SECTION_NODE: {
        newnode = (NodeImpl)doc.createCDATASection(source.getNodeValue());
        break;
      }

      case DocumentImpl.ENTITY_REFERENCE_NODE: {
        newnode = (NodeImpl) doc.createEntityReference(source.getNodeName());
        break;
      }

      case DocumentImpl.ENTITY_NODE: {
        Entity srcentity = (Entity)source;
        EntityImpl newentity = (EntityImpl)doc.createEntity(source.getNodeName());
        newentity.setPublicId(srcentity.getPublicId());
        newentity.setSystemId(srcentity.getSystemId());
        newentity.setNotationName(srcentity.getNotationName());
        // Kids carry additional value
        newnode = newentity;
        break;
      }

      case DocumentImpl.PROCESSING_INSTRUCTION_NODE: {
        newnode = (ProcessingInstructionImpl)doc.createProcessingInstruction(source.getNodeName(), source.getNodeValue());
        break;
      }

      case DocumentImpl.COMMENT_NODE: {
        newnode = (NodeImpl)doc.createComment(source.getNodeValue());
        break;
      }

      case DocumentImpl.DOCUMENT_TYPE_NODE: {
        DocumentTypeImpl doctype = (DocumentTypeImpl)source;
        DocumentTypeImpl newdoctype =(DocumentTypeImpl)doc.createDocumentType(doctype.getNodeName(),doctype.getPublicId(),doctype.getSystemId());
        // Values are on NamedNodeMaps
        NamedNodeMap smap = ((DocumentType)source).getEntities();
        NamedNodeMap tmap = newdoctype.getEntities();
        if (smap != null) {
          for(int i = 0; i < smap.getLength(); i++) {
            tmap.setNamedItem((EntityImpl)importNodeWithId(doc, idTable, smap.item(i)));
          }
        }
        smap = ((DocumentType)source).getNotations();
        tmap = newdoctype.getNotations();
        if (smap != null) {
          for (int i = 0; i < smap.getLength(); i++) {
            tmap.setNamedItem((NotationImpl)importNodeWithId(doc, idTable, smap.item(i)));
          }
        }
        // NOTE: At this time, the DOM definition of DocumentType
        // doesn't cover Elements and their Attributes. domimpl's
        // extentions in that area will not be preserved, even if
        // copying from domimpl to domimpl. We could special-case
        // that here. Arguably we should. Consider. ?????
        newnode = newdoctype;
        break;
      }

      case DocumentImpl.DOCUMENT_FRAGMENT_NODE: {
        newnode = (NodeImpl)doc.createDocumentFragment();
        // No name, kids carry value
        break;
      }

      case DocumentImpl.NOTATION_NODE: {
        Notation srcnotation = (Notation)source;
        NotationImpl newnotation = (NotationImpl)doc.createNotation(source.getNodeName());
        newnotation.setPublicId(srcnotation.getPublicId());
        newnotation.setSystemId(srcnotation.getSystemId());
        // Kids carry additional value
        newnode = newnotation;
        // No name, no value
        break;
      }

      case DocumentImpl.DOCUMENT_NODE : // Document can't be child of Document

      default: {
        // Unknown node type
        //throw new DOMExceptionImpl(DOMException.HIERARCHY_REQUEST_ERR, "DOM006 Hierarchy request error");
      }
    }

    // If deep, replicate and attach the kids.
    for (Node srckid = source.getFirstChild(); srckid != null; srckid = srckid.getNextSibling()) {
      newnode.appendChild(importNodeWithId(doc, idTable, srckid));
    }

    return newnode;
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



