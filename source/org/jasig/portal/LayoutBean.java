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

package org.jasig.portal;

import org.jasig.portal.security.IPerson;

import javax.servlet.*;
import javax.servlet.jsp.*;
import javax.servlet.http.*;

import java.io.*;
import java.util.*;
import java.text.*;
import java.sql.*;
import java.net.*;
import org.w3c.dom.*;
import org.apache.xalan.xpath.*;
import org.apache.xalan.xslt.*;
import org.apache.xml.serialize.*;

/**
 * LayoutBean is the central piece of the portal. It is responsible for presenting
 * content to the client given a request. It also handles basic user interactions,
 * passing appropriate parameters to the stylesheets, channels or userLayoutManager
 * @author Peter Kharchenko
 * @version $Revision$
 */
public class LayoutBean
{
  // all channel content/parameters/caches/etc are managed here
  ChannelManager channelManager;
  UserLayoutManager uLayoutManager;

  // contains information relating client names to media and mime types
  private MediaManager mediaM;

  XSLTProcessor sLayoutProcessor;
  XSLTProcessor uLayoutProcessor;


  /**
   * Constructor initializes media manager and stylesheet sets.
   */
  public LayoutBean ()
  {
    // init the media manager
    String fs = System.getProperty ("file.separator");
    String propertiesDir = GenericPortalBean.getPortalBaseDir () + "properties" + fs;
    String stylesheetDir = GenericPortalBean.getPortalBaseDir () + "webpages" + fs + "stylesheets" + fs + "org" + fs + "jasig" + fs + "portal" + fs + "LayoutBean" + fs;
    mediaM = new MediaManager (propertiesDir + "media.properties", propertiesDir + "mime.properties", propertiesDir + "serializer.properties");

    // instantiate the processors
    try
    {
      sLayoutProcessor = XSLTProcessorFactory.getProcessor ();
      uLayoutProcessor = XSLTProcessorFactory.getProcessor (new org.apache.xalan.xpath.xdom.XercesLiaison ());
    }
    catch (Exception e)
    {
      Logger.log (Logger.ERROR, "LayoutBean::LayoutBean() : caught an exception while trying initialize XLST processors. "+e);
    }
  }

  /**
   * Gets the person object from the session.  Null is returned if
   * no person is logged in
   * @param req the servlet request object
   * @return the person object, null if no person is logged in
   */
  public IPerson getPerson (HttpServletRequest req)
  {
    HttpSession session = req.getSession (false);
    IPerson person = (IPerson) session.getAttribute ("up_person");
    return person;
  }

