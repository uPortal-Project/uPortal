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

/**
 * A simple example of IXMLChannel that views an RSS Document.
 * @author Peter Kharchenko
 * @version $Revision$
 */


public class CRSSChannel extends GenericPortalBean implements IXMLChannel
{
    String uri;
    StylesheetSet set;
    ChannelRuntimeData runtimeData;


    String fs=System.getProperty("file.separator");
    String stylesheetDir=getPortalBaseDir()+"webpages"+fs+"stylesheets"+fs;

    public CRSSChannel() {
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
