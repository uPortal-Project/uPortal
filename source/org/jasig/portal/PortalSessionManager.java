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

import java.io.*;
import java.util.*;

import java.lang.SecurityManager;

import javax.naming.Context;
import javax.naming.InitialContext;

import javax.servlet.*;
import javax.servlet.http.*;

import java.security.AccessController;

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
public class PortalSessionManager extends HttpServlet
{
    private boolean baseDirSet=false;
    public static String renderBase="render.uP";
    public static String detachBaseStart="detach_";
    private static int sizeLimit = 3000000; // Should be channel specific


    public void init() throws ServletException {
        JNDIManager.initializePortalContext();
    }

    public void doPost (HttpServletRequest req, HttpServletResponse res)
        throws ServletException, IOException  {
        doGet( req, res );
    }

    public void doGet (HttpServletRequest req, HttpServletResponse res)
        throws ServletException, IOException
    {
        // Should be text/xml !
        if(!baseDirSet) {
            ServletConfig sc=this.getServletConfig();

            if(sc!=null) {
                String sPortalBaseDir=this.getServletConfig().getInitParameter("portalBaseDir");

                if(sPortalBaseDir!=null)  {
                    File portalBaseDir = new java.io.File (sPortalBaseDir);

                    if(portalBaseDir.exists())  {
                        GenericPortalBean.setPortalBaseDir (sPortalBaseDir);
                        baseDirSet=true;
                    }
                }
            }
        }

        if(baseDirSet) {
            // forwarding
            ServletContext sc=this.getServletContext();

            HttpSession session = req.getSession ();
            if(session!=null) {
                //		Logger.log(Logger.DEBUG,"PortalSessionManager::doGet() : request path \""+req.getServletPath()+"\".");
                String redirectBase=null;
                RequestParamWrapper myReq = new RequestParamWrapper(req);
                myReq.setBaseRequest(req);
                if((redirectBase=this.doRedirect(myReq))!=null) {
                    // cache request
                    sc.setAttribute("oreqp_"+session.getId(),myReq);
                    // initial request, requeres forwarding
                    session.setAttribute("forwarded",new Boolean(true));
                    // forward
                    //		    Logger.log(Logger.DEBUG,"PortalSessionManager::doGet() : caching request, sending redirect");
                    //this.getServletContext().getRequestDispatcher("/render.uP").forward(req,res);
                    res.sendRedirect(req.getContextPath()+redirectBase);
                } else {
                    // delete old request
                    Boolean forwarded = (Boolean) session.getAttribute ("forwarded");
                    if(forwarded!=null) session.removeAttribute("forwarded");
                    // proceed with rendering
                    //		    Logger.log(Logger.DEBUG,"PortalSessionManager::doGet() : processing redirected (clean) request");
                    // look if the LayoutBean object is already in the session, otherwise
                    // make a new one
                    LayoutBean layout=(LayoutBean) session.getAttribute("LayoutBean");
                    if(layout==null) {
                        layout=new LayoutBean();
                        session.setAttribute("LayoutBean",layout);
                        //			Logger.log(Logger.DEBUG,"PortalSessionManager;:doGet() : instantiating new LayoutBean");
                    }
                    RequestParamWrapper oreqp=null;
                    if(forwarded!=null && forwarded.booleanValue()) oreqp=(RequestParamWrapper) sc.getAttribute("oreqp_"+session.getId());

                    if(oreqp!=null) {
                        oreqp.setBaseRequest(req);
                        layout.writeContent(oreqp,res,res.getWriter());
                    } else {
                        layout.writeContent(myReq,res,res.getWriter());
                    }
                }
            } else {
                res.setContentType("text/html");
                PrintWriter out = res.getWriter ();
                out.println("<html>");
                out.println("<body>");

                out.println("<h1>" + getServletConfig().getServletName() + "</h1>");
                out.println("Session object is null !??");
                out.println("</body></html>");
            }

        } else {
            res.setContentType("text/html");
            PrintWriter out = res.getWriter ();
            out.println("<html>");
            out.println("<body>");

            out.println("<h1>" + getServletConfig().getServletName() + "</h1>");
            out.println("Base portal directory is not set ! Unable to proceed");
            out.println("</body></html>");
        }

    }
    // this function determines if a given request needs to be redirected
    private String doRedirect(HttpServletRequest req) {
        HttpSession session = req.getSession ();
        if(session!=null) {
            Boolean forwarded = (Boolean) session.getAttribute ("forwarded");
            if(forwarded!=null && forwarded.booleanValue()) return null;
            // don't forward request with no parameters
            if(!req.getParameterNames().hasMoreElements()) return null;
            String servletPath=req.getServletPath();
            // redirect to renderBase
            if(servletPath.equals("/"+renderBase)) return servletPath;
            // redirect to the same base, but no parameters
            String uPFile=servletPath.substring(servletPath.lastIndexOf('/'),servletPath.length());
            //	    Logger.log(Logger.DEBUG,"PortalSessionManager::doRedirect() : uPFile=\""+uPFile+"\".");
            if(uPFile.startsWith("/"+detachBaseStart) && uPFile.endsWith(".uP")) return uPFile;
        }
        // redirect by default
        return "/"+renderBase;
    }

