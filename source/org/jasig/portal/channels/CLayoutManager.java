/**
 * Copyright (c) 2000 The JA-SIG Collaborative.  All rights reserved.
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
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import org.w3c.dom.*;
import org.apache.xalan.xslt.*;
import org.xml.sax.DocumentHandler;
import java.io.*;

/** <p>Manages User Layout</p>
 * @author Ken Weiner, kweiner@interactivebusiness.com
 * @version $Revision$
 */
public class CLayoutManager implements ISpecialChannel
{

    UserLayoutManager ulm;

  ChannelStaticData staticData = null;
  ChannelRuntimeData runtimeData = null;
  StylesheetSet set = null;

  private static final String fs = File.separator;
  private static final String portalBaseDir = GenericPortalBean.getPortalBaseDir ();
  String stylesheetDir = portalBaseDir + fs + "webpages" + fs + "stylesheets" + fs + "org" + fs + "jasig" + fs + "portal" + fs + "channels" + fs + "CLayoutManager";
  
  // Various modes
  private static final int NONE = 0;
  private static final int BROWSE = 1;
  private static final int MOVE = 2;
  private static final int MOVETO = 3;
  private static final int REORDER = 4;
  
  private int mode = NONE;
  private Document userLayoutXML = null;
  private String action = null;
  private String folderID = null;
  private String[] moveIDs = null; // contains the IDs of channels/folders to be moved
  private boolean modified = false; // becomes true after user makes changes to layout
  
  private static final String layoutID = "top"; // just a way to refer to the layout element since it doesn't have an ID attribute
  
  /** Constructs a CLayoutManager.
   */
  public CLayoutManager ()
  {
    this.staticData = new ChannelStaticData ();
    this.runtimeData = new ChannelRuntimeData ();
    this.set = new StylesheetSet (stylesheetDir + fs + "CLayoutManager.ssl");
    this.set.setMediaProps (portalBaseDir + fs + "properties" + fs + "media.properties");
  }


    public void setPortalControlStructures(PortalControlStructures pcs) {
	ulm=pcs.getUserLayoutManager();
    }


  /** Returns static channel properties to the portal
   * @return handle to subscription properties
   */
  public ChannelSubscriptionProperties getSubscriptionProperties ()
  {
    ChannelSubscriptionProperties csb = new ChannelSubscriptionProperties ();
    
    // Properties which are not specifically set here will assume default
    // values as determined by ChannelSubscriptionProperties
    csb.setName ("Layout Manager");
    return csb;
  }

  /** Returns channel runtime properties
   * @return handle to runtime properties
   */
  public ChannelRuntimeProperties getRuntimeProperties ()
  {
    // Channel will always render, so the default values are ok
    return new ChannelRuntimeProperties ();
  }

