package org.jasig.portal.xmlchannels;

import org.xml.sax.*;
import org.xml.sax.helpers.*;
import org.apache.xalan.xslt.*;
import java.io.*;
import java.util.Hashtable;
import java.util.Enumeration;
import javax.servlet.jsp.*;
import javax.servlet.http.*; 
import org.jasig.portal.*;


/**
 * A sample XMLChannel that views an XML document 
 * that has a default stylesheet binding in it.
 * @author Peter Kharchenko
 * @version $Revision$
 */


// This is a quick&dirty example of how to render
// XML files with default (W3C recommended) XSL stylesheet
// attachment. Note that in the current version document is
// parsed twice. This demonstrates the fundamental wrongdoing
// in including style information in the document content itself.
// The easy way around that is to use a DOM structure instead
// of SAX streams. 


public class XMLDocumentChannel implements IChannel
{
    String uri;
    ChannelRuntimeData runtimeData;

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
	csb.setName("XMLDocumentChannel");
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
	    Parser documentParser = ParserFactory.makeParser("org.apache.xerces.parsers.SAXParser");
	    StylesheetSet set=new StylesheetSet(uri);
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
