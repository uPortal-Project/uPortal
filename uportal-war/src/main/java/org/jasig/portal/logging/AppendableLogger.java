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

import java.io.IOException;

import org.apache.commons.lang.Validate;
import org.slf4j.Logger;
import org.slf4j.Marker;

/**
 * Wraps an SLF4J Logger adding in the Appendable interface. Requires the log
 * level to use when appending to be specified in the constructor
 * 
 * @author Eric Dalquist
 * @version $Revision$
 */
public class AppendableLogger implements Appendable, Logger {
    public enum Level {
        TRACE {
            @Override
            public void log(Logger logger, String msg) {
                logger.trace(msg);
            }
        },
        DEBUG {
            @Override
            public void log(Logger logger, String msg) {
                logger.debug(msg);
            }
        },
        INFO {
            @Override
            public void log(Logger logger, String msg) {
                logger.info(msg);
            }
        },
        WARN {
            @Override
            public void log(Logger logger, String msg) {
                logger.warn(msg);
            }
        },
        ERROR {
            @Override
            public void log(Logger logger, String msg) {
                logger.error(msg);
            }
        };

        public abstract void log(Logger logger, String msg);
    }

    private static final String NEWLINE = System.getProperty("line.separator");
    private final StringBuffer builder = new StringBuffer();
    private final Logger logger;
    private final Level appendLevel;

    public AppendableLogger(Logger logger, Level appendLevel) {
        Validate.notNull(logger);
        Validate.notNull(appendLevel);

        this.logger = logger;
        this.appendLevel = appendLevel;
    }
    
    private void logIfNeeded() {
        while (true) {
            final int newlineIndex = builder.indexOf(NEWLINE);
            if (newlineIndex < 0) {
                return;
            }
            if (newlineIndex == 0) {
                builder.delete(0, NEWLINE.length());
                this.appendLevel.log(this.logger, "");
            }
            
            final String msg = builder.substring(0, newlineIndex);
            builder.delete(0, newlineIndex + NEWLINE.length());
            this.appendLevel.log(this.logger, msg);
        }
    }

    /* (non-Javadoc)
     * @see java.lang.Appendable#append(java.lang.CharSequence)
     */
    @Override
    public Appendable append(CharSequence csq) throws IOException {
        builder.append(csq);
        logIfNeeded();
        return this;
    }

    /* (non-Javadoc)
     * @see java.lang.Appendable#append(java.lang.CharSequence, int, int)
     */
    @Override
    public Appendable append(CharSequence csq, int start, int end) throws IOException {
        builder.append(csq, start, end);
        logIfNeeded();
        return this;
    }

    /* (non-Javadoc)
     * @see java.lang.Appendable#append(char)
     */
    @Override
    public Appendable append(char c) throws IOException {
        builder.append(c);
        logIfNeeded();
        return this;
    }

    /**
     * @return
     * @see org.slf4j.Logger#getName()
     */
    @Override
    public String getName() {
        return this.logger.getName();
    }

    /**
     * @return
     * @see org.slf4j.Logger#isTraceEnabled()
     */
    @Override
    public boolean isTraceEnabled() {
        return this.logger.isTraceEnabled();
    }

    /**
     * @param msg
     * @see org.slf4j.Logger#trace(java.lang.String)
     */
    @Override
    public void trace(String msg) {
        this.logger.trace(msg);
    }

    /**
     * @param format
     * @param arg
     * @see org.slf4j.Logger#trace(java.lang.String, java.lang.Object)
     */
    @Override
    public void trace(String format, Object arg) {
        this.logger.trace(format, arg);
    }

    /**
     * @param format
     * @param arg1
     * @param arg2
     * @see org.slf4j.Logger#trace(java.lang.String, java.lang.Object, java.lang.Object)
     */
    @Override
    public void trace(String format, Object arg1, Object arg2) {
        this.logger.trace(format, arg1, arg2);
    }

    /**
     * @param format
     * @param argArray
     * @see org.slf4j.Logger#trace(java.lang.String, java.lang.Object[])
     */
    @Override
    public void trace(String format, Object[] argArray) {
        this.logger.trace(format, argArray);
    }

    /**
     * @param msg
     * @param t
     * @see org.slf4j.Logger#trace(java.lang.String, java.lang.Throwable)
     */
    @Override
    public void trace(String msg, Throwable t) {
        this.logger.trace(msg, t);
    }

    /**
     * @param marker
     * @return
     * @see org.slf4j.Logger#isTraceEnabled(org.slf4j.Marker)
     */
    @Override
    public boolean isTraceEnabled(Marker marker) {
        return this.logger.isTraceEnabled(marker);
    }

