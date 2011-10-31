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

package org.jasig.portal.logging;

import org.slf4j.Logger;
import org.slf4j.Marker;

/**
 * @author Eric Dalquist
 * @version $Revision$
 */
public class ConditionalExceptionLoggerImpl implements ConditionalExceptionLogger {
    private final Logger logger;

    public ConditionalExceptionLoggerImpl(Logger logger) {
        this.logger = logger;
    }

    /* (non-Javadoc)
     * @see org.jasig.portal.logging.ConditionalExceptionLogger#infoDebug(java.lang.String, java.lang.Throwable)
     */
    @Override
    public void infoDebug(String msg, Throwable t) {
        if (this.logger.isDebugEnabled()) {
            this.logger.info(msg, t);
        }
        else {
            this.logger.info(msg + " - " + t.getMessage());
        }
    }

    /* (non-Javadoc)
     * @see org.jasig.portal.logging.ConditionalExceptionLogger#warnDebug(java.lang.String, java.lang.Throwable)
     */
    @Override
    public void warnDebug(String msg, Throwable t) {
        if (this.logger.isDebugEnabled()) {
            this.logger.warn(msg, t);
        }
        else {
            this.logger.warn(msg + " - " + t.getMessage());
        }
    }

    /* (non-Javadoc)
     * @see org.jasig.portal.logging.ConditionalExceptionLogger#errorDebug(java.lang.String, java.lang.Throwable)
     */
    @Override
    public void errorDebug(String msg, Throwable t) {
        if (this.logger.isDebugEnabled()) {
            this.logger.error(msg, t);
        }
        else {
            this.logger.error(msg + " - " + t.getMessage());
        }
    }

    /* (non-Javadoc)
     * @see org.jasig.portal.logging.ConditionalExceptionLogger#infoTrace(java.lang.String, java.lang.Throwable)
     */
    @Override
    public void infoTrace(String msg, Throwable t) {
        if (this.logger.isTraceEnabled()) {
            this.logger.info(msg, t);
        }
        else {
            this.logger.info(msg + " - " + t.getMessage());
        }
    }

    /* (non-Javadoc)
     * @see org.jasig.portal.logging.ConditionalExceptionLogger#warnTrace(java.lang.String, java.lang.Throwable)
     */
    @Override
    public void warnTrace(String msg, Throwable t) {
        if (this.logger.isTraceEnabled()) {
            this.logger.warn(msg, t);
        }
        else {
            this.logger.warn(msg + " - " + t.getMessage());
        }
    }

    /* (non-Javadoc)
     * @see org.jasig.portal.logging.ConditionalExceptionLogger#errorTrace(java.lang.String, java.lang.Throwable)
     */
    @Override
    public void errorTrace(String msg, Throwable t) {
        if (this.logger.isTraceEnabled()) {
            this.logger.error(msg, t);
        }
        else {
            this.logger.error(msg + " - " + t.getMessage());
        }
    }

    @Override
    public String getName() {
        return this.logger.getName();
    }

    @Override
    public boolean isTraceEnabled() {
        return this.logger.isTraceEnabled();
    }

    @Override
    public void trace(String msg) {
        this.logger.trace(msg);
    }

    @Override
    public void trace(String format, Object arg) {
        this.logger.trace(format, arg);
    }

    @Override
    public void trace(String format, Object arg1, Object arg2) {
        this.logger.trace(format, arg1, arg2);
    }

    @Override
    public void trace(String format, Object[] argArray) {
        this.logger.trace(format, argArray);
    }

    @Override
    public void trace(String msg, Throwable t) {
        this.logger.trace(msg, t);
    }

    @Override
    public boolean isTraceEnabled(Marker marker) {
        return this.logger.isTraceEnabled(marker);
    }

    @Override
    public void trace(Marker marker, String msg) {
        this.logger.trace(marker, msg);
    }

    @Override
    public void trace(Marker marker, String format, Object arg) {
        this.logger.trace(marker, format, arg);
    }

    @Override
    public void trace(Marker marker, String format, Object arg1, Object arg2) {
        this.logger.trace(marker, format, arg1, arg2);
    }

    @Override
    public void trace(Marker marker, String format, Object[] argArray) {
        this.logger.trace(marker, format, argArray);
    }

    @Override
    public void trace(Marker marker, String msg, Throwable t) {
        this.logger.trace(marker, msg, t);
    }

    @Override
    public boolean isDebugEnabled() {
        return this.logger.isDebugEnabled();
    }

    @Override
    public void debug(String msg) {
        this.logger.debug(msg);
    }

    @Override
    public void debug(String format, Object arg) {
        this.logger.debug(format, arg);
    }

    @Override
    public void debug(String format, Object arg1, Object arg2) {
        this.logger.debug(format, arg1, arg2);
    }

    @Override
    public void debug(String format, Object[] argArray) {
        this.logger.debug(format, argArray);
    }

