/**
 * Copyright © 2004 The JA-SIG Collaborative.  All rights reserved.
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
        this.log.error( aThrowable);
    }

}