    /**
     * @param marker
     * @param msg
     * @see org.slf4j.Logger#trace(org.slf4j.Marker, java.lang.String)
     */
    @Override
    public void trace(Marker marker, String msg) {
        this.logger.trace(marker, msg);
    }

    /**
     * @param marker
     * @param format
     * @param arg
     * @see org.slf4j.Logger#trace(org.slf4j.Marker, java.lang.String, java.lang.Object)
     */
    @Override
    public void trace(Marker marker, String format, Object arg) {
        this.logger.trace(marker, format, arg);
    }

    /**
     * @param marker
     * @param format
     * @param arg1
     * @param arg2
     * @see org.slf4j.Logger#trace(org.slf4j.Marker, java.lang.String, java.lang.Object, java.lang.Object)
     */
    @Override
    public void trace(Marker marker, String format, Object arg1, Object arg2) {
        this.logger.trace(marker, format, arg1, arg2);
    }

    /**
     * @param marker
     * @param format
     * @param argArray
     * @see org.slf4j.Logger#trace(org.slf4j.Marker, java.lang.String, java.lang.Object[])
     */
    @Override
    public void trace(Marker marker, String format, Object[] argArray) {
        this.logger.trace(marker, format, argArray);
    }

    /**
     * @param marker
     * @param msg
     * @param t
     * @see org.slf4j.Logger#trace(org.slf4j.Marker, java.lang.String, java.lang.Throwable)
     */
    @Override
    public void trace(Marker marker, String msg, Throwable t) {
        this.logger.trace(marker, msg, t);
    }

    /**
     * @return
     * @see org.slf4j.Logger#isDebugEnabled()
     */
    @Override
    public boolean isDebugEnabled() {
        return this.logger.isDebugEnabled();
    }

    /**
     * @param msg
     * @see org.slf4j.Logger#debug(java.lang.String)
     */
    @Override
    public void debug(String msg) {
        this.logger.debug(msg);
    }

    /**
     * @param format
     * @param arg
     * @see org.slf4j.Logger#debug(java.lang.String, java.lang.Object)
     */
    @Override
    public void debug(String format, Object arg) {
        this.logger.debug(format, arg);
    }

    /**
     * @param format
     * @param arg1
     * @param arg2
     * @see org.slf4j.Logger#debug(java.lang.String, java.lang.Object, java.lang.Object)
     */
    @Override
    public void debug(String format, Object arg1, Object arg2) {
        this.logger.debug(format, arg1, arg2);
    }

    /**
     * @param format
     * @param argArray
     * @see org.slf4j.Logger#debug(java.lang.String, java.lang.Object[])
     */
    @Override
    public void debug(String format, Object[] argArray) {
        this.logger.debug(format, argArray);
    }

    /**
     * @param msg
     * @param t
     * @see org.slf4j.Logger#debug(java.lang.String, java.lang.Throwable)
     */
    @Override
    public void debug(String msg, Throwable t) {
        this.logger.debug(msg, t);
    }

    /**
     * @param marker
     * @return
     * @see org.slf4j.Logger#isDebugEnabled(org.slf4j.Marker)
     */
    @Override
    public boolean isDebugEnabled(Marker marker) {
        return this.logger.isDebugEnabled(marker);
    }

    /**
     * @param marker
     * @param msg
     * @see org.slf4j.Logger#debug(org.slf4j.Marker, java.lang.String)
     */
    @Override
    public void debug(Marker marker, String msg) {
        this.logger.debug(marker, msg);
    }

    /**
     * @param marker
     * @param format
     * @param arg
     * @see org.slf4j.Logger#debug(org.slf4j.Marker, java.lang.String, java.lang.Object)
     */
    @Override
    public void debug(Marker marker, String format, Object arg) {
        this.logger.debug(marker, format, arg);
    }

    /**
     * @param marker
     * @param format
     * @param arg1
     * @param arg2
     * @see org.slf4j.Logger#debug(org.slf4j.Marker, java.lang.String, java.lang.Object, java.lang.Object)
     */
    @Override
    public void debug(Marker marker, String format, Object arg1, Object arg2) {
        this.logger.debug(marker, format, arg1, arg2);
    }

    /**
     * @param marker
     * @param format
     * @param argArray
     * @see org.slf4j.Logger#debug(org.slf4j.Marker, java.lang.String, java.lang.Object[])
     */
    @Override
    public void debug(Marker marker, String format, Object[] argArray) {
        this.logger.debug(marker, format, argArray);
    }

    /**
     * @param marker
     * @param msg
     * @param t
     * @see org.slf4j.Logger#debug(org.slf4j.Marker, java.lang.String, java.lang.Throwable)
     */
    @Override
    public void debug(Marker marker, String msg, Throwable t) {
        this.logger.debug(marker, msg, t);
    }

