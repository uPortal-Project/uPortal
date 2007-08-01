/* Copyright 2004 The JA-SIG Collaborative.  All rights reserved.
 *  See license distributed with this file and
 *  available online at http://www.uportal.org/license.html
 */
package org.jasig.portal.serialize;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jasig.portal.utils.SAX2FilterImpl;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

public class ProxyFilter extends SAX2FilterImpl implements CachingSerializer {
    
    private Log log = LogFactory.getLog(getClass());
    private ProxyResourceMap proxyResourceMap;
    
    private CachingSerializer handler = null;
    
    public ProxyFilter(ContentHandler ch, ProxyResourceMap proxyResourceMap) {
        super(ch);
        if (ch instanceof CachingSerializer) {
            this.handler = (CachingSerializer)ch;
        }
        this.proxyResourceMap = proxyResourceMap;
    }

    public void startElement(String uri, String localName,
        String qName, Attributes atts) throws SAXException {
        
        if (atts == null) {
            if (log.isDebugEnabled()) {
                log.debug("No attributes to proxy tag: " + localName);
            }
            super.startElement(uri, localName, qName, atts);
            return;
        }
        
        AttributesImpl filteredAttributes = new AttributesImpl();
        
        for (int i=0; i<atts.getLength(); i++) {
            String name = atts.getQName(i);
            if (HTMLdtd.isURI(localName, name)) {
                String value = atts.getValue(i);
                value = ProxyWriter.considerProxyRewrite(name,localName,value, proxyResourceMap);
                if (log.isDebugEnabled()) {
                    log.debug("Proxied attribute tag/name/uri: " + localName + "/" + name + "/" + value);
                }
                filteredAttributes.addAttribute(atts.getURI(i), atts.getLocalName(i), atts.getQName(i), atts.getType(i), value);
            } else {
                if (log.isDebugEnabled()) {
                    log.debug("Unproxied attribute tag/name/uri: " + localName + "/" + name + "/" + atts.getValue(i));
                }
                filteredAttributes.addAttribute(atts.getURI(i), atts.getLocalName(i), atts.getQName(i), atts.getType(i), atts.getValue(i));
            }
        }
        
        
        super.startElement(uri, localName, qName, filteredAttributes);
    }

    public void flush() throws IOException {
        if (handler != null) {
            handler.flush();
        }
    }

    public String getCache() throws UnsupportedEncodingException, IOException {
        String cache = null;
        if (handler != null) {
            cache = handler.getCache();
        }
        return cache;
    }

    public void printRawCharacters(String text) throws IOException {
        if (handler != null) {
            handler.printRawCharacters(text);
        }
    }

    public void setDocumentStarted(boolean setting) {
        if (handler != null) {
            handler.setDocumentStarted(setting);
        }
    }

    public boolean startCaching() throws IOException {
        if (handler != null) {
            return handler.startCaching();
        }
        return false;
    }

    public boolean stopCaching() throws IOException {
        if (handler != null) {
            return handler.stopCaching();
        }
        return false;
    }

}
