/* Copyright 2001 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/

package org.jasig.portal;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
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
        return trimStackTrace(stktr);
    }
    
    /**
     * Trims a String representation of a Stack Trace to remove
     * the portion of the trace that is in the servlet container layer.
     * @param stackTrace - String result of printStackTrace
     * @return the stack trace with portions of the trace that dive into the container
     * layer removed.
     */
    static String trimStackTrace(String stackTrace) {
        
        StringBuffer trimmedTrace = new StringBuffer();
        
        // a List of Strings to be trimmed and appended to the buffer
        // these represent elements in the causal chain
        List fragments = new ArrayList();
        
        int causeCut = (stackTrace.indexOf("Caused by"));
        
        if (causeCut > 0) {
            // there are one or more Caused by fragments to consider
            // we traverse stackTrace, parsing out fragments for later processing
            // and updating stackTrace to contain the remaining unparsed portion
            // as we go
            
            while (stackTrace.length() > 0) {
                
                if (stackTrace.startsWith("Caused by")){
                    // don't count the "Caused by" leading the stackTrace
                    causeCut = stackTrace.substring(9).indexOf("Caused by");
                    if (causeCut > 0)
                        causeCut += 9;
                } else {
                    causeCut = stackTrace.indexOf("Caused by");
                }
                
                if (causeCut > -1) {
                    // stackTrace currently includes multiple fragments
                    // parse out the first and leave the rest for next iteration
                    
                    fragments.add(stackTrace.substring(0, causeCut));
                    stackTrace = stackTrace.substring(causeCut);
                } else {
                    // stackTrace currently is a bare fragment
                    // grab it
                    fragments.add(stackTrace);
                    stackTrace = "";
                }
            }
        } else {
            // there's ony a single Throwable in the chain
            fragments.add(stackTrace);
        }
        
        // now that we have fragments to consider
        
        for (Iterator iter = fragments.iterator(); iter.hasNext();){
            String consideredFragment = (String) iter.next();
            
            // flag to indicate that a trimmed form of this fragment has been appended
            // to the trimmed stack trace buffer
            boolean appended = false;
            for (int i=0; i < boundaries.length; i++) {
                int cut = consideredFragment.indexOf(boundaries[i]);
                if (cut > 0) {
                    // stack trace includes a trace through our container
                    // in which we are not interested: trim it.           
                    // grab the desired portion up to the boundary
                    trimmedTrace.append(consideredFragment.substring(0, cut).trim());
                    trimmedTrace.append("\n");
                    appended = true;
                    break;
                }
            }
            
            if (! appended) {
                // a trimmed version of this fragment was not appended
                // because it doesn't need to be trimmed -- append the whole thing.
                trimmedTrace.append(consideredFragment.trim());
                trimmedTrace.append("\n");
            }
        }
        
        return trimmedTrace.toString();
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