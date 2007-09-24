/* Copyright 2005 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/

package org.jasig.portal.tools.checks;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Safely executes a configured List of checks.
 * @version $Revision$ $Date$
 * @since uPortal 2.5
 */
public class BasicCheckRunner implements ICheckRunner {

    protected Log log = LogFactory.getLog(getClass());
    
    private List checks = Collections.EMPTY_LIST;
    
    public List doChecks() {
        // List of CheckAndResult instances
        List checksAndResults = new ArrayList(this.checks.size());
        
        for (Iterator iter = this.checks.iterator(); iter.hasNext(); ) {
            Object listEntry = iter.next();
            if (listEntry instanceof ICheck) {

                ICheck check = (ICheck) listEntry;
                CheckAndResult result = executeCheck(check);
                checksAndResults.add(result);
                
            } else {
                // entry in list of checks was not actually an instance of ICheck.
                // log this annoyance.
                
                String classWas = listEntry.getClass().getName();
                CheckResult entryNotAnICheck = CheckResult
                        .createFailure(
                                "Entry in list of checks did not implement ICheck.  "
                                        + "It was an instance of ["
                                        + classWas + "]",
                                "Fix the list of checks to include only instances of ICheck.");

                CheckAndResult notAnEntryCheckAndResult = new CheckAndResult(
                        "Check whether entries in the list of checks we're going to execute implement the required ICheck interface.",
                        entryNotAnICheck);
                checksAndResults.add(notAnEntryCheckAndResult);

            }
            
        }
        
        
        return checksAndResults;
    }
    
    
    /**
     * Helper method to safely execute a given check.
     * @param check
     * @return
     */
    private CheckAndResult executeCheck(ICheck check) {
        
        // default the check description in case the check fails to provide one
        String checkDescription = "Check failed to provide a description.";
        
        try {
            checkDescription = check.getDescription();
            CheckResult result = check.doCheck();
            return new CheckAndResult(checkDescription, result);
        } catch (Throwable t) {
            // why catch throwable?  Because no matter what goes wrong, we
            // don't want any check to be able to break this CheckRunner.
            
            // we especially log here to get this stack trace in the log
            log.error("Check failed by throwing.", t);
            
            String message = "Check failed by throwing exception. " + t;
            
            String remediation = "Since the check failed by throwing an exception, we cannot advise any particular solution.";
            
            CheckResult result = CheckResult.createFailure(message, remediation);
            
            
            return new CheckAndResult(checkDescription, result);
            
        }
        
    }
    
    /**
     * @return Returns the checks.
     */
    public List getChecks() {
        return this.checks;
    }
    
    /**
     * Set the List of checks to be executed by this CheckRunner instance.
     * @param checks a non-null list of ICheck instances.
     */
    public void setChecks(List checks) {
        if (checks == null) {
            throw new IllegalArgumentException("Cannot set checks to null.");
        }
        
        this.checks = checks;
    }
}

