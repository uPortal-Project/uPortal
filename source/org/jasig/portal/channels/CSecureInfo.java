/* Copyright 2001 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/

package org.jasig.portal.channels;

import java.io.PrintWriter;
import java.io.StringWriter;

import org.jasig.portal.ChannelCacheKey;
import org.jasig.portal.ICacheable;
import org.jasig.portal.IChannel;
import org.jasig.portal.IPrivilegedChannel;
import org.jasig.portal.PortalControlStructures;
import org.jasig.portal.i18n.LocaleManager;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jasig.portal.utils.DocumentFactory;
import org.jasig.portal.utils.XSLT;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.ContentHandler;

/**
 * CSecureInfo is designed to replace channel instances that are required
 * to be rendered securely, yet the request does not warrant it.
 * <p>
 * CSecureInfo provides information in place of the actual channel content
 * as it relates to rendering channels that are tagged as secure.
 * <p>
 * The channel is modeled after CError and borrows code from it liberally.
 * 
 * @author Keith Stacks, kstacks@sct.com
 * @version $Revision$
 */
public class CSecureInfo extends BaseChannel implements IPrivilegedChannel, ICacheable
{
    private static final Log log = LogFactory.getLog(CSecureInfo.class);
    protected String str_channelSubscribeId=null;
    protected IChannel the_channel=null;
    
    private static final String ssTitle = "info";
    private static final String sslLocation = "CSecureInfo/CSecureInfo.ssl";
    
    private PortalControlStructures portcs;

    public CSecureInfo() {
    }

    public CSecureInfo(String channelSubscribeId,IChannel channelInstance) {
        this();
        this.str_channelSubscribeId=channelSubscribeId;
        this.the_channel=channelInstance;
    }

    public void setPortalControlStructures(PortalControlStructures pcs) {
        this.portcs=pcs;
    }


    public void renderXML(ContentHandler out) {
        // XML of the following type is generated:
        // <secure>
        //  <channel>
        //   <id>$channelID</id>
        //   <name>$channelName</name>
        //  </channel>
        // </secure>
        //
        Document doc = DocumentFactory.getNewDocument();
        Element secureEl=doc.createElement("secure");
        if(str_channelSubscribeId!=null) {
            Element channelEl=doc.createElement("channel");
            Element idEl=doc.createElement("id");
            idEl.appendChild(doc.createTextNode(str_channelSubscribeId));
            channelEl.appendChild(idEl);

            // determine channel name
            if(portcs!=null) {
                String chName=null;
                try {
                    chName=portcs.getUserPreferencesManager().getUserLayoutManager()
                        .getNode(str_channelSubscribeId).getName();
                } catch (Exception e) {
                    chName="undetermined name";
                }
                if(chName!=null) {
                    Element nameEl=doc.createElement("name");
                    nameEl.appendChild(doc.createTextNode(chName));
                    channelEl.appendChild(nameEl);
                }
                secureEl.appendChild(channelEl);
            }
        }

        doc.appendChild(secureEl);

        // debug block
        try {
            java.io.StringWriter outString = new java.io.StringWriter ();
            org.apache.xml.serialize.OutputFormat format=new org.apache.xml.serialize.OutputFormat();
            format.setOmitXMLDeclaration(true);
            format.setIndenting(true);
            org.apache.xml.serialize.XMLSerializer xsl = new org.apache.xml.serialize.XMLSerializer (outString,format);
            xsl.serialize (doc);
            log.debug(outString.toString());
        } catch (Exception e) {
            log.debug(e, e);
        }
        // end of debug block

        try {
            XSLT xslt = XSLT.getTransformer(this, runtimeData.getLocales());
            xslt.setXML(doc);
            xslt.setXSL(sslLocation, ssTitle, runtimeData.getBrowserInfo());
            xslt.setTarget(out);
            xslt.setStylesheetParameter("baseActionURL", runtimeData.getBaseActionURL());
            xslt.transform();
        } catch (Exception e) {
            StringWriter sw=new StringWriter();
            e.printStackTrace(new PrintWriter(sw));
            sw.flush();
            log.error( "CSecureInfo::renderXML() : Things are bad. Secure info channel threw: " + sw.toString());
        }        
    }


    public ChannelCacheKey generateKey() {
        ChannelCacheKey k=new ChannelCacheKey();
        StringBuffer sbKey = new StringBuffer(1024);

        // assume that security information can be cached system-wide
        k.setKeyScope(ChannelCacheKey.SYSTEM_KEY_SCOPE);
        
        sbKey.append("org.jasig.portal.channels.CSecureInfo: channelID=");
        sbKey.append(str_channelSubscribeId);
        sbKey.append("locales:").append(LocaleManager.stringValueOf(runtimeData.getLocales()));

        k.setKey(sbKey.toString());
        return k;
    }

    public boolean isCacheValid(Object validity) {
        return true;
    }

    private String toString(boolean b) {
        if(b) return("true"); else return ("false");
    }
}
