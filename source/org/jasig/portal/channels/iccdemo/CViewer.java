/**
 * Copyright © 2002 The JA-SIG Collaborative.  All rights reserved.
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

package org.jasig.portal.channels.iccdemo;

import javax.naming.Context;
import javax.naming.NamingException;
import javax.naming.NotContextException;

import org.jasig.portal.ChannelStaticData;
import org.jasig.portal.ICCRegistry;
import org.jasig.portal.PortalException;
import org.jasig.portal.channels.BaseChannel;
import org.jasig.portal.services.LogService;
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
            LogService.log(LogService.ERROR, "CHistory.getUserXML(): Could not find subcontext /channel-obj in JNDI");
        } catch (NamingException e) {
            LogService.log(LogService.ERROR, e);
        }

        // bind new ViewerURL object
        try {
            globalObjContext.bind(staticData.getChannelSubscribeId(),new ViewerURL(this));
        } catch (NotContextException nce) {
            LogService.log(LogService.ERROR, "CHistory.getUserXML(): Could not bind channel object for channel id="+staticData.getChannelSubscribeId());
        } catch (NamingException e) {
            LogService.log(LogService.ERROR, e);
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
        
        XSLT xslt = new XSLT(this);
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
            LogService.log(LogService.ERROR, "CURLSelector.getUserXML(): Could not find subcontext /channel-ids in JNDI");
        } catch (NamingException e) {
            LogService.log(LogService.ERROR, e);
        }
        try {
            id=(String)globalIDContext.lookup(fname);
        } catch (NotContextException nce) {
            LogService.log(LogService.ERROR, "CURLSelector.getUserXML(): Could not find channel ID for fname="+fname);
        } catch (NamingException e) {
            LogService.log(LogService.ERROR, e);
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
            LogService.log(LogService.ERROR, "CURLSelector.getUserXML(): Could not find subcontext /channel-obj in JNDI");
        } catch (NamingException e) {
            LogService.log(LogService.ERROR, e);
        }

        try {
            o=globalObjContext.lookup(channelSubscribeId);
        } catch (NotContextException nce) {
            LogService.log(LogService.ERROR, "CURLSelector.getUserXML(): Could not find channel bound object for channel id="+channelSubscribeId);
        } catch (NamingException e) {
            LogService.log(LogService.ERROR, e);
        }
        return o;        
    }
}



