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

package org.jasig.portal.xmlchannels;

import org.xml.sax.*;
import org.xml.sax.helpers.*;
import java.util.Hashtable;
import java.util.Enumeration;
import javax.servlet.jsp.*;
import javax.servlet.http.*;
import org.jasig.portal.*;
import org.apache.xalan.xslt.*;
import org.w3c.dom.*;
import org.apache.xerces.dom.*;


/**
 * A simple example of IXMLChannel that views an RSS Document.
 * currently this channel will cache the xml document in a DOM
 * which will live as long as the channel instance (ie user's session).
 * There is currently no provision to refresh the contents after an interval.
 * @author Peter Kharchenko
 * @version $Revision$
 */


public class CRSSChannel extends GenericPortalBean implements IXMLChannel
{
    String uri;
    StylesheetSet set;
    ChannelRuntimeData runtimeData;
    ChannelSubscriptionProperties csb;

    // a DOM for rss information
    // Since we get the channel name from the title element, we parse
    // the whole rss document into a DOM to be used for
    // both the name and the rendered contents.
    protected Document RssDom;


    String fs=System.getProperty("file.separator");
    String stylesheetDir=getPortalBaseDir()+"webpages"+fs+"stylesheets"+fs;

    public CRSSChannel() {
	// initialize a stylesheet set from a file
	set=new StylesheetSet(stylesheetDir+"CRSSChannel"+fs+"CRSSChannel.ssl");
	set.setMediaProps(getPortalBaseDir()+"properties"+fs+"media.properties");
    }

	/* Save the ChannelStaticData object and also
    get the title of the rss document for the channel name
    save it in the static data.    */
    public void setStaticData(ChannelStaticData sd) {
	this.uri= sd.getParameter("uri");
	Logger.log(Logger.DEBUG,"CRSSChannel::setStaticData uri=\""+uri+"\"");
  sd.setParameter("Name",uri);
    try {
    int i=0;
    StringBuffer titleString = new StringBuffer("");
	  Document root=getDoc();
    if (root==null) return;
    NodeList elements=root.getElementsByTagName("title");
    if (elements==null) return;
    while (i<elements.getLength()) {
      Node title=elements.item(i);
      if (title==null) {i++; continue;}
      if (title.getParentNode().getNodeName().equals("channel")) {
        int j=0;
        NodeList titleNodes = title.getChildNodes();
        while (j<titleNodes.getLength()) {
          Node child = titleNodes.item(j);
          if (child.getNodeType()== child.TEXT_NODE) {
            titleString=titleString.append(child.getNodeValue());
            }
          j++;
        }
      }
      i++;
      };
      csb.setName(titleString.toString());
    }
    catch (Exception e) {
      Logger.log(Logger.ERROR,"RSSChannel::setStaticData : "+e);
    }
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
	  csb=new ChannelSubscriptionProperties();
	  // leave most properties at their default values, except a couple.
  	csb.setName("CRSSChannel");
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
        // This allows relative paths in the stylesheet set
        String sPortalBaseDir = GenericPortalBean.getPortalBaseDir ();
        stylesheet.setSystemId (sPortalBaseDir + stylesheet.getSystemId ());
		if(stylesheet!=null) {
		    XSLTProcessor processor = XSLTProcessorFactory.getProcessor();
		    processor.process(new XSLTInputSource(RssDom),stylesheet,new XSLTResultTarget(out));
		}
	    }
	} catch (Exception e) {};
    }
    /* parse the rss document into a DOM
    which will be around for getting the name and for eventually rendering. */
    private Document getDoc() {
  	try{
	    if(RssDom==null) {
  		org.apache.xerces.parsers.DOMParser parser=new org.apache.xerces.parsers.DOMParser();
  		parser.parse(new org.xml.sax.InputSource(uri));
  		RssDom=parser.getDocument();
  	    }
  	} catch (Exception e) { Logger.log(Logger.ERROR,"RSSChannel::getDoc() : unable to get input from \""+uri+"\"\n"+e); }
	return RssDom;
    }

}
