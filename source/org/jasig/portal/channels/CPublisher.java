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
import org.jasig.portal.utils.*;
import org.jasig.portal.services.Authorization;
import org.jasig.portal.security.*;
import org.w3c.dom.*;
import org.apache.xalan.xslt.*;
import org.apache.xerces.dom.*;
import org.xml.sax.DocumentHandler;
import java.net.URL;
import java.io.*;
import java.util.*;
import java.sql.*;

/**
 * Provides methods associated with subscribing to a channel.
 * This includes preview, listing all available channels
 * and placement on a users page.
 * @author John Laker
 * @version $Revision$
 */
public class CPublisher implements IPrivilegedChannel
{
  private boolean DEBUG = false;

  private ChannelStaticData staticData = null;
  private ChannelRuntimeData runtimeData = null;
  private StylesheetSet set = null;
  private ChannelRegistryImpl chanReg = null;
  private RdbmServices rdbmService = new RdbmServices ();
  private Connection con = null;
  private String media;

  private static final String fs = File.separator;
  private static final String portalBaseDir = GenericPortalBean.getPortalBaseDir ();
  String stylesheetDir = portalBaseDir + fs + "webpages" + fs + "stylesheets" + fs + "org" + fs + "jasig" + fs + "portal" + fs + "channels" + fs + "CPublisher";

  // channel modes
  private static final int NONE     = 0;
  private static final int CHOOSE   = 1;
  private static final int PUBLISH  = 2;
  private static final int PUBCATS  = 3;
  private static final int CATS     = 4;
  private static final int ROLES    = 5;
  private static final int PUBROLES = 6;
  private static final int NAME = 7;
  private static final int PREVIEW  = 8;
  
  //number of extra steps
  private static final int EXTRA = 2;

  private int mode = NONE;
  private Document channelTypes = null;
  private Document channelDecl = null;
  private Document chanDoc = null;
  private String action = null;
  private String currentStep = "1";
  private String specialStep = "";
  private int numSteps = 0;
  private int newNumSteps = 0;
  private String declURI;
  private String catID[] = null;
  private boolean modified = false; // modification flag
  public static Vector vReservedParams = getReservedParams();
  private Hashtable hParams = null;
  

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

