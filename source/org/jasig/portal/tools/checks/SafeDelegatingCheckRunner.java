/* Copyright 2005 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/

package org.jasig.portal.tools.checks;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jasig.portal.spring.PortalApplicationContextFacade;

/**
 * SafeDelegatingCheckRunner safely attempts to delegate to a Spring-configured
 * ICheckRunner.  Even if Spring isn't present or that ICheckRunner is misconfigured
 * or broken, instances of this class will still implement the ICheckRunner API - that is,
 * they will express this failure as a return value from the interface method.
 * @author andrew.petro@yale.edu
 * @version $Revision$ $Date$
 */
public class SafeDelegatingCheckRunner
    implements ICheckRunner {

    private Log log = LogFactory.getLog(getClass());
    
    /**
     * The name of the Spring bean we expect will be an instance of ICheckRunner
     * to which we will be delegating.
     */
    public static final String CHECKS_KEY = "checkRunner";
    
    public List doChecks() {
        
        // check our own dependencies
        
        // List of checks for our own dependencies
        List localChecks = new ArrayList();
        localChecks.add(new SpringPresenceCheck());
        localChecks.add(new SpringBeanCheck(CHECKS_KEY, ICheckRunner.class.getName()));
        
        BasicCheckRunner localCheckRunner = new BasicCheckRunner();
        localCheckRunner.setChecks(localChecks);
        
        List results = localCheckRunner.doChecks();
        
        if (! containsFailedCheck(results)) {
            
            try {
                // great, local dependencies look good, let's try to run the
                // Spring configured ICheckRunner.
                
                ICheckRunner configuredDelegate = (ICheckRunner) PortalApplicationContextFacade.getPortalApplicationContext().getBean(SafeDelegatingCheckRunner.CHECKS_KEY, ICheckRunner.class);
                results.addAll(configuredDelegate.doChecks());
                
                
            } catch (Throwable t) {
                log.error("Failed to obtain Spring-configured ICheckRunner named [" + CHECKS_KEY + "].", t);
                CheckResult delegateFailedResult = CheckResult.createFailure("The Spring-configured ICheckRunner threw an exception: " + t, "Examine the configuration of the Spring bean named [" + SafeDelegatingCheckRunner.CHECKS_KEY + "]");
                CheckAndResult delegateFailedCheckAndResult = new CheckAndResult("Check that the configured ICheckRunner delegate did not throw any Throwable.", delegateFailedResult);
                results.add(delegateFailedCheckAndResult);
            }
            
        }
        
        return results;
    }

    /**
     * Return true if any CheckAndResult in the list of results is not a success.
     * @param results a List of CheckAndResult instances.
     * @return true if any failures, false otherwise
     */
    private boolean containsFailedCheck(List results) {
        for (Iterator iter = results.iterator(); iter.hasNext(); ) {
            CheckAndResult checkAndResult = (CheckAndResult) iter.next();
            if (! checkAndResult.isSuccess()) {
                return true;
            }
        }
        return false;
    }

}
