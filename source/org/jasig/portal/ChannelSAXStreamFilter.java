package org.jasig.portal;

import org.xml.sax.*;
import org.xml.sax.helpers.*;
import java.io.*;

public class ChannelSAXStreamFilter extends SAXFilterImpl
{
 public ChannelSAXStreamFilter(DocumentHandler handler)
 {
    super(handler);
 }
 
 public void startDocument()
    throws SAXException
 { 
 }
   
 public void endDocument()
    throws SAXException
 {
 }
  
 public void processingInstruction(java.lang.String target, java.lang.String data)
    throws SAXException
 {
 }
}
