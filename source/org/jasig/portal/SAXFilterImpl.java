/**
* This is intended as a temporary replacement for the XMLFilterImpl
* defined in SAX2 API. Current filter implementation allows to build
* filters on top of the SAX1 DocumentHandler interface. 
* @author Peter Kharchenko
* @version $Revision$
*/

package org.jasig.portal;

import org.xml.sax.DocumentHandler;
import org.xml.sax.SAXException;



public class SAXFilterImpl implements DocumentHandler
{
  protected DocumentHandler outDocumentHandler;
  
  public SAXFilterImpl() {}
  
  public SAXFilterImpl(DocumentHandler handler)
  {
    this.outDocumentHandler=handler;
  }
  
  public DocumentHandler getDocumentHandler() { return outDocumentHandler; }
  
  public void setDocumentHandler(DocumentHandler handler)
  {
    this.outDocumentHandler=handler;
  }
   
 public void characters (char ch[], int start, int length) 
   throws SAXException
 {
	if (outDocumentHandler != null) {
	    outDocumentHandler.characters(ch, start, length);
	}
 }
 
 public void startDocument ()
	throws SAXException
 {
	if (outDocumentHandler != null) {
	    outDocumentHandler.startDocument();
	}
 }

 
 public void endDocument ()
	throws SAXException
 {
	if (outDocumentHandler != null) {
	    outDocumentHandler.endDocument();
	}
 }
 
 public void startElement(java.lang.String name, org.xml.sax.AttributeList atts)
	throws SAXException
 {
	if (outDocumentHandler != null) {
	    outDocumentHandler.startElement(name,atts);
	}
 }
 
 public void endElement(java.lang.String name)
    throws SAXException
 {
    if (outDocumentHandler != null) {
        outDocumentHandler.endElement(name);
    }
 }
 
 public void ignorableWhitespace(char[] ch, int start, int length)
    throws SAXException
 {
    if (outDocumentHandler != null) {
        outDocumentHandler.ignorableWhitespace(ch,start,length);
    }
 } 

 public void processingInstruction(java.lang.String target, java.lang.String data)
    throws SAXException
 {
    if (outDocumentHandler != null) {
        outDocumentHandler.processingInstruction(target,data);
    }
 }
 
 public void setDocumentLocator(org.xml.sax.Locator locator)
 {
    if (outDocumentHandler != null) {
        outDocumentHandler.setDocumentLocator(locator);
    }
 }
  
};
