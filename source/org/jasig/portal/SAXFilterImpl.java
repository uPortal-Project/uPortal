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

import org.xml.sax.DocumentHandler;
import org.xml.sax.SAXException;

/**
* This is intended as a temporary replacement for the XMLFilterImpl
* defined in SAX2 API. Current filter implementation allows to build
* filters on top of the SAX1 DocumentHandler interface. 
* @author Peter Kharchenko
* @version $Revision$
*/
public class SAXFilterImpl implements DocumentHandler
{
  protected DocumentHandler outDocumentHandler;

  public SAXFilterImpl() {}

  public SAXFilterImpl (DocumentHandler handler)
  {
    this.outDocumentHandler=handler;
  }

  public DocumentHandler getDocumentHandler() { return outDocumentHandler; }

  public void setDocumentHandler(DocumentHandler handler)
  {
    this.outDocumentHandler = handler;
  }

  public void characters (char ch[], int start, int length) throws SAXException
  {
    if (outDocumentHandler != null) 
    {
      outDocumentHandler.characters (ch, start, length);
    }
  }
 
  public void startDocument () throws SAXException
  {
  	if (outDocumentHandler != null) 
    {
  	  outDocumentHandler.startDocument();
	  }
  }

  public void endDocument () throws SAXException
  {
	  if (outDocumentHandler != null) 
    {
	    outDocumentHandler.endDocument ();
    }
  }
 
  public void startElement (String name, org.xml.sax.AttributeList atts) throws SAXException
  {
    if (outDocumentHandler != null) 
    {
      outDocumentHandler.startElement(name,atts);
	  }
  }
 
  public void endElement(String name) throws SAXException
  {
    if (outDocumentHandler != null) 
    {
      outDocumentHandler.endElement(name);
    }
  }
 
  public void ignorableWhitespace(char[] ch, int start, int length) throws SAXException
  {
    if (outDocumentHandler != null) 
    {
      outDocumentHandler.ignorableWhitespace(ch, start, length);
    }
  } 

  public void processingInstruction (String target, String data) throws SAXException
  {
    if (outDocumentHandler != null) 
    {
      outDocumentHandler.processingInstruction (target,data);
    }
  }
 
  public void setDocumentLocator (org.xml.sax.Locator locator)
  {
    if (outDocumentHandler != null) 
    {
      outDocumentHandler.setDocumentLocator (locator);
    }
  }
}
