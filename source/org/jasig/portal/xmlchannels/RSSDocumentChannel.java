package org.jasig.portal.xmlchannels;

import org.xml.sax.*;
import org.xml.sax.helpers.*;
import java.util.Hashtable;
import java.util.Enumeration;
import javax.servlet.jsp.*;
import javax.servlet.http.*; 
import org.jasig.portal.*;
import org.apache.xalan.xslt.*;

/**
 * A simple example of IChannel that views an RSS Document.
 * @author Peter Kharchenko
 * @version $Revision$
 */


public class RSSDocumentChannel extends GenericPortalBean implements IChannel
{
    String uri;
    StylesheetSet set;
    ChannelRuntimeData runtimeData;

    
    String fs=System.getProperty("file.separator");
    String stylesheetDir=getPortalBaseDir()+"webpages"+fs+"stylesheets"+fs;

    public RSSDocumentChannel() {
	// initialize a stylesheet set from a file
	set=new StylesheetSet(stylesheetDir+"RSSDocumentChannel"+fs+"RSSDocumentChannel.ssl");
	set.setMediaProps(getPortalBaseDir()+"properties"+fs+"media.properties");
    }
    
    public void setStaticData(ChannelStaticData sd) {
	// all we need from the static data is the location of the document
	this.uri= sd.getParameter("uri");
    };
    
    public void setRuntimeData(ChannelRuntimeData rd) {
	// need to save runtimedata so we can determine media from the
	// request object contained in it
	runtimeData=rd;
    }
    
    public void receiveEvent(LayoutEvent ev) {
	// we have no events to process here
    }

    // report static channel properties to the portal
    public ChannelSubscriptionProperties getSubscriptionProperties() {
	ChannelSubscriptionProperties csb=new ChannelSubscriptionProperties();
	// leave most properties at their default values, except a couple.
	csb.setName("RSSDocumentChannel");
	return csb;
    }
    
    // report runtime channel properties to the portal
    public ChannelRuntimeProperties getRuntimeProperties() {
	// channel will always render, so the default values are ok
	return new ChannelRuntimeProperties();
    }
    
    public void renderXML(DocumentHandler out)
    {
	try {
	    if (set!=null) {
		XSLTInputSource stylesheet=set.getStylesheet(runtimeData.getHttpRequest());
		if(stylesheet!=null) {
		    XSLTProcessor processor = XSLTProcessorFactory.getProcessor();
		    processor.process(new XSLTInputSource(uri),stylesheet,new XSLTResultTarget(out));
		}
	    }
	} catch (Exception e) {};
    }
}