    /**
     * @return
     * @see org.slf4j.Logger#isInfoEnabled()
     */
    @Override
    public boolean isInfoEnabled() {
        return this.logger.isInfoEnabled();
    }

    /**
     * @param msg
     * @see org.slf4j.Logger#info(java.lang.String)
     */
    @Override
    public void info(String msg) {
        this.logger.info(msg);
    }

    /**
     * @param format
     * @param arg
     * @see org.slf4j.Logger#info(java.lang.String, java.lang.Object)
     */
    @Override
    public void info(String format, Object arg) {
        this.logger.info(format, arg);
    }

    /**
     * @param format
     * @param arg1
     * @param arg2
     * @see org.slf4j.Logger#info(java.lang.String, java.lang.Object, java.lang.Object)
     */
    @Override
    public void info(String format, Object arg1, Object arg2) {
        this.logger.info(format, arg1, arg2);
    }

    /**
     * @param format
     * @param argArray
     * @see org.slf4j.Logger#info(java.lang.String, java.lang.Object[])
     */
    @Override
    public void info(String format, Object[] argArray) {
        this.logger.info(format, argArray);
    }

    /**
     * @param msg
     * @param t
     * @see org.slf4j.Logger#info(java.lang.String, java.lang.Throwable)
     */
    @Override
    public void info(String msg, Throwable t) {
        this.logger.info(msg, t);
    }

    /**
     * @param marker
     * @return
     * @see org.slf4j.Logger#isInfoEnabled(org.slf4j.Marker)
     */
    @Override
    public boolean isInfoEnabled(Marker marker) {
        return this.logger.isInfoEnabled(marker);
    }

    /**
     * @param marker
     * @param msg
     * @see org.slf4j.Logger#info(org.slf4j.Marker, java.lang.String)
     */
    @Override
    public void info(Marker marker, String msg) {
        this.logger.info(marker, msg);
    }

    /**
     * @param marker
     * @param format
     * @param arg
     * @see org.slf4j.Logger#info(org.slf4j.Marker, java.lang.String, java.lang.Object)
     */
    @Override
    public void info(Marker marker, String format, Object arg) {
        this.logger.info(marker, format, arg);
    }

    /**
     * @param marker
     * @param format
     * @param arg1
     * @param arg2
     * @see org.slf4j.Logger#info(org.slf4j.Marker, java.lang.String, java.lang.Object, java.lang.Object)
     */
    @Override
    public void info(Marker marker, String format, Object arg1, Object arg2) {
        this.logger.info(marker, format, arg1, arg2);
    }

    /**
     * @param marker
     * @param format
     * @param argArray
     * @see org.slf4j.Logger#info(org.slf4j.Marker, java.lang.String, java.lang.Object[])
     */
    @Override
    public void info(Marker marker, String format, Object[] argArray) {
        this.logger.info(marker, format, argArray);
    }

    /**
     * @param marker
     * @param msg
     * @param t
     * @see org.slf4j.Logger#info(org.slf4j.Marker, java.lang.String, java.lang.Throwable)
     */
    @Override
    public void info(Marker marker, String msg, Throwable t) {
        this.logger.info(marker, msg, t);
    }

    /**
     * @return
     * @see org.slf4j.Logger#isWarnEnabled()
     */
    @Override
    public boolean isWarnEnabled() {
        return this.logger.isWarnEnabled();
    }

    /**
     * @param msg
     * @see org.slf4j.Logger#warn(java.lang.String)
     */
    @Override
    public void warn(String msg) {
        this.logger.warn(msg);
    }

    /**
     * @param format
     * @param arg
     * @see org.slf4j.Logger#warn(java.lang.String, java.lang.Object)
     */
    @Override
    public void warn(String format, Object arg) {
        this.logger.warn(format, arg);
    }

    /**
     * @param format
     * @param argArray
     * @see org.slf4j.Logger#warn(java.lang.String, java.lang.Object[])
     */
    @Override
    public void warn(String format, Object[] argArray) {
        this.logger.warn(format, argArray);
    }

    /**
     * @param format
     * @param arg1
     * @param arg2
     * @see org.slf4j.Logger#warn(java.lang.String, java.lang.Object, java.lang.Object)
     */
    @Override
    public void warn(String format, Object arg1, Object arg2) {
        this.logger.warn(format, arg1, arg2);
    }

    /**
     * @param msg
     * @param t
     * @see org.slf4j.Logger#warn(java.lang.String, java.lang.Throwable)
     */
    @Override
    public void warn(String msg, Throwable t) {
        this.logger.warn(msg, t);
    }

    /**
     * @param marker
     * @return
     * @see org.slf4j.Logger#isWarnEnabled(org.slf4j.Marker)
     */
    @Override
    public boolean isWarnEnabled(Marker marker) {
        return this.logger.isWarnEnabled(marker);
    }