  /**
   * Renders the current state of the portal into the target markup language
   * (basically, this is the main method that does all the work)
   * @param the servlet request object
   * @param the servlet response object
   * @param the JspWriter object
   */
  public void writeContent (HttpServletRequest req, HttpServletResponse res, java.io.PrintWriter out)
  {
      // This function does ALL the content gathering/presentation work.
      // The following filter sequence is processed:
      //        userLayoutXML (in UserLayoutManager)
      //              |
      //        incorporate StructureAttributes
      //              |
      //        Structure transformation
      //              + (buffering step)
      //        ChannelRendering Buffer
      //              |
      //        ThemeAttributesIncorporation Filter
      //              |
      //        Theme Transformation
      //              |
      //        ChannelIncorporation filter
      //              |
      //        Serializer (XHTML/WML/HTML/etc.)
      //              |
      //        JspWriter
      //

    try
    {
      //debug
      Logger.log(Logger.DEBUG,"--------Context Path=\""+req.getContextPath()+"\".");
      Logger.log(Logger.DEBUG,"--------Path info=\""+req.getPathInfo()+"\".");
      Logger.log(Logger.DEBUG,"--------Path translated=\""+req.getPathTranslated()+"\".");
      Logger.log(Logger.DEBUG,"--------Query String=\""+req.getQueryString()+"\".");
      Logger.log(Logger.DEBUG,"--------Servlet Path=\""+req.getServletPath()+"\".");

      // get the layout manager
      if (uLayoutManager == null) {
        uLayoutManager = new UserLayoutManager (req, getPerson(req));
      }
      if(uLayoutManager.userAgentUnmapped()) {
          // do the redirect
          // for debug purposes, we do the fake mapping to the "netscape" layout
          IUserPreferencesDB updb=new UserPreferencesDBImpl();
          IPerson person=getPerson(req);
          if(person==null) {
              int guestUserId = 1;
              person=new org.jasig.portal.security.provider.PersonImpl();
              person.setID(guestUserId);
          }
          // establish mapping
          updb.setUserBrowserMapping(person.getID(),req.getHeader("user-Agent"),"netscape");
          Logger.log(Logger.DEBUG,"LayoutBean::writeContent() : establishing UA mapping for user=\""+person.getID()+"\" and UA=\""+req.getHeader("user-Agent")+"\".");
          uLayoutManager = new UserLayoutManager(req,getPerson(req));
      }

      // process events that have to be handed directly to the userLayoutManager.
      // (examples of such events are "remove channel", "minimize channel", etc.
      //  basically things that directly affect the userLayout structure)
      processUserLayoutParameters (req, uLayoutManager);




      // set up the channelManager
      if (channelManager == null)
        channelManager = new ChannelManager (req, res,uLayoutManager);
      else
        channelManager.setReqNRes (req, res);


      // set the response mime type
      res.setContentType (uLayoutManager.getMimeType());



      // get a serializer appropriate for the target media
      BaseMarkupSerializer markupSerializer = mediaM.getSerializerByName(uLayoutManager.getSerializerName(), out);

      // set up the serializer
      markupSerializer.asContentHandler ();

      // set up the transformation pipeline

      // initialize ChannelIncorporationFilter
      ChannelIncorporationFilter cif = new ChannelIncorporationFilter (markupSerializer, channelManager);


      // set up the "theme" transformation
      XSLTInputSource themeStylesheet = uLayoutManager.getThemeStylesheet ();
      sLayoutProcessor.processStylesheet (themeStylesheet);
      sLayoutProcessor.setDocumentHandler (cif);

      // Peter, this is just temporary until you get the CHeader channel working.
      // I just needed a way for the Welcome message to change when a user logged in.
      // Just delete it at any time.
      IPerson person = getPerson(req);
      String userDisplayName;
      if(person != null)
        userDisplayName = person.getFullName();
      else
        userDisplayName = "Guest";
      sLayoutProcessor.setStylesheetParam("userName", sLayoutProcessor.createXString(userDisplayName));
      // End of temporary section

      // call layout manager to process all stylesheet-related request parameters
      uLayoutManager.processUserPreferencesParameters(req);

      // The preferences we get below are complete, that is all of the default
      // values that are usually null are filled out
      UserPreferences cup=uLayoutManager.getCompleteCurrentUserPreferences();

      // initialize a filter to fill in channel attributes for the
      // "theme" (second) transformation.
      ThemeAttributesIncorporationFilter taif=new ThemeAttributesIncorporationFilter(sLayoutProcessor,cup.getThemeStylesheetUserPreferences());


      // initialize ChannelRenderingBuffer
      ChannelRenderingBuffer crb = new ChannelRenderingBuffer (taif, channelManager);

      // filter to fill in channel/folder attributes for the "structure" transformation.
      //      StructureAttributesIncorporationFilter saif=new StructureAttributesIncorporationFilter(sLayoutProcessor,cup.getStructureStylesheetUserPreferences());



      // now that pipeline is set up, determine and set the stylesheet params

      // deal with parameters that are meant for the LayoutBean
      HttpSession session = req.getSession (false);

      // "layoutRoot" signifies a node of the userLayout structure
      // that will serve as a root for constructing structuredLayout
      //      String req_layoutRoot = req.getParameter ("userLayoutRoot");
      //String ses_layoutRoot = (String) session.getAttribute ("userLayoutRoot");

      /*      if (req_layoutRoot != null)
      {
        session.setAttribute ("userLayoutRoot", req_layoutRoot);
        rElement = uLayoutManager.getNode (req_layoutRoot);

        if (rElement == null)
        {
          rElement = uLayoutManager.getRoot ();
          Logger.log (Logger.DEBUG, "LayoutBean::writeChanels() : attempted to set layoutRoot to nonexistent node \"" + req_layoutRoot + "\", setting to the main root node instead.");
        }
        else
        {
          // Logger.log(Logger.DEBUG,"LayoutBean::writeChanels() : set layoutRoot to " + req_layoutRoot);
        }
      }
      else if (ses_layoutRoot != null)
      {
        rElement=uLayoutManager.getNode (ses_layoutRoot);
        // Logger.log(Logger.DEBUG,"LayoutBean::writeChannels() : retrieved the session value for layoutRoot=\""+ses_layoutRoot+"\"");
      }
      else
      */

      Node rElement=uLayoutManager.getRoot ();

      Hashtable supTable=cup.getStructureStylesheetUserPreferences().getParameterValues();
      Hashtable tupTable=cup.getThemeStylesheetUserPreferences().getParameterValues();

      for (Enumeration e = supTable.keys (); e.hasMoreElements ();) {
          String pName= (String) e.nextElement ();
          String pValue= (String) supTable.get (pName);
          Logger.log(Logger.DEBUG,"LayoutBean::writeContent() : setting sparam \""+pName+"\"=\""+pValue+"\".");
          uLayoutProcessor.setStylesheetParam (pName,uLayoutProcessor.createXString (pValue));
      }

      for (Enumeration e = tupTable.keys (); e.hasMoreElements ();) {
          String pName= (String) e.nextElement ();
          String pValue= (String) tupTable.get (pName);
          Logger.log(Logger.DEBUG,"LayoutBean::writeContent() : setting tparam \""+pName+"\"=\""+pValue+"\".");
          sLayoutProcessor.setStylesheetParam (pName,sLayoutProcessor.createXString (pValue));
      }



      // all the parameters are set up, fire up the filter transforms
      uLayoutProcessor.process (new XSLTInputSource (rElement),uLayoutManager.getStructureStylesheet(),new XSLTResultTarget (crb));
      uLayoutProcessor.reset();
      sLayoutProcessor.reset();
    }
    catch (Exception e)
    {
      Logger.log (Logger.ERROR, e);
    }
  }


