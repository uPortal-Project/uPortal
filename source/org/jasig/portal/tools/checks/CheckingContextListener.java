/* Copyright 2005 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/

package org.jasig.portal.tools.checks;

import java.util.Iterator;
import java.util.List;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * This context listener executes SafeDelegatingCheckRunner, logs its results,
 * and exposes its results in the ServletContext.  
 * 
 * The intent is that the ICheckRunner delegate of SafeDelegatingCheckRunner,
 * configured in Spring, be configured to run useful runtime sanity checks that will
 * help uPortal deployers to understand what went wrong in their deployments
 * when dependencies are not present.
 * 
 * 
 * The current implementation logs successes only via Commons Logging whereas
 * it logs failures both via Commons Logging and via the Servlet API's logging 
 * mechanism and to System.err.  This overkill of logging is intended to give the 
 * primary intended audience --
 * new uPortal deployers who may not yet be comfortable with configuring uPortal
 * logging -- the maximum chance of encountering the output.
 * 
 * Subclassing this class: this class is designed to be subclassed to replace the
 * implementation of the method that logs the results from the SafeDelegatingCheckRunner.
 * 
 * @author andrew.petro@yale.edu
 * @version $Revision$ $Date$
 */
public class CheckingContextListener 
    implements ServletContextListener {
    
    /**
     * The name of the servlet context attribute which this listener will set at
     * context initialization to contain the List of CheckAndResult instances representing
     * the results of running the configured checks.
     */
    public static final String RESULTS_SC_KEY = "org.jasig.portal.tools.checks.CheckingContextListener.RESULTS";
    
    protected final Log log = LogFactory.getLog(getClass());
    
    public final void contextInitialized(ServletContextEvent sce) {
        
        ServletContext servletContext = sce.getServletContext();
        
        SafeDelegatingCheckRunner checkRunner = new SafeDelegatingCheckRunner();
            
        List results = checkRunner.doChecks();
        
        logResults(results, servletContext);
        
        // we've already logged the results of each individual check.  Now we
        // expose the results of these checks in the ServletContext so that 
        // views such as JSPs, channels can render them.
        
        servletContext.setAttribute(RESULTS_SC_KEY, results);
 
    }

    public void contextDestroyed(ServletContextEvent sce) {
        // do nothing
    }

   
    
    /**
     * Log the results reported by the check runner.
     * 
     * You can subclass this class and override this method to change the
     * logging behavior.
     * 
     * @param results List of CheckAndResult instances
     */
    protected void logResults(List results, ServletContext servletContext) {
        
        if (results == null) {
            log.error("Cannot log null results.");
            return;
        }
        
        for (Iterator iter = results.iterator(); iter.hasNext(); ) {
            
            try {
                CheckAndResult checkAndResult = (CheckAndResult) iter.next();
                
                if (checkAndResult.isSuccess()) {
                    log.info("Check [" + checkAndResult.getCheckDescription() + "] succeeded with message [" + checkAndResult.getResult().getMessage() + "]");
                } else {
                    // the following overkill of logging is intended to help deployers who
                    // might not know where to look find a record of what went wrong.
                    
                    String logMessage = 
                        "Check [" + checkAndResult.getCheckDescription() + "] failed with message [" + 
                        checkAndResult.getResult().getMessage() + "] and remediation advice [" + 
                        checkAndResult.getResult().getRemediationAdvice() + "]";
                    
                    
                    log.fatal(logMessage);
                    System.err.println(logMessage);
                    servletContext.log(logMessage);
                }
            } catch (Throwable t) {
                // we cannot let a logging error break our context listener and thereby
                // bring down our whole application context.
                
                log.error("Error in logging results of checks", t);
            }
            
           
        }
        
       
    }
    
}
