/**
 * Copyright © 2001 The JA-SIG Collaborative.  All rights reserved.
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
 *
 * formatted with JxBeauty (c) johann.langhofer@nextra.at
 */


package  org.jasig.portal.channels;

import  org.jasig.portal.*;
import  org.jasig.portal.utils.XSLT;
import  org.jasig.portal.services.LogService;
import  org.apache.xerces.dom.*;
import  org.w3c.dom.Node;
import  org.w3c.dom.Element;
import  org.w3c.dom.NodeList;
import  org.w3c.dom.Document;
import  org.xml.sax.ContentHandler;
import  java.io.File;
import  java.net.URL;
import  java.util.*;


/**
 * Provides methods associated with subscribing to a channel.
 * This includes preview, listing all available channels
 * and placement on a users page.
 * @author John Laker
 * @version $Revision$
 */
public class CSubscriber
    implements IPrivilegedChannel {
  IUserLayoutManager ulm;
  ChannelStaticData staticData = null;
  ChannelRuntimeData runtimeData = null;
  StylesheetSet set = null;
  IChannelRegistryStore chanReg = null;
  private static final String fs = File.separator;
  private static final String portalBaseDir = GenericPortalBean.getPortalBaseDir();
  String stylesheetDir = portalBaseDir + fs + "webpages" + fs + "stylesheets" + fs + "org" + fs + "jasig" + fs + "portal"
      + fs + "channels" + fs + "CSubscriber";
  // channel modes
  private static final int NONE = 0;
  private static final int BROWSE = 1;
  private static final int SUBSCRIBE = 2;
  private static final int SUBTO = 3;
  private static final int PREVIEW = 4;
  private int mode = NONE;
  private DocumentImpl userLayoutXML = null;
  private Document channelRegistry = null;
  private String action = null;
  private String categoryID = null;
  private String folderID = null;
  private String[] subIDs = null;               // contains the IDs of channels to be subscribed
  private boolean modified = false;             // modification flag
  private static final String regID = "top";                    // mark the top level of the categories

  /**
   * Construct a CSubscriber.
   */
  public CSubscriber () throws PortalException {
    this.staticData = new ChannelStaticData();
    this.runtimeData = new ChannelRuntimeData();
    this.set = new StylesheetSet(stylesheetDir + fs + "CSubscriber.ssl");
    this.set.setMediaProps(portalBaseDir + fs + "properties" + fs + "media.properties");
    // RDBMChannelRegistryStore should be retrieved in a different way!!
    this.chanReg = RdbmServices.getChannelRegistryStoreImpl();
  }

  /**
   * Get a handle on the UserLayoutManager
   * @param pcs portal control structures
   */
  public void setPortalControlStructures (PortalControlStructures pcs) {
    ulm = pcs.getUserLayoutManager();
  }

  /**
   * Returns channel runtime properties
   * @return handle to runtime properties
   */
  public ChannelRuntimeProperties getRuntimeProperties () {
    // Channel will always render, so the default values are ok
    return  new ChannelRuntimeProperties();
  }

  /**
   * Receive any events from the layout
   * @param ev layout event
   */
  public void receiveEvent (PortalEvent ev) {
  // no events for this channel
  }

  /**
   * Receive static channel data from the portal
   * @param sd static channel data
   */
  public void setStaticData (ChannelStaticData sd) {
    this.staticData = sd;
  }

  /**
   * Receives channel runtime data from the portal and processes actions
   * passed to it.  The names of these parameters are entirely up to the channel.
   * @param rd handle to channel runtime data
   */
  public void setRuntimeData (ChannelRuntimeData rd) throws PortalException {
    this.runtimeData = rd;
    String catID = null;
    //get fresh copies of both since we don't really know if changes have been made
    if (userLayoutXML == null)
      userLayoutXML = (DocumentImpl) ulm.getUserLayoutCopy();
    if(channelRegistry == null) {
        try {
        channelRegistry = chanReg.getChannelRegistryXML();
        } catch (Exception e) {
          LogService.instance().log(LogService.ERROR, e);
        }
    }
    action = runtimeData.getParameter("action");
    if (action != null) {
      try {
        if (action.equals("browse")) {
          prepareBrowse();
        }
        else if (action.equals("subscribe")) {
          prepareSubscribe();
        }
        else if (action.equals("subscribeTo")) {
          prepareSubscribeTo();
        }
        else if (action.equals("saveChanges")) {
          prepareSaveChanges();
        }
      } catch (Exception e) {
        LogService.instance().log(LogService.ERROR, e);
        throw  new GeneralRenderingException(e.getMessage());
      }
    }
    if (categoryID == null)
      categoryID = this.regID;
  }

  /**
   * Output channel content to the portal
   * @param out a sax document handler
   */
  public void renderXML (ContentHandler out) {
    try {
      switch (mode) {
        case BROWSE:
          processXML("browse", channelRegistry, out);
          break;
        case SUBSCRIBE:
          processXML("subscribe", userLayoutXML, out);
          break;
        case SUBTO:
          processXML("browse", channelRegistry, out);
          break;
        default:
          processXML("browse", channelRegistry, out);
          break;
      }
    } catch (Exception e) {
      LogService.instance().log(LogService.ERROR, e);
    }
  }

  /**
   * put your documentation comment here
   * @param stylesheetName
   * @param xmlSource
   * @param out
   * @exception org.xml.sax.SAXException
   */
  private void processXML (String stylesheetName, Document xmlSource, ContentHandler out) throws org.xml.sax.SAXException,
      PortalException {
    String xsl = set.getStylesheetURI(stylesheetName, runtimeData.getBrowserInfo());
    try {
      if (xsl != null) {
        Hashtable ssParams = new Hashtable();
        ssParams.put("baseActionURL", runtimeData.getBaseActionURL());
        ssParams.put("categoryID", categoryID);
        ssParams.put("modified", new Boolean(modified));
        XSLT.transform(xmlSource, new URL(xsl), out, ssParams);
      }
      else
        LogService.instance().log(LogService.ERROR, "org.jasig.portal.channels.CSubscriber: unable to find a stylesheet for rendering");
    } catch (Exception e) {
      LogService.instance().log(LogService.ERROR, e);
    }
  }

  /**
   * put your documentation comment here
   */
  private void prepareBrowse () {
    mode = BROWSE;
    String runtimeCatID = runtimeData.getParameter("categoryID");
    if (runtimeCatID != null)
      categoryID = runtimeCatID;
  }

  /**
   * put your documentation comment here
   */
  private void prepareSubscribe () {
    mode = SUBSCRIBE;
    subIDs = runtimeData.getParameterValues("sub");
  }

  /**
   * put your documentation comment here
   * @exception Exception
   */
  private void prepareSubscribeTo () throws Exception {
    mode = SUBTO;
    String destinationID = runtimeData.getParameter("destination");
    Node destination = null;
    if (destinationID == null) {
      LogService.instance().log(LogService.ERROR, "CSubscriber::prepareSubscribeTo() : received a null destinationID !");
    }
    else {
      if (destinationID.equals(this.regID))
        destination = userLayoutXML.getDocumentElement();       // the layout element
      else
        destination = userLayoutXML.getElementById(destinationID);
      if (destination == null) {
        LogService.instance().log(LogService.ERROR, "CSubscriber::prepareSubscribeTo() : destinationID=\"" + destinationID + "\" results in an empty node !");
      }
      else {
        for (int i = 0; i < subIDs.length; i++) {
            Element channel = channelRegistry.getElementById(subIDs[i]);
            // user wants to add an entire category to layout
            if (subIDs[i].startsWith("cat")) {
                NodeList channels = channel.getChildNodes();
                for (int j = 0; j < channels.getLength(); j++) {
                    Element newNode=(Element)userLayoutXML.importNode(channels.item(j),true);
                    setNextInstanceID(newNode);
                    destination.insertBefore(newNode, null);
                    // set the id (Xerces-specific)
                    userLayoutXML.putIdentifier(newNode.getAttribute("ID"),newNode);
                }
            }
            else {
                Element newNode=(Element)userLayoutXML.importNode(channel,true);
                setNextInstanceID(newNode);
                destination.insertBefore(newNode, null);
                // set the id (Xerces-specific)
                userLayoutXML.putIdentifier(newNode.getAttribute("ID"),newNode);
            }
        }
        modified = true;
      }
    }
  }

  /**
   * put your documentation comment here
   * @exception PortalException
   */
  private void prepareSaveChanges () throws PortalException {
    // save layout copy
    categoryID = this.regID;
    modified = false;
    ulm.setNewUserLayoutAndUserPreferences(userLayoutXML, null);
  }

  /**
   * Returns the next instance id
   * to be added to user's layout.xml
   * @return String
   */
  public void setNextInstanceID (Node channel) throws Exception {
    String sInstanceID = GenericPortalBean.getUserLayoutStore().getNextStructChannelId(staticData.getPerson());
    ((Element)channel).setAttribute("ID", sInstanceID);
  }
}