  /** Processes layout-level events coming from the portal
   * @param ev a portal layout event
   */
  public void receiveEvent (LayoutEvent ev)
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
  public void setRuntimeData (ChannelRuntimeData rd)
  {
    this.runtimeData = rd;    
    
    /* This won't work because the cloneNode method doesn't support the 
     * cloning of IDs.  We need another way to clone the DOM.
     *
    */
    // Get an initial copy of the user layout xml
    /*
    if (userLayoutXML == null)	{
	HttpServletRequest req = runtimeData.getHttpRequest ();
	HttpSession session = req.getSession (false);
	Document userLayoutXMLFromSession = (Document) session.getAttribute ("userLayoutXML");
	userLayoutXML = (Document) UtilitiesBean.cloneDocument(userLayoutXMLFromSession);
    }
    */
    
    
    // For now, use these 3 lines which will modify original user layout xml
    // When we figure out how to clone the document properly, replace with the
    // section above.
    /*
    HttpServletRequest req = runtimeData.getHttpRequest ();
    HttpSession session = req.getSession (false);
    userLayoutXML = (Document) session.getAttribute ("userLayoutXML");
    */
    
    //need to get a fresh copy in case another channel has made changes
    if(userLayoutXML==null) {
	userLayoutXML=ulm.getUserLayoutCopy();
    }
    
    action = runtimeData.getParameter ("action");
    
    if (action != null)
    {
      if (action.equals ("browse"))
        prepareBrowse ();
      else if (action.equals ("move"))
        prepareMove ();
      else if (action.equals ("moveTo"))
        prepareMoveTo ();
      else if (action.equals ("reorder"))
        prepareReorder ();
      else if (action.equals ("saveChanges"))
        prepareSaveChanges ();
    }
        
    if (folderID == null)
      folderID = this.layoutID;
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
          processXML ("browse", out);
          break;
        case MOVE:
          processXML ("moveTo", out);
          break;
        case MOVETO:
          processXML ("browse", out);
          break;
        case REORDER:
          processXML ("browse", out);
          break;
        default:
          processXML ("browse", out);
          break;
      }
    } 
    catch (Exception e)
    {
      Logger.log (Logger.ERROR, e); 
    }
  }
  
  private void processXML (String stylesheetName, DocumentHandler out) throws org.xml.sax.SAXException
  {
    XSLTInputSource xmlSource = new XSLTInputSource (userLayoutXML);
    XSLTInputSource xslSource = set.getStylesheet(stylesheetName, runtimeData.getHttpRequest());
    XSLTResultTarget xmlResult = new XSLTResultTarget(out);

    if (xslSource != null)
    {
      XSLTProcessor processor = XSLTProcessorFactory.getProcessor (new org.apache.xalan.xpath.xdom.XercesLiaison ());
      processor.setStylesheetParam("baseActionURL", processor.createXString (runtimeData.getBaseActionURL()));        
      processor.setStylesheetParam("folderID", processor.createXString (folderID));  
      processor.setStylesheetParam("modified", processor.createXBoolean (modified));        
      processor.process (xmlSource, xslSource, xmlResult);
    }
    else 
      Logger.log(Logger.ERROR, "org.jasig.portal.channels.CLayoutManager: unable to find a stylesheet for rendering");
  }
  
  private void prepareBrowse ()
  {
    mode = BROWSE;
    String runtimeFolderID = runtimeData.getParameter ("folderID");

    if (runtimeFolderID != null)
      folderID = runtimeFolderID;
  }

  private void prepareMove ()
  {
    mode = MOVE;
    // getParameterValues() should be a method in ChannelRuntimeData.
    // For now, I'll use the request object -- ask Peter about this!
    HttpServletRequest req = runtimeData.getHttpRequest ();
    moveIDs = req.getParameterValues ("move");    
  }
   
  private void prepareMoveTo ()
  {
    mode = MOVETO;
    String destinationID = runtimeData.getParameter ("destination");
    Node destination = null;
    
    if (destinationID == null) {
	Logger.log(Logger.ERROR,"CLayoutManager::prepareMove() : received a null destinationID !");
    } else {
	if (destinationID.equals (this.layoutID))
	    destination = userLayoutXML.getDocumentElement (); // the layout element
	else
	    destination = userLayoutXML.getElementById (destinationID);

	if(destination==null) {
	    Logger.log(Logger.ERROR,"CLayoutManager::prepareMove() : destinationID=\""+destinationID+"\" results in an empty node !"); 
	} else {
	    for (int i = 0; i < moveIDs.length; i++) {
		Node relocating = userLayoutXML.getElementById (moveIDs[i]);
		destination.insertBefore (relocating, null); // adds to end of children nodes
	    }
	    modified = true;
	}
    }
  }
  
  private void prepareReorder ()
  {
    mode = REORDER;
    String elementID = runtimeData.getParameter ("elementID"); // the folder or channel ID
    String direction = runtimeData.getParameter ("dir"); // "up" or "down"
   
    Node element = userLayoutXML.getElementById (elementID);
    Node parent = element.getParentNode ();

    if (direction.equals ("up"))
    {
      Node prev;
      // Goto the previous channel or folder element
      for (prev = element.getPreviousSibling (); prev != null && prev.getNodeType () != Node.ELEMENT_NODE && (!prev.getNodeName ().equals ("channel") || !prev.getNodeName ().equals ("folder")); prev = prev.getPreviousSibling ());
      parent.insertBefore (element, prev);
    }
    else if (direction.equals ("down"))
    {
      Node next;
      // Goto the next channel or folder element
      for (next = element.getNextSibling (); next != null && next.getNodeType () != Node.ELEMENT_NODE && (!next.getNodeName ().equals ("channel") || !next.getNodeName ().equals ("folder")); next = next.getNextSibling ());
      parent.insertBefore (next, element);    
    }
    
    modified = true;
  }
  
  private void prepareSaveChanges ()
  {
    // write code to persist the userLayoutXML to the session
    // and the database (remember, as the user interacts with this
    // channel, changes are only made to a copy of the userLayoutXML
    // until this method is called)
    
    folderID = this.layoutID;
    modified = false;
    ulm.setNewUserLayoutAndUserPreferences(userLayoutXML,null);
  }
}
