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
 */

package org.jasig.portal.channels;

import org.jasig.portal.*;
import org.jasig.portal.utils.XSLT;
import org.w3c.dom.Node;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.w3c.dom.Document;
import org.xml.sax.DocumentHandler;
import java.io.File;
import java.net.URL;
import java.util.*;

/**
 * Provides methods associated with subscribing to a channel.
 * This includes preview, listing all available channels
 * and placement on a users page.
 * @author John Laker
 * @version $Revision$
 */
public class CSubscriber implements IPrivilegedChannel
{

  UserLayoutManager ulm;
  ChannelStaticData staticData = null;
  ChannelRuntimeData runtimeData = null;
  StylesheetSet set = null;
  ChannelRegistryImpl chanReg = null;
  private String media;

  private static final String fs = File.separator;
  private static final String portalBaseDir = GenericPortalBean.getPortalBaseDir ();
  String stylesheetDir = portalBaseDir + fs + "webpages" + fs + "stylesheets" + fs + "org" + fs + "jasig" + fs + "portal" + fs + "channels" + fs + "CSubscriber";

  // channel modes
  private static final int NONE = 0;
  private static final int BROWSE = 1;
  private static final int SUBSCRIBE = 2;
  private static final int SUBTO = 3;
  private static final int PREVIEW = 4;

  private int mode = NONE;
  private Document userLayoutXML = null;
  private Document channelRegistry = null;
  private String action = null;
  private String categoryID = null;
  private String folderID = null;
  private String[] subIDs = null; // contains the IDs of channels to be subscribed
  private boolean modified = false; // modification flag
  private static final String regID = "top"; // mark the top level of the categories

  /** Construct a CSubscriber.
   */
  public CSubscriber ()
  {
    this.staticData = new ChannelStaticData ();
    this.runtimeData = new ChannelRuntimeData ();
    this.set = new StylesheetSet (stylesheetDir + fs + "CSubscriber.ssl");
    this.set.setMediaProps (portalBaseDir + fs + "properties" + fs + "media.properties");
    this.chanReg = new ChannelRegistryImpl ();
  }


  /**Get a handle on the UserLayoutManager
   * @param pcs portal control structures
   */
    public void setPortalControlStructures(PortalControlStructures pcs) {
        ulm=pcs.getUserLayoutManager();
    }

  /** Returns channel runtime properties
   * @return handle to runtime properties
   */
  public ChannelRuntimeProperties getRuntimeProperties ()
  {
    // Channel will always render, so the default values are ok
    return new ChannelRuntimeProperties ();
  }

  /** Receive any events from the layout
   * @param ev layout event
   */
  public void receiveEvent (PortalEvent ev)
  {
    // no events for this channel
  }

  /** Receive static channel data from the portal
   * @param sd static channel data
   */
  public void setStaticData (ChannelStaticData sd)
  {
    this.staticData = sd;
  }

  /** Receives channel runtime data from the portal and processes actions
   * passed to it.  The names of these parameters are entirely up to the channel.
   * @param rd handle to channel runtime data
   */
  public void setRuntimeData (ChannelRuntimeData rd) throws PortalException
  {
    this.runtimeData = rd;

    media = runtimeData.getMedia();

    String catID = null;
    //catID = runtimeData.getParameter("catID");
    String role = "student"; //need to get from current user
    //chanReg = new ChannelRegistryImpl();

    //get fresh copies of both since we don't really know if changes have been made

    if(userLayoutXML==null)userLayoutXML=ulm.getUserLayoutCopy();

    channelRegistry = chanReg.getRegistryXML(catID, role);


    action = runtimeData.getParameter ("action");

    if (action != null)
    {
      if (action.equals ("browse"))
        prepareBrowse ();
      else if (action.equals ("subscribe"))
        prepareSubscribe ();
      else if (action.equals ("subscribeTo"))
        prepareSubscribeTo ();
      else if (action.equals ("saveChanges"))
        prepareSaveChanges ();
    }

    if (categoryID == null)
      categoryID = this.regID;
  }


