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

package org.jasig.portal;

import org.jasig.portal.security.IPerson;
import org.jasig.portal.utils.XSLT;

import javax.servlet.*;
import javax.servlet.jsp.*;
import javax.servlet.http.*;

import java.io.*;
import java.util.*;
import java.text.*;
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

  /**
   * Constructor initializes media manager and stylesheet sets.
   */
  public LayoutBean ()
  {
    // init the media manager
    String fs = System.getProperty ("file.separator");
    String propertiesDir = GenericPortalBean.getPortalBaseDir () + "properties" + fs;
    mediaM = new MediaManager (propertiesDir + "media.properties", propertiesDir + "mime.properties", propertiesDir + "serializer.properties");
  }

  /**
   * Gets the person object from the session.  Null is returned if
   * no person is logged in
   * NOTE: this should be changed so that IPerson object is obtained
   * from the AuthenicationService directly. -peter
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
          updb.setUserBrowserMapping(person.getID(),req.getHeader("User-Agent"),1);
          Logger.log(Logger.DEBUG,"LayoutBean::writeContent() : establishing UA mapping for user=\""+person.getID()+"\" and UA=\""+req.getHeader("User-Agent")+"\".");
          uLayoutManager = new UserLayoutManager(req,getPerson(req));
      }

      // deal with parameters that are meant for the LayoutBean
      HttpSession session = req.getSession (false);

      // determine rendering root -start
      // In general transformations will start at the userLayoutRoot node, unless
      // we are rendering something in a detach mode.
      Node rElement=null;
      boolean detachMode=false;

      // see if an old detach target exists in the servlet path
      String detachId=null;
      String servletPath=req.getServletPath();
      String upFile=servletPath.substring(servletPath.lastIndexOf('/'),servletPath.length());
      int upInd=upFile.indexOf(".uP");
      if(upInd!=-1) {
          // found a .uP specification at the end of the context path
          int detachInd=upFile.indexOf("detach_");
          if(detachInd!=-1) {
              detachId=upFile.substring(detachInd+7,upInd);
              //		  Logger.log(Logger.DEBUG,"LayoutBean::writeContent() : found detachId=\""+detachId+"\" in the .uP spec.");
          }
      }

      // see if a new detach target has been specified
      String newDetachId=req.getParameter("uP_detach_target");
      if(newDetachId!=null && (!newDetachId.equals(detachId))) {
          // see if the new detach traget is valid
          rElement=uLayoutManager.getNode(newDetachId);
          if(rElement!=null) {
              // valid new detach id was specified. need to redirect
              res.sendRedirect(req.getContextPath()+"/detach_"+newDetachId+".uP");
              return;
          }
      }

      // else ignore new id, proceed with the old detach target (or the lack of such)
      if(detachId!=null) {
          // Logger.log(Logger.DEBUG,"LayoutBean::writeContent() : uP_detach_target=\""+detachId+"\".");
          rElement=uLayoutManager.getNode (detachId);
          detachMode=true;
      }

      // if we haven't found root node so far, set it to the userLayoutRoot
      if(rElement==null) {
          rElement=uLayoutManager.getRoot ();
          detachMode=false;
      }

      String uPElement = "render.uP";
      if(detachMode) {
          Logger.log(Logger.DEBUG,"LayoutBean::writeContent() : entering detach mode for nodeId=\""+detachId+"\".");
          uPElement = "detach_" + detachId + ".uP";
      }


      // determine rendering root -end

      // process events that have to be handed directly to the userLayoutManager.
      // (examples of such events are "remove channel", "minimize channel", etc.
      //  basically things that directly affect the userLayout structure)
      processUserLayoutParameters (req, uLayoutManager);

      // call layout manager to process all user-preferences-related request parameters
      // this will update UserPreference object contained by UserLayoutManager, so that
      // appropriate attribute incorporation filters and parameter tables can be constructed.
      uLayoutManager.processUserPreferencesParameters(req);

      // obtain both structure and theme transformation stylesheet roots
      StylesheetRoot ss=XSLT.getStylesheetRoot(uLayoutManager.getStructureStylesheet());
      StylesheetRoot ts=XSLT.getStylesheetRoot(uLayoutManager.getThemeStylesheet());

      // obtain an XSLT processor
      XSLTProcessor processor = XSLTProcessorFactory.getProcessor();

      // prepare .uP element and detach flag to be passed to the stylesheets
      // Including the context path in front of uPElement is necessary for phone.com browsers to work
      XString xuPElement=processor.createXString(req.getContextPath() + "/" + uPElement);

      // set up the channelManager
      if (channelManager == null)
          channelManager = new ChannelManager (req, res,uLayoutManager,uPElement);
      else
          channelManager.setReqNRes (req, res,uPElement);

      // set the response mime type
      res.setContentType (uLayoutManager.getMimeType());

      // get a serializer appropriate for the target media
      BaseMarkupSerializer markupSerializer = mediaM.getSerializerByName(uLayoutManager.getSerializerName(), out);

      // set up the serializer
      markupSerializer.asContentHandler ();

      // initialize ChannelIncorporationFilter
      ChannelIncorporationFilter cif = new ChannelIncorporationFilter (markupSerializer, channelManager);

      // The preferences we get below are complete, that is all of the default
      // values that are sometimes null are filled out
      UserPreferences cup=uLayoutManager.getCompleteCurrentUserPreferences();

      // initialize ChannelRenderingBuffer
      ChannelRenderingBuffer crb = new ChannelRenderingBuffer (channelManager);


      // now that pipeline is set up, determine and set the stylesheet params
      processor.setStylesheetParam ("baseActionURL",xuPElement);

      Hashtable supTable=cup.getStructureStylesheetUserPreferences().getParameterValues();
      for (Enumeration e = supTable.keys (); e.hasMoreElements ();) {
          String pName= (String) e.nextElement ();
          String pValue= (String) supTable.get (pName);
          Logger.log(Logger.DEBUG,"LayoutBean::writeContent() : setting sparam \""+pName+"\"=\""+pValue+"\".");
          processor.setStylesheetParam (pName,processor.createXString (pValue));
      }


      // all the parameters are set up, fire up structure transformation
      processor.setStylesheet(ss);
      processor.setDocumentHandler(crb);

      // filter to fill in channel/folder attributes for the "structure" transformation.
      StructureAttributesIncorporationFilter saif=new StructureAttributesIncorporationFilter(processor,cup.getStructureStylesheetUserPreferences());

      // if operating in the detach mode, need wrap everything
      // in a document node and a <layout_fragment> node
      if(detachMode) {
          saif.startDocument();
          saif.startElement("layout_fragment",new org.xml.sax.helpers.AttributeListImpl());
          UtilitiesBean.node2SAX(rElement,saif);
          saif.endElement("layout_fragment");
          saif.endDocument();
      } else if(rElement.getNodeType() == Node.DOCUMENT_NODE) {
          UtilitiesBean.node2SAX(rElement,saif);
      } else {
          // as it is, this should never happen
          saif.startDocument();
          UtilitiesBean.node2SAX(rElement,saif);
          saif.endDocument();
      }


      // all channels should be rendering now
      // prepare processor for the theme transformation
      processor.reset();
      // set up of the parameters
      processor.setStylesheetParam ("baseActionURL",xuPElement);

      Hashtable tupTable=cup.getThemeStylesheetUserPreferences().getParameterValues();
      for (Enumeration e = tupTable.keys (); e.hasMoreElements ();) {
          String pName= (String) e.nextElement ();
          String pValue= (String) tupTable.get (pName);
          Logger.log(Logger.DEBUG,"LayoutBean::writeContent() : setting tparam \""+pName+"\"=\""+pValue+"\".");
          processor.setStylesheetParam (pName,processor.createXString (pValue));
      }

      processor.setStylesheet(ts);
      // initialize a filter to fill in channel attributes for the
      // "theme" (second) transformation.

      ThemeAttributesIncorporationFilter taif=new ThemeAttributesIncorporationFilter(processor,cup.getThemeStylesheetUserPreferences());
      processor.setDocumentHandler (cif);

      // fire up theme transformation
      crb.setDocumentHandler(taif);
      crb.stopBuffering();
    }
    catch (Exception e)
    {
      Logger.log (Logger.ERROR, e);
    }
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
              channelManager.passPortalEvent(values[i], new PortalEvent(PortalEvent.HELP_BUTTON_EVENT));
          }
      }

      if((values=req.getParameterValues("uP_about_target"))!=null) {
          for(int i=0;i<values.length;i++) {
              channelManager.passPortalEvent(values[i], new PortalEvent(PortalEvent.ABOUT_BUTTON_EVENT));
          }
      }

      if((values=req.getParameterValues("uP_edit_target"))!=null) {
          for(int i=0;i<values.length;i++) {
              channelManager.passPortalEvent(values[i], new PortalEvent(PortalEvent.EDIT_BUTTON_EVENT));
          }
      }

      if((values=req.getParameterValues("uP_detach_target"))!=null) {
          for(int i=0;i<values.length;i++) {
              channelManager.passPortalEvent(values[i], new PortalEvent(PortalEvent.DETACH_BUTTON_EVENT));
          }
      }

      if((values=req.getParameterValues("uP_remove_target"))!=null) {
          for(int i=0;i<values.length;i++) {
              man.removeChannel(values[i]);
          }
      }
  }
}


