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


package  org.jasig.portal;

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
import  com.oreilly.servlet.multipart.MultipartParser;
import  com.oreilly.servlet.multipart.FilePart;
import  com.oreilly.servlet.multipart.ParamPart;


/**
 * This is an entry point into the uPortal.
 * @author Peter Kharchenko <pkharchenko@interactivebusiness.com>
 * @version: $Revision$
 */
public class PortalSessionManager extends HttpServlet {
  private boolean m_userLayoutStoreSet = false;
  public static String renderBase = "render.uP";
  public static String detachBaseStart = "detach_";
  private static int sizeLimit = 3000000;       // Should be channel specific
  private static boolean initialized = false;

  /**
   * Initialize the PortalSessionManager servlet
   * @exception ServletException
   */
  public void init () throws ServletException {
    if(!initialized)
    {
      // Retrieve the servlet configuration object from the servlet container
      ServletConfig sc = getServletConfig();
      // Make sure the ServletConfig object is available
      if (sc == null) {
        throw  new ServletException("PortalSessionManager.init(): ServletConfig object was returned as null");
      }
      // Get the portal base directory
      String sPortalBaseDir = sc.getInitParameter("portalBaseDir");
      // Make sure the directory is a properly formatted string
      sPortalBaseDir.replace('/', File.separatorChar);
      sPortalBaseDir.replace('\\', File.separatorChar);
      // Add a seperator on the end of the path if it's missing
      if (!sPortalBaseDir.endsWith(File.separator)) {
        sPortalBaseDir += File.separator;
      }
      // Make sure the portal base directory is properly set
      if (sPortalBaseDir == null) {
        throw  new ServletException("PortalSessionManager.init(): portalBaseDir not found, check web.xml file");
      }
      // Try to create a file pointing to the portal base directory
      File portalBaseDir = new java.io.File(sPortalBaseDir);
      // Make sure the directory exists
      if (!portalBaseDir.exists()) {
        throw  new ServletException("PortalSessionManager.init(): Portal base directory " + sPortalBaseDir + " does not exist");
      }
      // Set the portal base directory
      GenericPortalBean.setPortalBaseDir(sPortalBaseDir);
      JNDIManager.initializePortalContext();
      // Flag that the portal has been initialized
      initialized = true;
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
    if (!m_userLayoutStoreSet) {
      try {
        // Should obtain implementation in a better way!
        GenericPortalBean.setUserLayoutStore(RdbmServices.getUserLayoutStoreImpl());
      } catch (Exception e) {
        throw  new ServletException(e);
      }
      m_userLayoutStoreSet = true;
    }
    // forwarding
    ServletContext sc = this.getServletContext();
    HttpSession session = req.getSession();
    if (session != null) {
      //		Logger.log(Logger.DEBUG,"PortalSessionManager::doGet() : request path \""+req.getServletPath()+"\".");
      String redirectBase = null;
      RequestParamWrapper myReq = new RequestParamWrapper(req);
      myReq.setBaseRequest(req);
      if ((redirectBase = this.doRedirect(myReq)) != null) {
        // cache request
        sc.setAttribute("oreqp_" + session.getId(), myReq);
        // initial request, requeres forwarding
        session.setAttribute("forwarded", new Boolean(true));
        // forward
        //		    Logger.log(Logger.DEBUG,"PortalSessionManager::doGet() : caching request, sending redirect");
        //this.getServletContext().getRequestDispatcher("/render.uP").forward(req,res);
        res.sendRedirect(req.getContextPath() + redirectBase);
      } 
      else {
        // delete old request
        Boolean forwarded = (Boolean)session.getAttribute("forwarded");
        if (forwarded != null)
          session.removeAttribute("forwarded");
        // proceed with rendering
        //		    Logger.log(Logger.DEBUG,"PortalSessionManager::doGet() : processing redirected (clean) request");
        // look if the LayoutBean object is already in the session, otherwise
        // make a new one
        LayoutBean layout = (LayoutBean)session.getAttribute("LayoutBean");
        if (layout == null) {
          layout = new LayoutBean();
          session.setAttribute("LayoutBean", layout);
          //			Logger.log(Logger.DEBUG,"PortalSessionManager;:doGet() : instantiating new LayoutBean");
        }
        RequestParamWrapper oreqp = null;
        if (forwarded != null && forwarded.booleanValue())
          oreqp = (RequestParamWrapper)sc.getAttribute("oreqp_" + session.getId());
        if (oreqp != null) {
          oreqp.setBaseRequest(req);
          layout.writeContent(oreqp, res, res.getWriter());
        } 
        else {
          layout.writeContent(myReq, res, res.getWriter());
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
    return  "/" + renderBase;
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
              MultipartDataSource fileUpload = new MultipartDataSource(filePart);
              ;
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
        } catch (Exception e) {
          Logger.log(Logger.ERROR, e);
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

    /**
     * put your documentation comment here
     * @return 
     */
    public String getPathInfo () {
      return  pathInfo;
    }

    /**
     * put your documentation comment here
     * @return 
     */
    public String getPathTranslated () {
      return  pathTranslated;
    }

    /**
     * put your documentation comment here
     * @return 
     */
    public String getContextPath () {
      return  contextPath;
    }

    /**
     * put your documentation comment here
     * @return 
     */
    public String getQueryString () {
      return  queryString;
    }

    /**
     * put your documentation comment here
     * @return 
     */
    public String getServletPath () {
      return  servletPath;
    }

    /**
     * put your documentation comment here
     * @return 
     */
    public String getRequestURI () {
      return  requestURI;
    }

    // pass through methods
    public String getAuthType () {
      return  req.getAuthType();
    }

    /**
     * put your documentation comment here
     * @return 
     */
    public Cookie[] getCookies () {
      return  req.getCookies();
    }

    /**
     * put your documentation comment here
     * @param name
     * @return 
     */
    public long getDateHeader (String name) {
      return  req.getDateHeader(name);
    }

    /**
     * put your documentation comment here
     * @param name
     * @return 
     */
    public String getHeader (String name) {
      return  req.getHeader(name);
    }

    /**
     * put your documentation comment here
     * @param name
     * @return 
     */
    public Enumeration getHeaders (String name) {
      return  req.getHeaders(name);
    }

    /**
     * put your documentation comment here
     * @return 
     */
    public Enumeration getHeaderNames () {
      return  req.getHeaderNames();
    }

    /**
     * put your documentation comment here
     * @param name
     * @return 
     */
    public int getIntHeader (String name) {
      return  req.getIntHeader(name);
    }

    /**
     * put your documentation comment here
     * @return 
     */
    public String getMethod () {
      return  req.getMethod();
    }

    /**
     * put your documentation comment here
     * @return 
     */
    public String getRemoteUser () {
      return  req.getRemoteUser();
    }

    /**
     * put your documentation comment here
     * @param role
     * @return 
     */
    public boolean isUserInRole (String role) {
      return  req.isUserInRole(role);
    }

    /**
     * put your documentation comment here
     * @return 
     */
    public java.security.Principal getUserPrincipal () {
      return  req.getUserPrincipal();
    }

    /**
     * put your documentation comment here
     * @return 
     */
    public String getRequestedSessionId () {
      return  req.getRequestedSessionId();
    }

    /**
     * put your documentation comment here
     * @param create
     * @return 
     */
    public HttpSession getSession (boolean create) {
      return  req.getSession(create);
    }

    /**
     * put your documentation comment here
     * @return 
     */
    public HttpSession getSession () {
      return  req.getSession();
    }

    /**
     * put your documentation comment here
     * @return 
     */
    public boolean isRequestedSessionIdValid () {
      return  req.isRequestedSessionIdValid();
    }

    /**
     * put your documentation comment here
     * @return 
     */
    public boolean isRequestedSessionIdFromCookie () {
      return  req.isRequestedSessionIdFromCookie();
    }

    /**
     * put your documentation comment here
     * @return 
     */
    public boolean isRequestedSessionIdFromURL () {
      return  req.isRequestedSessionIdFromURL();
    }

    /**
     * put your documentation comment here
     * @return 
     */
    public boolean isRequestedSessionIdFromUrl () {
      return  req.isRequestedSessionIdFromUrl();
    }

    /**
     * put your documentation comment here
     * @param name
     * @return 
     */
    public Object getAttribute (String name) {
      return  req.getAttribute(name);
    }

    /**
     * put your documentation comment here
     * @return 
     */
    public Enumeration getAttributeNames () {
      return  req.getAttributeNames();
    }

    /**
     * put your documentation comment here
     * @return 
     */
    public String getCharacterEncoding () {
      return  req.getCharacterEncoding();
    }

    /**
     * put your documentation comment here
     * @return 
     */
    public int getContentLength () {
      return  req.getContentLength();
    }

    /**
     * put your documentation comment here
     * @return 
     */
    public String getContentType () {
      return  req.getContentType();
    }

    /**
     * put your documentation comment here
     * @return 
     * @exception IOException
     */
    public ServletInputStream getInputStream () throws IOException {
      return  req.getInputStream();
    }

    /**
     * put your documentation comment here
     * @return 
     */
    public String getProtocol () {
      return  req.getProtocol();
    }

    /**
     * put your documentation comment here
     * @return 
     */
    public String getScheme () {
      return  req.getScheme();
    }

    /**
     * put your documentation comment here
     * @return 
     */
    public String getServerName () {
      return  req.getServerName();
    }

    /**
     * put your documentation comment here
     * @return 
     */
    public int getServerPort () {
      return  req.getServerPort();
    }

    /**
     * put your documentation comment here
     * @return 
     * @exception IOException
     */
    public BufferedReader getReader () throws IOException {
      return  req.getReader();
    }

    /**
     * put your documentation comment here
     * @return 
     */
    public String getRemoteAddr () {
      return  req.getRemoteAddr();
    }

    /**
     * put your documentation comment here
     * @return 
     */
    public String getRemoteHost () {
      return  req.getRemoteHost();
    }

    /**
     * put your documentation comment here
     * @param name
     * @param o
     */
    public void setAttribute (String name, Object o) {
      req.setAttribute(name, o);
    }

    /**
     * put your documentation comment here
     * @param name
     */
    public void removeAttribute (String name) {
      req.removeAttribute(name);
    }

    /**
     * put your documentation comment here
     * @return 
     */
    public Locale getLocale () {
      return  req.getLocale();
    }

    /**
     * put your documentation comment here
     * @return 
     */
    public Enumeration getLocales () {
      return  req.getLocales();
    }

    /**
     * put your documentation comment here
     * @return 
     */
    public boolean isSecure () {
      return  req.isSecure();
    }

    /**
     * put your documentation comment here
     * @param path
     * @return 
     */
    public RequestDispatcher getRequestDispatcher (String path) {
      return  req.getRequestDispatcher(path);
    }

    /**
     * put your documentation comment here
     * @param path
     * @return 
     */
    public String getRealPath (String path) {
      return  req.getRealPath(path);
    }
  }
}



