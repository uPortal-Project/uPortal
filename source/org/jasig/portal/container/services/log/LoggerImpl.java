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
import org.jasig.portal.services.LogService;

/**
 * Implementation of Apache Pluto Logger.
 * @author Ken Weiner, kweiner@unicon.net
 * @version $Revision$
 */
public class LoggerImpl implements Logger {
    
    String component = null;
    Class klass = null;
    
    public LoggerImpl() {
    }
    
    public LoggerImpl(String component) {
        this.component = component;
    }
    
    public LoggerImpl(Class klass) {
        this.klass = klass;
    }

    // Logger methods
    
    public boolean isDebugEnabled() {
        return true;
    }

    public boolean isInfoEnabled() {
        return true;
    }

    public boolean isWarnEnabled() {
        return true;
    }

    public boolean isErrorEnabled() {
        return true;
    }

    public void debug(String aMessage) {
        LogService.log(LogService.DEBUG, createMessage(aMessage));
    }

    public void debug(String aMessage, Throwable aThrowable) {
        LogService.log(LogService.DEBUG, createMessage(aMessage), aThrowable);
    }

    public void info(String aMessage) {
        LogService.log(LogService.INFO, createMessage(aMessage));
    }

    public void warn(String aMessage) {
        LogService.log(LogService.WARN, createMessage(aMessage));
    }

    public void error(String aMessage) {
        LogService.log(LogService.ERROR, createMessage(aMessage));
    }
    
    public void error(String aMessage, Throwable aThrowable) {
        LogService.log(LogService.ERROR, createMessage(aMessage), aThrowable);
    }    
    
    public void error(Throwable aThrowable) {
        LogService.log(LogService.ERROR, aThrowable);
    }
    
    protected String createMessage(String aMessage) {
        StringBuffer sb = new StringBuffer(512);
        if (component != null) {
            sb.append(component).append(": ");
        } else if (klass != null) {
            sb.append(klass.getName()).append(": ");
        }
        sb.append(aMessage);
        return sb.toString();
    }

}