    /* RequestParamWrapper persists various information
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
    public class RequestParamWrapper implements HttpServletRequest, Serializable {
  // the request being wrapped
  private HttpServletRequest req;
  private Hashtable parameters;
  private String pathInfo;
  private String pathTranslated;
  private String contextPath;
  private String queryString;
  private String servletPath;
  private String requestURI;

  public RequestParamWrapper(HttpServletRequest source) {
      // leech all of the information from the source request
      parameters=new Hashtable();
      String contentType = source.getContentType();
      if (contentType != null && contentType.startsWith("multipart/form-data")) {
        com.oreilly.servlet.multipart.Part attachmentPart;
        try {
          MultipartParser multi = new MultipartParser (source, sizeLimit, true, true);
          while ((attachmentPart = multi.readNextPart()) != null) {
            String partName = attachmentPart.getName();
            if (attachmentPart.isParam()) {
              ParamPart parameterPart = (ParamPart) attachmentPart;
              String paramValue = parameterPart.getStringValue();
              if (parameters.containsKey(partName)) {
                /* Assume they meant a multivalued tag, like a checkbox */

                String[] oldValueArray = (String []) parameters.get(partName);
                String[] valueArray = new String[oldValueArray.length+1];
                for (int i = 0; i < oldValueArray.length; i++) {
                  valueArray[i] = oldValueArray[i];
                }
                valueArray[oldValueArray.length] = paramValue;
                parameters.put(partName,valueArray);
              } else {
                String[] valueArray=new String[1];
                valueArray[0]=paramValue;
                parameters.put(partName,valueArray);
              }
           } else if (attachmentPart.isFile()) {
              FilePart filePart = (FilePart) attachmentPart;
              String filename = filePart.getFileName();
              MultipartDataSource fileUpload = new MultipartDataSource(filePart);;
              if (parameters.containsKey(partName)) {
                MultipartDataSource[] oldValueArray = (MultipartDataSource []) parameters.get(partName);
                MultipartDataSource[] valueArray = new MultipartDataSource[oldValueArray.length + 1];
                for (int i = 0; i < oldValueArray.length; i++) {
                  valueArray[i] = oldValueArray[i];
                }
                valueArray[oldValueArray.length] = fileUpload;
                parameters.put(partName,valueArray);
              } else {
                MultipartDataSource[] valueArray=new MultipartDataSource[1];
                valueArray[0]=fileUpload;
                parameters.put(partName, valueArray);
              }
            }
          }
        } catch (Exception e) {
          Logger.log(Logger.ERROR, e);
        }
      }

      Enumeration en = source.getParameterNames ();
      if (en != null) {
        while (en.hasMoreElements ()) {
          String pName= (String) en.nextElement ();
          parameters.put (pName, source.getParameterValues(pName));
        }
      }

      pathInfo=source.getPathInfo();
      pathTranslated=source.getPathTranslated();
      contextPath=source.getContextPath();
      queryString=source.getQueryString();
      servletPath=source.getServletPath();
      requestURI=source.getRequestURI();
  }

  public void setBaseRequest(HttpServletRequest base) {
      this.req=base;
  }

  // overloaded methods
  public String getParameter(String name){
      String[] value_array=this.getParameterValues(name);
      if((value_array!=null) && (value_array.length>0)) return value_array[0];
      else return null;
  }
  public Enumeration getParameterNames(){ return this.parameters.keys(); }
  /**
   * Return a String[] for this parameter
   * @param parameter name
   * @result String[] if parameter is not an Object[]
   */
  public String[] getParameterValues(String name){
      Object[] pars = (Object[]) this.parameters.get(name);
      if (pars instanceof String[]) {
        return (String[]) this.parameters.get(name);
      } else {
        return null;
      }
  }
  /**
   * Return the Object represented by this parameter name
   * @param parameter name
   * @result Object
   */
  public Object[] getObjectParameterValues(String name){
      return (Object[]) this.parameters.get(name);
  }

  public String getPathInfo(){ return pathInfo; }
  public String getPathTranslated(){ return pathTranslated; }
  public String getContextPath(){ return contextPath; }
  public String getQueryString(){ return queryString; }
  public String getServletPath(){ return servletPath; }
  public String getRequestURI(){ return requestURI; }

  // pass through methods
  public String getAuthType(){ return req.getAuthType(); }
  public Cookie[] getCookies(){ return req.getCookies(); }
  public long getDateHeader(String name) { return req.getDateHeader(name); }

  public String getHeader(String name){ return req.getHeader(name); }
  public Enumeration getHeaders(String name){ return req.getHeaders(name); }
  public Enumeration getHeaderNames(){ return req.getHeaderNames(); }
  public int getIntHeader(String name){ return req.getIntHeader(name); }

  public String getMethod(){ return req.getMethod(); }
  public String getRemoteUser(){ return req.getRemoteUser(); }
  public boolean isUserInRole(String role){ return req.isUserInRole(role); }
  public java.security.Principal getUserPrincipal(){ return req.getUserPrincipal(); }
  public String getRequestedSessionId(){ return req.getRequestedSessionId(); }
  public HttpSession getSession(boolean create){ return req.getSession(create); }
  public HttpSession getSession(){ return req.getSession(); }
  public boolean isRequestedSessionIdValid(){ return req.isRequestedSessionIdValid(); }
  public boolean isRequestedSessionIdFromCookie(){ return req.isRequestedSessionIdFromCookie(); }
  public boolean isRequestedSessionIdFromURL(){ return req.isRequestedSessionIdFromURL(); }
  public boolean isRequestedSessionIdFromUrl(){ return req.isRequestedSessionIdFromUrl(); }
  public Object getAttribute(String name){ return req.getAttribute(name); }
  public Enumeration getAttributeNames(){ return req.getAttributeNames(); }
  public String getCharacterEncoding(){ return req.getCharacterEncoding(); }
  public int getContentLength(){ return req.getContentLength(); }
  public String getContentType(){ return req.getContentType(); }
  public ServletInputStream getInputStream() throws IOException { return req.getInputStream(); }
  public String getProtocol(){ return req.getProtocol(); }
  public String getScheme(){ return req.getScheme(); }
  public String getServerName(){ return req.getServerName(); }
  public int getServerPort(){ return req.getServerPort(); }
  public BufferedReader getReader() throws IOException { return req.getReader(); }
  public String getRemoteAddr(){ return req.getRemoteAddr(); }
  public String getRemoteHost(){ return req.getRemoteHost(); }
  public void setAttribute(String name, Object o){ req.setAttribute(name,o); }
  public void removeAttribute(String name){ req.removeAttribute(name); }
  public Locale getLocale(){ return req.getLocale(); }
  public Enumeration getLocales(){ return req.getLocales(); }
  public boolean isSecure(){ return req.isSecure(); }
  public RequestDispatcher getRequestDispatcher(String path){ return req.getRequestDispatcher(path); }
  public String getRealPath(String path){ return req.getRealPath(path); }

    }

}
