/* Copyright 2005 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/

package org.jasig.portal.channels;

import java.util.Date;
import java.util.Map;
import javax.xml.parsers.DocumentBuilderFactory;

import org.jasig.portal.ChannelRuntimeProperties;
import org.jasig.portal.ChannelStaticData;
import org.jasig.portal.PortalEvent;
import org.jasig.portal.channels.support.TitledChannelRuntimeProperties;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * An example channel that includes the current time in its dynamically generated title.
 * @since uPortal 2.4.4, 2.5.1
 * @version $Revision$ $Date$
 */
public final class CTimeTitle 
	extends CAbstractXslt {

    /**
     * "xslUri" is the name of the ChannelStaticData attribute that we will read and,
     * if set to a non-null value, we will use its value as our XSLT URI.  If this
     * ChannelStaticData attribute is not set, we will fall back on our default.
     */
    public static final String XSL_PARAM_KEY = "xslUri";
    
    /**
     * By default, we use the XSLT 'TimeTitle.xsl' which will be found
     * in the stylesheets subdirectory corresponding to the package of
     * this CTimeTitle channel.
     */
    public static final String DEFAULT_XSL_URI = "CTimeTitle/CTimeTitle.xsl";
	
	protected final Document getXml() throws Exception {
        /*
         * Here we build a Document conveying the current time.
         */
        
        Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
        
        Element missingPropsElem = doc.createElement("time");
        String currentDateTime = new Date().toString();
        missingPropsElem.setTextContent(currentDateTime);
        
        doc.appendChild(missingPropsElem);
        
        return doc;
	}

	protected final String getXsltUri() throws Exception {
        try {
            ChannelStaticData staticData = getStaticData();
            String xsltUri = staticData.getParameter(XSL_PARAM_KEY);
            
            if (xsltUri != null) {
                return xsltUri;
            }
            
            // if xsltUri was null we will fall back on returning our default.
            
        } catch (RuntimeException rte) {
            log.error("Error checking ChannelStaticData attribute [" 
                    + XSL_PARAM_KEY 
                    + "] for alternate XSLT; falling back on default value: [" 
                    + DEFAULT_XSL_URI + "]", rte);
        }

        
        // return our default value
        return DEFAULT_XSL_URI;
	}

	protected final Map getStylesheetParams() throws Exception {
		// no parameters
		return null;
	}

	public final void receiveEvent(PortalEvent ev) {
		// do nothing - handles no events
	}
	
	public final ChannelRuntimeProperties getRuntimeProperties() {
		// this channel returns ChannelRuntimeProperties that specify the
		// dynamic channel title to be the current time.
		
	    log.trace("CTimeTitle.getRuntimeProperties()");
	    
		String currentTime = new Date().toString();
		
		return new TitledChannelRuntimeProperties(currentTime);
		
	}

}