  /**
   * Loads the reserved parameter names.
   */
    private static Vector getReservedParams()
    {
        Vector v = new Vector();
        v.addElement("action");
        v.addElement("currentStep");
        v.addElement("numSteps");
        v.addElement("ssl");
        v.addElement("class");
        v.addElement("chanName");
        return v;
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
   * @throws PortalException generic portal exception
 */
  public void setStaticData(final org.jasig.portal.ChannelStaticData sd) throws org.jasig.portal.PortalException
  {
    this.staticData = sd;
  }

  /** Receives channel runtime data from the portal and processes actions
   * passed to it.  The names of these parameters are entirely up to the channel.
   * @param rd handle to channel runtime data
   * @throws PortalException generic portal exception
 */
  public void setRuntimeData(final org.jasig.portal.ChannelRuntimeData rd) throws org.jasig.portal.PortalException
  {
    this.runtimeData = rd;
    media = runtimeData.getMedia();
    //catID = runtimeData.getParameter("catID");
    String role = "student"; //need to get from current user
    chanReg = new ChannelRegistryImpl();

    //get fresh copy of both since we don't really know if changes have been made

    if (channelTypes==null) channelTypes = chanReg.getTypesXML(role);

    action = runtimeData.getParameter ("action");
    //System.out.println("action: "+ action);
    if (action != null)
    {
      if (action.equals ("choose"))
        prepareChoose ();
      else if (action.equals ("publish"))
        preparePublish ();
      else if (action.equals ("publishCats"))
        preparePublishCats ();
      else if (action.equals ("saveChanges"))
        prepareSaveChanges ();
      else if (action.equals("cancel"))
          mode = NONE;
    }

  }


  /** Output channel content to the portal
   * @param out a sax document handler
   * @throws PortalException generic portal exception
 */
  public void renderXML(final org.xml.sax.DocumentHandler out) throws org.jasig.portal.PortalException
  {
    try
    {
      switch (mode)
      {
        case CHOOSE:
          processXML ("main",channelDecl, out);
          break;
        case PUBLISH:
          processXML ("main",channelDecl, out);
          break;
        case CATS:
          processXML("main", chanReg.getCategoryXML(null), out);
          break;
        default:
          processXML ("main",channelTypes, out);
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
      ssParams.put("currentStep", currentStep);
      ssParams.put("specialStep", specialStep);
      ssParams.put("numSteps", Integer.toString(newNumSteps));
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

  private void prepareChoose ()
  {
    mode = CHOOSE;
    currentStep = "1";
    catID = null;
    String runtimeURI = runtimeData.getParameter ("channel");

    if (runtimeURI != null)
      declURI = runtimeURI;

    try{
        org.apache.xerces.parsers.DOMParser parser = new org.apache.xerces.parsers.DOMParser ();
        parser.parse(UtilitiesBean.fixURI(declURI));
        //System.out.println("declURI: "+ UtilitiesBean.fixURI(declURI));
        channelDecl = parser.getDocument();
    }
    catch (Exception e) {}
  }

  private void preparePublish ()
  {
    mode = PUBLISH;
    if (hParams==null) hParams = new Hashtable();
    currentStep = runtimeData.getParameter("currentStep");
    if(numSteps==0) numSteps = Integer.parseInt(runtimeData.getParameter("numSteps"));
    if(newNumSteps==0) newNumSteps = numSteps + EXTRA;
    Enumeration e = runtimeData.getParameterNames();

    if(!currentStep.equals("end")) {

        int i = Integer.parseInt(currentStep);

        if(i < numSteps){
            currentStep = Integer.toString(i+1);
        }
        else if(i == numSteps){
            mode = CATS;
            currentStep = Integer.toString(i+1);
        }
        else if(i == numSteps+1){
            mode = NAME;
            specialStep = "name";
            currentStep = Integer.toString(i+1);
        }
       // else if(i == numSteps + 1) {
       //     mode = ROLES;
       //     currentStep = Integer.toString(i+1);
       // }
        else {
            publishChannel();
            specialStep = "end";
        }

    //System.out.println("numSteps: "+ numSteps);
    //System.out.println("currentStep: "+ currentStep);

    while(e.hasMoreElements()) {
        String s = (String)e.nextElement();

            if (runtimeData.getParameter(s)!=null) {
                //System.out.println("adding param: "+ s);
                //System.out.println("adding param value: "+ runtimeData.getParameter(s));
            hParams.put(s, runtimeData.getParameter(s));
            }
    }
  }
  }

    private void publishChannel () {

        String nextID = chanReg.getNextId();
        Document doc = new DocumentImpl();
        Element chan = doc.createElement("channel");

        chan.setAttribute("timeout", "5000");
        chan.setAttribute("priority", "1");
        chan.setAttribute("minimized", "false");
        chan.setAttribute("editable", "false");
        chan.setAttribute("hasHelp", "false");
        chan.setAttribute("removable", "true");
        chan.setAttribute("detachable", "true");
        chan.setAttribute("class", (String)hParams.get("class"));
        
        String cName = (String)hParams.get("chanName");
        if(cName == null) cName = "new channel";
        
        chan.setAttribute("name", cName);
        
        if (nextID!=null) chan.setAttribute("ID", "chan"+nextID);

        Enumeration e = hParams.keys();
        while (e.hasMoreElements()) {
            String name = (String)e.nextElement();
            String value = (String) hParams.get(name);
            
            if(!vReservedParams.contains(name)){
            Element el = doc.createElement("parameter");
            el.setAttribute("name", XMLEscaper.escape(name));
            el.setAttribute("value", XMLEscaper.escape(value));
            chan.appendChild(el);
            }
        }
        doc.appendChild(chan);

        chanReg.addChannel(nextID, cName, doc, catID);
    }


  private void preparePublishCats ()
  {
    mode = PUBLISH;
    catID = runtimeData.getParameterValues("cat");
  }

  private void prepareSaveChanges ()
  {
    // save layout copy
    catID = this.catID;
    modified = false;
  }

/** Method for setting the portal control structures that a privledged channel has access to
 * @param p1 a handle to the PortalControlStructures
 * @throws PortalException a generic portal exception
 */  
  public void setPortalControlStructures(final org.jasig.portal.PortalControlStructures p1) throws org.jasig.portal.PortalException {
  }
  
/** Method for approving a channel by setting the <CODE>APPROVED</CODE> field to 1
 */
  public void approveChannel() {
    
    String sChanId = runtimeData.getParameter("CHAN_ID");

    try
    {
      this.con = this.rdbmService.getConnection ();
      Statement stmt = con.createStatement();

      String sUpdate = "UPDATE UP_CHANNELS SET APPROVED = 1 WHERE CHAN_ID = " + sChanId;

      int iUpdated = stmt.executeUpdate(sUpdate);

      Logger.log(Logger.DEBUG, "Approving channel ID: " + sChanId + ". Updated " + iUpdated + " rows.");

      stmt.close();
    }
    catch (Exception e)
    {
      Logger.log(Logger.ERROR, e);
    }
    finally
    {
      rdbmService.releaseConnection(con);
    }

  }
  
  private Document getRoles()
  {
    Document roleDoc = null;
    try
    {
      Authorization authorization = new Authorization();

      Vector vRoles = authorization.getAllRoles();

      roleDoc = new DocumentImpl();
      Element root = roleDoc.createElement("roles");
      Element role = null;
      for(int i = 0; i < vRoles.size(); i++)
      {
        role = roleDoc.createElement("role");
        role.setAttribute("name", ((IRole)vRoles.elementAt(i)).getRoleTitle());
        root.appendChild(role);
      }
      roleDoc.appendChild(root);
    }
    catch(Exception e)
    {
      Logger.log(Logger.ERROR, e);
    }
    return roleDoc;
  }
  
}