    /**
     * @param marker
     * @param msg
     * @see org.slf4j.Logger#warn(org.slf4j.Marker, java.lang.String)
     */
    @Override
    public void warn(Marker marker, String msg) {
        this.logger.warn(marker, msg);
    }

    /**
     * @param marker
     * @param format
     * @param arg
     * @see org.slf4j.Logger#warn(org.slf4j.Marker, java.lang.String, java.lang.Object)
     */
    @Override
    public void warn(Marker marker, String format, Object arg) {
        this.logger.warn(marker, format, arg);
    }

    /**
     * @param marker
     * @param format
     * @param arg1
     * @param arg2
     * @see org.slf4j.Logger#warn(org.slf4j.Marker, java.lang.String, java.lang.Object, java.lang.Object)
     */
    @Override
    public void warn(Marker marker, String format, Object arg1, Object arg2) {
        this.logger.warn(marker, format, arg1, arg2);
    }

    /**
     * @param marker
     * @param format
     * @param argArray
     * @see org.slf4j.Logger#warn(org.slf4j.Marker, java.lang.String, java.lang.Object[])
     */
    @Override
    public void warn(Marker marker, String format, Object[] argArray) {
        this.logger.warn(marker, format, argArray);
    }

    /**
     * @param marker
     * @param msg
     * @param t
     * @see org.slf4j.Logger#warn(org.slf4j.Marker, java.lang.String, java.lang.Throwable)
     */
    @Override
    public void warn(Marker marker, String msg, Throwable t) {
        this.logger.warn(marker, msg, t);
    }

    /**
     * @return
     * @see org.slf4j.Logger#isErrorEnabled()
     */
    @Override
    public boolean isErrorEnabled() {
        return this.logger.isErrorEnabled();
    }

    /**
     * @param msg
     * @see org.slf4j.Logger#error(java.lang.String)
     */
    @Override
    public void error(String msg) {
        this.logger.error(msg);
    }

    /**
     * @param format
     * @param arg
     * @see org.slf4j.Logger#error(java.lang.String, java.lang.Object)
     */
    @Override
    public void error(String format, Object arg) {
        this.logger.error(format, arg);
    }

    /**
     * @param format
     * @param arg1
     * @param arg2
     * @see org.slf4j.Logger#error(java.lang.String, java.lang.Object, java.lang.Object)
     */
    @Override
    public void error(String format, Object arg1, Object arg2) {
        this.logger.error(format, arg1, arg2);
    }

    /**
     * @param format
     * @param argArray
     * @see org.slf4j.Logger#error(java.lang.String, java.lang.Object[])
     */
    @Override
    public void error(String format, Object[] argArray) {
        this.logger.error(format, argArray);
    }

    /**
     * @param msg
     * @param t
     * @see org.slf4j.Logger#error(java.lang.String, java.lang.Throwable)
     */
    @Override
    public void error(String msg, Throwable t) {
        this.logger.error(msg, t);
    }

    /**
     * @param marker
     * @return
     * @see org.slf4j.Logger#isErrorEnabled(org.slf4j.Marker)
     */
    @Override
    public boolean isErrorEnabled(Marker marker) {
        return this.logger.isErrorEnabled(marker);
    }

    /**
     * @param marker
     * @param msg
     * @see org.slf4j.Logger#error(org.slf4j.Marker, java.lang.String)
     */
    @Override
    public void error(Marker marker, String msg) {
        this.logger.error(marker, msg);
    }

    /**
     * @param marker
     * @param format
     * @param arg
     * @see org.slf4j.Logger#error(org.slf4j.Marker, java.lang.String, java.lang.Object)
     */
    @Override
    public void error(Marker marker, String format, Object arg) {
        this.logger.error(marker, format, arg);
    }

    /**
     * @param marker
     * @param format
     * @param arg1
     * @param arg2
     * @see org.slf4j.Logger#error(org.slf4j.Marker, java.lang.String, java.lang.Object, java.lang.Object)
     */
    @Override
    public void error(Marker marker, String format, Object arg1, Object arg2) {
        this.logger.error(marker, format, arg1, arg2);
    }

    /**
     * @param marker
     * @param format
     * @param argArray
     * @see org.slf4j.Logger#error(org.slf4j.Marker, java.lang.String, java.lang.Object[])
     */
    @Override
    public void error(Marker marker, String format, Object[] argArray) {
        this.logger.error(marker, format, argArray);
    }

    /**
     * @param marker
     * @param msg
     * @param t
     * @see org.slf4j.Logger#error(org.slf4j.Marker, java.lang.String, java.lang.Throwable)
     */
    @Override
    public void error(Marker marker, String msg, Throwable t) {
        this.logger.error(marker, msg, t);
    }

}
