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
	    List al=ssup.getChannelAttributeNames();
	    for(int i=0;i<al.size();i++) {
		String attrName=(String) al.get(i);
		attsImpl.addAttribute(attrName,"CDATA",ssup.getChannelAttributeValue(channelID,attrName));
		//		Logger.log(Logger.DEBUG,"ThemeAttributesIncorporationFilter::startElement() : adding attribute to channel="+channelID+" "+attrName+"="+ssup.getChannelAttributeValue(channelID,attrName));
	    }
	    super.startElement(name,attsImpl);
	} 
	else 
	    super.startElement (name, atts);
    }
}

