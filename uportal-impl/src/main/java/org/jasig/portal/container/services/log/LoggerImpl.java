/* Copyright 2004 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/

package org.jasig.portal.container.services.log;

import org.apache.pluto.services.log.Logger;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Implementation of Apache Pluto Logger by delegation to Apache Commons Logging.
 * @author Ken Weiner, kweiner@unicon.net
 * @author andrew.petro@yale.edu
 * @version $Revision$
 */
public class LoggerImpl implements Logger {
    private final Log log;
    
    public LoggerImpl() {
        // TODO: revisit to obtain correct logger for this case.
        this.log = LogFactory.getLog(LoggerImpl.class);
    }
    
    public LoggerImpl(String component) {
        this.log = LogFactory.getLog(component);
    }
    
    /**
     * Instantiate a LoggerImpl for a particular class.
     * This implementation passes the class to the Commons
     * LogFactory to obtain the underlying Apache Commons Log 
     * implementation.
     * @param klass - the class for which a logger is desired
     */
    public LoggerImpl(Class klass) {
        this.log = LogFactory.getLog(klass);
    }

    // Logger methods
    
    public boolean isDebugEnabled() {
        return this.log.isDebugEnabled();
    }

    public boolean isInfoEnabled() {
        return this.log.isInfoEnabled();
    }

    public boolean isWarnEnabled() {
        return this.log.isWarnEnabled();
    }

    public boolean isErrorEnabled() {
        return this.log.isErrorEnabled();
    }

    public void debug(String aMessage) {
        this.log.debug(aMessage);
    }

    public void debug(String aMessage, Throwable aThrowable) {
        this.log.debug(aMessage, aThrowable);
    }

    public void info(String aMessage) {
        this.log.info(aMessage);
    }

    public void warn(String aMessage) {
        this.log.warn(aMessage);
    }

    public void error(String aMessage) {
        this.log.error(aMessage);
    }
    
    public void error(String aMessage, Throwable aThrowable) {
        this.log.error(aMessage, aThrowable);
    }    
    
    public void error(Throwable aThrowable) {
        this.log.error(aThrowable, aThrowable);
    }

}
