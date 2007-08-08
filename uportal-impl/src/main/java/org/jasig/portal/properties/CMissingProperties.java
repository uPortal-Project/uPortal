/* Copyright 2005 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/

package org.jasig.portal.properties;

import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import javax.xml.parsers.DocumentBuilderFactory;

import org.jasig.portal.ChannelStaticData;
import org.jasig.portal.PortalEvent;
import org.jasig.portal.channels.CAbstractXslt;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * An IChannel for viewing the missing properties.
 * @version $Revision$ $Date$
 * @since uPortal 2.5
 */
public class CMissingProperties 
    extends CAbstractXslt {

    /**
     * "xslUri" is the name of the ChannelStaticData attribute that we will read and,
     * if set to a non-null value, we will use its value as our XSLT URI.  If this
     * ChannelStaticData attribute is not set, we will fall back on our default.
     */
    public static final String XSL_PARAM_KEY = "xslUri";
    
    /**
     * By default, we use the XSLT 'MissingProperties.xsl' which will be found
     * in the stylesheets subdirectory corresponding to the package of
     * this CMissingProperties channel.
     */
    public static final String DEFAULT_XSL_URI = "MissingProperties.xsl";
    
    protected Document getXml() throws Exception {
        /*
         * Here we build a Document conveying the missing properties.
         */
        
        Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
        
        Element missingPropsElem = doc.createElement("missingProperties");
        
        Set missingProperties = PropertiesManager.getMissingProperties();

        for (Iterator iter = missingProperties.iterator(); iter.hasNext(); ){
            
            String missingProperty = (String) iter.next();
            Element propertyElement = doc.createElement("property");
            propertyElement.setTextContent(missingProperty);
            
            missingPropsElem.appendChild(propertyElement);
        }
        
        doc.appendChild(missingPropsElem);
        
        return doc;
    }

    /**
     * This implementation reads and returns the ChannelStaticData attribute 'xsltUri'; 
     * if that attribute is not set we return the default value 'MissingProperties.xsl'.
     * @return the ChannelStaticData attribute 'xsltUri' or 'MissingProperties.xsl' if the attribute was null.
     */
    protected String getXsltUri() {
        
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

    /**
     * This implementation returns null because we have no stylesheet parameters.
     * @return null
     */
    protected Map getStylesheetParams() {
        return null;
    }

    public void receiveEvent(PortalEvent ev) {
        // we ignore all events
    }

}