    public void outputContent(HttpServletRequest req, HttpServletResponse res, java.io.PrintWriter out) {
        // at this point the response can be commited


    }

  /**
   * Process layout action events.
   * Events are described by the following request params:
   * uP_help_target
   * uP_about_target
   * uP_edit_target
   * uP_remove_target
   * uP_detach_target
   * @param the servlet request object
   * @param the userLayout manager object
   */
  private void processUserLayoutParameters (HttpServletRequest req, UserLayoutManager man)
  {
      String[] values;
      if((values=req.getParameterValues("uP_help_target"))!=null) {
          for(int i=0;i<values.length;i++) {
              channelManager.passLayoutEvent(values[i], new LayoutEvent(LayoutEvent.HELP_BUTTON_EVENT));
          }
      }

      if((values=req.getParameterValues("uP_about_target"))!=null) {
          for(int i=0;i<values.length;i++) {
              channelManager.passLayoutEvent(values[i], new LayoutEvent(LayoutEvent.ABOUT_BUTTON_EVENT));
          }
      }

      if((values=req.getParameterValues("uP_edit_target"))!=null) {
          for(int i=0;i<values.length;i++) {
              channelManager.passLayoutEvent(values[i], new LayoutEvent(LayoutEvent.EDIT_BUTTON_EVENT));
          }
      }

      if((values=req.getParameterValues("uP_detach_target"))!=null) {
          for(int i=0;i<values.length;i++) {
              channelManager.passLayoutEvent(values[i], new LayoutEvent(LayoutEvent.DETACH_BUTTON_EVENT));
          }
      }

      if((values=req.getParameterValues("uP_remove_target"))!=null) {
          for(int i=0;i<values.length;i++) {
              man.removeChannel(values[i]);
          }
      }
  }
}


