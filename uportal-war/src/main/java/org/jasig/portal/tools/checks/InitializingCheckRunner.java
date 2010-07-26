/**
 * Licensed to Jasig under one or more contributor license
 * agreements. See the NOTICE file distributed with this work
 * for additional information regarding copyright ownership.
 * Jasig licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a
 * copy of the License at:
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.jasig.portal.tools.checks;

import java.util.List;

import javax.servlet.ServletContext;

import org.apache.commons.lang.Validate;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.BeanInitializationException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.web.context.ServletContextAware;

/**
 * Runs an injected {@link ICheckRunner} when {@link #afterPropertiesSet()} is called and
 * logs the results.
 * 
 * @author Eric Dalquist
 * @version $Revision$
 */
public class InitializingCheckRunner implements InitializingBean, ServletContextAware {
    public static final String INIT_CHECK_RESULTS = "INIT_CHECK_RESULTS";

    protected final Log logger = LogFactory.getLog(this.getClass());
    private ServletContext servletContext;
    private ICheckRunner checkRunner;
    

    /**
     * @return the checkRunner
     */
    public ICheckRunner getCheckRunner() {
        return this.checkRunner;
    }
    /**
     * @param checkRunner the checkRunner to set
     */
    @Required
    public void setCheckRunner(ICheckRunner checkRunner) {
        Validate.notNull(checkRunner, "ICheckRunner must not be null");
        this.checkRunner = checkRunner;
    }

    /* (non-Javadoc)
     * @see org.springframework.beans.factory.InitializingBean#afterPropertiesSet()
     */
    public void afterPropertiesSet() throws Exception {
        final List<CheckAndResult> results = this.checkRunner.doChecks();
        
        if (this.servletContext != null) {
            this.servletContext.setAttribute(INIT_CHECK_RESULTS, results);
        }
        else {
            this.logger.warn("No ServletContext is available, the results will not be acessible");
        }
        
        this.logResults(results);
    }
    
    /* (non-Javadoc)
     * @see org.springframework.web.context.ServletContextAware#setServletContext(javax.servlet.ServletContext)
     */
    public void setServletContext(ServletContext servletContext) {
        this.servletContext = servletContext;
    }
    
    /**
     * Log the results reported by the check runner.
     * 
     * You can subclass this class and override this method to change the
     * logging behavior.  You can also, by overriding this method and throwing
     * a RuntimeException, veto the loading of the context.
     * 
     * The default implementation of this method is safe such that it will not throw.
     * Since the rest of contextInitialized is also safe, the default implementation
     * of this ContextListener is safe and will not, no matter how abjectly the
     * checks fail, itself abort the context initialization.
     * 
     * @param results List of CheckAndResult instances
     * @param servletContext the context in which we're running
     */
    protected void logResults(List<CheckAndResult> results) {
        if (results == null) {
            logger.error("Cannot log null results.");
            return;
        }
        
        final StringBuilder fatalErrorMsg = new StringBuilder();

        for (final CheckAndResult checkAndResult : results) {
            try {
                if (checkAndResult.isSuccess()) {
                    logger.info("Check [" + checkAndResult.getCheckDescription() + "] succeeded with message ["
                            + checkAndResult.getResult().getMessage() + "]");
                }
                else {
                    // the following overkill of logging is intended to help deployers who
                    // might not know where to look find a record of what went wrong.

                    final String logMessage = "Check [" + checkAndResult.getCheckDescription() + "] failed with message ["
                            + checkAndResult.getResult().getMessage() + "] and remediation advice ["
                            + checkAndResult.getResult().getRemediationAdvice() + "]";
                    
                    if (checkAndResult.isFatal()) {
                        fatalErrorMsg.append(logMessage).append('\n');
                    }

                    logger.fatal(logMessage);
                    System.err.println(logMessage);
                }
            }
            catch (Throwable t) {
                // we cannot let a logging error break our context listener and thereby
                // bring down our whole application context.

                logger.error("Error in logging results of check: " + checkAndResult, t);
            }
            
            if (fatalErrorMsg.length() > 0) {
                throw new BeanInitializationException(fatalErrorMsg.toString());
            }
        }

    }
}
