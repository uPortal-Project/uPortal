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

package org.jasig.portal.channels;

import org.xml.sax.*;
import org.xml.sax.helpers.*;
import java.io.*;
import java.util.Hashtable;
import java.util.Enumeration;
import org.jasig.portal.*;
import org.w3c.dom.*;
import org.apache.xalan.xslt.*;


/**
 * This pseudo-channel provides header XML document fragment
 * to be used in compilation of the structuredLayoutXML
 * @author Peter Kharchenko
 * @version $Revision 1.1$
 */
public class CHeader extends BaseChannel
{

    String fs=File.separator;
    StylesheetSet set;
    String stylesheetDir = GenericPortalBean.getPortalBaseDir () + "webpages" + fs + "stylesheets" + fs + "org" + fs + "jasig" + fs + "portal" + fs + "channels" + fs + "CHeader" + fs;

    public CHeader() {
        set = new StylesheetSet (stylesheetDir + "CHeader.ssl");
        set.setMediaProps (GenericPortalBean.getPortalBaseDir () + "properties" + fs + "media.properties");
    }

    public void renderXML (DocumentHandler out)
    {
	    
	String userName= (String) staticData.getPerson().getFullName();

        Document doc = new org.apache.xerces.dom.DocumentImpl();
        Element headerEl=doc.createElement("header");
        Element titleEl=doc.createElement("title");
        titleEl.appendChild(doc.createTextNode("Welcome "+userName+" !"));
        headerEl.appendChild(titleEl);
        doc.appendChild(headerEl);

        try {

            XSLTInputSource xmlSource = new XSLTInputSource (doc);
            XSLTInputSource xslSource = runtimeData.getStylesheet(set);
            if(xslSource==null) {
            Logger.log(Logger.ERROR,"CHeader::renderXML() : unable to locate a stylesheet");
            }
            XSLTResultTarget xmlResult = new XSLTResultTarget(out);

            XSLTProcessor processor = XSLTProcessorFactory.getProcessor (new org.apache.xalan.xpath.xdom.XercesLiaison ());
	    if(userName!=null && userName.equals("Guest")) 
		processor.setStylesheetParam("guest",processor.createXString("true"));
            processor.process (xmlSource, xslSource, xmlResult);
        } catch (Exception e) { Logger.log(Logger.ERROR,e); }
    }
}
