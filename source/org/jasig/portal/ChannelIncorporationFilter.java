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
		channelID=atts.getValue("ID");
	    } else super.startElement(name,atts);
	} 
    }
    
    public void endElement(java.lang.String name)
	throws SAXException
    {
	if(insideChannelElement){
	    if(name.equals("channel")) {
		if(super.outDocumentHandler != null) {
		    cm.outputChannel(channelID,this.getDocumentHandler());
		    insideChannelElement=false;
		}
	    }
	} else super.endElement(name);
    }
    
};