    @Override
    public void debug(String msg, Throwable t) {
        this.logger.debug(msg, t);
    }

    @Override
    public boolean isDebugEnabled(Marker marker) {
        return this.logger.isDebugEnabled(marker);
    }

    @Override
    public void debug(Marker marker, String msg) {
        this.logger.debug(marker, msg);
    }

    @Override
    public void debug(Marker marker, String format, Object arg) {
        this.logger.debug(marker, format, arg);
    }

    @Override
    public void debug(Marker marker, String format, Object arg1, Object arg2) {
        this.logger.debug(marker, format, arg1, arg2);
    }

    @Override
    public void debug(Marker marker, String format, Object[] argArray) {
        this.logger.debug(marker, format, argArray);
    }

    @Override
    public void debug(Marker marker, String msg, Throwable t) {
        this.logger.debug(marker, msg, t);
    }

    @Override
    public boolean isInfoEnabled() {
        return this.logger.isInfoEnabled();
    }

    @Override
    public void info(String msg) {
        this.logger.info(msg);
    }

    @Override
    public void info(String format, Object arg) {
        this.logger.info(format, arg);
    }

    @Override
    public void info(String format, Object arg1, Object arg2) {
        this.logger.info(format, arg1, arg2);
    }

    @Override
    public void info(String format, Object[] argArray) {
        this.logger.info(format, argArray);
    }

    @Override
    public void info(String msg, Throwable t) {
        this.logger.info(msg, t);
    }

    @Override
    public boolean isInfoEnabled(Marker marker) {
        return this.logger.isInfoEnabled(marker);
    }

    @Override
    public void info(Marker marker, String msg) {
        this.logger.info(marker, msg);
    }

    @Override
    public void info(Marker marker, String format, Object arg) {
        this.logger.info(marker, format, arg);
    }

    @Override
    public void info(Marker marker, String format, Object arg1, Object arg2) {
        this.logger.info(marker, format, arg1, arg2);
    }

    @Override
    public void info(Marker marker, String format, Object[] argArray) {
        this.logger.info(marker, format, argArray);
    }

    @Override
    public void info(Marker marker, String msg, Throwable t) {
        this.logger.info(marker, msg, t);
    }

    @Override
    public boolean isWarnEnabled() {
        return this.logger.isWarnEnabled();
    }

    @Override
    public void warn(String msg) {
        this.logger.warn(msg);
    }

    @Override
    public void warn(String format, Object arg) {
        this.logger.warn(format, arg);
    }

    @Override
    public void warn(String format, Object[] argArray) {
        this.logger.warn(format, argArray);
    }

    @Override
    public void warn(String format, Object arg1, Object arg2) {
        this.logger.warn(format, arg1, arg2);
    }

    @Override
    public void warn(String msg, Throwable t) {
        this.logger.warn(msg, t);
    }

    @Override
    public boolean isWarnEnabled(Marker marker) {
        return this.logger.isWarnEnabled(marker);
    }

    @Override
    public void warn(Marker marker, String msg) {
        this.logger.warn(marker, msg);
    }

    @Override
    public void warn(Marker marker, String format, Object arg) {
        this.logger.warn(marker, format, arg);
    }

    @Override
    public void warn(Marker marker, String format, Object arg1, Object arg2) {
        this.logger.warn(marker, format, arg1, arg2);
    }

    @Override
    public void warn(Marker marker, String format, Object[] argArray) {
        this.logger.warn(marker, format, argArray);
    }

    @Override
    public void warn(Marker marker, String msg, Throwable t) {
        this.logger.warn(marker, msg, t);
    }

    @Override
    public boolean isErrorEnabled() {
        return this.logger.isErrorEnabled();
    }

    @Override
    public void error(String msg) {
        this.logger.error(msg);
    }

    @Override
    public void error(String format, Object arg) {
        this.logger.error(format, arg);
    }

    @Override
    public void error(String format, Object arg1, Object arg2) {
        this.logger.error(format, arg1, arg2);
    }

    @Override
    public void error(String format, Object[] argArray) {
        this.logger.error(format, argArray);
    }

    @Override
    public void error(String msg, Throwable t) {
        this.logger.error(msg, t);
    }

    @Override
    public boolean isErrorEnabled(Marker marker) {
        return this.logger.isErrorEnabled(marker);
    }

    @Override
    public void error(Marker marker, String msg) {
        this.logger.error(marker, msg);
    }

    @Override
    public void error(Marker marker, String format, Object arg) {
        this.logger.error(marker, format, arg);
    }

    @Override
    public void error(Marker marker, String format, Object arg1, Object arg2) {
        this.logger.error(marker, format, arg1, arg2);
    }

    @Override
    public void error(Marker marker, String format, Object[] argArray) {
        this.logger.error(marker, format, argArray);
    }

    @Override
    public void error(Marker marker, String msg, Throwable t) {
        this.logger.error(marker, msg, t);
    }
}
