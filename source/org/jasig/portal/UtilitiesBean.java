/**
 * Copyright (c) 2000 The JA-SIG Collaborative.  All rights reserved.
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

package org.jasig.portal;

import javax.servlet.http.*;
import java.text.*;
import org.apache.xerces.dom.*;
import org.w3c.dom.*;
import java.util.*;
import java.io.*;
import java.net.*;

/**
 * Provides methods useful for the portal.  Later on, it may be necessary
 * to create an org.jasig.portal.util package
 * and several utilities classes.
 * @author Ken Weiner, kweiner@interactivebusiness.com
 * @version $Revision$
 */
public class UtilitiesBean extends GenericPortalBean
{
  /**
   * Prevents an html page from being cached by the browser
   * @param the servlet response object
   */
  public static void preventPageCaching (HttpServletResponse res)
  {
    try
    {
      res.setHeader("pragma", "no-cache");
      res.setHeader( "Cache-Control","no-cache" );
      res.setHeader( "Cache-Control","no-store" );
      res.setDateHeader( "Expires", 0 );
    }
    catch (Exception e)
    {
      Logger.log (Logger.ERROR, e);
    }
  }

  /**
   * Gets the current date/time
   * @return a formatted date and time string
   */
  public static String getDate ()
  {
    try
    {
      // Format the current time.
      SimpleDateFormat formatter = new SimpleDateFormat ("EEEE, MMM d, yyyy 'at' hh:mm a");
      java.util.Date currentTime = new java.util.Date();
      return formatter.format(currentTime);
    }
    catch (Exception e)
    {
      Logger.log (Logger.ERROR, e);
    }

    return "&nbsp;";
  }

  /**
   * Allows the hrefs in each .ssl file to be entered in one
   * of 3 ways:
   * 1) http://...
   * 2) An absolute file system path optionally beginning with file://
   *    e.g. C:\WinNT\whatever.xsl or /usr/local/whatever.xsl
   *    or file://C:\WinNT\whatever.xsl or file:///usr/local/whatever.xsl
   * 3) A path relative to the portal base dir as determined from
   *    GenericPortalBean.getPortalBaseDir()
   */
  public static String fixURI (String str)
  {
    boolean bWindows = (System.getProperty ("os.name").indexOf ("Windows") != -1) ? true : false;
    char ch0 = str.charAt (0);
    char ch1 = str.charAt (1);

    if (bWindows && str.startsWith ("file://"))
    {
      // Replace "file://" with "file:/" on Windows machines
      str = "file:/" + str.substring (7);
    }
    else if (ch0 == java.io.File.separatorChar || ch1 == ':')
    {
      // It's a full path without "file://"
      str = (bWindows ? "file:/" : "file://") + str;
    }
    else if (str.indexOf ("://") == -1 && str.indexOf (":/") == -1 && ch1 != ':')
    {
      // Relative path was specified, so prepend portal base dir
      str = (bWindows ? "file:/" : "file://") + GenericPortalBean.getPortalBaseDir () + str;
    }

    // Handle platform-dependent strings
    str = str.replace (java.io.File.separatorChar, '/');

    return str;
  }


    /*
     * This allows to create a deep copy of a DocumentImpl that preserves
     * ID tables, so that getElementById() would work on the returned clone
     */
    public static DocumentImpl cloneDocument(DocumentImpl olddoc) {
        DocumentImpl newdoc = new DocumentImpl();

        // construct the idTable which is a reverse lookup table of
        // identifier table in NodeImpl
        Hashtable idTable=new Hashtable();
        for(Enumeration e=olddoc.getIdentifiers(); e.hasMoreElements();) {
            String id=(String) e.nextElement();
            idTable.put(olddoc.getIdentifier(id),id);
        }

        for(NodeImpl n = (NodeImpl) olddoc.getFirstChild(); n != null; n = (NodeImpl) n.getNextSibling()) {
            newdoc.appendChild(importNodeWithId(newdoc,idTable,n));
        }

        return newdoc;

    }

    /*
     * This is similar to the improtNode() method of DocumentImpl, except that the current method
     * preserves the ID table of the document.
     */
    protected static NodeImpl importNodeWithId(DocumentImpl doc,Hashtable idTable, Node source) {
        NodeImpl newnode=null;

        int type = source.getNodeType();
        switch (type) {

        case DocumentImpl.ELEMENT_NODE: {
            Element newelement = doc.createElement(source.getNodeName());
            // copy the identifier
            String id=(String)idTable.get((Element) source);
            if(id!=null)
                doc.putIdentifier(id,newelement);

            NamedNodeMap srcattr = source.getAttributes();
            if (srcattr != null) {
                for(int i = 0; i < srcattr.getLength(); i++) {
                    newelement.setAttributeNode((AttrImpl)importNodeWithId(doc,idTable,srcattr.item(i)));
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
            newnode = (ProcessingInstructionImpl) doc.createProcessingInstruction(source.getNodeName(), source.getNodeValue());
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
            if(smap != null) {
                for(int i = 0; i < smap.getLength(); i++) {
                    tmap.setNamedItem((EntityImpl)importNodeWithId(doc,idTable,smap.item(i)));
                }
            }
            smap = ((DocumentType)source).getNotations();
            tmap = newdoctype.getNotations();
            if (smap != null) {
                for(int i = 0; i < smap.getLength(); i++) {
                    tmap.setNamedItem((NotationImpl)importNodeWithId(doc,idTable,smap.item(i)));
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
        default: {			 // Unknown node type
//            throw new DOMExceptionImpl(DOMException.HIERARCHY_REQUEST_ERR,
//                                       "DOM006 Hierarchy request error");
        }
        }

        // If deep, replicate and attach the kids.
        for (Node srckid = source.getFirstChild();
             srckid != null;
             srckid = srckid.getNextSibling()) {
            newnode.appendChild(importNodeWithId(doc,idTable,srckid));
        }

        return newnode;
    }


    public static Element getChildElement(Element el,String elementName) {
        Element theElement=null;
        NodeList nl=el.getChildNodes();
        int nnodes=nl.getLength();
        for(int i=0;i<nnodes;i++) {
            Node currentNode=nl.item(i);
            if((currentNode.getNodeType() == Node.ELEMENT_NODE) && (currentNode.getNodeName().equals(elementName))) {
                theElement=(Element) currentNode;
                break;
            }
        }
        return theElement;
    }

  /**
   * Get the contents of a URI as a String
   * @param uri the URI
   * @return the data pointed to by a URI
   */
  public static String getContentsAsString (String uri) throws IOException, MalformedURLException
  {
    String line = null;
    URL url = new URL (fixURI(uri));
    BufferedReader in = new BufferedReader (new InputStreamReader (url.openStream()));
    StringBuffer sbText = new StringBuffer (1024);

    while ((line = in.readLine()) != null)
      sbText.append (line).append ("\n");

    return sbText.toString ();
  }
}
