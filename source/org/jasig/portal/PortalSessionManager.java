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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Locale;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletInputStream;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.jasig.portal.jndi.JNDIManager;
import org.jasig.portal.services.LogService;
import org.jasig.portal.utils.SubstitutionServletOutputStream;
import org.jasig.portal.utils.SubstitutionWriter;

import com.oreilly.servlet.multipart.FilePart;
import com.oreilly.servlet.multipart.MultipartParser;
import com.oreilly.servlet.multipart.ParamPart;


/**
 * This is an entry point into the uPortal.
 * @author Peter Kharchenko <pkharchenko@interactivebusiness.com>
 * @version: $Revision$
 */
public class PortalSessionManager extends HttpServlet {

    //    public static final String SESSION_TAG_VARIABLE="uP_session_tag";
    public static final String INTERNAL_TAG_VALUE=Long.toHexString((new Random()).nextLong());
    public static final String IDEMPOTENT_URL_TAG="idempotent";

  private static final int sizeLimit = PropertiesManager.getPropertyAsInt("org.jasig.portal.PortalSessionManager.File_upload_max_size");
  private static boolean initialized = false;
  private static ServletContext servletContext = null;
  private static PortalSessionManager instance = null;

  /**
   * Provides access to the servlet instance ultimately to provide access
   * to the servlet context of the portal.
   * @return instance, the PortalSessionManager servlet instance
   */
  public static final PortalSessionManager getInstance() {
     return instance;
  }

  // Following flag allows to disable features that prevent
  // repeated requests from going through. This is useful
  // when debugging and typing things in on a command line.
  // Otherwise, the flag should be set to false.
  private static final boolean ALLOW_REPEATED_REQUESTS = PropertiesManager.getPropertyAsBoolean("org.jasig.portal.PortalSessionManager.allow_repeated_requests");

  // random number generator
  private static final Random randomGenerator = new Random();

  static {
    LogService.log(LogService.INFO, "uPortal started");
  }

