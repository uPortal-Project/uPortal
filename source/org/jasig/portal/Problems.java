/*
 * Copyright © 2003-2004 The JA-SIG Collaborative.  All rights reserved.
 * Please see notice at end of file.
 */

package org.jasig.portal;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.DateFormat;
import java.util.Iterator;
import java.util.TreeMap;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


/**
 * A servlet that reports portal problems.
 * This servlet attempts to accomodate both the ErrorID approach and the
 * display recent PortalExceptions approach.
 * @author Howard Gilbert
 * @author andrew.petro@yale.edu
 * @version $Revision$
 */
public class Problems extends HttpServlet {
	
    private static final Log log = LogFactory.getLog(Problems.class);
    
    /**
     * The simple formatter used to format the times of the PortalExceptions.
     */
    private DateFormat simpleTimeFormat = DateFormat.getDateTimeInstance();

    protected void doGet(
        HttpServletRequest request,
        HttpServletResponse response)
        throws ServletException, IOException {

        response.setContentType("text/html");
        PrintWriter out = response.getWriter();

        out.println("<head><title>uPortal Problem Status</title></head>");
        out.println("<body>");
        out.println("<h1>uPortal Problem Status</h1>");

        String majorname = request.getParameter("major");
        String minorname = request.getParameter("minor");
        String peDetails = request.getParameter("peDetails");
        boolean showPeDetails = ("true".equals(peDetails));
        
        if (majorname == null) {
			out.println(listRecent());
            out.println(listRecentPortalExceptions(showPeDetails));
            out.println(listRegistered());
        } else {
            out.println(listdetail(majorname, minorname));
        }

        out.println("</body>");

    }

    /**
     * Generate HTML for stack trace and detail on one ID.
     * @param majorname first key of ErrorID
     * @param minorname second key of ErrorID
     * @return HTML in String
     */
    private String listdetail(
        String majorname,
        String minorname) {
        StringBuffer sb = new StringBuffer(1000);
        TreeMap submap = (TreeMap) ProblemsTable.registeredIds.get(majorname);
        CountID countid = null;
        if (submap != null) {
            countid = (CountID) submap.get(minorname);
        }
        if (countid == null) {
            return "No registered error with these parameters.";
        }
        sb.append("<pre>");
        PortalException pe = countid.lastPortalException;
        Exception ex = pe.getRecordedException();
        if (ex==null)
        	ex=pe;
        sb.append("at "+ pe.getTimestamp().toString()+"\n");
		sb.append(
            ExceptionHelper.errorInfo(pe.getErrorID(),pe.getParameter(),ex));
		sb.append("</pre>");
		return sb.toString();
    }

    /**
     * List all registered ErrorIDs
     * @return HTML String with list
     */
    private String listRegistered() {
        StringBuffer sb = new StringBuffer(1000);
        sb.append("<h2>Registered Error IDs</h2>");
        Iterator minors = ProblemsTable.registeredIds.values().iterator();
        sb.append("<table>");
        while (minors.hasNext()) {
            TreeMap minor = (TreeMap) minors.next();
            Iterator ids = minor.values().iterator();
            while (ids.hasNext()) {
                CountID nextid = (CountID) ids.next();
                sb.append("<tr><td>");
                sb.append(nextid.errorID.category);
                sb.append("</td><td>");
                sb.append(nextid.errorID.specific);
                sb.append("</td><td>");
                if (nextid.count > 0) {
                    sb.append(
                        "<a href='problems?major="
                            + nextid.errorID.category
                            + "&minor="
							+ nextid.errorID.specific
                            + "'>");
                }
                sb.append(nextid.errorID.message);
                if (nextid.count>0)
                	sb.append("</a>");
                sb.append("</td><td>");
                sb.append(nextid.count);
                sb.append("</td></tr>");
            }
        }
        sb.append("</table>");
        return sb.toString();
    }

	private String listRecent() {
		StringBuffer sb = new StringBuffer(1000);
		sb.append("<h2>Recent Error IDs</h2>");
		Iterator list = ProblemsTable.recentIds.iterator();
		sb.append("<table>");
		while (list.hasNext()) {
			CountID nextid = (CountID) list.next();
			sb.append("<tr><td>");
			sb.append(nextid.errorID.category);
			sb.append("</td><td>");
			sb.append(nextid.errorID.specific);
			sb.append("</td><td>");
			if (nextid.count > 0) {
				sb.append(
					"<a href='problems?major="
						+ nextid.errorID.category
						+ "&minor="
						+ nextid.errorID.specific
						+ "'>");
			}
			sb.append(nextid.errorID.message);
			if (nextid.count>0)
				sb.append("</a>");
			sb.append("</td><td>");
			sb.append(nextid.count);
			sb.append("</td></tr>");
		}
		sb.append("</table>");
		return sb.toString();
	}
    
    private String listRecentPortalExceptions(boolean showStackTraces){
        StringBuffer sb = new StringBuffer();
        sb.append("<h2>Recent PortalExceptions</h2>");

        Iterator peIter = ProblemsTable.getRecentPortalExceptions().iterator();
        if (peIter.hasNext()){
            if (showStackTraces){
                sb.append("<p><a href='problems?peDetails=false'>Click to view without stack traces</a></p>");
            } else {
                sb.append("<p><a href='problems?peDetails=true'>Click to view with stack traces</a></p>");
            }
            sb.append("<table>");
            while(peIter.hasNext()){
                PortalException portalException = (PortalException) peIter.next();
                sb.append("<tr>");
                sb.append("<td>");
                sb.append(this.simpleTimeFormat.format(portalException.getTimestamp()));
                sb.append("</td>");
                sb.append("<td>");
                sb.append(portalException.getClass().getName());
                sb.append("</td>");
                sb.append("<td>");
                sb.append(portalException.getLocalizedMessage());
                sb.append("</td>");
                sb.append("</tr>");
                if (showStackTraces){
                    sb.append("<tr >");
                    sb.append("<td colspan='3'>");
                    sb.append("<pre>");
                    
                    // I so wish we were in JDK 1.4...
                    // this is boilerplate acrobatics to get our hands on the StackTrace
                    StringWriter stringWriter = new StringWriter();
                    PrintWriter printWriter = new PrintWriter(stringWriter, false);
                    portalException.printStackTrace(printWriter);
                    try {
                        stringWriter.close();
                    } catch (IOException e) {
                        log.error("Problem closing our StringWriter used to obtain the stack trace.", e);
                    }
                    printWriter.close();
                    sb.append(stringWriter.toString());
                    sb.append("</pre>");
                    sb.append("</td>");
                    sb.append("</tr>");
                }
            }
            sb.append("</table>");
        }
        return sb.toString();
    }

}

/*
 * Copyright © 2003-2004 The JA-SIG Collaborative.  All rights reserved.
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