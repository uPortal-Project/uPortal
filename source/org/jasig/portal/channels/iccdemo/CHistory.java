/* Copyright 2002 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/

package org.jasig.portal.channels.iccdemo;

import java.util.Iterator;

import javax.naming.Context;
import javax.naming.NamingException;
import javax.naming.NotContextException;

import org.jasig.portal.ChannelRuntimeData;
import org.jasig.portal.ChannelStaticData;
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
 * A channel showing a list of history URLs, as a part of the inter-channel communication demo.
 *
 * @author <a href="mailto:pkharchenko@interactivebusiness.com">Peter Kharchenko</a>
 * @version $Revision$
 */
public class CHistory extends BaseChannel {
    
    private static final Log log = LogFactory.getLog(CHistory.class);

    private static final String sslLocation = "urlhistory.ssl";

    private static final String viewerFname="/portal/iccdemo/viewer";
    private static final String urlselectorFname="/portal/iccdemo/urlselector";

    // variable determines if URLs to CView will be passed directly through internal
    // jndi objects, or through portal URL attributes (using uP_channelTarget) to 
    // the CURLSelect channel first.
    private boolean passExternally=false;

    HistoryRecord records=new HistoryRecord();

    public void setStaticData(ChannelStaticData sd) throws PortalException {
        super.setStaticData(sd);
        // bind history record to the jndi context
        Context globalObjContext = null;
        try {
            globalObjContext = (Context)staticData.getJNDIContext().lookup("/channel-obj");
        } catch (NotContextException nce) {
            log.error("CHistory.getUserXML(): Could not find subcontext /channel-obj in JNDI");
        } catch (NamingException e) {
            log.error("Naming exception when looking up /channel-obj in JNDI", e);
        }

        try {
            globalObjContext.bind(staticData.getChannelSubscribeId(),records);
        } catch (NotContextException nce) {
            log.error("CHistory.getUserXML(): Could not bind channel object for channel id="+staticData.getChannelSubscribeId());
        } catch (NamingException e) {
            log.error("Exception binding in CHistory.", e);
        }
    }


    
    private Document getUserXML() {
        // Get a new DOM instance
        Document doc = DocumentFactory.getNewDocument();

        Element urlselectorEl = doc.createElement("urlselector");

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

        for(Iterator i=records.constIterator();i.hasNext();) {
            Element urlEl=doc.createElement("url");
            urlEl.appendChild(doc.createTextNode((String)i.next()));
            urlselectorEl.appendChild(urlEl);
        }
        
        doc.appendChild(urlselectorEl);
        return doc;
    }
    
    public void setRuntimeData (ChannelRuntimeData rd)  throws PortalException {
        super.setRuntimeData(rd);

        // check if the URL number was passed
        String urlN = runtimeData.getParameter ("urlN");
        if (urlN != null) {
            int recordNumber=Integer.parseInt(urlN);
            setViewerURL((String)records.get(recordNumber-1));
        }

        // check if the passExternally was toggled
        String pe=runtimeData.getParameter("passExternally");
        if(pe!=null) {
            // switch to a different passing method
            passExternally=!passExternally;
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
            log.error("CURLSelector.getUserXML(): Could not find subcontext /channel-ids in JNDI");
        } catch (NamingException e) {
            log.error("Could not lookip /channel-ids.", e);
        }
        try {
            id=(String)globalIDContext.lookup(fname);
        } catch (NotContextException nce) {
            log.error("CURLSelector.getUserXML(): Could not find channel ID for fname="+fname);
        } catch (NamingException e) {
            log.error("Could not lookup channel " + fname, e);
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
            log.error("CURLSelector.getUserXML(): Could not find subcontext /channel-obj in JNDI");
        } catch (NamingException e) {
            log.error("Could not lookup /channel-obj", e);
        }

        try {
            o=globalObjContext.lookup(channelSubscribeId);
        } catch (NotContextException nce) {
            log.error("CURLSelector.getUserXML(): Could not find channel bound object for channel id="+channelSubscribeId);
        } catch (NamingException e) {
            log.error("Could not lookup " + channelSubscribeId, e);
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
        if(passExternally) {
            xslt.setStylesheetParameter("passExternally", "true");
            xslt.setStylesheetParameter("CURLSelectId", getChannelId(urlselectorFname));
        } else {
            xslt.setStylesheetParameter("passExternally", "false");
        }
        xslt.transform();
    }
}
