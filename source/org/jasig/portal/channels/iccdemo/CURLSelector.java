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

import org.jasig.portal.ChannelRuntimeData;
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

/**
 * A url selector channel (part of the Inter-channel communication demo).
 *
 * @author <a href="mailto:pkharchenko@interactivebusiness.com">Peter Kharchenko</a>
 * @version $Revision$
 */
public class CURLSelector extends BaseChannel {
    private static final Log log = LogFactory.getLog(CURLSelector.class);
    private static final String sslLocation = "urlselector.ssl";

    private static final String viewerFname="/portal/iccdemo/viewer";
    private static final String historyFname="/portal/iccdemo/history";

    private boolean usingRenderingGroups=false;


    public void setStaticData(ChannelStaticData sd) throws PortalException {
        super.setStaticData(sd);

        if(usingRenderingGroups) {
            registerListeners();
        }
    }

    /**
     * A utility method to register viewer channel as 
     * listeners of the current channel
     * Note that we're only registering viewer channel as a listener,
     * since CURLSelector does not talk to CHistory directly (CViewer handles that)
     */
    private void registerListeners() {
        ICCRegistry r=staticData.getICCRegistry();
        String viewerId=getChannelId(viewerFname);
        if(viewerId!=null) {
            // add a listener channel
            r.addListenerChannel(viewerId);
        }
    }

    /**
     * A utility method to remove a veriwer channel as a listener of
     * this channel.
     * This will cause 1cycle delays in relating the url to the viewer channel
     */
    private void deRegisterListeners() {
        ICCRegistry r=staticData.getICCRegistry();
        String viewerId=getChannelId(viewerFname);
        if(viewerId!=null) {
            // remove a listener channel
            r.removeListenerChannel(viewerId);
        }
    }

    private Document getUserXML() {
        // Get a new DOM instance
        Document doc = DocumentFactory.getNewDocument();

        Element urlselectorEl = doc.createElement("urlselector");
        if(usingRenderingGroups) {
            urlselectorEl.setAttribute("grouped","true");
        } else {
            urlselectorEl.setAttribute("grouped","false");
        }

        // check if the two other channels are there, and that their objects are bound
        String viewerId=getChannelId(viewerFname);
        if(viewerId==null) {
            Element warningEl=doc.createElement("warning");
            warningEl.appendChild(doc.createTextNode("Unable to find viewer channel (fname="+viewerFname+"). Please subscribe to a viewer channel"));
            urlselectorEl.appendChild(warningEl);
        } else {
            Object bo=getBoundObject(viewerId);
            if(bo==null) {
                Element warningEl=doc.createElement("warning");
                warningEl.appendChild(doc.createTextNode("Viewer channel found, but no object was found bound in viewer's jndi context. Perhaps viewer should be moved to the same tab."));
                urlselectorEl.appendChild(warningEl);
            }
        }
        

        String historyId=getChannelId(historyFname);
        if(historyId==null) {
            Element warningEl=doc.createElement("warning");
            warningEl.appendChild(doc.createTextNode("Unable to find history channel (fname="+historyFname+"). Please subscribe to a history channel"));
            urlselectorEl.appendChild(warningEl);
        } else {
            Object bo=getBoundObject(historyId);
            if(bo==null) {
                Element warningEl=doc.createElement("warning");
                warningEl.appendChild(doc.createTextNode("History channel found, but no object was found bound in history's jndi context. Perhaps history should be moved to the same tab."));
                urlselectorEl.appendChild(warningEl);
            }
        }

        String[] urls={"http://www.google.com","http://www.cnn.com","http://slashdot.org","http://www.yahoo.com"};
        for(int i=0;i<urls.length;i++) {
            Element urlEl=doc.createElement("url");
            urlEl.appendChild(doc.createTextNode(urls[i]));
            urlselectorEl.appendChild(urlEl);
        }
        
        doc.appendChild(urlselectorEl);
        return doc;
    }
    
    public void setRuntimeData (ChannelRuntimeData rd)  throws PortalException {
        super.setRuntimeData(rd);

        // toggle grouped rendering
        String gr=rd.getParameter("groupedRendering");
        if(gr!=null) {
            // toggle grouped rendering
            if(usingRenderingGroups) {
                usingRenderingGroups=false;
                // remove listeners
                deRegisterListeners();
            } else {
                usingRenderingGroups=true;
                // add listeners
                registerListeners();                
            }
        }

        // listen to URL switches
        String url = rd.getParameter ("url");
        if (url != null) {
            // make the channel stall here, so others will, most likely, have
            // time to render and miss the event in the case when the rendering
            // groups are not used.
            try {
                Thread.sleep(1000);
            } catch (InterruptedException ie) {}

            // now, finally pass the url
            setViewerURL(url);
        }
        
    }

    /**
     * A utility method that communicates the new url to the viewer channel
     *
     * @param url a <code>String</code> value
     */
    private void setViewerURL(String url) {
        // find viewer's id
        String viewerId=getChannelId(viewerFname);
        if(viewerId!=null) {
            ViewerURL v=(ViewerURL) getBoundObject(viewerId);
            if(v!=null) {
                v.setNewURL(url);
            }
        }
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
            log.error( e);
        }
        try {
            id=(String)globalIDContext.lookup(fname);
        } catch (NotContextException nce) {
            log.error( "CURLSelector.getUserXML(): Could not find channel ID for fname="+fname);
        } catch (NamingException e) {
            log.error( e);
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
            log.error( e);
        }

        try {
            o=globalObjContext.lookup(channelSubscribeId);
        } catch (NotContextException nce) {
            log.error( "CURLSelector.getUserXML(): Could not find channel bound object for channel id="+channelSubscribeId);
        } catch (NamingException e) {
            log.error( e);
        }
        return o;        
    }
    

    /**
     * Render method.
     * @param out the content handler
     * @exception PortalException
     */
    public void renderXML (ContentHandler out) throws PortalException {
        // Perform the transformation
        XSLT xslt = XSLT.getTransformer(this, runtimeData.getLocales());
        xslt.setXML(getUserXML());
        xslt.setXSL(sslLocation, runtimeData.getBrowserInfo());
        xslt.setTarget(out);
        xslt.setStylesheetParameter("baseActionURL", runtimeData.getBaseActionURL());
        xslt.transform();
    }
}
