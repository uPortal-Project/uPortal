package org.jasig.portal;

import org.xml.sax.*;
import org.xml.sax.helpers.*;
import java.io.*;
import java.util.Hashtable;
import java.util.Enumeration;
import java.util.Map;

import javax.servlet.*;
import javax.servlet.jsp.*;
import javax.servlet.http.*;

import org.jasig.portal.xmlchannels.*;

public class ChannelIncorporationFilter extends SAXFilterImpl
{
    // keep track if we are "in" the <channel> element
    private boolean insideChannelElement=false;
    ChannelManager cm;

    // information about the current channel 
    private Hashtable params;
    private String channelClassName;
    private String channelID;
    

    
    
    // public functions
    
    public ChannelIncorporationFilter(DocumentHandler handler,ChannelManager chanm)
    { 
	super(handler); 
	this.cm=chanm;
    }
  
    public void startElement(java.lang.String name, org.xml.sax.AttributeList atts)
	throws SAXException
    {
	if(!insideChannelElement) {
	    // recognizing "channel"   
	    if(name.equals("channel")) {
		insideChannelElement=true;
		// get class attribute
		channelClassName=atts.getValue("class");
		channelID=atts.getValue("ID");
		params=new Hashtable();
	    } else super.startElement(name,atts);
	} else if(name.equals("parameter")) { params.put(atts.getValue("name"),atts.getValue("value")); }
    }
    
    public void endElement(java.lang.String name)
	throws SAXException
    {
	if(insideChannelElement){
	    if(name.equals("channel")) {
		if(super.outDocumentHandler != null) {
		    cm.processChannel(channelID,channelClassName,params,this.getDocumentHandler());
		    insideChannelElement=false;
		}
	    }
	} else super.endElement(name);
    }
    
    public void characters (char ch[], int start, int length) 
	throws SAXException
    {
	if (!insideChannelElement) super.characters(ch,start,length);
    } 
    
    
    public void ignorableWhitespace(char[] ch, int start, int length)
	throws SAXException
    {
	if (!insideChannelElement) super.ignorableWhitespace(ch,start,length);
    } 
    
    public void processingInstruction(java.lang.String target, java.lang.String data)
	throws SAXException
    {
	if (!insideChannelElement) super.processingInstruction(target,data);
    }
    
};
