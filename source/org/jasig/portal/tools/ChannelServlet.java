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


package  org.jasig.portal.tools;

import  java.io.*;
import  java.util.*;
import  java.lang.SecurityManager;
import  javax.naming.Context;
import  javax.naming.InitialContext;
import  javax.servlet.*;
import  javax.servlet.http.*;
import  java.security.AccessController;
import  org.jasig.portal.services.LogService;
import  org.jasig.portal.jndi.JNDIManager;
import  org.jasig.portal.StylesheetSet;
import  org.jasig.portal.MediaManager;
import  org.jasig.portal.IChannel;
import  org.jasig.portal.IPrivilegedChannel;
import  org.jasig.portal.ChannelRuntimeData;
import  org.jasig.portal.ChannelStaticData;
import  org.jasig.portal.PortalControlStructures;
import  org.jasig.portal.BrowserInfo;
import  org.jasig.portal.SAXBufferImpl;
import  org.jasig.portal.GenericPortalBean;
import  org.jasig.portal.security.IPerson;
import  org.jasig.portal.ChannelSAXStreamFilter;
import  org.jasig.portal.RdbmServices;
import  org.jasig.portal.utils.XSLT;
import  org.xml.sax.*;
import  org.apache.xalan.xslt.*;
import  org.apache.xml.serialize.*;


/**
 * A servlet that allows one to render an IChannel outside of the portal.
 * @author Peter Kharchenko <pkharchenko@interactivebusiness.com>
 * @version: $Revision$
 */
public class ChannelServlet extends HttpServlet {
  private boolean baseDirSet = false;
  public static String renderBase = "render.uP";
  public static String detachBaseStart = "detach_";
  private static int sizeLimit = 3000000;       // Should be channel specific
  StylesheetSet set;
  MediaManager mediaM;
  private boolean initialized = false;
  private String baseDir;
  private IChannel channel;
  private String channelName;
  private boolean hasEdit = false;
  private boolean hasAbout = false;
  private boolean hasHelp = false;
  private long timeOut = 10000;                 // 10 seconds is the default timeout value
  private static final String fs = File.separator;
  private static final String relativeSSLLocation = "webpages" + fs + "stylesheets" + fs + "org" + fs + "jasig" + fs + "portal"
      + fs + "tools" + fs + "ChannelServlet" + fs + "ChannelServlet.ssl";

  /**
   * put your documentation comment here
   * @exception ServletException
   */
  public void init () throws ServletException {
    ServletConfig sc = this.getServletConfig();
    if (sc != null) {
      // determine base directory
      baseDir = sc.getInitParameter("baseDir");
      if (baseDir != null) {
        File portalBaseDir = new java.io.File(baseDir);
        if (portalBaseDir.exists()) {
          // initialize stylesheet set
          GenericPortalBean.setPortalBaseDir(baseDir);
          // once JNDI DB access is in place the following line can be removed
          GenericPortalBean.setUserLayoutStore(RdbmServices.getUserLayoutStoreImpl());
          this.set = new StylesheetSet(baseDir + fs + relativeSSLLocation);
          String propertiesDir = baseDir + fs + "properties" + fs;
          this.set.setMediaProps(propertiesDir + "media.properties");
          this.mediaM = new MediaManager(propertiesDir + "media.properties", propertiesDir + "mime.properties", propertiesDir
              + "serializer.properties");
          // determine the channel with its parameters
          String className = sc.getInitParameter("className");
          channelName = sc.getInitParameter("channelName");
          hasEdit = Boolean.getBoolean(sc.getInitParameter("hasEdit"));
          hasHelp = Boolean.getBoolean(sc.getInitParameter("hasHelp"));
          hasAbout = Boolean.getBoolean(sc.getInitParameter("hasAbout"));
          String s_timeOut = sc.getInitParameter("timeOut");
          if (s_timeOut != null)
            this.timeOut = Long.parseLong(s_timeOut);
          // instantiate channel class
          try {
            channel = (org.jasig.portal.IChannel)Class.forName(className).newInstance();
            // construct a ChannelStaticData object
            ChannelStaticData sd = new ChannelStaticData();
            sd.setChannelID("singlet");
            sd.setTimeout(timeOut);
            // determine the IPerson object
            int guestUserId = 1;
            IPerson person = new org.jasig.portal.security.provider.PersonImpl();
            person.setID(guestUserId);
            person.setFullName("Guest");
            sd.setPerson(person);
            // todo: determine and pass channel publish/subscribe parameters.
            //		    sd.setParameters (params);
            channel.setStaticData(sd);
            initialized = true;
          } catch (Exception e) {
            // some diagnostic state can be saved here
            e.printStackTrace();
          }
        } 
        else 
          System.out.println("base diretctory does not exist");
      } 
      else 
        System.out.println("base directory is null");
    }
  }

  /**
   * put your documentation comment here
   * @param req
   * @param res
   * @exception ServletException, IOException
   */
  public void doPost (HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
    doGet(req, res);
  }