  /** Output channel content to the portal
   * @param out a sax document handler
   */
  public void renderXML (DocumentHandler out)
  {
    try
    {
      switch (mode)
      {
        case BROWSE:
          processXML ("browse", channelRegistry, out);
          break;
        case SUBSCRIBE:
          processXML ("subscribe", userLayoutXML, out);
          break;
        case SUBTO:
          processXML ("browse", channelRegistry, out);
          break;
        default:
          processXML ("browse", channelRegistry, out);
          break;
      }
    }
    catch (Exception e)
    {
      Logger.log (Logger.ERROR, e);
    }
  }

  private void processXML (String stylesheetName, Document xmlSource, DocumentHandler out) throws org.xml.sax.SAXException
  {

    String xsl = set.getStylesheetURI(stylesheetName, media);

    try{
    if (xsl != null)
    {
      Hashtable ssParams = new Hashtable();
      ssParams.put("baseActionURL", runtimeData.getBaseActionURL());
      ssParams.put("categoryID", categoryID);
      ssParams.put("modified", new Boolean(modified));
      XSLT.transform(xmlSource, new URL(xsl), out, ssParams);
    }
    else
      Logger.log(Logger.ERROR, "org.jasig.portal.channels.CSubscriber: unable to find a stylesheet for rendering");
    }
    catch(Exception e){
        Logger.log(Logger.ERROR, e);
    }
  }

  private void prepareBrowse ()
  {
    mode = BROWSE;
    String runtimeCatID = runtimeData.getParameter ("categoryID");

    if (runtimeCatID != null)
      categoryID = runtimeCatID;
  }

  private void prepareSubscribe ()
  {
    mode = SUBSCRIBE;
    subIDs = runtimeData.getParameterValues ("sub");
  }

  private void prepareSubscribeTo ()
  {
    mode = SUBTO;
    String destinationID = runtimeData.getParameter ("destination");
    Node destination = null;

    if (destinationID == null) {
        Logger.log(Logger.ERROR,"CSubscriber::prepareSubscribeTo() : received a null destinationID !");
    } else {
        if (destinationID.equals (this.regID))
            destination = userLayoutXML.getDocumentElement (); // the layout element
        else
            destination = userLayoutXML.getElementById (destinationID);

        if(destination==null) {
            Logger.log(Logger.ERROR,"CSubscriber::prepareSubscribeTo() : destinationID=\""+destinationID+"\" results in an empty node !");
        } else {
            for (int i = 0; i < subIDs.length; i++) {

                Node channel = channelRegistry.getElementById (subIDs[i]);

                // user wants to add an entire category to layout
                if(subIDs[i].startsWith("cat")) {
                    NodeList channels = channel.getChildNodes();

                    for (int j=0; j<channels.getLength(); j++) {
                        channel = channels.item(j);
                        setNextInstanceID(channel);
                        destination.insertBefore (userLayoutXML.importNode(channel, true), null);
                    }
                }
                else {
                    setNextInstanceID(channel);
                    destination.insertBefore (userLayoutXML.importNode(channel, true), null);
                }
            }
            modified = true;
        }
    }
  }

  private void prepareSaveChanges () throws PortalException
  {
    // save layout copy
    categoryID = this.regID;
    modified = false;
    ulm.setNewUserLayoutAndUserPreferences(userLayoutXML,null);
  }

    /**
   * Returns the next instance id
   * to be added to user's layout.xml
   * @return String
   */
   public void setNextInstanceID(Node channel)
   {
    NodeList chans = userLayoutXML.getElementsByTagName("channel");

    List instanceIDs = new ArrayList();

    for (int iChan = 0; iChan < chans.getLength(); iChan++)
    {
        Integer id;
        Element nChan = (Element)chans.item(iChan);
        String sInstanceID = nChan.getAttribute("ID");
        if (!sInstanceID.startsWith("hchan")) {
            id = new Integer (sInstanceID.substring (4));
            instanceIDs.add (id);
        }
    }

    Collections.sort(instanceIDs);
    int iHighest = -1;
    if (instanceIDs.size() > 0)
    {
       iHighest = ((Integer)instanceIDs.get (instanceIDs.size () - 1)).intValue ();
    }
    String sInstanceID = "chan" + (iHighest + 1);
    ((Element)channel).setAttribute("ID", sInstanceID);
   }

}