  /**
   * Initialize the PortalSessionManager servlet
   * @throws ServletException
   */
  public void init() throws ServletException {
    if(!initialized) {
      instance = this;
      // Retrieve the servlet configuration object from the servlet container
      // and make sure it's available
      ServletConfig sc = getServletConfig();
      if (sc == null) {
        throw new ServletException("PortalSessionManager.init(): ServletConfig object was returned as null");
      }
      servletContext = sc.getServletContext();

      try {
          JNDIManager.initializePortalContext();
      } catch (PortalException pe) {
          if(pe.getRecordedException()!=null) {
              StringWriter sw=new StringWriter();
              pe.getRecordedException().printStackTrace(new PrintWriter(sw));
              sw.flush();
              LogService.log(LogService.ERROR,"PortalSessionManager::doGet() : a PortalException has occurred : "+sw.toString());
              throw new ServletException(pe.getRecordedException());
          } else {
              StringWriter sw=new StringWriter();
              pe.printStackTrace(new PrintWriter(sw));
              sw.flush();
              LogService.log(LogService.ERROR,"PortalSessionManager::doGet() : an unknown exception occurred : "+sw.toString());
              throw new ServletException(pe);
          }
      }



      // Flag that the portal has been initialized
      initialized = true;

      // Get the SAX implementation
      if (System.getProperty("org.xml.sax.driver") == null) {
          System.setProperty("org.xml.sax.driver", PropertiesManager.getProperty("org.xml.sax.driver"));
      }
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
        // Send the uPortal version in a header
        res.setHeader("uPortal-version", "uPortal_rel-2-1-5");

        HttpSession session = req.getSession();

        if (session != null) {
            Set requestTags=null;
            boolean request_verified=false;

            if(!ALLOW_REPEATED_REQUESTS) {
                // obtain a tag table
                synchronized(session) {
                    requestTags=(Set)session.getAttribute("uP_requestTags");
                    if(requestTags==null) {
                        requestTags=Collections.synchronizedSet(new HashSet());
                        session.setAttribute("uP_requestTags",requestTags);
                    }
                }
                // determine current tag
                UPFileSpec upfs=new UPFileSpec(req);

                String tag=upfs.getTagId();

                // see if the tag was registered
                if(tag!=null) {
                    request_verified=(tag.equals(IDEMPOTENT_URL_TAG) || requestTags.remove(tag));
                }

                LogService.log(LogService.DEBUG, "PortalSessionManager::doGet() : request verified: "+request_verified);
            }

            try {
                UserInstance userInstance = null;
                try {
                    // Retrieve the user's UserInstance object
                    userInstance = UserInstanceManager.getUserInstance(req);
                } catch(Exception e) {
                    // NOTE: Should probably be forwarded to error page if the user instance could not be properly retrieved.
                    LogService.log(LogService.ERROR, e);
                    // invalidate session, throw exception
                    if(session!=null) {
                        session.invalidate();
                    }
                    if(e instanceof PortalException) {
                        Exception ie=((PortalException) e).getRecordedException();
                        if(ie!=null) {
                            throw new ServletException(ie);
                        }
                    }
                    throw new ServletException(e);
                }


                // fire away
                if(ALLOW_REPEATED_REQUESTS) {
                    userInstance.writeContent(new RequestParamWrapper(req,true),res);
                } else {
                    // generate and register a new tag
                    String newTag=Long.toHexString(randomGenerator.nextLong());
                    LogService.log(LogService.DEBUG,"PortalSessionManager::doGet() : generated new tag \""+newTag+"\" for the session "+req.getSession(false).getId());
                    // no need to check for duplicates :) we'd have to wait a lifetime of a universe for this time happen
                    if(!requestTags.add(newTag)) {
                        LogService.log(LogService.ERROR,"PortalSessionManager::doGet() : a duplicate tag has been generated ! Time's up !");
                    }

                    userInstance.writeContent(new RequestParamWrapper(req,request_verified), new ResponseSubstitutionWrapper(res,INTERNAL_TAG_VALUE,newTag));
                }
            } catch (PortalException pe) {
                // Go through all the possible nested exceptions...
                // Right now, all the exceptions are logged, but we might
                // want to consider logging only the root cause
                //   -Ken
                StringWriter sw = new StringWriter();
                pe.printStackTrace(new PrintWriter(sw));
                sw.flush();
                LogService.log(LogService.ERROR, "PortalSessionManager::doGet() : a PortalException has occurred: " + sw.toString());
                Throwable t = pe.getRecordedException();
                if (t == null)
                  throw new ServletException(pe);
                while (t != null) {
                  sw = new StringWriter();
                  t.printStackTrace(new PrintWriter(sw));
                  sw.flush();
                  LogService.log(LogService.ERROR, "PortalSessionManager::doGet() : with nested Exception: " + sw.toString());
                  if (t instanceof PortalException) {
                    t = ((PortalException)t).getRecordedException();
                  } else if (t instanceof java.lang.reflect.InvocationTargetException) {
                    t = ((java.lang.reflect.InvocationTargetException)t).getTargetException();
                  } else {
                    // Throw the root cause
                    throw new ServletException(t);
                  }
                }
            } catch (Exception e) {
                StringWriter sw=new StringWriter();
                e.printStackTrace(new PrintWriter(sw));
                sw.flush();
                LogService.log(LogService.ERROR,"PortalSessionManager::doGet() : an unknown exception occurred : "+sw.toString());
                throw new ServletException(e);
            }

        } else {
            throw new ServletException("Session object is null !");
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





    public class ResponseSubstitutionWrapper implements HttpServletResponse {
        protected final HttpServletResponse res;
        protected String sessionTag;
        protected String newTag;

        public ResponseSubstitutionWrapper(HttpServletResponse res,String sessionTag, String newTag) {
            this.res=res;
            this.sessionTag=sessionTag;
            this.newTag=newTag;
        }

        public ServletOutputStream getOutputStream() throws IOException {
            String encoding=this.getCharacterEncoding();
            byte[] target,substitute;
            if(encoding!=null) {
                // use specified encoding
                target=sessionTag.getBytes(encoding);
                substitute=newTag.getBytes(encoding);
            } else {
                // use default system encoding
                target=sessionTag.getBytes();
                substitute=newTag.getBytes();
            }
            return new SubstitutionServletOutputStream(res.getOutputStream(),target,substitute,this.getBufferSize());
        }

        public PrintWriter getWriter() throws IOException {
            return new PrintWriter(new SubstitutionWriter(res.getWriter(),sessionTag.toCharArray(),newTag.toCharArray(),this.getBufferSize()));
        }


        // pass-through implementation methods

        // implementation of javax.servlet.ServletResponse interface
        public String getCharacterEncoding() {
            return res.getCharacterEncoding();
        }
        public void reset() {
            res.reset();
        }
        public void flushBuffer() throws IOException {
            res.flushBuffer();
        }
        public void setContentType(String type) {
            res.setContentType(type);
        }
        public void setContentLength(int len) {
            res.setContentLength(len);
        }
        public void setBufferSize(int size) {
            res.setBufferSize(size);
        }
        public int getBufferSize() {
            return res.getBufferSize();
        }
        public boolean isCommitted() {
            return res.isCommitted();
        }
        public void setLocale(Locale loc) {
            res.setLocale(loc);
        }
        public Locale getLocale() {
            return res.getLocale();
        }
        // servlet 2.3 methods, inderect invokation:
        public void resetBuffer() {
            try {
                java.lang.reflect.Method m = res.getClass().getMethod("reseBuffer", null);
                m.invoke(res, null);
            } catch (Exception e) {
            }
        }


        // implementation of javax.servlet.http.HttpServletResponse interface
        public void addCookie(Cookie cookie) {
            this.res.addCookie(cookie);
        }
        public boolean containsHeader(String name) {
            return this.res.containsHeader(name);
        }
        public String encodeURL(String url) {
            return this.res.encodeURL(url);
        }
        public String encodeRedirectURL(String url) {
            return this.res.encodeRedirectURL(url);
        }
        public String encodeUrl(String url) {
            return this.res.encodeUrl(url);
        }
        public String encodeRedirectUrl(String url) {
            return this.res.encodeRedirectUrl(url);
        }
        public void sendError(int sc, String msg) throws IOException {
            this.res.sendError(sc, msg);
        }
        public void sendError(int sc) throws IOException {
            this.res.sendError(sc);
        }
        public void sendRedirect(String location) throws IOException {
            this.res.sendRedirect(location);
        }
        public void setDateHeader(String name, long date) {
            this.res.setDateHeader(name, date);
        }
        public void addDateHeader(String name, long date) {
            this.res.addDateHeader(name, date);
        }
        public void setHeader(String name, String value) {
            this.res.setHeader(name, value);
        }
        public void addHeader(String name, String value) {
            this.res.addHeader(name, value);
        }
        public void setIntHeader(String name, int value) {
            this.res.setIntHeader(name, value);
        }
        public void addIntHeader(String name, int value) {
            this.res.addIntHeader(name, value);
        }
        public void setStatus(int sc) {
            this.res.setStatus(sc);
        }
        public void setStatus(int sc, String sm) {
            this.res.setStatus(sc, sm);
        }
    }



    /**
     * A wrapper around http request object to prevent unverified requests from
     * accessing any of the request parameters.
     *
     * @author <a href="mailto:pkharchenko@interactivebusiness.com">Peter Kharchenko</a>
     */
    public class RequestParamWrapper implements HttpServletRequest {
        // the request being wrapped
        protected final HttpServletRequest req;
        protected Hashtable parameters;
        protected boolean request_verified;


        /**
         * Creates a new <code>RequestParamWrapper</code> instance.
         *
         * @param source an <code>HttpServletRequest</code> value that's being wrapped.
         * @param request_verified a <code>boolean</code> flag that determines if the request params should be accessable.
         */
        public RequestParamWrapper (HttpServletRequest source,boolean request_verified) {
            // leech all of the information from the source request
            this.req=source;
            this.request_verified=request_verified;

            parameters = new Hashtable();

            // only bother with parameter work if should be accessable
            if(request_verified) {
                // parse request body
                String contentType = source.getContentType();
                if (contentType != null && contentType.startsWith("multipart/form-data")) {
                    com.oreilly.servlet.multipart.Part attachmentPart;
                    try {
                        MultipartParser multi = new MultipartParser(source, sizeLimit, true, true, "UTF-8");
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
                        LogService.log(LogService.ERROR, e);
                    }
                }
                // regular params
                try {
                    source.setCharacterEncoding("UTF-8");
                } catch (UnsupportedEncodingException uee) {
                    LogService.log(LogService.ERROR, "PortalSessionManager.RequestParamWrapper(): unable to set UTF-8 character encoding! "+uee);
                }
                Enumeration en = source.getParameterNames();
                if (en != null) {
                    while (en.hasMoreElements()) {
                        String pName = (String)en.nextElement();
                        parameters.put(pName, source.getParameterValues(pName));
                    }
                }
            }
        }

        /**
         * Overloaded method
         * @param name
         * @return parameter
         */
        public String getParameter(String name) {
            String[] value_array = this.getParameterValues(name);
            if ((value_array != null) && (value_array.length > 0)) {
                return  value_array[0];
            } else {
                return  null;
            }
        }

        /**
         * Overloaded method
         * @return parameter names
         */
        public Enumeration getParameterNames() {
            return  this.parameters.keys();
        }

        /**
         * Return a String[] for this parameter
         * @param parameter name
         * @result String[] if parameter is not an Object[]
         */
        public String[] getParameterValues(String name) {
            Object[] pars = (Object[])this.parameters.get(name);
            if (pars!=null && pars instanceof String[]) {
                return  (String[])this.parameters.get(name);
            } else {
                return  null;
            }
        }

        /**
         * Overloaded method
         *
         * @return a <code>Map</code> value
         */
        public Map getParameterMap() {
            return parameters;
        }

        /**
         * Return the Object represented by this parameter name
         * @param parameter name
         * @result Object
         */
        public Object[] getObjectParameterValues(String name) {
            return  (Object[])this.parameters.get(name);
        }


        // this method should be overloaded, otherwise params will be visible
        public String getRequestURI() {
            return  req.getRequestURI();
        }


        // pass through methods
        public String getPathInfo() {
            return  req.getPathInfo();
        }

        public String getPathTranslated() {
            return  req.getPathTranslated();
        }

        public String getContextPath() {
            return  req.getContextPath();
        }

        public String getQueryString() {
            return  req.getQueryString();
        }

        public String getServletPath() {
            return  req.getServletPath();
        }

        // This method is new in Servlet 2.3.
        // java.lang.reflect methods are used here in an effort
        // to be compatible with older servlet APIs.
        public StringBuffer getRequestURL() {
            try {
                java.lang.reflect.Method m = req.getClass().getMethod("getRequestURL", null);
                return (StringBuffer)m.invoke(req, null);
            } catch (Exception e) {
                return null;
            }
        }


        // peterk: this won't work. Spec says that this method has to be executed prior to
        // reading request body, and we do exactly this in the constructor of this class :(

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

        public String getAuthType() {
            return  req.getAuthType();
        }

        public Cookie[] getCookies() {
            return  req.getCookies();
        }

        public long getDateHeader(String name) {
            return  req.getDateHeader(name);
        }

        public String getHeader(String name) {
            return  req.getHeader(name);
        }

        public Enumeration getHeaders(String name) {
            return  req.getHeaders(name);
        }

        public Enumeration getHeaderNames() {
            return  req.getHeaderNames();
        }

        public int getIntHeader(String name) {
            return  req.getIntHeader(name);
        }

        public String getMethod() {
            return  req.getMethod();
        }

        public String getRemoteUser() {
            return  req.getRemoteUser();
        }

        public boolean isUserInRole(String role) {
            return  req.isUserInRole(role);
        }

        public java.security.Principal getUserPrincipal() {
            return  req.getUserPrincipal();
        }

        public String getRequestedSessionId() {
            return  req.getRequestedSessionId();
        }

        public HttpSession getSession(boolean create) {
            return  req.getSession(create);
        }

        public HttpSession getSession() {
            return  req.getSession();
        }

        public boolean isRequestedSessionIdValid() {
            return  req.isRequestedSessionIdValid();
        }

        public boolean isRequestedSessionIdFromCookie() {
            return  req.isRequestedSessionIdFromCookie();
        }

        public boolean isRequestedSessionIdFromURL() {
            return  req.isRequestedSessionIdFromURL();
        }

        public boolean isRequestedSessionIdFromUrl() {
            return  req.isRequestedSessionIdFromURL();
        }

        public Object getAttribute(String name) {
            return  req.getAttribute(name);
        }

        public Enumeration getAttributeNames() {
            return  req.getAttributeNames();
        }

        public String getCharacterEncoding() {
            return  req.getCharacterEncoding();
        }

        public int getContentLength() {
            return  req.getContentLength();
        }

        public String getContentType() {
            return  req.getContentType();
        }

        public ServletInputStream getInputStream() throws IOException {
            return  req.getInputStream();
        }

        public String getProtocol() {
            return  req.getProtocol();
        }

        public String getScheme() {
            return  req.getScheme();
        }

        public String getServerName() {
            return  req.getServerName();
        }

        public int getServerPort() {
            return  req.getServerPort();
        }

        public BufferedReader getReader() throws IOException {
            return  req.getReader();
        }

        public String getRemoteAddr() {
            return  req.getRemoteAddr();
        }

        public String getRemoteHost() {
            return  req.getRemoteHost();
        }

        public void setAttribute(String name, Object o) {
            req.setAttribute(name, o);
        }

        public void removeAttribute(String name) {
            req.removeAttribute(name);
        }

        public Locale getLocale() {
            return  req.getLocale();
        }

        public Enumeration getLocales() {
            return  req.getLocales();
        }

        public boolean isSecure() {
            return  req.isSecure();
        }

        public RequestDispatcher getRequestDispatcher(String path) {
            return  req.getRequestDispatcher(path);
        }

        public String getRealPath(String path) {
            throw new RuntimeException("HttpServletRequest.getRealPath(String path) is deprectated!  Use ServletContext.getRealPath(String path) instead.");
        }
    }
}



