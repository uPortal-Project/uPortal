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
import org.jasig.portal.utils.XSLT;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import org.w3c.dom.*;
import org.apache.xalan.xslt.*;
import org.xml.sax.DocumentHandler;
import java.io.*;

/**
 * Provides methods associated with subscribing to a channel.
 * This includes preview, listing all available channels
 * and placement on a users page.
 * @author John Laker
 * @version $Revision$
 */
public class CPublisher implements ISpecialChannel
{

  ChannelStaticData staticData = null;
  ChannelRuntimeData runtimeData = null;
  StylesheetSet set = null;
  ChannelRegistryImpl chanReg = null;

  private static final String fs = File.separator;
  private static final String portalBaseDir = GenericPortalBean.getPortalBaseDir ();
  String stylesheetDir = portalBaseDir + fs + "webpages" + fs + "stylesheets" + fs + "org" + fs + "jasig" + fs + "portal" + fs + "channels" + fs + "CPublisher";
  
  // channel modes
  private static final int NONE = 0;
  private static final int CHOOSE = 1;
  private static final int PUBLISH = 2;
  private static final int PUBTO = 3;
  private static final int PREVIEW = 4;
  
  private int mode = NONE;
  private Document channelTypes = null;
  private Document channelDecl = null;
  private String action = null;
  private String currentStep;
  private String declURI;
  private String catID;
  private String[] catIDs = null; // contains the IDs of channels categories
  private boolean modified = false; // modification flag
  
  /** Construct a CPublisher.
   */
  public CPublisher ()
  {
    this.staticData = new ChannelStaticData ();
    this.runtimeData = new ChannelRuntimeData ();
    this.set = new StylesheetSet (stylesheetDir + fs + "CPublisher.ssl");
    this.set.setMediaProps (portalBaseDir + fs + "properties" + fs + "media.properties");
    this.chanReg = new ChannelRegistryImpl ();
  }


  /** Returns static channel properties to the portal
   * @return handle to publish properties
   */
  public ChannelSubscriptionProperties getSubscriptionProperties ()
  {
    ChannelSubscriptionProperties csb = new ChannelSubscriptionProperties ();
    csb.setName ("Publisher");
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

  /** Receive any events from the layout
   * @param ev layout event
   */
  public void receiveEvent (LayoutEvent ev)
  {
    // no events for this channel
  }

  /** Receive static channel data from the portal
   * @param sd static channel data
   */
  public void setStaticData(final org.jasig.portal.ChannelStaticData sd) throws org.jasig.portal.PortalException
  { 
    this.staticData = sd;
  }

  /** Receives channel runtime data from the portal and processes actions 
   * passed to it.  The names of these parameters are entirely up to the channel. 
   * @param rd handle to channel runtime data
   */
  public void setRuntimeData(final org.jasig.portal.ChannelRuntimeData rd) throws org.jasig.portal.PortalException
  {
    this.runtimeData = rd;    
    currentStep = "1";
    //catID = runtimeData.getParameter("catID");
    String role = "student"; //need to get from current user
    chanReg = new ChannelRegistryImpl();
    
    //get fresh copy of both since we don't really know if changes have been made
   
    if (channelTypes==null) channelTypes = chanReg.getTypesXML(role);
    
    action = runtimeData.getParameter ("action");
    
    if (action != null)
    {
      if (action.equals ("choose"))
        prepareChoose ();
      else if (action.equals ("publish"))
        preparePublish ();
      else if (action.equals ("publishTo"))
        preparePublishTo ();
      else if (action.equals ("saveChanges"))
        prepareSaveChanges ();
    }
        
  }
      

  /** Output channel content to the portal
   * @param out a sax document handler
   */
  public void renderXML(final org.xml.sax.DocumentHandler out) throws org.jasig.portal.PortalException
  {
    try
    {
      switch (mode)
      {
        case CHOOSE:
          processXML ("main", out);
          break;
        case PUBLISH:
          processXML ("publish", out);
          break;
        default:
          processXML ("main", out);
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
    XSLTInputSource xmlSource = null;
    
    switch (mode)
      {
        case CHOOSE:
            xmlSource = new XSLTInputSource(channelDecl);
        case PUBLISH:
          xmlSource = new XSLTInputSource (channelDecl);
          break;
        default:
          xmlSource = new XSLTInputSource (channelTypes);
          break;
      }
      
    XSLTInputSource xslSource = set.getStylesheet(stylesheetName, runtimeData.getHttpRequest());
    XSLTResultTarget xmlResult = new XSLTResultTarget(out);

    if (xslSource != null)
    {
      XSLTProcessor processor = XSLTProcessorFactory.getProcessor (new org.apache.xalan.xpath.xdom.XercesLiaison ());
      processor.setStylesheetParam("baseActionURL", processor.createXString (runtimeData.getBaseActionURL()));        
      processor.setStylesheetParam("currentStep", processor.createXString (currentStep));  
      processor.setStylesheetParam("modified", processor.createXBoolean (modified));        
      processor.process (xmlSource, xslSource, xmlResult);
    }
    else 
      Logger.log(Logger.ERROR, "org.jasig.portal.channels.CPublisher: unable to find a stylesheet for rendering");
  }
  
  private void prepareChoose ()
  {
    mode = CHOOSE;
    String runtimeURI = runtimeData.getParameter ("channel");

    if (runtimeURI != null)
      declURI = runtimeURI;
    
    try{ 
        org.apache.xerces.parsers.DOMParser parser = new org.apache.xerces.parsers.DOMParser ();
        parser.parse(UtilitiesBean.fixURI(declURI));
        System.out.println("declURI: "+ UtilitiesBean.fixURI(declURI));
        channelDecl = parser.getDocument();
    }
    catch (Exception e) {}
  }

  private void preparePublish ()
  {
    mode = PUBLISH;
    HttpServletRequest req = runtimeData.getHttpRequest ();
    catIDs = req.getParameterValues ("cat");    
  }
   
  private void preparePublishTo ()
  {
    mode = PUBTO;
    String destinationID = runtimeData.getParameter ("destination");
    Node destination = null;
    
    if (destinationID == null) {
	Logger.log(Logger.ERROR,"CPublisher::preparePublishTo() : received a null destinationID !");
    } else {
	if (destinationID.equals (this.catID))
	    destination = channelDecl.getDocumentElement (); // the layout element
	else
	    destination = channelDecl.getElementById (destinationID);

	if(destination==null) {
	    Logger.log(Logger.ERROR,"CPublisher::preparePublishTo() : destinationID=\""+destinationID+"\" results in an empty node !"); 
	} else {
	    for (int i = 0; i < catIDs.length; i++) {
                
                Node channel = channelTypes.getElementById (catIDs[i]); 
              
                // user wants to add an entire category to layout
                if(catIDs[i].startsWith("cat")) {       
                   // Node channel = channelRegistry.getElementById (subIDs[i]);
                    NodeList channels = channel.getChildNodes();
                    
                    for (int j=0; j<channels.getLength(); j++) {
                        channel = channels.item(j);
                        destination.insertBefore (channelDecl.importNode(channel, false), null);
                    }
                }
                else {
                 
                    //Node channel = channelRegistry.getElementById (subIDs[i]);
                    destination.insertBefore (channelDecl.importNode(channel, false), null);
                }
	    }
	    modified = true;
	}
    }
  }
  
  private void prepareSaveChanges ()
  {
    // save layout copy
    catID = this.catID;
    modified = false;
  }
  
  public void setPortalControlStructures(final org.jasig.portal.PortalControlStructures p1) throws org.jasig.portal.PortalException {
  }
  
}
