package org.jasig.portal;

import org.xml.sax.*;
import org.xml.sax.helpers.*;
import java.io.*;

/**
 * Filters out startDocument and endDocument from the 
 * channel content.
 * This filter is invoked by the XMLChannelManager
 * prior to passing channel content to the ChannelIncorporationFilter.
 * @author Peter Kharchenko
 * @version $Revision$
 */



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