  /**
   * put your documentation comment here
   * @param req
   * @param res
   * @exception ServletException, IOException
   */
  public void doGet (HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
    if (initialized) {
      // construct runtime data object
      ChannelRuntimeData rd = new ChannelRuntimeData();
      for (Enumeration en = req.getParameterNames(); en.hasMoreElements();) {
        String pName = (String)en.nextElement();
        if (!pName.startsWith("uP_")) {
          String[] val = (String[])req.getParameterValues(pName);
          rd.put(pName, val);
        }
      }
      rd.setHttpRequest(req);
      rd.setBrowserInfo(new BrowserInfo(req));
      rd.setBaseActionURL(req.getRequestURI());
      if (channel instanceof IPrivilegedChannel) {
        // provide as much of PCS as we can
        PortalControlStructures pcs = new PortalControlStructures();
        pcs.setHttpServletRequest(req);
        pcs.setHttpServletResponse(res);
        try {
          ((IPrivilegedChannel)channel).setPortalControlStructures(pcs);
        } catch (Exception e) {
          // channel failed to accept portal control structures
          System.out.println("channel failed to accept portal control structures.");
        }
      }
      // start rendering in a separate thread
      SAXBufferImpl buffer = new SAXBufferImpl();
      Worker worker = new Worker(channel, rd, buffer);
      Thread workerThread = new Thread(worker);
      workerThread.start();
      long startTime = System.currentTimeMillis();
      // set the mime type
      res.setContentType(mediaM.getReturnMimeType(req));
      // set up the serializer
      BaseMarkupSerializer ser = mediaM.getSerializer(mediaM.getMedia(req), res.getWriter());
      ser.asContentHandler();
      // get the framing stylesheet
      String xslURI = set.getStylesheetURI(req);
      try {
        XSLTProcessor processor = XSLTProcessorFactory.getProcessor();
        processor.setDocumentHandler(ser);
        processor.setStylesheet(XSLT.getStylesheetRoot(xslURI));
        try {
          long wait = timeOut - System.currentTimeMillis() + startTime;
          if (wait > 0)
            workerThread.join(wait);
        } catch (InterruptedException e) {
          // thread waiting on the worker has been interrupted
          System.out.println("thread waiting on the worker has been interrupted.");
        }
        // kill the working thread
        // yes, this is terribly crude and unsafe, but I don't see an alternative
        workerThread.stop();
        if (worker.done()) {
          if (worker.successful()) {
            // unplug the buffer
            try {
              processor.startDocument();
              org.xml.sax.helpers.AttributeListImpl atl = new org.xml.sax.helpers.AttributeListImpl();
              atl.addAttribute("name", "CDATA", channelName);
              // add other attributes: hasHelp, hasAbout, hasEdit
              processor.startElement("channel", atl);
              ChannelSAXStreamFilter custodian = new ChannelSAXStreamFilter(processor);
              buffer.setDocumentHandler(custodian);
              buffer.stopBuffering();
              processor.endElement("channel");
              processor.endDocument();
            } catch (SAXException e) {
              // worst case scenario: partial content output :(
              System.out.println("error during unbuffering");
              e.printStackTrace();
            }
          } 
          else {
            // rendering was not successful
            Exception e;
            if ((e = worker.getException()) != null) {
              // channel generated an exception ... this should be handled
              StringWriter sw = new StringWriter();
              e.printStackTrace(new PrintWriter(sw));
              sw.flush();
              showErrorMessage("channel generated exception " + e.toString() + ". Stack trace: " + sw.toString(), res);
            }
            // should never get there, unless thread.stop() has seriously messed things up for the worker thread.
          }
        } 
        else {
          // rendering has timed out
          showErrorMessage("channel rendering timed out", res);
        }
      } catch (Exception e) {
        // some exception occurred during processor initialization or framing transformation
        showErrorMessage("Exception occurred during the framing transformation or XSLT processor initialization", res);
      }
    } 
    else 
      showErrorMessage("failed to initialize", res);
  }

  /**
   * put your documentation comment here
   * @param message
   * @param res
   */
  private void showErrorMessage (String message, HttpServletResponse res) {
    res.setContentType("text/html");
    try {
      PrintWriter out = res.getWriter();
      out.println("<html>");
      out.println("<body>");
      if (channelName != null)
        out.println("<h1>" + channelName + "</h1>"); 
      else 
        out.println("<h1>" + getServletConfig().getServletName() + "</h1>");
      out.println("<h3>Error !</h3>");
      out.println("<p>" + message + "<p>");
      out.println("</body></html>");
    } catch (Exception e) {
    // unable to get a writer from the HttpServletResponse object
    }
  }

  // the object that does the actual rendering
  protected class Worker
      implements Runnable {
    private boolean successful;
    private boolean done;
    private IChannel channel;
    private ChannelRuntimeData rd;
    private DocumentHandler documentHandler;
    private Exception exc = null;

    /**
     * put your documentation comment here
     * @param     IChannel ch
     * @param     ChannelRuntimeData runtimeData
     * @param     DocumentHandler dh
     */
    public Worker (IChannel ch, ChannelRuntimeData runtimeData, DocumentHandler dh) {
      this.channel = ch;
      this.documentHandler = dh;
      this.rd = runtimeData;
    }

    /**
     * put your documentation comment here
     */
    public void run () {
      successful = false;
      done = false;
      try {
        if (rd != null)
          channel.setRuntimeData(rd);
        channel.renderXML(documentHandler);
        successful = true;
      } catch (Exception e) {
        this.exc = e;
      }
      done = true;
    }

    /**
     * put your documentation comment here
     * @return 
     */
    public boolean successful () {
      return  this.successful;
    }

    /**
     * put your documentation comment here
     * @return 
     */
    public boolean done () {
      return  this.done;
    }

    /**
     * put your documentation comment here
     * @return 
     */
    public Exception getException () {
      return  exc;
    }
  }

  /**
   * put your documentation comment here
   * @param req
   * @return 
   */
  protected IPerson getPerson (HttpServletRequest req) {
    HttpSession session = req.getSession(false);
    IPerson person = (IPerson)session.getAttribute("up_person");
    return  person;
  }
}



