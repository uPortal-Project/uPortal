/* Copyright 2002 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/

package org.jasig.portal.channels.iccdemo;

import javax.naming.Context;
import javax.naming.NamingException;
import javax.naming.NotContextException;

import org.jasig.portal.ChannelStaticData;
import org.jasig.portal.ICCRegistry;
import org.jasig.portal.PortalException;
import org.jasig.portal.channels.BaseChannel;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jasig.portal.utils.DocumentFactory;
import org.jasig.portal.utils.XSLT;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.ContentHandler;

/*
 * This is a modified version of the CInlineFrame channel that uses inter-channel communications.
 *
 * @author Peter Kharchenko
 * @author Susan Bramhall
 * @version $Revision$
 */
public class CViewer extends BaseChannel {
    private static final Log log = LogFactory.getLog(CViewer.class);
    private static final String sslLocation = "CInlineFrame/CInlineFrame.ssl";
    private static final String historyFname="/portal/iccdemo/history";

    private String currentURL = "";

    public void setStaticData(ChannelStaticData sd) throws PortalException {
        super.setStaticData(sd);

        // bind viewer url object to the jndi context
        // other channels will access this object to switch the url

        // find chan-obj context
        Context globalObjContext = null;
        try {
            globalObjContext = (Context)staticData.getJNDIContext().lookup("/channel-obj");
        } catch (NotContextException nce) {
            log.error( "CHistory.getUserXML(): Could not find subcontext /channel-obj in JNDI");
        } catch (NamingException e) {
            log.error("Failed lookup of /channel-obj", e);
        }

        // bind new ViewerURL object
        try {
            globalObjContext.bind(staticData.getChannelSubscribeId(),new ViewerURL(this));
        } catch (NotContextException nce) {
            log.error( "CHistory.getUserXML(): Could not bind channel object for channel id="+staticData.getChannelSubscribeId());
        } catch (NamingException e) {
            log.error("Failed bind", e);
        }

        // regsiter history channel
        registerHistory();
    }

    /**
     * A utility method to register history channel as both listener
     * and instructor.
     */
    private void registerHistory() {
        ICCRegistry r=staticData.getICCRegistry();
        String historyId=getChannelId(historyFname);
        if(historyId!=null) {
            // register history as a listener
            r.addListenerChannel(historyId);
            // register hsitory as an event source as well
            r.addInstructorChannel(historyId);
        }
    }

    
    /**
     * Change URL that's being shown
     *
     * @param newURL a <code>String</code> value
     */
    void changeURL(String newURL) {
        this.currentURL=newURL;
        // report to the history channel
        addHistoryURL(newURL);
    }

    /**
     * An internal method to report new URL to the history's channel registry
     *
     * @param url a <code>String</code> value
     */
    private void addHistoryURL(String url) {
        String historyId=getChannelId(historyFname);
        if(historyId!=null) {
            HistoryRecord hr=(HistoryRecord) getBoundObject(historyId);
            if(hr!=null) {
                hr.addHistoryRecord(url);
            }
        }
    }
    
    public void renderXML (ContentHandler out) throws PortalException {
        // get url from the jndi context (that object can be updated by both URL selector and history channels)
        String frameHeight = "600";

        Document doc = DocumentFactory.getNewDocument();

        // Create XML doc
        Element iframeE = doc.createElement("iframe");

        // create warnings if the history channel can not be found
        String historyId=getChannelId(historyFname);
        if(historyId==null) {
            Element warningEl=doc.createElement("warning");
            warningEl.appendChild(doc.createTextNode("Unable to find history channel (fname="+historyFname+"). Please subscribe to a history channel"));
            iframeE.appendChild(warningEl);
        } else {
            Object bo=getBoundObject(historyId);
            if(bo==null) {
                Element warningEl=doc.createElement("warning");
                warningEl.appendChild(doc.createTextNode("History channel found, but no object was found bound in history's jndi context. Perhaps history should be moved to the same tab."));
                iframeE.appendChild(warningEl);
            }
        }

        Element urlE = doc.createElement("url");
        urlE.appendChild(doc.createTextNode(currentURL));
        iframeE.appendChild(urlE);
        Element heightE = doc.createElement("height");
        heightE.appendChild(doc.createTextNode(frameHeight));
        iframeE.appendChild(heightE);
        doc.appendChild(iframeE);
        
        XSLT xslt = XSLT.getTransformer(this, runtimeData.getLocales());
        xslt.setXML(doc);
        xslt.setXSL(sslLocation, getStylesheetTitle(runtimeData.getBrowserInfo().getUserAgent()), runtimeData.getBrowserInfo());
        xslt.setTarget(out);
        xslt.transform();
    }

    /**
     * Uses the user agent string to determine which stylesheet title to use.
     * We wouldn't need this method if stylesheet sets could distinguish between browser versions
     * @param userAgent the user agent string
     * @return ssTitle the stylesheet title
     */
    private String getStylesheetTitle (String userAgent) {
        String ssTitle = "noIFrameSupport";
        if ((userAgent.indexOf("MSIE 3") >= 0) || (userAgent.indexOf("MSIE 4") >= 0) ||
            (userAgent.indexOf("MSIE 5") >= 0) || (userAgent.indexOf("MSIE 6") >= 0) ||
            (userAgent.indexOf("Mozilla/5") >= 0 || (userAgent.indexOf("Opera/6") >= 0))) {
            ssTitle = "IFrameSupport";
        }
        return  ssTitle;
    }


    /**
     * A utility method for obtaining a channelSubscribeId given a channel fname
     *
     * @param fname a <code>String</code> value
     * @return channel's subscribe id, or <code>null</code> if no channel with given fname was found
     */
    private String getChannelId(String fname) {
        String id=null;
        Context globalIDContext = null;
        try {
            // Get the context that holds the global IDs for this user
            globalIDContext = (Context)staticData.getJNDIContext().lookup("/channel-ids");
        } catch (NotContextException nce) {
            log.error( "CURLSelector.getUserXML(): Could not find subcontext /channel-ids in JNDI");
        } catch (NamingException e) {
            log.error("Failed lookup /channel-ids", e);
        }
        try {
            id=(String)globalIDContext.lookup(fname);
        } catch (NotContextException nce) {
            log.error( "CURLSelector.getUserXML(): Could not find channel ID for fname="+fname);
        } catch (NamingException e) {
            log.error("Failed lookup " + fname, e);
        }
        return id;
    }

    /**
     * A utility method to determine an object bound to the "chan-obj" branch
     * for a particular channel id.
     *
     * @param channelSubscribeId a <code>String</code> value of the channel who's object we're looking for
     * @return an <code>Object</code> value bound to that jndi location (or <code>null</code> if channel didn't bind anything)
     */
    private Object getBoundObject(String channelSubscribeId) {
        Object o=null;
        Context globalObjContext = null;
        try {
            globalObjContext = (Context)staticData.getJNDIContext().lookup("/channel-obj");
        } catch (NotContextException nce) {
            log.error( "CURLSelector.getUserXML(): Could not find subcontext /channel-obj in JNDI");
        } catch (NamingException e) {
            log.error("Failed lookup /channel-obj", e);
        }

        try {
            o=globalObjContext.lookup(channelSubscribeId);
        } catch (NotContextException nce) {
            log.error( "CURLSelector.getUserXML(): Could not find channel bound object for channel id="+channelSubscribeId);
        } catch (NamingException e) {
            log.error("Failed lookup " + channelSubscribeId, e);
        }
        return o;        
    }
}



