/** 
 * This pseudo-channel provides header XML document fragment
 * to be used in compilation of the structuredLayoutXML 
 * @author Peter Kharchenko
 * @version $Revision 1.1$
 */

package org.jasig.portal.xmlchannels;

import org.xml.sax.*;
import org.xml.sax.helpers.*;
import java.io.*;
import java.util.Hashtable;
import java.util.Enumeration;
import javax.servlet.jsp.*;
import javax.servlet.http.*; 
import org.jasig.portal.*;



public class HeaderChannel implements IXMLChannel
{
    
    private ChannelRuntimeData runtimeData;


    // most of these are empty
    public void setStaticData(ChannelStaticData sd) {
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
	csb.setName("HeaderChannel");
	return csb;
    }
    
    // report runtime channel properties to the portal
    public ChannelRuntimeProperties getRuntimeProperties() {
	// channel will always render, so the default values are ok
	return new ChannelRuntimeProperties();
    }


    
    public void renderXML(DocumentHandler out)
    {
	HttpSession session = (runtimeData.getHttpRequest()).getSession (false);
	String userName= (String) session.getAttribute ("userName");
	String headerXMLString= new String("<header>\n<title>"+userName+", welcome !</title>");
	headerXMLString+="<description>XML version \n   </description> \n    <image> \n    <link>http://localhost:8080/xmlportal/</link> \n    <url>images/MyIBS.gif</url> \n    <description>IBS logo</description> \n    <width>100</width> \n    <height>50</height> \n   </image> \n  </header>"; 
	
	try {
	    Parser documentParser = ParserFactory.makeParser("org.apache.xerces.parsers.SAXParser");
	    documentParser.setDocumentHandler(out);
	    documentParser.parse(new org.xml.sax.InputSource(new StringReader(headerXMLString)));
	} catch (Exception e) {};
	
    }
    
}
