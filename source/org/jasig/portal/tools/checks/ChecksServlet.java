/* Copyright 2005 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/

package org.jasig.portal.tools.checks;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Iterator;
import java.util.List;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * A Servlet which renders the results of the checks.
 * @version $Revision$ $Date$
 * @since uPortal 2.5
 */
public class ChecksServlet 
    extends HttpServlet{
    
    protected final void doPost(HttpServletRequest request, HttpServletResponse response) 
        throws IOException {
        
        if ("rerun".equals(request.getParameter("rerun"))) {
            
            SafeDelegatingCheckRunner checkRunner = new SafeDelegatingCheckRunner();
            List results = checkRunner.doChecks();
            getServletContext().setAttribute(CheckingContextListener.RESULTS_SC_KEY, results);
        }
        
        doGet(request, response);
    }
    
    protected final void doGet(HttpServletRequest request, HttpServletResponse response) 
        throws IOException {
        
        List results = (List) this.getServletContext().getAttribute(CheckingContextListener.RESULTS_SC_KEY);
        
        response.setHeader("pragma", "no-cache");
        response.setHeader("Cache-Control", "no-cache, max-age=0, must-revalidate");
        response.setDateHeader("Expires", 0);
        
        PrintWriter writer = response.getWriter();
        
        writer.println("<html>");
        writer.println("<head>");
        writer.println("<title>");
        writer.println("uPortal instrumentation");
        writer.println("</title>");
        writer.println("</head>");
        
        writer.println("<body>");

        
        if (results == null) {
            writer.println("<p>Could not find check results in servlet context.</p>");
        } else {
            writer.println("<p>Results of checks:</p>");
            writer.println("<table>");
            writer.println("<tr>");
            writer.println("<td>");
            writer.println("Status");
            writer.println("</td>");
            writer.println("<td>");
            writer.println("Check description");
            writer.println("</td>");
            writer.println("<td>");
            writer.println("Check result message");
            writer.println("</td>");
            writer.println("<td>");
            writer.println("Remediation advice");
            writer.println("</td>");
            writer.println("</tr>");
            
            for (Iterator iter = results.iterator(); iter.hasNext(); ) {
                CheckAndResult checkAndResult = (CheckAndResult) iter.next();
                
                writer.println("<tr>");
                writer.println("<td>");
                if (checkAndResult.isSuccess()) {
                    writer.println("OK");
                } else {
                    writer.println("FAILURE");
                }
                writer.println("</td>");
                writer.println("<td>");
                writer.println(checkAndResult.getCheckDescription());
                writer.println("</td>");
                writer.println("<td>");
                writer.println(checkAndResult.getResult().getMessage());
                writer.println("</td>");
                writer.println("<td>");
                if (checkAndResult.isSuccess()) {
                    writer.println("Not applicable.");
                } else {
                    writer.println(checkAndResult.getResult().getRemediationAdvice());
                }
                writer.println("</td>");
                writer.println("</tr>");
                
            }
            
            writer.println("</table>");
        }
        
        writer.println("<form action='instrumentation' method='post'>");
        writer.println("<input name='rerun' value='rerun' type='submit'/>");
        writer.println("</form>");
        
        // print out package information
        
        writer.println("<br/>");
        writer.println("<p>Package information.</p>");
        
        writer.println("<table>");
        writer.println("<tr>");
        writer.println("<td>");
        writer.println("Implementation title");
        writer.println("</td>");
        writer.println("<td>");
        writer.println("Implementation version");
        writer.println("</td>");
        writer.println("<td>");
        writer.println("Package name");
        writer.println("</td>");
        writer.println("<td>");
        writer.println("Implementation vendor");
        writer.println("</td>");
        writer.println("<td>");
        writer.println("Specification title");
        writer.println("</td>");
        writer.println("<td>");
        writer.println("Specification vendor");
        writer.println("</td>");
        writer.println("<td>");
        writer.println("Specification version");
        writer.println("</td>");
        writer.println("</tr>");
        
        Package[] packs = Package.getPackages();
        
    
        
        for (int i = 0; i < packs.length; i++) {
            Package pack = packs[i];
            writer.println("<tr>");
            writer.println("<td>");
            writer.println(pack.getImplementationTitle());
            writer.println("</td>");
            writer.println("<td>");
            writer.println(pack.getImplementationVersion());
            writer.println("</td>");
            writer.println("<td>");
            writer.println(pack.getName());
            writer.println("</td>");
            writer.println("<td>");
            writer.println(pack.getImplementationVendor());
            writer.println("</td>");
            writer.println("<td>");
            writer.println(pack.getSpecificationTitle());
            writer.println("</td>");
            writer.println("<td>");
            writer.println(pack.getSpecificationVendor());
            writer.println("</td>");
            writer.println("<td>");
            writer.println(pack.getSpecificationVersion());
            writer.println("</td>");
            writer.println("</tr>");
        }

        writer.println("</table>");
        
        writer.println("</body>");
        
        writer.println("</html>");
        writer.flush();
        
    }
    

}
