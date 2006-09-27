/* Copyright 2001 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/

package  org.jasig.portal.tools;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Enumeration;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.xml.transform.sax.SAXResult;
import javax.xml.transform.sax.TransformerHandler;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jasig.portal.BrowserInfo;
import org.jasig.portal.ChannelRuntimeData;
import org.jasig.portal.ChannelSAXStreamFilter;
import org.jasig.portal.ChannelStaticData;
import org.jasig.portal.IChannel;
import org.jasig.portal.IPrivilegedChannel;
import org.jasig.portal.MediaManager;
import org.jasig.portal.PortalControlStructures;
import org.jasig.portal.PortalException;
import org.jasig.portal.PortalSessionManager;
import org.jasig.portal.StylesheetSet;
import org.jasig.portal.UPFileSpec;
import org.jasig.portal.security.IPerson;
import org.jasig.portal.security.PersonFactory;
import org.jasig.portal.serialize.BaseMarkupSerializer;
import org.jasig.portal.utils.ResourceLoader;
import org.jasig.portal.utils.SAX2BufferImpl;
import org.jasig.portal.utils.XSLT;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;

/**
 * A servlet that allows one to render an IChannel outside of the portal.
 * @author Peter Kharchenko <pkharchenko@interactivebusiness.com>
 * @version $Revision$
 */
public class ChannelServlet extends HttpServlet {
  private static final Log LOG = LogFactory.getLog(ChannelServlet.class);
  public static String detachBaseStart = "detach_";
  StylesheetSet set;
  MediaManager mediaM;
  private boolean initialized = false;
  private IChannel channel;
  private String channelName;
  private long timeOut = 10000;                 // 10 seconds is the default timeout value
  private static final String relativeSSLLocation = "ChannelServlet/ChannelServlet.ssl";

  public void init() throws ServletException {
    ServletConfig sc = this.getServletConfig();
    if (sc != null) {
      // initialize stylesheet set
      // once JNDI DB access is in place the following line can be removed
      try {
	this.set = new StylesheetSet(ResourceLoader.getResourceAsURLString(this.getClass(), relativeSSLLocation));
        String mediaPropsUrl = ResourceLoader.getResourceAsURLString(this.getClass(), "/properties/media.properties");
        this.set.setMediaProps(mediaPropsUrl);
        this.mediaM = MediaManager.getMediaManager();
      } catch (PortalException pe) {
        throw new ServletException(pe);
      }
      // determine the channel with its parameters
      String className = sc.getInitParameter("className");
      channelName = sc.getInitParameter("channelName");
      String s_timeOut = sc.getInitParameter("timeOut");
      if (s_timeOut != null) {
          this.timeOut = Long.parseLong(s_timeOut);
      }
      // instantiate channel class
      try {
        channel = (org.jasig.portal.IChannel)Class.forName(className).newInstance();
        // construct a ChannelStaticData object
        ChannelStaticData sd = new ChannelStaticData();
        sd.setChannelSubscribeId("singlet");
        sd.setTimeout(timeOut);
        // determine the IPerson object
        IPerson person = PersonFactory.createGuestPerson();
        sd.setPerson(person);
        // todo: determine and pass channel publish/subscribe parameters.
        //		    sd.setParameters (params);
        channel.setStaticData(sd);
        initialized = true;
      } catch (Exception e) {
          // some diagnostic state can be saved here
    	  LOG.error(e,e);
      }
    }
  }


