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


package  org.jasig.portal;

import java.io.Serializable;
import java.io.PrintWriter;
import java.io.IOException;
import java.io.BufferedReader;
import java.util.Enumeration;
import java.util.Map;
import java.util.Hashtable;
import java.util.Locale;
import java.net.URL;
import java.lang.SecurityManager;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.servlet.*;
import javax.servlet.http.*;
import java.security.AccessController;
import org.jasig.portal.services.ExternalServices;
import org.jasig.portal.services.LogService;
import org.jasig.portal.jndi.JNDIManager;
import com.oreilly.servlet.multipart.MultipartParser;
import com.oreilly.servlet.multipart.FilePart;
import com.oreilly.servlet.multipart.ParamPart;


/**
 * This is an entry point into the uPortal.
 * @author Peter Kharchenko <pkharchenko@interactivebusiness.com>
 * @version: $Revision$
 */
public class PortalSessionManager extends HttpServlet {
  public static final String renderBase = "render.uP";
  public static final String detachBaseStart = "detach_";
  private static final int sizeLimit = PropertiesManager.getPropertyAsInt("org.jasig.portal.PortalSessionManager.File_upload_max_size");
  private static boolean initialized = false;
  private static ServletContext servletContext = null;

  static {
    LogService.instance().log(LogService.INFO, "uPortal started");
  }

