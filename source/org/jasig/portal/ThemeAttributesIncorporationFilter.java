/**
 * Copyright © 2001 The JA-SIG Collaborative.  All rights reserved.
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

 /**
 * Filter incorporating channel attributes for the second
 * (theme) XSLT transformation.
 * @author Peter Kharchenko
 * @version $Revision$
 */

package org.jasig.portal;

import org.xml.sax.*;
import org.xml.sax.helpers.*;
import java.io.*;
import java.util.Hashtable;
import java.util.Enumeration;
import java.util.Map;
import java.util.List;

import org.xml.sax.helpers.AttributeListImpl;

import javax.servlet.*;
import javax.servlet.jsp.*;
import javax.servlet.http.*;

public class ThemeAttributesIncorporationFilter extends SAXFilterImpl
{
    protected ThemeStylesheetUserPreferences ssup;


    public ThemeAttributesIncorporationFilter (DocumentHandler handler, ThemeStylesheetUserPreferences prefs)
    {
        super (handler);
        this.ssup=prefs;
    }

    public void startElement (java.lang.String name, org.xml.sax.AttributeList atts) throws SAXException
    {
        // recognizing "channel"
        if (name.equals ("channel")) {
            AttributeListImpl attsImpl=new AttributeListImpl(atts);
            String channelID = attsImpl.getValue ("ID");
            for(Enumeration ca=ssup.getChannelAttributeNames(); ca.hasMoreElements(); ) {
                String attrName=(String) ca.nextElement();
                attsImpl.addAttribute(attrName,"CDATA",ssup.getChannelAttributeValue(channelID,attrName));
                //		Logger.log(Logger.DEBUG,"ThemeAttributesIncorporationFilter::startElement() : adding attribute to channel="+channelID+" "+attrName+"="+ssup.getChannelAttributeValue(channelID,attrName));
            }
            super.startElement(name,attsImpl);
        }
        else
            super.startElement (name, atts);
    }
}

