
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


/**
 * Filter incorporating channel and folder attributes for the structure transformation
 * @author Peter Kharchenko
 * @version $Revision$
 */

public class StructureAttributesIncorporationFilter extends SAXFilterImpl 
{
    protected StructureStylesheetUserPreferences fsup;

    
    public StructureAttributesIncorporationFilter (DocumentHandler handler, StructureStylesheetUserPreferences prefs)
    { 
	super (handler); 
	this.fsup=prefs;
    }
    
    public void startElement (java.lang.String name, org.xml.sax.AttributeList atts) throws SAXException 
    {
	// recognizing "channel"   
	if (name.equals ("channel")) {
	    AttributeListImpl attsImpl=new AttributeListImpl(atts);
	    String channelID = attsImpl.getValue ("ID");
	    for(Enumeration ca=fsup.getChannelAttributeNames(); ca.hasMoreElements(); ) {
		String attrName=(String) ca.nextElement();
		attsImpl.addAttribute(attrName,"CDATA",fsup.getChannelAttributeValue(channelID,attrName));
		Logger.log(Logger.DEBUG,"adding attribute to channel="+channelID+" "+attrName+"="+fsup.getChannelAttributeValue(channelID,attrName));
	    }
	    super.startElement(name,attsImpl);
	} else 	if (name.equals ("folder")) {
	    AttributeListImpl attsImpl=new AttributeListImpl(atts);
	    String folderID = attsImpl.getValue ("ID");
	    for(Enumeration fe=fsup.getFolderAttributeNames(); fe.hasMoreElements();) {
		String attrName=(String) fe.nextElement();
		attsImpl.addAttribute(attrName,"CDATA",fsup.getFolderAttributeValue(folderID,attrName));
		Logger.log(Logger.DEBUG,"adding attribute to folder="+folderID+" "+attrName+"="+fsup.getFolderAttributeValue(folderID,attrName));
	    }
	    super.startElement(name,attsImpl);

	} else 
	    super.startElement (name, atts);
    }
}