  /**
   * Initialize the PortalSessionManager servlet
   * @throws ServletException
   */
  public void init () throws ServletException {
    if(!initialized)
    {
      // Retrieve the servlet configuration object from the servlet container
      // and make sure it's available
      ServletConfig sc = getServletConfig();
      if (sc == null) {
        throw new ServletException("PortalSessionManager.init(): ServletConfig object was returned as null");
      }
      servletContext = sc.getServletContext();

      JNDIManager.initializePortalContext();

      // Start any portal services configured in services.xml
      try {
        ExternalServices.startServices();
      } catch (Exception ex) {
        LogService.instance().log(LogService.ERROR, ex);
        throw new ServletException ("Failed to start external portal services.");
      }

      // Flag that the portal has been initialized
      initialized = true;

      // Get the SAX implementation
      if (System.getProperty("org.xml.sax.driver") == null)
        System.setProperty("org.xml.sax.driver", PropertiesManager.getProperty("org.xml.sax.driver"));
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
    // Disable page caching
    res.setHeader("pragma", "no-cache");
    res.setHeader("Cache-Control", "no-cache, max-age=0, must-revalidate");
    res.setHeader("uPortal-version", "uPortal_2-0-pre-release-2002-01-14");
    res.setDateHeader("Expires", 0);

    // forwarding
    ServletContext sc = this.getServletContext();
    HttpSession session = req.getSession();
    if (session != null) {
      // LogService.instance().log(LogService.DEBUG, "PortalSessionManager::doGet() : request path \""+req.getServletPath()+"\".");
      String redirectBase = null;
      RequestParamWrapper myReq = new RequestParamWrapper(req);
      myReq.setBaseRequest(req);
      if ((redirectBase = this.doRedirect(myReq)) != null) {
        // cache request
        sc.setAttribute("oreqp_" + session.getId(), myReq);

        int index = myReq.requestURI.indexOf("worker");
        if ( index != -1) {
          String worker = myReq.requestURI.substring(index+7);
          worker = worker.substring(0, worker.indexOf('/'));
          workerTask (req, res, worker);
        } else {
          // initial request, requires forwarding
          session.setAttribute("forwarded", new Boolean(true));
          // forward
          // LogService.instance().log(LogService.DEBUG,"PortalSessionManager::doGet() : caching request, sending redirect");
          //this.getServletContext().getRequestDispatcher("/render.uP").forward(req,res);
          res.sendRedirect(req.getContextPath() + redirectBase);
        }
      }
      else {
        // delete old request
        Boolean forwarded = (Boolean)session.getAttribute("forwarded");
        if (forwarded != null)
        {
          session.removeAttribute("forwarded");
        }
        UserInstance userInstance = null;
        try
        {
          // Retrieve the user's UserInstance object
          userInstance = UserInstanceManager.getUserInstance(req);
        }
        catch(Exception e)
        {
          // NOTE: Should probably be forwarded to error page if the user instance could not be properly retrieved.
          LogService.instance().log(LogService.ERROR, e);
        }
        /**
        UserInstance layout = (UserInstance)session.getAttribute("UserInstance");
        if (layout == null) {
          layout = UserInstanceFactory.getUserInstance(myReq);
          session.setAttribute("UserInstance", layout);
          // LogService.instance().log(LogService.DEBUG,"PortalSessionManager;:doGet() : instantiating new UserInstance");
        }
        **/
        RequestParamWrapper oreqp = null;
        if (forwarded != null && forwarded.booleanValue())
        {
          oreqp = (RequestParamWrapper)sc.getAttribute("oreqp_" + session.getId());
        }
        if (oreqp != null)
        {
          oreqp.setBaseRequest(req);
          userInstance.writeContent(oreqp, res, res.getWriter());
        }
        else
        {
          userInstance.writeContent(myReq, res, res.getWriter());
        }
      }
    }
    else {
      res.setContentType("text/html");
      PrintWriter out = res.getWriter();
      out.println("<html>");
      out.println("<body>");
      out.println("<h1>" + getServletConfig().getServletName() + "</h1>");
      out.println("Session object is null !??");
      out.println("</body></html>");
    }
  }

  /**
   * This function determines if a given request needs to be redirected
   * @param req
   * @return
   */
  private String doRedirect (HttpServletRequest req) {
    HttpSession session = req.getSession();
    String redirectBase = "/" + renderBase;                     // default value
    if (session != null) {
      Boolean forwarded = (Boolean)session.getAttribute("forwarded");
      if (forwarded != null && forwarded.booleanValue())
        return  null;
      String servletPath = req.getServletPath();
      String uPFile = servletPath.substring(servletPath.lastIndexOf('/'), servletPath.length());
      // redirect everything except for render.uP and detach.uP with no parameters
      boolean detach_mode = uPFile.startsWith("/" + detachBaseStart);
      if (!req.getParameterNames().hasMoreElements()) {
        if (servletPath.equals("/" + renderBase) || servletPath.startsWith("/" + detachBaseStart))
          return  null;
      }
      // the only exception to redirect is the detach
      if (detach_mode)
        redirectBase = uPFile;
    }
    // redirect by default
    return '/' + renderBase;
  }

  /**
   * Workers perform tasks other than channel rendering.  An example is downloading
   * a MIME file from the portal server.
   * To add a new worker:
   *  1. Specify the worker name in the static final declarations.  This name will
   *     be used to construct the URL to invoke the worker.
   *  2. Call the worker method in the Channel manager from the workerTask() method.
   */
  public static final String FILE_DOWNLOAD = "download";

  protected void workerTask (HttpServletRequest req, HttpServletResponse res, String worker) throws ServletException, IOException {
    try
    {
      HttpSession session = req.getSession();

      UserInstance userInstance = null;
      try {
        // Retrieve the user's UserInstance object
        userInstance = UserInstanceManager.getUserInstance(req);
      } catch (Exception e) {
        LogService.instance().log(LogService.ERROR, e);
      }

      if (userInstance != null) {
        ChannelManager channelManager = userInstance.channelManager;
        if (channelManager != null) {
          channelManager.setReqNRes(req, res, "PortalWorker");
          if (worker.equals(FILE_DOWNLOAD))
            channelManager.getMimeContent(req, res);
          // else other workers.  Enter a value for each worker first.
        }
      }
    }  catch (Exception e) {
      LogService.instance().log(LogService.ERROR, "PortalSessionManager::workerTask(): Worker " + worker +
                                                  " task failed.", e);
    }
  }

  /**
   * Gets a URL associated with the named resource.
   * Call this to access files with paths relative to the
   * document root.  Paths should begin with a "/".
   * @param resource relative to the document root
   * @return a URL associated with the named resource or null if the URL isn't accessible
   * @throws java.net.MalformedURLException
   */
  public static URL getResourceAsURL(String resource) {
    //Make sure resource string starts with a "/"
    if (!resource.startsWith("/"))
      resource = "/" + resource;

    URL url = null;

    try {
      url = servletContext.getResource(resource);
    } catch (java.net.MalformedURLException murle) {
      // if the URL is bad, just return null
    }
    return url;
  }

  /**
   * Gets an input stream associated with the named resource.
   * Call this to access files with paths relative to the
   * document root. Paths should begin with a "/".
   * @param resource relative to the document root
   * @return an input stream assosiated with the named resource
   */
  public static java.io.InputStream getResourceAsStream(String resource) {
    //Make sure resource string starts with a "/"
    if (!resource.startsWith("/"))
      resource = "/" + resource;

    return servletContext.getResourceAsStream(resource);
  }


  /**
   * RequestParamWrapper persists various information
   * from the source request.
   * Once given a "base request", it substitutes corresponding
   * base information with that acquired from the source.
   *
   * The following information is extracted from the source:
   * request parameters
   * path info
   * path translated
   * context path
   * query string
   * servlet path
   * request uri
   */
  public class RequestParamWrapper
      implements HttpServletRequest, Serializable {
    // the request being wrapped
    private HttpServletRequest req;
    private Hashtable parameters;
    private String pathInfo;
    private String pathTranslated;
    private String contextPath;
    private String queryString;
    private String servletPath;
    private String requestURI;

    /**
     * put your documentation comment here
     * @param     HttpServletRequest source
     */
    public RequestParamWrapper (HttpServletRequest source) {
      // leech all of the information from the source request
      parameters = new Hashtable();
      String contentType = source.getContentType();
      if (contentType != null && contentType.startsWith("multipart/form-data")) {
        com.oreilly.servlet.multipart.Part attachmentPart;
        try {
          MultipartParser multi = new MultipartParser(source, sizeLimit, true, true);
          while ((attachmentPart = multi.readNextPart()) != null) {
            String partName = attachmentPart.getName();

            if (attachmentPart.isParam()) {
              ParamPart parameterPart = (ParamPart)attachmentPart;
              String paramValue = parameterPart.getStringValue();
              if (parameters.containsKey(partName)) {

                /* Assume they meant a multivalued tag, like a checkbox */
                String[] oldValueArray = (String[])parameters.get(partName);
                String[] valueArray = new String[oldValueArray.length + 1];
                for (int i = 0; i < oldValueArray.length; i++) {
                  valueArray[i] = oldValueArray[i];
                }
                valueArray[oldValueArray.length] = paramValue;
                parameters.put(partName, valueArray);
              }
              else {
                String[] valueArray = new String[1];
                valueArray[0] = paramValue;
                parameters.put(partName, valueArray);
              }
            }
            else if (attachmentPart.isFile()) {
              FilePart filePart = (FilePart)attachmentPart;
              String filename = filePart.getFileName();

              if (filename != null) {
                MultipartDataSource fileUpload = new MultipartDataSource(filePart);

                if (parameters.containsKey(partName)) {
                  MultipartDataSource[] oldValueArray = (MultipartDataSource[])parameters.get(partName);
                  MultipartDataSource[] valueArray = new MultipartDataSource[oldValueArray.length + 1];
                  for (int i = 0; i < oldValueArray.length; i++) {
                    valueArray[i] = oldValueArray[i];
                  }
                  valueArray[oldValueArray.length] = fileUpload;
                  parameters.put(partName, valueArray);
                }
                else {
                  MultipartDataSource[] valueArray = new MultipartDataSource[1];
                  valueArray[0] = fileUpload;
                  parameters.put(partName, valueArray);
                }
              }
            }
          }
        } catch (Exception e) {
          LogService.instance().log(LogService.ERROR, e);
        }
      }
      Enumeration en = source.getParameterNames();
      if (en != null) {
        while (en.hasMoreElements()) {
          String pName = (String)en.nextElement();
          parameters.put(pName, source.getParameterValues(pName));
        }
      }
      pathInfo = source.getPathInfo();
      pathTranslated = source.getPathTranslated();
      contextPath = source.getContextPath();
      queryString = source.getQueryString();
      servletPath = source.getServletPath();
      requestURI = source.getRequestURI();
    }

    /**
     * put your documentation comment here
     * @param base
     */
    public void setBaseRequest (HttpServletRequest base) {
      this.req = base;
    }

    /**
     * Overloaded method
     * @param name
     * @return
     */
    public String getParameter (String name) {
      String[] value_array = this.getParameterValues(name);
      if ((value_array != null) && (value_array.length > 0))
        return  value_array[0];
      else
        return  null;
    }

    /**
     * Overloaded method
     * @return
     */
    public Enumeration getParameterNames () {
      return  this.parameters.keys();
    }

    /**
     * Return a String[] for this parameter
     * @param parameter name
     * @result String[] if parameter is not an Object[]
     */
    public String[] getParameterValues (String name) {
      Object[] pars = (Object[])this.parameters.get(name);
      if (pars instanceof String[]) {
        return  (String[])this.parameters.get(name);
      }
      else {
        return  null;
      }
    }

    /**
     * Return the Object represented by this parameter name
     * @param parameter name
     * @result Object
     */
    public Object[] getObjectParameterValues (String name) {
      return  (Object[])this.parameters.get(name);
    }

    public String getPathInfo () {
      return  pathInfo;
    }

    public String getPathTranslated () {
      return  pathTranslated;
    }

    public String getContextPath () {
      return  contextPath;
    }

    public String getQueryString () {
      return  queryString;
    }

    public String getServletPath () {
      return  servletPath;
    }

    public String getRequestURI () {
      return  requestURI;
    }

    // pass through methods

    // This method is new in Servlet 2.3.
    // java.lang.reflect methods are used here in an effort
    // to be compatible with older servlet APIs.
    public StringBuffer getRequestURL () {
      try {
        java.lang.reflect.Method m = req.getClass().getMethod("getRequestURL", null);
        return (StringBuffer)m.invoke(req, null);
      } catch (Exception e) {
        return null;
      }
    }

    // This method is new in Servlet 2.3.
    // java.lang.reflect methods are used here in an effort
    // to be compatible with older servlet APIs.
    public Map getParameterMap() {
      try {
        java.lang.reflect.Method m = req.getClass().getMethod("getParameterMap", null);
        return (Map)m.invoke(req, null);
      } catch (Exception e) {
        return null;
      }
    }

    // This method is new in Servlet 2.3.
    // java.lang.reflect methods are used here in an effort
    // to be compatible with older servlet APIs.
    public void setCharacterEncoding(String env) throws java.io.UnsupportedEncodingException {
      try {
        Class[] paramTypes = new Class[] { new String().getClass() };
        java.lang.reflect.Method m = req.getClass().getMethod("setCharacterEncoding", paramTypes);
        Object[] args = new Object[] { env };
        m.invoke(req, args);
      } catch (Exception e) {
      }
    }

    public String getAuthType () {
      return  req.getAuthType();
    }

    public Cookie[] getCookies () {
      return  req.getCookies();
    }

    public long getDateHeader (String name) {
      return  req.getDateHeader(name);
    }

    public String getHeader (String name) {
      return  req.getHeader(name);
    }

    public Enumeration getHeaders (String name) {
      return  req.getHeaders(name);
    }

    public Enumeration getHeaderNames () {
      return  req.getHeaderNames();
    }

    public int getIntHeader (String name) {
      return  req.getIntHeader(name);
    }

    public String getMethod () {
      return  req.getMethod();
    }

    public String getRemoteUser () {
      return  req.getRemoteUser();
    }

    public boolean isUserInRole (String role) {
      return  req.isUserInRole(role);
    }

    public java.security.Principal getUserPrincipal () {
      return  req.getUserPrincipal();
    }

    public String getRequestedSessionId () {
      return  req.getRequestedSessionId();
    }

    public HttpSession getSession (boolean create) {
      return  req.getSession(create);
    }

    public HttpSession getSession () {
      return  req.getSession();
    }

    public boolean isRequestedSessionIdValid () {
      return  req.isRequestedSessionIdValid();
    }

    public boolean isRequestedSessionIdFromCookie () {
      return  req.isRequestedSessionIdFromCookie();
    }

    public boolean isRequestedSessionIdFromURL () {
      return  req.isRequestedSessionIdFromURL();
    }

    public boolean isRequestedSessionIdFromUrl () {
      return  req.isRequestedSessionIdFromURL();
    }

    public Object getAttribute (String name) {
      return  req.getAttribute(name);
    }

    public Enumeration getAttributeNames () {
      return  req.getAttributeNames();
    }

    public String getCharacterEncoding () {
      return  req.getCharacterEncoding();
    }

    public int getContentLength () {
      return  req.getContentLength();
    }

    public String getContentType () {
      return  req.getContentType();
    }

    public ServletInputStream getInputStream () throws IOException {
      return  req.getInputStream();
    }

    public String getProtocol () {
      return  req.getProtocol();
    }

    public String getScheme () {
      return  req.getScheme();
    }

    public String getServerName () {
      return  req.getServerName();
    }

    public int getServerPort () {
      return  req.getServerPort();
    }

    public BufferedReader getReader () throws IOException {
      return  req.getReader();
    }

    public String getRemoteAddr () {
      return  req.getRemoteAddr();
    }

    public String getRemoteHost () {
      return  req.getRemoteHost();
    }

    public void setAttribute (String name, Object o) {
      req.setAttribute(name, o);
    }

    public void removeAttribute (String name) {
      req.removeAttribute(name);
    }

    public Locale getLocale () {
      return  req.getLocale();
    }

    public Enumeration getLocales () {
      return  req.getLocales();
    }

    public boolean isSecure () {
      return  req.isSecure();
    }

    public RequestDispatcher getRequestDispatcher (String path) {
      return  req.getRequestDispatcher(path);
    }

    public String getRealPath (String path) {
      throw new RuntimeException("HttpServletRequest.getRealPath(String path) is deprectated!  Use ServletContext.getRealPath(String path) instead.");
    }
  }
}



