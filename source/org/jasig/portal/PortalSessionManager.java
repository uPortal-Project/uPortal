package org.jasig.portal;


import java.io.*;
import java.util.*;

import javax.servlet.*;
import javax.servlet.http.*;



/**
 * This is an entry point into the uPortal.
 * @author Peter Kharchenko <pkharchenko@interactivebusiness.com>
 */
public class PortalSessionManager extends HttpServlet {
    private boolean baseDirSet=false;
    
    public void doPost (HttpServletRequest req, HttpServletResponse res)
	throws ServletException, IOException
    {
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
		if(sPortalBaseDir!=null) {
		    File portalBaseDir = new java.io.File (sPortalBaseDir);
		    if (portalBaseDir.exists ()) {
			GenericPortalBean.setPortalBaseDir (sPortalBaseDir);
			baseDirSet=true;
		    }
		}
	    }
	}

	if(baseDirSet) {
	    // look if the LayoutBean object is already in the session, otherwise
	    // make a new one
	    HttpSession session=req.getSession();
	    if(session!=null) {
		LayoutBean layout=(LayoutBean) session.getAttribute("LayoutBean");
		if(layout==null) {
		    layout=new LayoutBean();
		    session.setAttribute("LayoutBean",layout);
		    Logger.log(Logger.DEBUG,"PortalSessionManager;:doGet() : instantiating new LayoutBean");
		}
		layout.writeContent(req,res,res.getWriter());
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
      
}
