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

import java.io.PrintWriter;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class ExceptionHelper {

    private static final Log log = LogFactory.getLog(ExceptionHelper.class);
    
    // List of strings to match in traceback for various containers
	// This array must be expanded as additional Application Server
	// containers become better known
    public static final String boundaries[] =
        {
    		"at javax.servlet.http.HttpServlet."
    	};

    /**
     * Generate traceback only to the Servlet-container interface.
     *
     * @param ex any throwable exception
     * @return stack trace string without container layers
     */
    public static String shortStackTrace(Throwable ex) {
    	if (ex == null)
    		return "";
        java.io.StringWriter sw = new java.io.StringWriter();
        ex.printStackTrace(new java.io.PrintWriter(sw));
        sw.flush();
        String stktr = sw.toString();
        for (int i=0;i<boundaries.length;i++) {
        	int cut = stktr.indexOf(boundaries[i]);
        	if (cut > 0)
        		return stktr.substring(0, cut).trim();
        }
        
        return stktr;
    }

    /**
     * Generic Exception Handler called from catch clause
     *
     * @param eid the ErrorID (as seen from catch)
     * @param parm
     * @param ex the Exception caught
     * @throws PortalException
     */
    public static void genericHandler(ErrorID eid, String parm, Throwable ex)
        throws PortalException {

        // *** Handle PortalExceptions ***
    	
    	// Log it if logging was deferred in .signal() call
    	// Rethrow it
        if (ex instanceof PortalException) {
        	if (((PortalException)ex).isLogPending())
        		traceToLog(eid,parm,ex);
			throw (PortalException) ex;
        }
        
        // *** Handle all other Exceptions ***

        // Log the message and traceback
        traceToLog(eid, parm, ex);

        // Create a derived PortalException chained to this
        PortalException nex;
        if (ex instanceof Exception) {
            nex = new PortalException(eid, (Exception) ex);
        } else {
        	// Sorry, at this point PortalExceptions don't chain to 
        	// non-Exception subclasses of Throwable
            nex = new PortalException(eid);
        }
        nex.setLogPending(false);    
        ProblemsTable.store(nex);    
        throw nex;
    }

	public static void genericHandler(ErrorID eid, Throwable ex)
		throws PortalException {
			genericHandler(eid,null,ex);
	}



	/**
	 * Create PortalException from ErrorID and throw it. Maybe trace it.
	 * @param eid  ErrorId
	 * @param parm  Additional error information
	 * @param tracenow Trace now or defer till first catch.
	 * @throws PortalException
	 */
    public static void signal(ErrorID eid, String parm, boolean tracenow)
        throws PortalException {
        PortalException nex = new PortalException(eid);
		nex.setParameter(parm);
        signal(nex,tracenow);
    }
    
    /**
     * Create PortalException from Errorid, trace, and throw it.
     * @param eid ErrorID to use to generate PortalException
     * @param parm Additional error information
     * @throws PortalException
     */
    public static void signal(ErrorID eid, String parm) throws PortalException {
    	signal(eid,parm,true);
    }

    /**
     * Throw PortalException provided by caller, maybe trace it.
     * @param nex Exception provided by caller
     * @param tracenow  Trace now, or later after first catch.
     * @throws PortalException
     */
    private static void signal(PortalException nex, boolean tracenow)
        throws PortalException {
        if (tracenow) {
        	traceToLog(nex.getErrorID(), nex.getParameter(), nex);
        	ProblemsTable.store(nex);
		}
        throw nex;
    }
    
    /**
     * Generate, trace, and throw Portal Exception given ErrorID.
     * @param eid ErrorID
     * @throws PortalException
     */
    public static void signal(ErrorID eid) 
    	throws PortalException {
    	signal(eid,null,true);
    }

    /**
     * Common logic for generating log entry of errors
     * @param eid ErrorID with initial message
     * @param parm Parameter string to append to eid msg
     * @param ex Old exception
     */
    private static void traceToLog(ErrorID eid, String parm, Throwable ex) {
    	
    	if (ex !=null &&
    		ex instanceof PortalException) {
	    		if (!((PortalException)ex).isLogPending())
	    			return; // This PortalException was already logged.
	    		else
	    			((PortalException)ex).setLogPending(false);
    	}
    	
        String logmsg = errorInfo(eid, parm, ex);
        log.error( logmsg);
    }

    /**
     * Generate error string for logging or /problems online display
     * @param eid Error ID
     * @param parm Parameter string
     * @param ex Exception
     * @return Multiline text with message and traceback
     */
    public static String errorInfo(ErrorID eid, String parm, Throwable ex) {
		StringBuffer errorinfobuffer = new StringBuffer(1000);
		
		if (eid!=Errors.legacy)
        	errorinfobuffer.append(eid.getMessage());  // Error ID message
        else
        	errorinfobuffer.append(ex.getMessage());
        
		if (parm != null) { // Parameter data
			errorinfobuffer.append("\n    [specific data: ");
			errorinfobuffer.append(parm);
			errorinfobuffer.append("] ");
		}
        
		errorinfobuffer.append("\n");
		
		if (ex!=null)
			errorinfobuffer.append(shortStackTrace(ex)); // Stack trace
		return errorinfobuffer.toString();
    }

    /**
     * Generic Top-Level Exception Handler caled from catch clause
     * (doesn't rethrow exception)
     * @param eid Error ID
     * @param parm Parameter string
     * @param ex Exception caught
     */
    public static void genericTopHandler(ErrorID eid, String parm, Throwable ex) {

 
        // If this is an already logged Portal Exception, we are done
        if (ex instanceof PortalException &&
        	!((PortalException)ex).isLogPending()) {
        	return;
        }

        traceToLog(eid, parm, ex);  
        
        if (ex instanceof PortalException) // already in the table
        	return;
        
		// Create a derived PortalException (just for Problems Table)
		PortalException nex=null;
		if (ex instanceof Exception)
			nex = new PortalException(eid, (Exception) ex);
		else
			nex = new PortalException(eid);
		ProblemsTable.store(nex);    
    }

	public static void genericTopHandler(ErrorID eid, Throwable ex) {
		genericTopHandler(eid, null, ex);
	}	

    /**
     * Generate HTML page to send to end user after fatal error
     * @param resp Servlet response object 
     * @param e PortalException received at Servlet code. 
     */
	public static void generateErrorPage(HttpServletResponse resp, Exception e) {
		resp.setContentType("text/html");
		try {
			PrintWriter out = resp.getWriter();
			out.println("<h1>Cannot start uPortal</h1>");
			out.println("<p>Sorry, but a problem is preventing the Portal from starting. "+
				"The error must be corrected by system administrators. Try again later.</p>");
			//out.println("<p><a href='http://www.yale.edu/portal'>Click here to display the static Yaleinfo page.</a></p>");	
			out.println("<!--");
			ErrorID eid = Errors.bug;
			String parm = "";
			if (e instanceof PortalException) {
				PortalException pe = (PortalException)e;
				if (pe.errorID!=null)
					eid=pe.errorID;
				if (pe.parameter!=null)
					parm=pe.parameter;	
			}
			out.println(errorInfo(eid,parm,e));
			out.println("-->");	
			out.flush();
		} catch (Exception ex) {
			;
		}
	}
}