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
import java.io.StringWriter;
import java.util.Enumeration;
import java.util.Map;
import java.util.Hashtable;
import java.util.Locale;
import java.net.URL;
import java.lang.SecurityManager;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletInputStream;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
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
    // some URL construction elements
    public static final String PORTAL_URL_SUFFIX="uP";
    public static final String PORTAL_URL_SEPARATOR=".";
    public static final String RENDER_URL_ELEMENT="render";
    public static final String DETACH_URL_ELEMENT="detach";
    public static final String WORKER_URL_ELEMENT="worker";
    public static final String CHANNEL_URL_ELEMENT="channel";

    // individual worker URL elements
    public static final String FILE_DOWNLOAD_WORKER = "download";    

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
  public void init() throws ServletException {
    if(!initialized) {
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
     * Process HTTP POST request
     *
     * @param req an incoming <code>HttpServletRequest</code> value
     * @param res an outgoing <code>HttpServletResponse</code> value
     * @exception ServletException if an error occurs
     * @exception IOException if an error occurs
     */
    public void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        doGet(req, res);
    }

    /**
     * Process HTTP GET request.
     *
     * @param req an incoming <code>HttpServletRequest</code> 
     * @param res an outgoing <code>HttpServletResponse</code>
     * @exception ServletException if an error occurs
     * @exception IOException if an error occurs
     */
    public void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
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
            String redirectURL = null;
            try {

                // determine if the request needs to be forwarded
                if ((redirectURL = this.getRedirectURL(req)) != null) {
                    // cache request
                    sc.setAttribute("oreqp_" + session.getId(), new RequestParamWrapper(req));
                    session.setAttribute("forwarded", new Boolean(true));
                    // forward
                    // LogService.instance().log(LogService.DEBUG,"PortalSessionManager::doGet() : caching request, sending redirect");
                    res.sendRedirect(redirectURL);

                } else {
                    // delete old request
                    Boolean forwarded = (Boolean)session.getAttribute("forwarded");
                    if (forwarded != null) {
                        session.removeAttribute("forwarded");
                    }
                    UserInstance userInstance = null;
                    try {
                        // Retrieve the user's UserInstance object
                        userInstance = UserInstanceManager.getUserInstance(req);
                    } catch(Exception e) {
                        // NOTE: Should probably be forwarded to error page if the user instance could not be properly retrieved.
                        LogService.instance().log(LogService.ERROR, e);
                    }
                    RequestParamWrapper oreqp = null;
                    if (forwarded != null && forwarded.booleanValue()) {
                        oreqp = (RequestParamWrapper)sc.getAttribute("oreqp_" + session.getId());
                    }
                    if (oreqp != null) {
                        oreqp.setBaseRequest(req);
                        userInstance.writeContent(oreqp, res);
                    } else {
                        userInstance.writeContent(req, res);
                    }
                }
            } catch (PortalException pe) {
                if(pe.getRecordedException()!=null) {
                    StringWriter sw=new StringWriter();
                    pe.getRecordedException().printStackTrace(new PrintWriter(sw));
                    sw.flush();
                    LogService.instance().log(LogService.ERROR,"PortalSessionManager::doGet() : a PortalException has occurred : "+sw.toString());
                    throw new ServletException(pe.getRecordedException());
                } else {
                StringWriter sw=new StringWriter();
                pe.printStackTrace(new PrintWriter(sw));
                sw.flush();
                LogService.instance().log(LogService.ERROR,"PortalSessionManager::doGet() : an unknown exception occurred : "+sw.toString());
                throw new ServletException(pe);
                }
            } catch (Exception e) {
                StringWriter sw=new StringWriter();
                e.printStackTrace(new PrintWriter(sw));
                sw.flush();
                LogService.instance().log(LogService.ERROR,"PortalSessionManager::doGet() : an unknown exception occurred : "+sw.toString());
                throw new ServletException(e);
            }

        } else {
            throw new ServletException("Session object is null !");
        }
    }

    /**
     * Determines if the current requests needs to be redirected.
     *
     * @param req an incoming <code>HttpServletRequest</code> value
     * @return a URL element relative to the root context path (without the '/')
     * if redirection is required, otherwise <code>null</code>
     */
    private String getRedirectURL(HttpServletRequest req) {
        HttpSession session = req.getSession();
        if(session!=null) {
            // see if the session has already been "forwarded"
            Boolean forwarded = (Boolean)session.getAttribute("forwarded");
            if (forwarded != null && forwarded.booleanValue()) {
                return null;
            }
            // get the uPFile URL element
            String servletPath = req.getServletPath();
            try {
                String uPFile = servletPath.substring(servletPath.lastIndexOf('/')+1, servletPath.length());
                
                // see if this is a worker dispatch
                if(uPFile.startsWith(WORKER_URL_ELEMENT)) {
                    // no redirection needed
                    return null;
                } else {
                    // do we have parameters ?
                    boolean params_present=req.getParameterNames().hasMoreElements();
                    // is this render or detach (or something else) ?
                    if(uPFile.startsWith(RENDER_URL_ELEMENT)) { 
                        if(params_present) {
                            return RENDER_URL_ELEMENT+PORTAL_URL_SEPARATOR+PORTAL_URL_SUFFIX;
                        } else {
                            return null;
                        }
                    } else {
                        if(uPFile.startsWith(DETACH_URL_ELEMENT)) {
                            if(params_present) {
                                // redirect to the same uPFile
                                return uPFile;
                            } else {
                                return null;
                            }
                        } else {
                            // unknown basic URL type
                            LogService.instance().log(LogService.ERROR, "PortalSessionManager::getRedirectURL() : unknown basic uPFile type encountered. uPFile=\""+uPFile+"\".");
                            // redirect to the base render
                            return RENDER_URL_ELEMENT+PORTAL_URL_SEPARATOR+PORTAL_URL_SUFFIX;
                        }
                    }
                    
                }
            } catch (IndexOutOfBoundsException iobe) {
                // request came in without a uPFile spec
                // redirect to the basic render
                return RENDER_URL_ELEMENT+PORTAL_URL_SEPARATOR+PORTAL_URL_SUFFIX;
            }
        } else {
            // not tat this would ever happen...
            return RENDER_URL_ELEMENT+PORTAL_URL_SEPARATOR+PORTAL_URL_SUFFIX;
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
      this.req=source;
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
     * @return parameter
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
     * @return parameter names
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



