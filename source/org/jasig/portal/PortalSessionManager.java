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

/**
 * This is an entry point into the uPortal.
 * @author Peter Kharchenko <pkharchenko@interactivebusiness.com>
 */
public class PortalSessionManager extends HttpServlet
{
    private boolean baseDirSet=false;
    public static String renderBase="render.uP";
    public static String detachBaseStart="detach_";
    
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
		if((redirectBase=this.doRedirect(req))!=null) {
		    // cache request
		    sc.setAttribute("oreqp_"+session.getId(),new RequestParamWrapper(req));
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
			layout.writeContent(req,res,res.getWriter());
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
    protected class RequestParamWrapper implements HttpServletRequest, Serializable {
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
      for(Enumeration e=source.getParameterNames(); e.hasMoreElements();) {
    String pName=(String) e.nextElement();
    this.parameters.put(pName,source.getParameterValues(pName));
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
  public String[] getParameterValues(String name){ 
      return (String[]) this.parameters.get(name);
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