  public void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
    doGet(req, res);
  }


  public void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
    if (initialized) {
      // construct runtime data object
      ChannelRuntimeData rd = new ChannelRuntimeData();
      rd.setBrowserInfo(new BrowserInfo(req));

      for (Enumeration en = req.getParameterNames(); en.hasMoreElements();) {
        String pName = (String)en.nextElement();
        if (!pName.startsWith("uP_")) {
          String[] val = (String[])req.getParameterValues(pName);
          rd.put(pName, val);
        }
      }

      try {
          rd.setUPFile(new UPFileSpec(null,UPFileSpec.RENDER_METHOD,"servletRoot","singlet",null));
      } catch (PortalException pe) {
    	  LOG.error("unable to construct a UPFile !",pe);
      }

      if (channel instanceof IPrivilegedChannel) {
        // provide as much of PCS as we can
        PortalControlStructures pcs = new PortalControlStructures();
        pcs.setHttpServletRequest(req);
        pcs.setHttpServletResponse(res);
        try {
          ((IPrivilegedChannel)channel).setPortalControlStructures(pcs);
        } catch (Exception e) {
          // channel failed to accept portal control structures
      	  LOG.error("channel failed to accept portal control structures.",e);
        }
      }
      // start rendering in a separate thread
      SAX2BufferImpl buffer = new SAX2BufferImpl();
      Worker worker = new Worker(channel, rd, buffer);
      Thread workerThread = new Thread(PortalSessionManager.getThreadGroup(), worker, "servlet renderer");
      workerThread.start();
      long startTime = System.currentTimeMillis();
      // set the mime type
      res.setContentType(mediaM.getReturnMimeType(req));
      // set up the serializer
      BaseMarkupSerializer ser = mediaM.getSerializer(mediaM.getMedia(req), res.getWriter());
      ser.asContentHandler();
      // get the framing stylesheet
      String xslURI = null;
      try
      {
        xslURI = set.getStylesheetURI(req);
      }
      catch(PortalException pe)
      {
        throw new ServletException(pe);
      }
      try {
          TransformerHandler th=XSLT.getTransformerHandler(xslURI);
          th.setResult(new SAXResult(ser));
          try {
              long wait = timeOut - System.currentTimeMillis() + startTime;
              if (wait > 0)
                  workerThread.join(wait);
          } catch (InterruptedException e) {
              // thread waiting on the worker has been interrupted
          	  LOG.error("thread waiting on the worker has been interrupted.",e);
          }
          // kill the working thread
          // yes, this is terribly crude and unsafe, but I don't see an alternative
          workerThread.stop();
          if (worker.done()) {
              if (worker.successful()) {
                  // unplug the buffer
                  try {
                      org.xml.sax.helpers.AttributesImpl atl = new org.xml.sax.helpers.AttributesImpl();
                      atl.addAttribute("","name","name", "CDATA", channelName);
                      // add other attributes: hasHelp, hasAbout, hasEdit
                      th.startDocument();
                      th.startElement("","channel","channel", atl);
                      ChannelSAXStreamFilter custodian = new ChannelSAXStreamFilter(th);
                      custodian.setParent(buffer);
                      buffer.stopBuffering(); buffer.outputBuffer();
                      th.endElement("","channel","channel");
                      th.endDocument();
                  } catch (SAXException e) {
                      // worst case scenario: partial content output :(
                  	  LOG.error("error during unbuffering",e);
                  }
              } else {
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
          } else {
              // rendering has timed out
              showErrorMessage("channel rendering timed out", res);
          }
      } catch (Exception e) {
          // some exception occurred during processor initialization or framing transformation
          showErrorMessage("Exception occurred during the framing transformation or XSLT processor initialization", res);
      }
    } else
      showErrorMessage("failed to initialize", res);
  }


  private void showErrorMessage(String message, HttpServletResponse res) {
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
	private ContentHandler contentHandler;
	private Exception exc = null;


    public Worker(IChannel ch, ChannelRuntimeData runtimeData, ContentHandler dh) {
      this.channel = ch;
      this.contentHandler = dh;
      this.rd = runtimeData;
    }

    public void run() {
      successful = false;
      done = false;
      try {
        if (rd != null)
          channel.setRuntimeData(rd);
        channel.renderXML(contentHandler);
        successful = true;
      } catch (Exception e) {
        this.exc = e;
      }
      done = true;
    }

    public boolean successful() {
      return  this.successful;
    }

    public boolean done() {
      return  this.done;
    }

    public Exception getException() {
      return  exc;
    }
  }


  protected IPerson getPerson(HttpServletRequest req) {
    HttpSession session = req.getSession(false);
    IPerson person = (IPerson)session.getAttribute("up_person");
    return  person;
  }
}



